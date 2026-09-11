export interface Categoria {
  id: number;
  nome: string;
  descricao: string | null;
  ativo: boolean;
}

export type CategoriaPayload = Omit<Categoria, 'id'>;
