package br.unitins.tp2.dto;

import java.math.BigDecimal;

import br.unitins.tp2.model.Produto;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer estoque,
        Boolean ativo,
        CategoriaResponseDTO categoria) {

    public static ProdutoResponseDTO valueOf(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getEstoque(),
                produto.getAtivo(),
                CategoriaResponseDTO.valueOf(produto.getCategoria()));
    }
}
