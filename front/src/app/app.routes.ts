import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'loja' },
  {
    path: 'admin',
    title: 'AuraFit | Administração',
    loadComponent: () => import('./admin/admin-layout').then(m => m.AdminLayout),
    loadChildren: () => import('./admin/admin.routes').then(m => m.adminRoutes),
  },
  {
    path: 'loja',
    title: 'AuraFit | Whey protein',
    loadChildren: () => import('./loja/loja.routes').then(m => m.lojaRoutes),
  },
  ...['produtos', 'marcas', 'categorias', 'clientes', 'usuarios'].flatMap(cadastro => [
    { path: cadastro, pathMatch: 'full' as const, redirectTo: `admin/${cadastro}` },
    { path: `${cadastro}/novo`, pathMatch: 'full' as const, redirectTo: `admin/${cadastro}/novo` },
    { path: `${cadastro}/editar/:id`, pathMatch: 'full' as const, redirectTo: `admin/${cadastro}/editar/:id` },
  ]),
  { path: '**', redirectTo: 'loja' },
];
