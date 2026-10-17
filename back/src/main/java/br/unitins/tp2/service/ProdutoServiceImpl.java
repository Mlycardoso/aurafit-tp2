package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.ProdutoDTO;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Categoria;
import br.unitins.tp2.model.Produto;
import br.unitins.tp2.repository.CategoriaRepository;
import br.unitins.tp2.repository.ProdutoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class ProdutoServiceImpl implements ProdutoService {

    @Inject
    ProdutoRepository repository;

    @Inject
    br.unitins.tp2.repository.FornecedorRepository fornecedorRepository;

    @Inject
    CategoriaRepository categoriaRepository;

    @Inject
    br.unitins.tp2.repository.MarcaRepository marcaRepository;

    @Override
    @Transactional
    public Produto create(ProdutoDTO dto) {
        validate(dto, null);
        Produto produto = new Produto();
        copy(dto, produto);
        repository.persist(produto);
        return produto;
    }

    @Override
    @Transactional
    public Produto update(long id, ProdutoDTO dto) {
        validate(dto, id);
        Produto produto = findById(id);
        copy(dto, produto);
        return produto;
    }

    @Override
    @Transactional
    public void delete(long id) {
        findById(id).setAtivo(false);
    }

    @Override
    public Produto findById(long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Produto nao encontrado."));
    }

    @Override
    public List<Produto> findAll(int page, int pageSize) {
        return repository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Produto> findByNome(String nome, int page, int pageSize) {
        return repository.searchByNome(nome).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public long count(String nome) {
        return repository.searchByNome(nome).count();
    }

    private void validate(ProdutoDTO dto, Long id) {
        if (dto.sku() != null) {
            Produto existente = repository.find("upper(sku)", dto.sku().toUpperCase(java.util.Locale.ROOT)).firstResult();
            if (existente != null && !existente.getId().equals(id))
                throw ValidationException.of("sku", "Ja existe um produto com este codigo.");
        }
        if (repository.findByNomeExceptId(dto.nome().trim(), id) != null) {
            throw ValidationException.of("nome", "Ja existe um produto cadastrado com esse nome.");
        }
    }

    private void copy(ProdutoDTO dto, Produto produto) {
        if (dto.idsFornecedores() != null) {
            if (dto.idsFornecedores().stream().distinct().count() != dto.idsFornecedores().size()) {
                throw ValidationException.of("idsFornecedores", "Nao repita o mesmo fornecedor.");
            }
            var fornecedores = dto.idsFornecedores().stream().map(id ->
                    fornecedorRepository.findByIdOptional(id).orElseThrow(() ->
                            ValidationException.of("idsFornecedores", "Fornecedor nao encontrado."))).toList();
            produto.getFornecedores().clear();
            produto.getFornecedores().addAll(fornecedores);
        }
        if (dto.sku() != null) produto.setSku(dto.sku().toUpperCase(java.util.Locale.ROOT));
        if (produto.getSku() == null) produto.setSku("AF-" + java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase(java.util.Locale.ROOT));
        produto.setSabor(normalize(dto.sabor()));
        produto.setPesoGramas(dto.pesoGramas());
        produto.setNome(dto.nome().trim());
        produto.setDescricao(normalize(dto.descricao()));
        produto.setPreco(dto.preco());
        produto.setEstoque(dto.estoque());
        produto.setAtivo(dto.ativo());
        produto.setCategoria(findCategoria(dto.idCategoria()));
        if (dto.idMarca() == null) {
            produto.setMarca(null);
        } else {
            var marca = marcaRepository.findById(dto.idMarca());
            if (marca == null) {
                throw ValidationException.of("idMarca", "Marca nao encontrada.");
            }
            produto.setMarca(marca);
        }
    }

    private Categoria findCategoria(Long idCategoria) {
        Categoria categoria = categoriaRepository.findById(idCategoria);
        if (categoria == null) {
            throw ValidationException.of("idCategoria", "Categoria nao encontrada.");
        }
        return categoria;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
