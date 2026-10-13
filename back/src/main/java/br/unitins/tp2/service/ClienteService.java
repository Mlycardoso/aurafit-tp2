package br.unitins.tp2.service;
import br.unitins.tp2.dto.ClienteDTO;
import br.unitins.tp2.model.Cliente;
import jakarta.validation.Valid;
public interface ClienteService {
    Cliente salvar(Long id, @Valid ClienteDTO dto);
}
