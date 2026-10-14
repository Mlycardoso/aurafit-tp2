package br.unitins.tp2.dto;
import java.time.LocalDate;
import java.util.List;

public record ClienteResponseDTO(
        Long id, String nome, String email, String telefone, String cep,
        String logradouro, String bairro, String numero, String complemento,
        MunicipioResponse municipio, String cpf, LocalDate dataNascimento, boolean ativo, List<String> telefones) {
    public record MunicipioResponse(Long id, String nome, String codigoIbge, EstadoResponse estado) {}
    public record EstadoResponse(Long id, String nome, String sigla) {}
}
