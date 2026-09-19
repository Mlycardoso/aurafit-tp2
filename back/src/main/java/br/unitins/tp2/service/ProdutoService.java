package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.ProdutoDTO;
import br.unitins.tp2.model.Produto;
import jakarta.validation.Valid;

public interface ProdutoService {

    Produto create(@Valid ProdutoDTO dto);
    Produto update(long id, @Valid ProdutoDTO dto);
    void delete(long id);
    Produto findById(long id);
    List<Produto> findAll(int page, int pageSize);
    List<Produto> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);
}
