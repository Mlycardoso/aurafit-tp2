package br.unitins.tp2.service;
import java.util.List;
import br.unitins.tp2.dto.ClienteRequestDTO;
import br.unitins.tp2.model.Cliente;
import jakarta.validation.Valid;
public interface ClienteService {
    Cliente create(@Valid ClienteRequestDTO dto);
    void update(long id, @Valid ClienteRequestDTO dto);
    void delete(long id);
    Cliente findById(long id);
    Cliente findByCpf(String cpf);
    List<Cliente> findAll(int page, int pageSize);
    List<Cliente> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);
}
