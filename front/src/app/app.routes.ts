import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'usuarios',
    loadComponent: () => import('./usuarios/usuario-lista').then((m) => m.UsuarioLista),
  },
  {
    path: 'usuarios/novo',
    loadComponent: () => import('./usuarios/usuario-formulario').then((m) => m.UsuarioFormulario),
  },
  {
    path: 'usuarios/editar/:id',
    loadComponent: () => import('./usuarios/usuario-formulario').then((m) => m.UsuarioFormulario),
  },
  {
    path: 'clientes',
    loadComponent: () => import('./clientes/cliente-lista').then((m) => m.ClienteLista),
  },
  {
    path: 'clientes/novo',
    loadComponent: () => import('./clientes/cliente-formulario').then((m) => m.ClienteFormulario),
  },
  {
    path: 'clientes/editar/:id',
    loadComponent: () => import('./clientes/cliente-formulario').then((m) => m.ClienteFormulario),
  },
  { path: '', pathMatch: 'full', redirectTo: 'loja' },
  { path: 'loja', loadComponent: () => import('./loja/loja').then((m) => m.Loja) },
  { path: 'loja/produto/:id', loadComponent: () => import('./loja/loja').then((m) => m.Loja) },
  { path: 'loja/carrinho', loadComponent: () => import('./loja/loja').then((m) => m.Loja) },
  { path: 'loja/favoritos', loadComponent: () => import('./loja/loja').then((m) => m.Loja) },
  { path: 'loja/checkout', loadComponent: () => import('./loja/loja').then((m) => m.Loja) },
  { path: 'loja/conta', loadComponent: () => import('./loja/loja').then((m) => m.Loja) },
  {
    path: 'produtos',
    loadComponent: () =>
      import('./produtos/produto-lista/produto-lista').then((m) => m.ProdutoLista),
  },
  {
    path: 'produtos/novo',
    loadComponent: () =>
      import('./produtos/produto-formulario/produto-formulario').then((m) => m.ProdutoFormulario),
  },
  {
    path: 'produtos/editar/:id',
    loadComponent: () =>
      import('./produtos/produto-formulario/produto-formulario').then((m) => m.ProdutoFormulario),
  },
  {
    path: 'marcas',
    loadComponent: () => import('./marcas/marca-lista/marca-lista').then((m) => m.MarcaLista),
  },
  {
    path: 'marcas/novo',
    loadComponent: () =>
      import('./marcas/marca-formulario/marca-formulario').then((m) => m.MarcaFormulario),
  },
  {
    path: 'marcas/editar/:id',
    loadComponent: () =>
      import('./marcas/marca-formulario/marca-formulario').then((m) => m.MarcaFormulario),
  },
  {
    path: 'categorias',
    loadComponent: () =>
      import('./categorias/categoria-lista/categoria-lista').then((m) => m.CategoriaLista),
  },
  {
    path: 'categorias/novo',
    loadComponent: () =>
      import('./categorias/categoria-formulario/categoria-formulario').then(
        (m) => m.CategoriaFormulario,
      ),
  },
  {
    path: 'categorias/editar/:id',
    loadComponent: () =>
      import('./categorias/categoria-formulario/categoria-formulario').then(
        (m) => m.CategoriaFormulario,
      ),
  },
  { path: '**', redirectTo: 'produtos' },
];
