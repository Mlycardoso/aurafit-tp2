package br.unitins.tp2.service;
import java.util.List;
import java.util.Locale;
import br.unitins.tp2.dto.*;
import br.unitins.tp2.model.*;
import br.unitins.tp2.repository.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import br.unitins.tp2.exception.ValidationException;
@ApplicationScoped
public class ClienteServiceImpl implements ClienteService {
    @Inject ClienteRepository clientes;
    @Inject MunicipioRepository municipios;
    @Inject EstadoRepository estados;
    @Inject CepService ceps;
    @Inject UsuarioRepository usuarios;
    @Override
    @Transactional
    public Cliente create(ClienteRequestDTO dto) {
        return salvar(null, dto);
    }
    @Override
    @Transactional
    public void update(long id, ClienteRequestDTO dto) {
        salvar(id, dto);
    }
    @Override
    @Transactional
    public void delete(long id) {
        if (usuarios.count("cliente.id", id) > 0) {
            throw ValidationException.of("cliente", "Desvincule o usuario antes de excluir este cliente.");
        }
        if (!clientes.deleteById(id)) throw new NotFoundException();
    }
    @Override
    public Cliente findById(long id) {
        return clientes.findByIdOptional(id).orElseThrow(NotFoundException::new);
    }
    @Override
    public Cliente findByCpf(String cpf) {
        return clientes.find("cpf", cpf).firstResultOptional().orElseThrow(NotFoundException::new);
    }
    @Override
    public List<Cliente> findAll(int page, int pageSize) {
        return clientes.find("order by nome, id").page(page, pageSize).list();
    }
    @Override
    public List<Cliente> findByNome(String nome, int page, int pageSize) {
        return clientes.find("lower(nome) like ?1 order by nome, id", filtro(nome))
                .page(page, pageSize).list();
    }
    @Override
    public long count() { return clientes.count(); }
    @Override
    public long count(String nome) { return clientes.count("lower(nome) like ?1", filtro(nome)); }
    private String filtro(String nome) {
        return "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
    }
    private Cliente salvar(Long id, ClienteRequestDTO dto) {
        Cliente cliente = id == null ? new Cliente() : clientes.findByIdOptional(id).orElseThrow(NotFoundException::new);
        if (dto.cpf() != null) {
            Cliente existente = clientes.find("cpf", dto.cpf()).firstResult();
            if (existente != null && !existente.getId().equals(id)) {
                throw ValidationException.of("cpf", "Ja existe um cliente com este CPF.");
            }
        }
        if (dto.telefones() != null && dto.telefones().stream().distinct().count() != dto.telefones().size()) {
            throw ValidationException.of("telefones", "Nao repita o mesmo telefone.");
        }
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
        cliente.cpf = dto.cpf();
        cliente.dataNascimento = dto.dataNascimento();
        if (dto.ativo() != null) cliente.ativo = dto.ativo();
        cliente.telefones.clear();
        cliente.telefones.addAll(dto.telefones() == null ? List.of(dto.telefone()) : dto.telefones());
        cliente.telefone = cliente.telefones.getFirst(); cliente.cep = dto.cep();
        cliente.logradouro = dto.logradouro().trim(); cliente.bairro = dto.bairro().trim();
        cliente.numero = dto.numero().trim(); cliente.complemento = dto.complemento() == null ? "" : dto.complemento().trim();
        cliente.municipio = municipio;
        if (id == null) clientes.persist(cliente);
        return cliente;
    }
}
