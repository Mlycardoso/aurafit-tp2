package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.ClienteResponseDTO;
import br.unitins.tp2.dto.ClienteResponseDTO.EstadoResponse;
import br.unitins.tp2.dto.ClienteResponseDTO.MunicipioResponse;
import br.unitins.tp2.model.Cliente;

public final class ClienteResponseMapper {
    private ClienteResponseMapper() {}

    public static ClienteResponseDTO toResponse(Cliente cliente) {
        var municipio = cliente.municipio;
        var estado = municipio.estado;
        return new ClienteResponseDTO(
                cliente.getId(), cliente.nome, cliente.email, cliente.telefone,
                cliente.cep, cliente.logradouro, cliente.bairro, cliente.numero,
                cliente.complemento,
                new MunicipioResponse(municipio.getId(), municipio.nome, municipio.codigoIbge,
                        new EstadoResponse(estado.getId(), estado.nome, estado.sigla)));
    }
}
