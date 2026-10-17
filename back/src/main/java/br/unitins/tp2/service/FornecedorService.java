package br.unitins.tp2.service;
import java.util.List;
import br.unitins.tp2.dto.FornecedorRequestDTO;
import br.unitins.tp2.model.Fornecedor;
import jakarta.validation.Valid;
public interface FornecedorService {
    Fornecedor create(@Valid FornecedorRequestDTO dto);
    void update(long id, @Valid FornecedorRequestDTO dto);
    void delete(long id);
    Fornecedor findById(long id);
    Fornecedor findByCnpj(String cnpj);
    List<Fornecedor> findAll(int page, int pageSize);
    List<Fornecedor> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);
}
