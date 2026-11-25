import { Component, computed, inject, signal, DestroyRef } from '@angular/core';
import { CurrencyPipe, DatePipe, TitleCasePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, expand, EMPTY, finalize, map, reduce, timeout } from 'rxjs';
import { ProdutoService } from '../produtos/produto.service';
import { flavor, LojaState, ShopProduct, weight } from './loja-state';
import { PageResponse } from '../shared/page-response.model';
import { AuthService } from '../auth/auth.service';
import { Pedido, PedidoService, TipoEntrega } from '../pedidos/pedido.service';

@Component({
  selector: 'app-loja',
  imports: [RouterLink, FormsModule, CurrencyPipe, DatePipe, TitleCasePipe],
  templateUrl: './loja.html',
  styleUrl: './loja.scss',
})
export class Loja {
  readonly state = inject(LojaState);
  readonly auth = inject(AuthService);
  private readonly pedidos = inject(PedidoService);
  private readonly api = inject(ProdutoService);
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  readonly products = signal<ShopProduct[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly imagensIndisponiveis = signal<number[]>([]);
  readonly query = signal('');
  readonly category = signal('');
  readonly brand = signal('');
  readonly taste = signal('');
  readonly size = signal('');
  readonly maxPrice = signal(1000);
  readonly sort = signal('name');
  readonly page = signal(1);
  readonly filtersOpen = signal(false);
  readonly quantity = signal(1);
  readonly cepBusy = signal(false);
  readonly cepError = signal('');
  readonly address = signal('');
  cep = '';
  readonly flavor = flavor;
  readonly weight = weight;
  readonly view = signal('catalog');
  readonly productId = signal(0);
  readonly selected = computed(() => this.products().find((p) => p.id === this.productId()));
  readonly tipoEntrega = signal<TipoEntrega>('NORMAL');
  readonly finalizando = signal(false);
  readonly pedidoError = signal('');
  readonly pedidoCriado = signal<Pedido | null>(null);
  readonly frete = computed(() => this.tipoEntrega() === 'RAPIDA' ? 29.9 : 14.9);
  readonly totalComFrete = computed(() => this.total() + this.frete());
  readonly enderecoEntrega = computed(() => {
    const c = this.auth.usuario()?.cliente;
    return c ? `${c.logradouro}, ${c.numero}${c.complemento ? ' · ' + c.complemento : ''} · ${c.bairro} · ${c.municipio.nome}/${c.municipio.estado.sigla} · CEP ${c.cep}` : '';
  });
  protected sair(): void { this.auth.logout().subscribe(() => this.router.navigate(['/loja'])); }
  protected finalizarPedido(): void {
    if (this.finalizando() || this.cartInvalid()) return;
    this.finalizando.set(true); this.pedidoError.set('');
    this.pedidos.criar(this.tipoEntrega(), this.state.cart().map(item => ({ produtoId: item.id, quantidade: item.quantity })))
      .pipe(finalize(() => this.finalizando.set(false)))
      .subscribe({
        next: order => { this.pedidoCriado.set(order); this.state.clearCart(); },
        error: error => this.pedidoError.set(error.status === 422 ? 'O estoque mudou. Revise as quantidades do carrinho.' : 'Não foi possível finalizar o pedido. Tente novamente.'),
      });
  }
  readonly types = computed(() => [...new Set(this.products().map((p) => p.categoria.nome))]);
  readonly brands = computed(() => [
    ...new Set(this.products().map((p) => p.marca?.nome || 'Não informada')),
  ]);
  readonly flavors = computed(() => [...new Set(this.products().map(flavor).filter(Boolean))]);
  readonly sizes = computed(() => [...new Set(this.products().map(weight).filter(Boolean))]);
  readonly ceiling = computed(() =>
    Math.max(100, ...this.products().map((p) => Math.ceil(p.preco / 50) * 50)),
  );
  readonly filtered = computed(() => {
    const normalize = (s: string) =>
      s
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase();
    const q = normalize(this.query().trim());
    return this.products()
      .filter(
        (p) =>
          p.ativo &&
          (this.view() !== 'favorites' || this.state.favorites().includes(p.id)) &&
          normalize(p.nome + ' ' + p.categoria.nome).includes(q) &&
          (!this.category() || p.categoria.nome === this.category()) &&
          (!this.brand() || (p.marca?.nome || 'Não informada') === this.brand()) &&
          (!this.taste() || flavor(p) === this.taste()) &&
          (!this.size() || weight(p) === this.size()) &&
          p.preco <= this.maxPrice(),
      )
      .sort((a, b) =>
        this.sort() === 'low'
          ? a.preco - b.preco
          : this.sort() === 'high'
            ? b.preco - a.preco
            : a.nome.localeCompare(b.nome),
      );
  });
  readonly pages = computed(() => Math.max(1, Math.ceil(this.filtered().length / 8)));
  readonly currentPage = computed(() => Math.min(this.page(), this.pages()));
  readonly visible = computed(() => this.filtered().slice((this.currentPage() - 1) * 8, this.currentPage() * 8));
  readonly rows = computed(() =>
    this.state.cart().map((row) => ({
      ...row,
      product: this.products().find((p) => p.id === row.id),
    })),
  );
  readonly total = computed(() =>
    this.rows().reduce((sum, row) => sum + (row.product?.preco || 0) * row.quantity, 0),
  );
  readonly cartInvalid = computed(
    () =>
      !this.rows().length ||
      this.rows().some((row) => !row.product?.ativo || row.quantity > (row.product?.estoque || 0)),
  );
  readonly variants = computed(() => {
    const p = this.selected();
    return p
      ? this.products().filter(
          (item) =>
            item.ativo &&
            item.categoria.id === p.categoria.id &&
            item.marca?.nome === p.marca?.nome,
        )
      : [];
  });
  constructor() {
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      this.query.set(params.get('busca') || '');
      this.category.set(params.get('tipo') || '');
      this.page.set(1);
    });
    this.route.url.pipe(takeUntilDestroyed()).subscribe((segments) => {
      const path = segments.map((s) => s.path).join('/');
      this.view.set(
        path.includes('produto/')
          ? 'detail'
          : path.endsWith('carrinho')
            ? 'cart'
            : path.endsWith('checkout')
              ? 'checkout'
              : path.endsWith('favoritos')
                ? 'favorites'
                : path.endsWith('conta')
                  ? 'account'
                  : 'catalog',
      );
      this.productId.set(Number(this.route.snapshot.paramMap.get('id')) || 0);
      this.quantity.set(1);
      window.scrollTo({ top: 0, behavior: 'instant' });
    });
    this.load();
  }
  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.api
      .findAll('', 0, 100)
      .pipe(
        expand((page: PageResponse<ShopProduct>) =>
          page.page + 1 < page.totalPages ? this.api.findAll('', page.page + 1, 100) : EMPTY,
        ),
        map((page) => page.items),
        reduce((all: ShopProduct[], items) => [...all, ...items], []),
        timeout(20000),
        takeUntilDestroyed(this.destroyRef),
        catchError(() => {
          this.error.set('Não conseguimos carregar os produtos. Tente novamente.');
          this.loading.set(false);
          return EMPTY;
        }),
      )
      .subscribe((products) => {
        this.products.set(products);
        this.maxPrice.set(this.ceiling());
        this.loading.set(false);
      });
  }
  search(value: string): void {
    this.query.set(value);
    this.page.set(1);
    if (!['catalog', 'favorites'].includes(this.view()))
      void this.router.navigate(['/loja'], { queryParams: { busca: value } });
  }
  chooseCategory(value: string): void {
    this.category.set(value);
    this.page.set(1);
    if (!['catalog', 'favorites'].includes(this.view()))
      void this.router.navigate(['/loja'], { queryParams: { tipo: value } });
    document.getElementById('catalogo')?.scrollIntoView({ behavior: 'auto' });
  }
  clear(): void {
    this.query.set('');
    this.category.set('');
    this.brand.set('');
    this.taste.set('');
    this.size.set('');
    this.maxPrice.set(this.ceiling());
    this.page.set(1);
  }
  image(p: ShopProduct): string {
    return this.temImagem(p) ? p.imagemUrl! : '/whey-pouch.png';
  }

  temImagem(p: ShopProduct): boolean {
    return !!p.imagemUrl && !this.imagensIndisponiveis().includes(p.id);
  }

  imagemFalhou(p: ShopProduct): void {
    if (!this.imagensIndisponiveis().includes(p.id))
      this.imagensIndisponiveis.update(ids => [...ids, p.id]);
  }
  selectVariant(id: number): void {
    void this.router.navigate(['/loja/produto', id]);
  }
  scrollTo(id: string, event: Event): void {
    event.preventDefault();
    document.getElementById(id)?.scrollIntoView({ behavior: 'auto' });
    document.getElementById(id)?.focus({ preventScroll: true });
  }
  lookupCep(): void {
    const cep = this.cep.replace(/\D/g, '');
    this.address.set('');
    this.cepError.set('');
    if (!/^\d{8}$/.test(cep)) {
      this.cepError.set('Informe um CEP com 8 dígitos.');
      return;
    }
    this.cepBusy.set(true);
    this.http
      .get<{ logradouro: string; bairro: string; municipio: string; uf: string }>(
        'http://localhost:8080/clientes/cep/' + cep,
      )
      .pipe(timeout(15000), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          if (this.cep.replace(/\D/g, '') === cep)
            this.address.set(
              [result.logradouro, result.bairro, result.municipio, result.uf]
                .filter(Boolean)
                .join(', '),
            );
          this.cepBusy.set(false);
        },
        error: () => {
          this.cepError.set(
            'Não foi possível consultar o CEP. Confira o número e tente novamente.',
          );
          this.cepBusy.set(false);
        },
      });
  }
}
