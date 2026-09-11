export interface Marca {
  id: number;
  nome: string;
  descricao: string | null;
  ativo: boolean;
}

export type MarcaPayload = Omit<Marca, 'id'>;
