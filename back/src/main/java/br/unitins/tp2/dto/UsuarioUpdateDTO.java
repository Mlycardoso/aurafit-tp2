package br.unitins.tp2.dto;

import java.util.Set;
import br.unitins.tp2.model.Perfil;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record UsuarioUpdateDTO(
        @NotBlank @Size(min = 2, max = 120) String nome,
        @NotBlank @Email @Size(max = 160) String email,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Size(min = 15, max = 128) String senha,
        @NotNull Boolean ativo,
        @NotEmpty Set<@NotNull Perfil> perfis) {
    @Override
    public String toString() { return "UsuarioUpdateDTO[PROTEGIDO]"; }
}
