import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Marca, MarcaPayload } from './marca.model';

@Injectable({ providedIn: 'root' })
export class MarcaService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8080/marcas';

  findAll(nome = ''): Observable<Marca[]> {
    const endpoint = nome.trim() ? `${this.url}/nome/${encodeURIComponent(nome.trim())}` : this.url;
    return this.http.get<Marca[]>(endpoint);
  }

  findById(id: number): Observable<Marca> {
    return this.http.get<Marca>(`${this.url}/${id}`);
  }

  create(payload: MarcaPayload): Observable<Marca> {
    return this.http.post<Marca>(this.url, payload);
  }

  update(id: number, payload: MarcaPayload): Observable<Marca> {
    return this.http.put<Marca>(`${this.url}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
