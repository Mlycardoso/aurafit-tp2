import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'admin/usuarios',
    loadComponent: () => import('./usuarios/usuario-lista').then((m) => m.UsuarioLista),
  },
  {
    path: 'admin/usuarios/novo',
    loadComponent: () => import('./usuarios/usuario-formulario').then((m) => m.UsuarioFormulario),
  },
  {
    path: 'admin/usuarios/editar/:id',
    loadComponent: () => import('./usuarios/usuario-formulario').then((m) => m.UsuarioFormulario),
  },
  {
    path: 'admin/clientes',
    loadComponent: () => import('./clientes/cliente-lista').then((m) => m.ClienteLista),
  },
  {
    path: 'admin/clientes/novo',
    loadComponent: () => import('./clientes/cliente-formulario').then((m) => m.ClienteFormulario),
  },
  {
    path: 'admin/clientes/editar/:id',
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
    path: 'admin/produtos',
    loadComponent: () =>
      import('./produtos/produto-lista/produto-lista').then((m) => m.ProdutoLista),
  },
  {
    path: 'admin/produtos/novo',
    loadComponent: () =>
      import('./produtos/produto-formulario/produto-formulario').then((m) => m.ProdutoFormulario),
  },
  {
    path: 'admin/produtos/editar/:id',
    loadComponent: () =>
      import('./produtos/produto-formulario/produto-formulario').then((m) => m.ProdutoFormulario),
  },
  {
    path: 'admin/marcas',
    loadComponent: () => import('./marcas/marca-lista/marca-lista').then((m) => m.MarcaLista),
  },
  {
    path: 'admin/marcas/novo',
    loadComponent: () =>
      import('./marcas/marca-formulario/marca-formulario').then((m) => m.MarcaFormulario),
  },
  {
    path: 'admin/marcas/editar/:id',
    loadComponent: () =>
      import('./marcas/marca-formulario/marca-formulario').then((m) => m.MarcaFormulario),
  },
  {
    path: 'admin/categorias',
    loadComponent: () =>
      import('./categorias/categoria-lista/categoria-lista').then((m) => m.CategoriaLista),
  },
  {
    path: 'admin/categorias/novo',
    loadComponent: () =>
      import('./categorias/categoria-formulario/categoria-formulario').then(
        (m) => m.CategoriaFormulario,
      ),
  },
  {
    path: 'admin/categorias/editar/:id',
    loadComponent: () =>
      import('./categorias/categoria-formulario/categoria-formulario').then(
        (m) => m.CategoriaFormulario,
      ),
  },
  { path: 'admin', pathMatch: 'full', redirectTo: 'admin/produtos' },
  ...['produtos', 'marcas', 'categorias', 'clientes', 'usuarios'].flatMap((cadastro) => [
    { path: cadastro, pathMatch: 'full' as const, redirectTo: `admin/${cadastro}` },
    { path: `${cadastro}/novo`, pathMatch: 'full' as const, redirectTo: `admin/${cadastro}/novo` },
    { path: `${cadastro}/editar/:id`, pathMatch: 'full' as const, redirectTo: `admin/${cadastro}/editar/:id` },
  ]),
  { path: 'admin', children: [{ path: '**', redirectTo: '/admin/produtos' }] },
  { path: '**', redirectTo: 'loja' },
];
