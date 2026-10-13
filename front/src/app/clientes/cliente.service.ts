import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
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
    const url = nome.trim() ? this.url + '/nome/' + encodeURIComponent(nome.trim()) : this.url;
    return this.http.get<PageResponse<Cliente>>(url, { params: { page, pageSize: 10 } });
  }
  buscar(id: number) { return this.http.get<Cliente>(this.url + '/' + id); }
  consultar(cep: string) { return this.http.get<Cep>(this.url + '/cep/' + cep); }
  salvar(id: number | null, dto: ClientePayload): Observable<Cliente | void> {
    return id ? this.http.put<void>(this.url + '/' + id, dto) : this.http.post<Cliente>(this.url, dto);
  }
}
