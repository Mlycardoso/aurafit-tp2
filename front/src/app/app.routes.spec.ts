import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, Routes } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from './app.routes';
import { adminRoutes } from './admin/admin.routes';
import { lojaRoutes } from './loja/loja.routes';
import { AdminLayout } from './admin/admin-layout';

@Component({ template: '' })
class RouteTarget {}

describe('Rotas administrativas', () => {
  beforeEach(() => {
    const stubPages = (pages: Routes): Routes => pages.map(route => route.loadComponent
      ? { ...route, loadComponent: undefined, component: RouteTarget } : route);
    const testRoutes: Routes = routes.map(route => route.path === 'admin'
      ? { ...route, loadComponent: undefined, loadChildren: undefined, component: AdminLayout, children: stubPages(adminRoutes) }
      : route.path === 'loja'
        ? { ...route, loadChildren: undefined, children: stubPages(lojaRoutes) }
        : route);
    TestBed.configureTestingModule({ providers: [provideRouter(testRoutes)] });
  });

  it('separa o menu administrativo da loja', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin/produtos');
    expect(harness.routeNativeElement?.textContent).toContain('Ver loja');
    await harness.navigateByUrl('/loja');
    expect(harness.routeNativeElement?.textContent ?? '').not.toContain('Ver loja');
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
