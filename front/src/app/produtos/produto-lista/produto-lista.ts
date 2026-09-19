import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

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

  protected readonly items = signal<Produto[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly search = signal('');
  protected readonly page = signal(0);
  protected readonly totalItems = signal(0);
  protected readonly totalPages = signal(0);

  constructor() {
    this.load();
  }

  protected load(nome = this.search(), page = 0): void {
    const filtro = nome.trim();
    this.loading.set(true);
    this.error.set('');
    this.search.set(filtro);
    this.service.findAll(filtro, page, this.pageSize).subscribe({
      next: (response) => {
        this.items.set(response.items);
        this.page.set(response.page);
        this.totalItems.set(response.totalItems);
        this.totalPages.set(response.totalPages);
        this.loading.set(false);
      },
      error: (error) => {
        this.error.set(getApiError(error));
        this.loading.set(false);
      },
    });
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
