package br.unitins.tp2.dto;

import br.unitins.tp2.model.Categoria;

public record CategoriaResponseDTO(Long id, String nome, String descricao, Boolean ativo) {

    public static CategoriaResponseDTO valueOf(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(), categoria.getNome(), categoria.getDescricao(), categoria.getAtivo());
    }
}
