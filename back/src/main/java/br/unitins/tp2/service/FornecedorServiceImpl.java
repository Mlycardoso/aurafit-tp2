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
public class FornecedorServiceImpl implements FornecedorService {
    @Inject FornecedorRepository fornecedores;
    @Inject MunicipioRepository municipios;
    @Inject EstadoRepository estados;
    @Inject CepService ceps;
    @Inject ProdutoRepository produtos;
    @Override
    @Transactional
    public Fornecedor create(FornecedorRequestDTO dto) {
        return salvar(null, dto);
    }
    @Override
    @Transactional
    public void update(long id, FornecedorRequestDTO dto) {
        salvar(id, dto);
    }
    @Override
    @Transactional
    public void delete(long id) {
        if (produtos.count("select count(p) from Produto p join p.fornecedores f where f.id = ?1", id) > 0) {
            throw ValidationException.of("fornecedor", "Desvincule os produtos antes de excluir este fornecedor.");
        }
        if (!fornecedores.deleteById(id)) throw new NotFoundException();
    }
    @Override
    public Fornecedor findById(long id) {
        return fornecedores.findByIdOptional(id).orElseThrow(NotFoundException::new);
    }
    @Override
    public Fornecedor findByCnpj(String cnpj) {
        return fornecedores.find("cnpj", cnpj).firstResultOptional().orElseThrow(NotFoundException::new);
    }
    @Override
    public List<Fornecedor> findAll(int page, int pageSize) {
        return fornecedores.find("order by nome, id").page(page, pageSize).list();
    }
    @Override
    public List<Fornecedor> findByNome(String nome, int page, int pageSize) {
        return fornecedores.find("lower(nome) like ?1 order by nome, id", filtro(nome))
                .page(page, pageSize).list();
    }
    @Override
    public long count() { return fornecedores.count(); }
    @Override
    public long count(String nome) { return fornecedores.count("lower(nome) like ?1", filtro(nome)); }
    private String filtro(String nome) {
        return "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
    }
    private Fornecedor salvar(Long id, FornecedorRequestDTO dto) {
        Fornecedor fornecedor = id == null ? new Fornecedor() : fornecedores.findByIdOptional(id).orElseThrow(NotFoundException::new);
        if (dto.cnpj() != null) {
            Fornecedor existente = fornecedores.find("cnpj", dto.cnpj()).firstResult();
            if (existente != null && !existente.getId().equals(id)) {
                throw ValidationException.of("cnpj", "Ja existe um fornecedor com este CNPJ.");
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
        fornecedor.nome = dto.nome().trim(); fornecedor.email = dto.email().trim();
        fornecedor.cnpj = dto.cnpj();
        if (dto.ativo() != null) fornecedor.ativo = dto.ativo();
        fornecedor.telefones.clear();
        fornecedor.telefones.addAll(dto.telefones() == null ? List.of(dto.telefone()) : dto.telefones());
        fornecedor.telefone = fornecedor.telefones.getFirst(); fornecedor.cep = dto.cep();
        fornecedor.logradouro = dto.logradouro().trim(); fornecedor.bairro = dto.bairro().trim();
        fornecedor.numero = dto.numero().trim(); fornecedor.complemento = dto.complemento() == null ? "" : dto.complemento().trim();
        fornecedor.municipio = municipio;
        if (id == null) fornecedores.persist(fornecedor);
        return fornecedor;
    }
}
