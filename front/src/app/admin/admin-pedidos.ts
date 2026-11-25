import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Pedido, PedidoService, StatusPedido } from '../pedidos/pedido.service';

@Component({
  standalone: true, imports: [FormsModule, CurrencyPipe, DatePipe], templateUrl: './admin-pedidos.html', styleUrl: './admin-pedidos.scss',
})
export class AdminPedidos {
  private readonly service = inject(PedidoService);
  readonly pedidos = signal<Pedido[]>([]);
  readonly status = signal<StatusPedido | ''>('');
  readonly nome = signal('');
  readonly page = signal(0);
  readonly pageSize = 10;
  readonly totalPages = signal(0);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly updating = signal<number | null>(null);
  constructor() { this.load(); }
  load(): void {
    this.loading.set(true); this.error.set('');
    this.service.listar(this.page(), this.pageSize, this.status(), this.nome()).subscribe({
      next: result => { this.pedidos.set(result.items); this.totalPages.set(result.totalPages); this.loading.set(false); },
      error: () => { this.error.set('Não foi possível carregar os pedidos.'); this.loading.set(false); },
    });
  }
  changeStatus(value: string): void { this.status.set(value as StatusPedido | ''); this.page.set(0); this.load(); }
  search(): void { this.page.set(0); this.load(); }
  changePage(delta: number): void { this.page.update(value => Math.max(0, Math.min(this.totalPages() - 1, value + delta))); this.load(); }
  marcarEnviado(pedido: Pedido): void {
    if (pedido.status !== 'PENDENTE' || this.updating() !== null || !confirm(`Marcar o pedido #${pedido.id} como enviado?`)) return;
    this.updating.set(pedido.id);
    this.service.marcarEnviado(pedido.id).subscribe({
      next: updated => { this.pedidos.update(rows => rows.map(row => row.id === updated.id ? updated : row)); this.updating.set(null); },
      error: () => { this.error.set('Não foi possível atualizar o status.'); this.updating.set(null); },
    });
  }
}
