package br.unitins.tp2.repository;
import br.unitins.tp2.model.Fornecedor;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class FornecedorRepository implements PanacheRepository<Fornecedor> {}
