package br.unitins.tp2.repository;
import br.unitins.tp2.model.Municipio;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class MunicipioRepository implements PanacheRepository<Municipio> {}
