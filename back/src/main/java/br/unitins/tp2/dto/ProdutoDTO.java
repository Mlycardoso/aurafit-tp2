package br.unitins.tp2.dto;

import java.math.BigDecimal;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProdutoDTO(
        @NotBlank(message = "O nome deve ser informado.")
        @Length(min = 2, max = 80, message = "O nome deve conter entre 2 e 80 caracteres.")
        String nome,

        @Length(max = 255, message = "A descricao deve conter no maximo 255 caracteres.")
        String descricao,

        @NotNull(message = "O preco deve ser informado.")
        @DecimalMin(value = "0.01", message = "O preco deve ser maior que zero.")
        @Digits(integer = 8, fraction = 2, message = "O preco deve possuir no maximo duas casas decimais.")
        BigDecimal preco,

        @NotNull(message = "O estoque deve ser informado.")
        @Min(value = 0, message = "O estoque nao pode ser negativo.")
        Integer estoque,

        @NotNull(message = "A situacao deve ser informada.")
        Boolean ativo,

        @NotNull(message = "A categoria deve ser informada.")
        Long idCategoria) {
}
