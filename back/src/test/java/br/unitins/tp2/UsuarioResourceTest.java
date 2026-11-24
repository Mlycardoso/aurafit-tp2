package br.unitins.tp2;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import br.unitins.tp2.repository.UsuarioRepository;

@QuarkusTest
@io.quarkus.test.security.TestSecurity(user = "admin", roles = {"ADMIN"})
class UsuarioResourceTest {
    @Inject UsuarioRepository repository;
    @Inject jakarta.persistence.EntityManager entityManager;

    private Map<String, Object> dados() {
        return new HashMap<>(Map.of("nome", "Usuario " + UUID.randomUUID(),
                "email", UUID.randomUUID() + "@example.com", "ativo", true, "perfis", List.of("ADMIN")));
    }

    @Test
    void deveGerenciarUsuarioSincronizadoComIdentidade() {
        var dto = dados();
        int id = given().contentType("application/json").body(dto).post("/usuarios")
                .then().statusCode(201).body("$", not(hasKey("senha")))
                .body("$", not(hasKey("senhaHash"))).body("keycloakId", notNullValue()).extract().path("id");
        try {
            String keycloakId = repository.findById((long) id).keycloakId;
            dto.put("email", dto.get("email").toString().toUpperCase());
            given().contentType("application/json").body(dto).post("/usuarios").then().statusCode(400);
            dto.put("ativo", false);
            dto.put("perfis", List.of("CLIENTE"));
            given().contentType("application/json").body(dto).put("/usuarios/" + id)
                    .then().statusCode(200).body("ativo", is(false)).body("perfis", hasSize(1));
            assertEquals(keycloakId, repository.findById((long) id).keycloakId);
            given().queryParam("pageSize", 1).get("/usuarios/nome/" + dto.get("nome"))
                    .then().statusCode(200).body("totalItems", is(1))
                    .body("items[0]", not(hasKey("senhaHash")));
            given().get("/usuarios/" + id).then().statusCode(200).body("$", not(hasKey("senhaHash")));
        } finally {
            given().delete("/usuarios/" + id).then().statusCode(204);
        }
        given().get("/usuarios/" + id).then().statusCode(200).body("ativo", is(false));
    }

    @Test
    void deveValidarCamposEPerfis() {
        var dto = dados();
        dto = dados(); dto.put("perfis", List.of());
        given().contentType("application/json").body(dto).post("/usuarios").then().statusCode(400);
        dto = dados(); dto.put("perfis", List.of("PSICOLOGO"));
        given().contentType("application/json").body(dto).post("/usuarios").then().statusCode(400);
        dto = dados(); dto.put("email", "invalido");
        given().contentType("application/json").body(dto).post("/usuarios").then().statusCode(400);
        given().queryParam("page", -1).get("/usuarios").then().statusCode(400);
        given().get("/usuarios/perfis").then().statusCode(200)
                .body("$", containsInAnyOrder("ADMIN", "CLIENTE"));
        given().get("/").then().statusCode(200).body(is("AuraFit API em execucao."));
    }
}
