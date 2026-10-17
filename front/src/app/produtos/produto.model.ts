import { Categoria } from '../categorias/categoria.model';
import { Marca } from '../marcas/marca.model';

export interface Produto {
  id: number;
  sku?: string | null;
  sabor?: string | null;
  pesoGramas?: number | null;
  nome: string;
  descricao: string | null;
  preco: number;
  estoque: number;
  ativo: boolean;
  categoria: Categoria;
  marca?: Marca | null;
  fornecedores?: { id: number; nome: string; ativo: boolean }[];
}

export interface ProdutoPayload {
  sku?: string | null;
  sabor?: string | null;
  pesoGramas?: number | null;
  nome: string;
  descricao: string | null;
  preco: number;
  estoque: number;
  ativo: boolean;
  idCategoria: number;
  idMarca?: number | null;
  idsFornecedores?: number[];
}
