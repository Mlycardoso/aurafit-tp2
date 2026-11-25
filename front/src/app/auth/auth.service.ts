import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, switchMap, tap, throwError } from 'rxjs';

export interface AuthUser {
  id: number | null;
  nome: string;
  email: string;
  roles: string[];
  cliente?: { cep: string; logradouro: string; numero: string; complemento?: string | null; bairro: string; municipio: { nome: string; estado: { sigla: string } } } | null;
}
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  usuario: AuthUser | null;
}

const API = 'http://localhost:8080';
const REFRESH_KEY = 'aurafit.refresh';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  readonly usuario = signal<AuthUser | null>(null);
  readonly accessToken = signal<string | null>(null);
  readonly roles = computed(() => this.usuario()?.roles ?? []);
  readonly isAuthenticated = computed(() => !!this.accessToken() && !!this.usuario());
  readonly isAdmin = computed(() => this.roles().includes('ADMIN'));
  private refreshToken: string | null = null;

  login(email: string, senha: string): Observable<AuthUser> {
    return this.http.post<AuthResponse>(`${API}/auth/login`, { email, senha }).pipe(
      tap((response) => this.save(response)),
      switchMap((response) => response.usuario ? of(response.usuario) : this.carregarUsuario()),
    );
  }

  registrar(payload: unknown): Observable<AuthUser> {
    return this.http.post<AuthResponse>(`${API}/auth/registro`, payload).pipe(
      tap((response) => this.save(response)),
      switchMap((response) => response.usuario ? of(response.usuario) : this.carregarUsuario()),
    );
  }

  carregarUsuario(): Observable<AuthUser> {
    return this.http.get<AuthUser>(`${API}/auth/me`).pipe(tap((user) => this.usuario.set(user)));
  }

  refresh(): Observable<string> {
    if (!this.refreshToken) return throwError(() => new Error('Sessão expirada'));
    return this.http.post<AuthResponse>(`${API}/auth/refresh`, { refreshToken: this.refreshToken }).pipe(
      tap((response) => this.save(response)),
      switchMap((response) => response.usuario ? of(response.usuario) : this.carregarUsuario()),
      map(() => this.accessToken()!),
    );
  }

  restaurarSessao(): Observable<void> {
    this.refreshToken = sessionStorage.getItem(REFRESH_KEY);
    if (!this.refreshToken) return of(void 0);
    return this.refresh().pipe(map(() => void 0), catchError(() => { this.clear(); return of(void 0); }));
  }

  logout(): Observable<void> {
    const token = this.refreshToken;
    if (!token) { this.clear(); return of(void 0); }
    return this.http.post<void>(`${API}/auth/logout`, { refreshToken: token }).pipe(
      catchError(() => of(void 0)),
      tap(() => this.clear()),
    );
  }

  limparSessao(): void { this.clear(); }

  private save(response: AuthResponse): void {
    this.accessToken.set(response.accessToken);
    this.refreshToken = response.refreshToken;
    sessionStorage.setItem(REFRESH_KEY, response.refreshToken);
    if (response.usuario) this.usuario.set(response.usuario);
  }

  private clear(): void {
    this.accessToken.set(null);
    this.refreshToken = null;
    this.usuario.set(null);
    sessionStorage.removeItem(REFRESH_KEY);
  }
}
