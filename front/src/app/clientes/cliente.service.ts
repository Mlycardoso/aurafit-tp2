import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PageResponse } from '../shared/page-response.model';
export interface Cep { cep: string; logradouro: string; bairro: string; municipio: string; uf: string; }
export interface ClientePayload {
  nome: string; email: string; telefone: string; cep: string; logradouro: string;
  bairro: string; numero: string; complemento: string;
}
export interface Cliente extends ClientePayload {
  id: number; municipio: { nome: string; estado: { sigla: string } };
}
@Injectable({ providedIn: 'root' })
export class ClienteService {
  private http = inject(HttpClient);
  private url = 'http://localhost:8080/clientes';
  listar(nome: string, page: number) {
    return this.http.get<PageResponse<Cliente>>(this.url, { params: { nome, page, pageSize: 10 } });
  }
  buscar(id: number) { return this.http.get<Cliente>(this.url + '/' + id); }
  consultar(cep: string) { return this.http.get<Cep>(this.url + '/cep/' + cep); }
  salvar(id: number | null, dto: ClientePayload) {
    return id ? this.http.put<Cliente>(this.url + '/' + id, dto) : this.http.post<Cliente>(this.url, dto);
  }
}
