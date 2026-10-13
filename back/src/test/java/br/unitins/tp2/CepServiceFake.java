package br.unitins.tp2;
import br.unitins.tp2.dto.CepDTO;
import br.unitins.tp2.service.CepService;
import br.unitins.tp2.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.annotation.Priority;
@Alternative @Priority(1) @ApplicationScoped
public class CepServiceFake extends CepService {
    @Override public CepDTO consultar(String cep) {
        if (!"01001000".equals(cep)) throw ValidationException.of("cep", "CEP nao encontrado.");
        return new CepDTO(cep, "Praca da Se", "Se", "Sao Paulo", "SP", "Sao Paulo", "3550308");
    }
}
