import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, Routes } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from './app.routes';

@Component({ template: '' })
class RouteTarget {}

describe('Rotas administrativas', () => {
  beforeEach(() => {
    const testRoutes: Routes = routes.map(route => route.loadComponent
      ? { ...route, loadComponent: undefined, component: RouteTarget }
      : route);
    TestBed.configureTestingModule({ providers: [provideRouter(testRoutes)] });
  });

  for (const cadastro of ['produtos', 'marcas', 'categorias', 'clientes', 'usuarios']) {
    for (const sufixo of ['', '/novo', '/editar/42']) {
      it(`preserva o endereco antigo /${cadastro}${sufixo}`, async () => {
        const harness = await RouterTestingHarness.create();
        await harness.navigateByUrl(`/${cadastro}${sufixo}`);
        expect(TestBed.inject(Router).url).toBe(`/admin/${cadastro}${sufixo}`);
      });
    }
  }

  for (const [origem, destino] of [
    ['/admin', '/admin/produtos'],
    ['/admin/inexistente', '/admin/produtos'],
    ['/', '/loja'],
    ['/inexistente', '/loja'],
  ]) {
    it(`direciona ${origem} para ${destino}`, async () => {
      const harness = await RouterTestingHarness.create();
      await harness.navigateByUrl(origem);
      expect(TestBed.inject(Router).url).toBe(destino);
    });
  }
});
