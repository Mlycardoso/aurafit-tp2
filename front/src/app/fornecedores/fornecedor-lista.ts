import { DocumentoPipe } from '../shared/documento-mask';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Fornecedor, FornecedorService } from './fornecedor.service';
import { getApiError } from '../shared/api-error';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, Subject, catchError, switchMap, timer } from 'rxjs';
@Component({
  imports: [RouterLink, DocumentoPipe],
  template: `
    <section class="page-header"><h1>Fornecedores</h1>
      <a class="btn btn-primary" routerLink="/admin/fornecedores/novo"><img src="icons/plus.svg" alt="" />Novo fornecedor</a>
    </section>
    @if (error()) { <div class="alert error" role="alert">{{error()}}</div> }
    <section class="toolbar"><div class="search-box">
      <img src="icons/search.svg" alt="" />
      <input #busca type="search" aria-label="Buscar fornecedor" placeholder="Buscar fornecedor pelo nome" (input)="load(busca.value, 0, 300)" (keyup.enter)="load(busca.value, 0)" />
      <button class="btn btn-secondary" (click)="load(busca.value, 0)">Buscar</button>
    </div><span class="result-count">{{total()}} registro(s)</span></section>
    <section class="data-panel">
    @if (loading()) { <div class="loading-state">Carregando fornecedores...</div> }
    @else {
      <table><thead><tr><th>Razão social</th><th>CNPJ</th><th>E-mail</th><th>Telefone</th><th>Município / UF</th><th>Ações</th></tr></thead><tbody>
      @for (c of items(); track c.id) { <tr><td>{{c.nome}} <span class="badge" [class.active]="c.ativo !== false" [class.inactive]="c.ativo === false">{{c.ativo === false ? 'Inativo' : 'Ativo'}}</span></td><td>{{c.cnpj | documento: 'cnpj'}}</td><td>{{c.email}}</td><td>{{c.telefone}}</td>
      <td>{{c.municipio.nome}} / {{c.municipio.estado.sigla}}</td>
      <td><div class="actions"><a class="icon-button" [routerLink]="['/admin/fornecedores/editar', c.id]" title="Editar fornecedor"><img src="icons/pencil.svg" alt="Editar" /></a>
      <button class="icon-button danger" type="button" title="Excluir fornecedor" [disabled]="deleting() !== null" (click)="remove(c)"><img src="icons/trash-2.svg" alt="Excluir" /></button></div></td></tr> }
      </tbody></table>
      @if (!items().length) { <div class="empty-state">Nenhum fornecedor encontrado.</div> }
    }</section>
    <nav class="pagination" aria-label="Páginas de fornecedores">
      <button class="btn btn-secondary" [disabled]="loading() || page() === 0" (click)="load(filtro, page()-1)">Anterior</button>
      <span>Página {{page()+1}} de {{pages() || 1}}</span>
      <button class="btn btn-secondary" [disabled]="loading() || page()+1 >= pages()" (click)="load(filtro, page()+1)">Próxima</button>
    </nav>`
})
export class FornecedorLista {
  private service = inject(FornecedorService);
  protected items = signal<Fornecedor[]>([]);
  protected loading = signal(false); protected error = signal('');
  protected page = signal(0); protected pages = signal(0); protected total = signal(0);
  protected filtro = '';
  protected deleting = signal<number | null>(null);
  private requests = new Subject<{ nome: string; page: number; delay: number }>();
  constructor() {
    this.requests.pipe(
      switchMap(({nome, page, delay}) => timer(delay).pipe(
        switchMap(() => this.service.listar(nome, page)),
        catchError(e => { this.error.set(getApiError(e)); this.loading.set(false); return EMPTY; })
      )),
      takeUntilDestroyed()
    ).subscribe(r => {
      this.items.set(r.items); this.page.set(r.page); this.pages.set(r.totalPages);
      this.total.set(r.totalItems); this.loading.set(false);
    });
    this.load();
  }
  protected load(nome = this.filtro, page = 0, delay = 0) {
    this.filtro = nome; this.loading.set(true); this.error.set('');
    this.requests.next({nome, page, delay});
  }
  protected remove(fornecedor: Fornecedor) {
    if (this.deleting() !== null || !confirm('Excluir o fornecedor "' + fornecedor.nome + '"?')) return;
    this.deleting.set(fornecedor.id);
    this.service.delete(fornecedor.id).subscribe({
      next: () => {
        this.deleting.set(null);
        this.load(this.filtro, this.items().length === 1 ? Math.max(0, this.page() - 1) : this.page());
      },
      error: e => { this.deleting.set(null); this.error.set(getApiError(e)); }
    });
  }
}
