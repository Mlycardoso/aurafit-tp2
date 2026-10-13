import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Cliente, ClienteService } from './cliente.service';
import { getApiError } from '../shared/api-error';
@Component({
  imports: [RouterLink],
  template: `
    <section class="page-header"><h1>Clientes</h1>
      <a class="btn btn-primary" routerLink="/clientes/novo"><img src="icons/plus.svg" alt="" />Novo cliente</a>
    </section>
    @if (error()) { <div class="alert error" role="alert">{{error()}}</div> }
    <section class="toolbar"><div class="search-box">
      <img src="icons/search.svg" alt="" />
      <input #busca aria-label="Buscar cliente" placeholder="Buscar cliente pelo nome" (keyup.enter)="load(busca.value, 0)" />
      <button class="btn btn-secondary" (click)="load(busca.value, 0)">Buscar</button>
    </div><span>{{total()}} registro(s)</span></section>
    <section class="data-panel">
    @if (loading()) { <div class="loading-state">Carregando clientes...</div> }
    @else {
      <table><thead><tr><th>Nome</th><th>E-mail</th><th>Município / UF</th><th>Ações</th></tr></thead><tbody>
      @for (c of items(); track c.id) { <tr><td>{{c.nome}}</td><td>{{c.email}}</td>
      <td>{{c.municipio.nome}} / {{c.municipio.estado.sigla}}</td>
      <td><a class="icon-button" [routerLink]="['/clientes/editar', c.id]" title="Editar cliente"><img src="icons/pencil.svg" alt="Editar" /></a></td></tr> }
      </tbody></table>
      @if (!items().length) { <div class="empty-state">Nenhum cliente encontrado.</div> }
    }</section>
    <nav class="pagination" aria-label="Páginas de clientes">
      <button class="btn btn-secondary" [disabled]="loading() || page() === 0" (click)="load(filtro, page()-1)">Anterior</button>
      <span>Página {{page()+1}} de {{pages() || 1}}</span>
      <button class="btn btn-secondary" [disabled]="loading() || page()+1 >= pages()" (click)="load(filtro, page()+1)">Próxima</button>
    </nav>`
})
export class ClienteLista {
  private service = inject(ClienteService);
  protected items = signal<Cliente[]>([]);
  protected loading = signal(false); protected error = signal('');
  protected page = signal(0); protected pages = signal(0); protected total = signal(0);
  protected filtro = '';
  constructor() { this.load(); }
  protected load(nome = '', page = 0) {
    if (this.loading()) return;
    this.filtro = nome; this.loading.set(true); this.error.set('');
    this.service.listar(nome, page).subscribe({
      next: r => { this.items.set(r.items); this.page.set(r.page); this.pages.set(r.totalPages); this.total.set(r.totalItems); this.loading.set(false); },
      error: e => { this.error.set(getApiError(e)); this.loading.set(false); }
    });
  }
}
