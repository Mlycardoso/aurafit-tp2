import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';

import { getApiError } from '../../shared/api-error';
import { Marca } from '../marca.model';
import { MarcaService } from '../marca.service';

@Component({
  selector: 'app-marca-lista',
  imports: [RouterLink],
  templateUrl: './marca-lista.html',
})
export class MarcaLista {
  private readonly service = inject(MarcaService);

  protected readonly items = signal<Marca[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  private request?: Subscription;
  private filtro = '';

  constructor() {
    this.load();
  }

  ngOnDestroy(): void { this.request?.unsubscribe(); }

  protected load(nome = this.filtro): void {
    this.filtro = nome;
    this.request?.unsubscribe();
    this.loading.set(true);
    this.error.set('');
    this.request = this.service.findAll(nome).subscribe({
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

  protected remove(item: Marca): void {
    if (!confirm(`Excluir a marca "${item.nome}"?`)) return;
    this.service.delete(item.id).subscribe({
      next: () => this.load(),
      error: (error) => this.error.set(getApiError(error)),
    });
  }
}
