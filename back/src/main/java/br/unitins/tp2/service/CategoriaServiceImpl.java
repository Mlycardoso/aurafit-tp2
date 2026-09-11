package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.CategoriaDTO;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Categoria;
import br.unitins.tp2.repository.CategoriaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class CategoriaServiceImpl implements CategoriaService {

    @Inject
    CategoriaRepository repository;

    @Override
    @Transactional
    public Categoria create(CategoriaDTO dto) {
        validate(dto, null);
        Categoria categoria = new Categoria();
        copy(dto, categoria);
        repository.persist(categoria);
        return categoria;
    }

    @Override
    @Transactional
    public Categoria update(long id, CategoriaDTO dto) {
        validate(dto, id);
        Categoria categoria = findById(id);
        copy(dto, categoria);
        return categoria;
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (!repository.deleteById(id)) {
            throw new NotFoundException("Categoria nao encontrada.");
        }
    }

    @Override
    public Categoria findById(long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Categoria nao encontrada."));
    }

    @Override
    public List<Categoria> findAll(int page, int pageSize) {
        return repository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Categoria> findByNome(String nome, int page, int pageSize) {
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

    private void validate(CategoriaDTO dto, Long id) {
        if (repository.findByNomeExceptId(dto.nome().trim(), id) != null) {
            throw ValidationException.of("nome", "Ja existe uma categoria cadastrada com esse nome.");
        }
    }

    private void copy(CategoriaDTO dto, Categoria categoria) {
        categoria.setNome(dto.nome().trim());
        categoria.setDescricao(normalize(dto.descricao()));
        categoria.setAtivo(dto.ativo());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
