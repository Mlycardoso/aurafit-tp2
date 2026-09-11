package br.unitins.tp2.repository;

import br.unitins.tp2.model.Marca;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MarcaRepository implements PanacheRepository<Marca> {

    public Marca findByNome(String nome) {
        return find("LOWER(nome) = LOWER(?1)", nome).firstResult();
    }

    public Marca findByNomeExceptId(String nome, Long id) {
        if (id == null) {
            return findByNome(nome);
        }
        return find("LOWER(nome) = LOWER(?1) AND id <> ?2", nome, id).firstResult();
    }

    public PanacheQuery<Marca> searchByNome(String nome) {
        return find("LOWER(nome) LIKE LOWER(?1) ORDER BY nome", "%" + nome + "%");
    }

    @Override
    public PanacheQuery<Marca> findAll() {
        return find("ORDER BY nome");
    }
}
