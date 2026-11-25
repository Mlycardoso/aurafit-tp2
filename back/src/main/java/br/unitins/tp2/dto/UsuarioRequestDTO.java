package br.unitins.tp2.dto;

import java.util.Set;
import br.unitins.tp2.model.Perfil;
import jakarta.validation.constraints.*;

public record UsuarioRequestDTO(
        @NotBlank @Size(min = 2, max = 120) String nome,
        @NotBlank @Email @Size(max = 160) String email,
        @NotNull Boolean ativo,
        @NotEmpty Set<@NotNull Perfil> perfis,
        @Positive Long idCliente) {
    @Override
    public String toString() { return "UsuarioRequestDTO[PROTEGIDO]"; }
}
