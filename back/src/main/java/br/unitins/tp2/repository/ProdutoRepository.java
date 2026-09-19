package br.unitins.tp2.repository;

import br.unitins.tp2.model.Produto;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProdutoRepository implements PanacheRepository<Produto> {

    public Produto findByNome(String nome) {
        return find("LOWER(nome) = LOWER(?1)", nome).firstResult();
    }

    public Produto findByNomeExceptId(String nome, Long id) {
        if (id == null) {
            return findByNome(nome);
        }
        return find("LOWER(nome) = LOWER(?1) AND id <> ?2", nome, id).firstResult();
    }

    public PanacheQuery<Produto> searchByNome(String nome) {
        return find("LOWER(nome) LIKE LOWER(?1) ORDER BY nome", "%" + nome + "%");
    }

    @Override
    public PanacheQuery<Produto> findAll() {
        return find("ORDER BY nome");
    }
}
