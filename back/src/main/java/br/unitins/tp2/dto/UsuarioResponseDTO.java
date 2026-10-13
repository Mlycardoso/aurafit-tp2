package br.unitins.tp2.dto;

import java.util.Set;
import br.unitins.tp2.model.Perfil;

public record UsuarioResponseDTO(Long id, String nome, String email, boolean ativo, Set<Perfil> perfis) {}
