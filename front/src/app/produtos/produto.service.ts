import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';

import { PageResponse } from '../shared/page-response.model';
import { Produto, ProdutoPayload } from './produto.model';

@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8080/produtos';

  findAll(nome = '', page = 0, pageSize = 5): Observable<PageResponse<Produto>> {
    const endpoint = nome.trim() ? `${this.url}/nome/${encodeURIComponent(nome.trim())}` : this.url;
    const params = new HttpParams().set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<Produto>>(endpoint, { params }).pipe(
      map(page => ({ ...page, items: page.items.map(p => this.normalizar(p)) })));
  }

  findById(id: number): Observable<Produto> {
    return this.http.get<Produto>(`${this.url}/${id}`).pipe(map(p => this.normalizar(p)));
  }

  create(payload: ProdutoPayload): Observable<Produto> {
    return this.http.post<Produto>(this.url, payload);
  }

  update(id: number, payload: ProdutoPayload): Observable<Produto> {
    return this.http.put<Produto>(`${this.url}/${id}`, payload);
  }

  inativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.url}/${id}/inativar`, {});
  }

  private normalizar(produto: Produto): Produto {
    return { ...produto, imagemUrl: produto.imagemUrl ? new URL(produto.imagemUrl, this.url).href : null };
  }

  enviarImagem(id: number, arquivo: File): Observable<{ imagemUrl: string; aviso: string | null }> {
    const form = new FormData();
    form.append('arquivo', arquivo);
    return this.http.post<{ imagemUrl: string; aviso: string | null }>(`${this.url}/${id}/imagem`, form).pipe(
      map(result => ({ ...result, imagemUrl: new URL(result.imagemUrl, this.url).href })));
  }

  removerImagem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}/imagem`);
  }

  baixarImagem(id: number): Observable<Blob> {
    return this.http.get(`${this.url}/${id}/imagem?download=true`, { responseType: 'blob' });
  }
}
