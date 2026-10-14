package br.unitins.tp2.mapper;

import java.util.Set;
import br.unitins.tp2.dto.UsuarioResponseDTO;
import br.unitins.tp2.model.Usuario;

public final class UsuarioResponseMapper {
    private UsuarioResponseMapper() {}
    public static UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(usuario.getId(), usuario.nome, usuario.email,
                usuario.ativo, Set.copyOf(usuario.perfis),
                usuario.cliente == null ? null : ClienteResponseMapper.toResponse(usuario.cliente));
    }
}
