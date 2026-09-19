import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { PageResponse } from '../shared/page-response.model';
import { Produto, ProdutoPayload } from './produto.model';

@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8080/produtos';

  findAll(nome = '', page = 0, pageSize = 5): Observable<PageResponse<Produto>> {
    const endpoint = nome.trim() ? `${this.url}/nome/${encodeURIComponent(nome.trim())}` : this.url;
    const params = new HttpParams().set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<Produto>>(endpoint, { params });
  }

  findById(id: number): Observable<Produto> {
    return this.http.get<Produto>(`${this.url}/${id}`);
  }

  create(payload: ProdutoPayload): Observable<Produto> {
    return this.http.post<Produto>(this.url, payload);
  }

  update(id: number, payload: ProdutoPayload): Observable<Produto> {
    return this.http.put<Produto>(`${this.url}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
