import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, Subject, catchError, switchMap, timer } from 'rxjs';
import { Usuario, UsuarioService, perfis } from './usuario.service';
import { getApiError } from '../shared/api-error';

@Component({
  imports: [RouterLink],
  templateUrl: './usuario-lista.html'
})
export class UsuarioLista {
  private service = inject(UsuarioService);
  private requests = new Subject<{nome: string; page: number; delay: number}>();
  protected items = signal<Usuario[]>([]);
  protected loading = signal(false);
  protected deleting = signal<number | null>(null);
  protected error = signal('');
  protected search = signal('');
  protected page = signal(0);
  protected pages = signal(0);
  protected total = signal(0);
  constructor() {
    this.requests.pipe(
      switchMap(({nome, page, delay}) => timer(delay).pipe(
        switchMap(() => this.service.listar(nome, page)),
        catchError(e => { this.error.set(getApiError(e)); this.loading.set(false); return EMPTY; })
      )),
      takeUntilDestroyed()
    ).subscribe(r => {
      this.items.set(r.items); this.page.set(r.page); this.pages.set(r.totalPages);
      this.total.set(r.totalItems); this.loading.set(false);
    });
    this.load();
  }
  protected load(nome = this.search(), page = 0, delay = 0) {
    this.search.set(nome); this.error.set(''); this.loading.set(true);
    this.requests.next({nome, page, delay});
  }
  protected perfilNames(usuario: Usuario) {
    return usuario.perfis.map(p => perfis.find(item => item.id === p)?.nome ?? p).join(', ');
  }
  protected remove(usuario: Usuario) {
    if (this.deleting() !== null || !confirm('Excluir o usuário "' + usuario.nome + '"?')) return;
    this.deleting.set(usuario.id);
    this.service.excluir(usuario.id).subscribe({
      next: () => {
        this.deleting.set(null);
        this.load(this.search(), this.items().length === 1 ? Math.max(0, this.page() - 1) : this.page());
      },
      error: e => { this.deleting.set(null); this.error.set(getApiError(e)); }
    });
  }
}
