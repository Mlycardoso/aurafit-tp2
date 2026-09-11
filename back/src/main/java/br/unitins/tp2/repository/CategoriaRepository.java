package br.unitins.tp2.repository;

import br.unitins.tp2.model.Categoria;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CategoriaRepository implements PanacheRepository<Categoria> {

    public Categoria findByNome(String nome) {
        return find("LOWER(nome) = LOWER(?1)", nome).firstResult();
    }

    public Categoria findByNomeExceptId(String nome, Long id) {
        if (id == null) {
            return findByNome(nome);
        }
        return find("LOWER(nome) = LOWER(?1) AND id <> ?2", nome, id).firstResult();
    }

    public PanacheQuery<Categoria> searchByNome(String nome) {
        return find("LOWER(nome) LIKE LOWER(?1) ORDER BY nome", "%" + nome + "%");
    }

    @Override
    public PanacheQuery<Categoria> findAll() {
        return find("ORDER BY nome");
    }
}
