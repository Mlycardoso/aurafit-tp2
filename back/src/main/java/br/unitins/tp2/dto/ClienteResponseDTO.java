package br.unitins.tp2.dto;

public record ClienteResponseDTO(
        Long id, String nome, String email, String telefone, String cep,
        String logradouro, String bairro, String numero, String complemento,
        MunicipioResponse municipio) {
    public record MunicipioResponse(Long id, String nome, String codigoIbge, EstadoResponse estado) {}
    public record EstadoResponse(Long id, String nome, String sigla) {}
}
