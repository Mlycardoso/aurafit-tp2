import { Categoria } from '../categorias/categoria.model';

export interface Produto {
  id: number;
  nome: string;
  descricao: string | null;
  preco: number;
  estoque: number;
  ativo: boolean;
  categoria: Categoria;
}

export interface ProdutoPayload {
  nome: string;
  descricao: string | null;
  preco: number;
  estoque: number;
  ativo: boolean;
  idCategoria: number;
}
