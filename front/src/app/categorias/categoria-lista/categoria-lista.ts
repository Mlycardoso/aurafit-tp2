import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { getApiError } from '../../shared/api-error';
import { Categoria } from '../categoria.model';
import { CategoriaService } from '../categoria.service';

@Component({
  selector: 'app-categoria-lista',
  imports: [RouterLink],
  templateUrl: './categoria-lista.html',
})
export class CategoriaLista {
  private readonly service = inject(CategoriaService);

  protected readonly items = signal<Categoria[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal('');

  constructor() {
    this.load();
  }

  protected load(nome = ''): void {
    this.loading.set(true);
    this.error.set('');
    this.service.findAll(nome).subscribe({
      next: (items) => {
        this.items.set(items);
        this.loading.set(false);
      },
      error: (error) => {
        this.error.set(getApiError(error));
        this.loading.set(false);
      },
    });
  }

  protected remove(item: Categoria): void {
    if (!confirm(`Excluir a categoria "${item.nome}"?`)) return;
    this.service.delete(item.id).subscribe({
      next: () => this.load(),
      error: (error) => this.error.set(getApiError(error)),
    });
  }
}
