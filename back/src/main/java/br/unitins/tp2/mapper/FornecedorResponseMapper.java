package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.FornecedorResponseDTO;
import br.unitins.tp2.dto.FornecedorResponseDTO.EstadoResponse;
import br.unitins.tp2.dto.FornecedorResponseDTO.MunicipioResponse;
import br.unitins.tp2.model.Fornecedor;

public final class FornecedorResponseMapper {
    private FornecedorResponseMapper() {}

    public static FornecedorResponseDTO toResponse(Fornecedor fornecedor) {
        var municipio = fornecedor.municipio;
        var estado = municipio.estado;
        return new FornecedorResponseDTO(
                fornecedor.getId(), fornecedor.nome, fornecedor.email, fornecedor.telefone,
                fornecedor.cep, fornecedor.logradouro, fornecedor.bairro, fornecedor.numero,
                fornecedor.complemento,
                new MunicipioResponse(municipio.getId(), municipio.nome, municipio.codigoIbge,
                        new EstadoResponse(estado.getId(), estado.nome, estado.sigla)),
                fornecedor.cnpj, fornecedor.ativo,
                fornecedor.telefones.isEmpty() ? java.util.List.of(fornecedor.telefone) : java.util.List.copyOf(fornecedor.telefones));
    }
}
