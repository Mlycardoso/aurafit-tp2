package br.unitins.tp2.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
public record ClienteRequestDTO(
    @NotBlank @Size(min=2,max=120) String nome,
    @NotBlank @Email @Size(max=160) String email,
    @NotBlank @Pattern(regexp="[0-9]{10,11}", message="Informe um telefone com DDD.") String telefone,
    @NotBlank @Pattern(regexp="[0-9]{8}", message="Informe um CEP com 8 digitos.") String cep,
    @NotBlank @Size(max=160) String logradouro,
    @NotBlank @Size(max=100) String bairro,
    @NotBlank @Size(max=20) String numero,
    @Size(max=100) String complemento,
    @Pattern(regexp="[0-9]{11}", message="Informe um CPF com 11 digitos.") String cpf,
    @Past(message="O nascimento deve ser anterior a hoje.") LocalDate dataNascimento,
    Boolean ativo,
    @Size(min=1, max=5, message="Informe de 1 a 5 telefones.")
    List<@NotBlank @Pattern(regexp="[0-9]{10,11}", message="Informe telefone com DDD.") String> telefones
) {}
