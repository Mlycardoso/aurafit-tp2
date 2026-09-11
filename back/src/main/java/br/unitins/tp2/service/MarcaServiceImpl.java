package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.MarcaDTO;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Marca;
import br.unitins.tp2.repository.MarcaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class MarcaServiceImpl implements MarcaService {

    @Inject
    MarcaRepository repository;

    @Override
    @Transactional
    public Marca create(MarcaDTO dto) {
        validate(dto, null);
        Marca marca = new Marca();
        copy(dto, marca);
        repository.persist(marca);
        return marca;
    }

    @Override
    @Transactional
    public Marca update(long id, MarcaDTO dto) {
        validate(dto, id);
        Marca marca = findById(id);
        copy(dto, marca);
        return marca;
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (!repository.deleteById(id)) {
            throw new NotFoundException("Marca nao encontrada.");
        }
    }

    @Override
    public Marca findById(long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Marca nao encontrada."));
    }

    @Override
    public List<Marca> findAll(int page, int pageSize) {
        return repository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Marca> findByNome(String nome, int page, int pageSize) {
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

    private void validate(MarcaDTO dto, Long id) {
        if (repository.findByNomeExceptId(dto.nome().trim(), id) != null) {
            throw ValidationException.of("nome", "Ja existe uma marca cadastrada com esse nome.");
        }
    }

    private void copy(MarcaDTO dto, Marca marca) {
        marca.setNome(dto.nome().trim());
        marca.setDescricao(normalize(dto.descricao()));
        marca.setAtivo(dto.ativo());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
