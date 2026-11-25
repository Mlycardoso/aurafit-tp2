import { Routes } from '@angular/router';
import { authGuard, roleGuard } from '../auth/auth.guards';

export const lojaRoutes: Routes = [
  { path: '', loadComponent: () => import('./loja').then(m => m.Loja) },
  { path: 'produto/:id', loadComponent: () => import('./loja').then(m => m.Loja) },
  { path: 'carrinho', loadComponent: () => import('./loja').then(m => m.Loja) },
  { path: 'favoritos', loadComponent: () => import('./loja').then(m => m.Loja) },
  { path: 'checkout', canActivate: [authGuard, roleGuard], data: { roles: ['CLIENTE'] }, loadComponent: () => import('./loja').then(m => m.Loja) },
  { path: 'meus-pedidos', canActivate: [authGuard, roleGuard], data: { roles: ['CLIENTE'] }, loadComponent: () => import('./meus-pedidos').then(m => m.MeusPedidos) },
  { path: 'conta', loadComponent: () => import('./loja').then(m => m.Loja) },
  { path: '**', redirectTo: '' },
];
