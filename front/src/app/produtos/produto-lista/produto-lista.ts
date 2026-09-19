import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, Subject, catchError, switchMap, timer } from 'rxjs';

import { getApiError } from '../../shared/api-error';
import { Produto } from '../produto.model';
import { ProdutoService } from '../produto.service';

@Component({
  selector: 'app-produto-lista',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './produto-lista.html',
})
export class ProdutoLista {
  private readonly service = inject(ProdutoService);
  private readonly pageSize = 5;
  private readonly requests = new Subject<{ nome: string; page: number; delay: number }>();

  protected readonly items = signal<Produto[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly search = signal('');
  protected readonly page = signal(0);
  protected readonly totalItems = signal(0);
  protected readonly totalPages = signal(0);

  constructor() {
    this.requests.pipe(
      switchMap(({ nome, page, delay }) => timer(delay).pipe(
        switchMap(() => this.service.findAll(nome, page, this.pageSize)),
        catchError((error) => {
          this.error.set(getApiError(error));
          this.loading.set(false);
          return EMPTY;
        }),
      )),
      takeUntilDestroyed(),
    ).subscribe((response) => {
      this.items.set(response.items);
      this.page.set(response.page);
      this.totalItems.set(response.totalItems);
      this.totalPages.set(response.totalPages);
      this.loading.set(false);
    });
    this.load();
  }

  protected onSearch(nome: string): void {
    this.load(nome, 0, 300);
  }

  protected load(nome = this.search(), page = 0, delay = 0): void {
    const filtro = nome.trim();
    this.loading.set(true);
    this.error.set('');
    this.search.set(nome);
    this.requests.next({ nome: filtro, page, delay });
  }

  protected previousPage(): void {
    if (this.page() > 0) this.load(this.search(), this.page() - 1);
  }

  protected nextPage(): void {
    if (this.page() + 1 < this.totalPages()) this.load(this.search(), this.page() + 1);
  }

  protected remove(item: Produto): void {
    if (!confirm(`Excluir o produto "${item.nome}"?`)) return;
    this.service.delete(item.id).subscribe({
      next: () => {
        const targetPage = this.items().length === 1 && this.page() > 0 ? this.page() - 1 : this.page();
        this.load(this.search(), targetPage);
      },
      error: (error) => this.error.set(getApiError(error)),
    });
  }
}
