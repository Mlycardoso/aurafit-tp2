package br.unitins.tp2.service;

import java.util.List;
import br.unitins.tp2.dto.*;
import br.unitins.tp2.model.Usuario;
import jakarta.validation.Valid;

public interface UsuarioService {
    Usuario create(@Valid UsuarioRequestDTO dto);
    Usuario update(long id, @Valid UsuarioUpdateDTO dto);
    void delete(long id);
    Usuario findById(long id);
    List<Usuario> findByNome(String nome, int page, int pageSize);
    long count(String nome);
}
