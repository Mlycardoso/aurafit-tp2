import { Routes } from '@angular/router';

export const lojaRoutes: Routes = [
  { path: '', loadComponent: () => import('./loja').then(m => m.Loja) },
  { path: 'produto/:id', loadComponent: () => import('./loja').then(m => m.Loja) },
  { path: 'carrinho', loadComponent: () => import('./loja').then(m => m.Loja) },
  { path: 'favoritos', loadComponent: () => import('./loja').then(m => m.Loja) },
  { path: 'checkout', loadComponent: () => import('./loja').then(m => m.Loja) },
  { path: 'conta', loadComponent: () => import('./loja').then(m => m.Loja) },
  { path: '**', redirectTo: '' },
];
