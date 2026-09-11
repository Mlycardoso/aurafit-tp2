package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MarcaDTO(
        @NotBlank(message = "O nome deve ser informado.")
        @Length(min = 2, max = 80, message = "O nome deve conter entre 2 e 80 caracteres.")
        String nome,

        @Length(max = 255, message = "A descricao deve conter no maximo 255 caracteres.")
        String descricao,

        @NotNull(message = "A situacao deve ser informada.")
        Boolean ativo) {
}
