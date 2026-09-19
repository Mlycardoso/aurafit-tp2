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
    CategoriaRepository categoriaRepository;

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
        if (!repository.deleteById(id)) {
            throw new NotFoundException("Produto nao encontrado.");
        }
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
        if (repository.findByNomeExceptId(dto.nome().trim(), id) != null) {
            throw ValidationException.of("nome", "Ja existe um produto cadastrado com esse nome.");
        }
    }

    private void copy(ProdutoDTO dto, Produto produto) {
        produto.setNome(dto.nome().trim());
        produto.setDescricao(normalize(dto.descricao()));
        produto.setPreco(dto.preco());
        produto.setEstoque(dto.estoque());
        produto.setAtivo(dto.ativo());
        produto.setCategoria(findCategoria(dto.idCategoria()));
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
