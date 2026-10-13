package br.unitins.tp2.service;
import br.unitins.tp2.dto.*;
import br.unitins.tp2.model.*;
import br.unitins.tp2.repository.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
@ApplicationScoped
public class ClienteServiceImpl implements ClienteService {
    @Inject ClienteRepository clientes;
    @Inject MunicipioRepository municipios;
    @Inject EstadoRepository estados;
    @Inject CepService ceps;
    @Override
    @Transactional
    public Cliente salvar(Long id, ClienteDTO dto) {
        Cliente cliente = id == null ? new Cliente() : clientes.findByIdOptional(id).orElseThrow(NotFoundException::new);
        // A cidade e o estado sao confirmados pelo servidor, nunca aceitos do formulario.
        CepDTO cep = ceps.consultar(dto.cep());
        Estado estado = estados.find("sigla", cep.uf()).firstResult();
        if (estado == null) {
            estado = new Estado(); estado.sigla = cep.uf(); estado.nome = cep.estado(); estados.persist(estado);
        }
        Municipio municipio = municipios.find("codigoIbge", cep.codigoIbge()).firstResult();
        if (municipio == null) {
            municipio = new Municipio(); municipio.codigoIbge = cep.codigoIbge();
            municipio.nome = cep.municipio(); municipio.estado = estado; municipios.persist(municipio);
        }
        cliente.nome = dto.nome().trim(); cliente.email = dto.email().trim();
        cliente.telefone = dto.telefone(); cliente.cep = dto.cep();
        cliente.logradouro = dto.logradouro().trim(); cliente.bairro = dto.bairro().trim();
        cliente.numero = dto.numero().trim(); cliente.complemento = dto.complemento() == null ? "" : dto.complemento().trim();
        cliente.municipio = municipio;
        if (id == null) clientes.persist(cliente);
        return cliente;
    }
}
