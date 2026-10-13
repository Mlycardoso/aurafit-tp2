import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PageResponse } from '../shared/page-response.model';

export type Perfil = 'ADMINISTRADOR' | 'ATENDENTE' | 'CLIENTE';
export const perfis: {id: Perfil; nome: string}[] = [
  {id: 'ADMINISTRADOR', nome: 'Administrador'},
  {id: 'ATENDENTE', nome: 'Atendente'},
  {id: 'CLIENTE', nome: 'Cliente'}
];
export interface UsuarioPayload {
  nome: string; email: string; ativo: boolean; perfis: Perfil[]; senha?: string | null;
}
export interface Usuario extends Omit<UsuarioPayload, 'senha'> { id: number; }

@Injectable({providedIn: 'root'})
export class UsuarioService {
  private http = inject(HttpClient);
  private url = 'http://localhost:8080/usuarios';
  listar(nome = '', page = 0) {
    const url = nome.trim() ? this.url + '/nome/' + encodeURIComponent(nome.trim()) : this.url;
    return this.http.get<PageResponse<Usuario>>(url, {params: {page, pageSize: 10}});
  }
  buscar(id: number) { return this.http.get<Usuario>(this.url + '/' + id); }
  salvar(id: number | null, dto: UsuarioPayload) {
    return id ? this.http.put<Usuario>(this.url + '/' + id, dto) : this.http.post<Usuario>(this.url, dto);
  }
  excluir(id: number) { return this.http.delete<void>(this.url + '/' + id); }
}
