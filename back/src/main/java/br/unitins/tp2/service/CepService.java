package br.unitins.tp2.service;
import br.unitins.tp2.dto.CepDTO;
import br.unitins.tp2.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ServiceUnavailableException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
@ApplicationScoped
public class CepService {
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    @Inject ObjectMapper mapper;
    public CepDTO consultar(String cep) {
        if (cep == null || !cep.matches("[0-9]{8}"))
            throw ValidationException.of("cep", "Informe um CEP com 8 digitos.");
        try {
            var request = HttpRequest.newBuilder(URI.create("https://viacep.com.br/ws/" + cep + "/json/"))
                    .timeout(Duration.ofSeconds(8)).GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) throw new ServiceUnavailableException("Consulta de CEP indisponivel.");
            var json = mapper.readTree(response.body());
            if (json.path("erro").asBoolean(false))
                throw ValidationException.of("cep", "CEP nao encontrado.");
            if (!json.path("ibge").asText().matches("[0-9]{7}") || !json.path("uf").asText().matches("[A-Z]{2}")
                    || json.path("localidade").asText().isBlank())
                throw new ServiceUnavailableException("Resposta de CEP incompleta.");
            return new CepDTO(cep, json.path("logradouro").asText(), json.path("bairro").asText(),
                    json.path("localidade").asText(), json.path("uf").asText(),
                    json.path("estado").asText(json.path("uf").asText()), json.path("ibge").asText());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("Consulta interrompida.");
        } catch (java.io.IOException e) {
            throw new ServiceUnavailableException("Nao foi possivel consultar o CEP. Tente novamente.");
        }
    }
}
