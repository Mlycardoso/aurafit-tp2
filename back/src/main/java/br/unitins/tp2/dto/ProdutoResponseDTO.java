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
        CategoriaResponseDTO categoria,
        MarcaResponseDTO marca,
        java.util.List<FornecedorResumo> fornecedores) {

    public record FornecedorResumo(Long id, String nome, boolean ativo) {}

    public static ProdutoResponseDTO valueOf(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getEstoque(),
                produto.getAtivo(),
                CategoriaResponseDTO.valueOf(produto.getCategoria()),
                produto.getMarca() == null ? null : MarcaResponseDTO.valueOf(produto.getMarca()),
                produto.getFornecedores().stream()
                        .map(f -> new FornecedorResumo(f.getId(), f.nome, f.ativo)).toList());
    }
}
