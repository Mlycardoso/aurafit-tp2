import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { PageResponse } from '../shared/page-response.model';

export type StatusPedido = 'PENDENTE' | 'ENVIADO';
export type TipoEntrega = 'NORMAL' | 'RAPIDA';
export interface PedidoItem { produtoId: number | null; nomeProduto: string; sku: string | null; quantidade: number; precoUnitario: number; }
export interface Pedido {
  id: number; dataHora: string; status: StatusPedido; tipoEntrega: TipoEntrega; frete: number; prazoEntrega: string;
  nomeCliente: string; cep: string; logradouro: string; numero: string; complemento?: string; bairro: string;
  municipio: string; estado: string; total: number; itens: PedidoItem[];
}

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8080/pedidos';
  criar(tipoEntrega: TipoEntrega, itens: { produtoId: number; quantidade: number }[]) {
    return this.http.post<Pedido>(this.url, { tipoEntrega, itens });
  }
  meus() { return this.http.get<Pedido[]>(this.url + '/meus'); }
  listar(page: number, pageSize: number, status?: StatusPedido | '', nome?: string) {
    const params: Record<string, string | number> = { page, pageSize };
    if (status) params['status'] = status;
    if (nome?.trim()) params['nome'] = nome.trim();
    return this.http.get<PageResponse<Pedido>>(this.url, { params });
  }
  marcarEnviado(id: number) { return this.http.patch<Pedido>(`${this.url}/${id}/enviar`, {}); }
}
