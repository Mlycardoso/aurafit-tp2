package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.MarcaDTO;
import br.unitins.tp2.model.Marca;
import jakarta.validation.Valid;

public interface MarcaService {

    Marca create(@Valid MarcaDTO dto);
    Marca update(long id, @Valid MarcaDTO dto);
    void delete(long id);
    Marca findById(long id);
    List<Marca> findAll(int page, int pageSize);
    List<Marca> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);
}
