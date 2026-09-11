import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Categoria, CategoriaPayload } from './categoria.model';

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8080/categorias';

  findAll(nome = ''): Observable<Categoria[]> {
    const endpoint = nome.trim() ? `${this.url}/nome/${encodeURIComponent(nome.trim())}` : this.url;
    return this.http.get<Categoria[]>(endpoint);
  }

  findById(id: number): Observable<Categoria> {
    return this.http.get<Categoria>(`${this.url}/${id}`);
  }

  create(payload: CategoriaPayload): Observable<Categoria> {
    return this.http.post<Categoria>(this.url, payload);
  }

  update(id: number, payload: CategoriaPayload): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.url}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
