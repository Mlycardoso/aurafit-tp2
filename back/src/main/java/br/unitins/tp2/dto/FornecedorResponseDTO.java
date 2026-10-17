package br.unitins.tp2.dto;
import java.util.List;

public record FornecedorResponseDTO(
        Long id, String nome, String email, String telefone, String cep,
        String logradouro, String bairro, String numero, String complemento,
        MunicipioResponse municipio, String cnpj, boolean ativo, List<String> telefones) {
    public record MunicipioResponse(Long id, String nome, String codigoIbge, EstadoResponse estado) {}
    public record EstadoResponse(Long id, String nome, String sigla) {}
}
