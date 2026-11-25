import { DatePipe, CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Pedido, PedidoService } from '../pedidos/pedido.service';

@Component({
  standalone: true, imports: [DatePipe, CurrencyPipe, RouterLink], templateUrl: './meus-pedidos.html',
  styles: [`:host{display:block;min-height:100vh;background:#fff;color:#292d2c}.page{max-width:1100px;margin:auto;padding:42px 24px}.eyebrow{color:#bc2457;font-size:11px;font-weight:800;letter-spacing:.12em}.orders{display:grid;gap:16px;margin:26px 0}.order{padding:20px;border:1px solid #dce7df;border-top:3px solid #a8cdbb;border-radius:5px}.order-head{display:flex;justify-content:space-between;gap:14px;align-items:start}.order h2{font-size:18px;margin:0 0 8px}.muted,small{color:#67736a}.items{border-top:1px solid #e4eae6;margin-top:14px;padding-top:10px}.total{display:flex;justify-content:space-between;margin-top:12px}.badge{padding:5px 9px;border-radius:14px;background:#deeee7;color:#315745;font-size:12px}@media(max-width:600px){.page{padding:28px 16px}.order-head{flex-direction:column}}`]
})
export class MeusPedidos {
  private readonly service = inject(PedidoService);
  readonly pedidos = signal<Pedido[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  constructor() {
    this.service.meus().subscribe({ next: values => { this.pedidos.set(values); this.loading.set(false); }, error: () => { this.error.set('Não foi possível carregar seus pedidos.'); this.loading.set(false); } });
  }
}
