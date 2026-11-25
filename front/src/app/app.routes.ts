import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './auth/auth.guards';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'loja' },
  {
    path: 'admin',
    title: 'AuraFit | Administração',
    loadComponent: () => import('./admin/admin-layout').then(m => m.AdminLayout),
    loadChildren: () => import('./admin/admin.routes').then(m => m.adminRoutes),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
  },
  {
    path: 'loja',
    title: 'AuraFit | Whey protein',
    loadChildren: () => import('./loja/loja.routes').then(m => m.lojaRoutes),
  },
  { path: 'login', title: 'AuraFit | Entrar', canActivate: [guestGuard], loadComponent: () => import('./auth/login').then(m => m.Login) },
  { path: 'registro', title: 'AuraFit | Criar conta', canActivate: [guestGuard], loadComponent: () => import('./auth/registro').then(m => m.Registro) },
  { path: 'acesso-negado', title: 'AuraFit | Acesso negado', loadComponent: () => import('./auth/acesso-negado').then(m => m.AcessoNegado) },
  ...['produtos', 'marcas', 'categorias', 'clientes', 'usuarios'].flatMap(cadastro => [
    { path: cadastro, pathMatch: 'full' as const, redirectTo: `admin/${cadastro}` },
    { path: `${cadastro}/novo`, pathMatch: 'full' as const, redirectTo: `admin/${cadastro}/novo` },
    { path: `${cadastro}/editar/:id`, pathMatch: 'full' as const, redirectTo: `admin/${cadastro}/editar/:id` },
  ]),
  { path: '**', redirectTo: 'loja' },
];
