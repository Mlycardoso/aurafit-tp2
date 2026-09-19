import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'produtos' },
  {
    path: 'produtos',
    loadComponent: () => import('./produtos/produto-lista/produto-lista').then((m) => m.ProdutoLista),
  },
  {
    path: 'produtos/novo',
    loadComponent: () => import('./produtos/produto-formulario/produto-formulario').then((m) => m.ProdutoFormulario),
  },
  {
    path: 'produtos/editar/:id',
    loadComponent: () => import('./produtos/produto-formulario/produto-formulario').then((m) => m.ProdutoFormulario),
  },
  {
    path: 'marcas',
    loadComponent: () => import('./marcas/marca-lista/marca-lista').then((m) => m.MarcaLista),
  },
  {
    path: 'marcas/novo',
    loadComponent: () => import('./marcas/marca-formulario/marca-formulario').then((m) => m.MarcaFormulario),
  },
  {
    path: 'marcas/editar/:id',
    loadComponent: () => import('./marcas/marca-formulario/marca-formulario').then((m) => m.MarcaFormulario),
  },
  {
    path: 'categorias',
    loadComponent: () => import('./categorias/categoria-lista/categoria-lista').then((m) => m.CategoriaLista),
  },
  {
    path: 'categorias/novo',
    loadComponent: () => import('./categorias/categoria-formulario/categoria-formulario').then((m) => m.CategoriaFormulario),
  },
  {
    path: 'categorias/editar/:id',
    loadComponent: () => import('./categorias/categoria-formulario/categoria-formulario').then((m) => m.CategoriaFormulario),
  },
  { path: '**', redirectTo: 'produtos' },
];
