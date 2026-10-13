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
import br.unitins.tp2.service.SenhaService;
import br.unitins.tp2.repository.UsuarioRepository;

@QuarkusTest
class UsuarioResourceTest {
    @Inject SenhaService senhas;
    @Inject UsuarioRepository repository;
    @Inject jakarta.persistence.EntityManager entityManager;

    private Map<String, Object> dados() {
        return new HashMap<>(Map.of("nome", "Usuario " + UUID.randomUUID(),
                "email", UUID.randomUUID() + "@example.com", "senha", "Senha de teste 123456",
                "ativo", true, "perfis", List.of("ADMINISTRADOR")));
    }

    @Test
    void deveGerenciarUsuarioSemExporSenha() {
        var dto = dados();
        int id = given().contentType("application/json").body(dto).post("/usuarios")
                .then().statusCode(201).body("$", not(hasKey("senha")))
                .body("$", not(hasKey("senhaHash"))).extract().path("id");
        try {
            String hash = repository.findById((long) id).senhaHash;
            assertNotEquals(dto.get("senha"), hash);
            assertTrue(senhas.verificar((String) dto.get("senha"), hash));
            assertFalse(senhas.verificar("incorreta", hash));
            dto.put("email", dto.get("email").toString().toUpperCase());
            given().contentType("application/json").body(dto).post("/usuarios").then().statusCode(400);
            dto.remove("senha");
            dto.put("ativo", false);
            dto.put("perfis", List.of("ATENDENTE", "CLIENTE"));
            given().contentType("application/json").body(dto).put("/usuarios/" + id)
                    .then().statusCode(200).body("ativo", is(false)).body("perfis", hasSize(2));
            assertEquals(hash, repository.findById((long) id).senhaHash);
            given().queryParam("pageSize", 1).get("/usuarios/nome/" + dto.get("nome"))
                    .then().statusCode(200).body("totalItems", is(1))
                    .body("items[0]", not(hasKey("senhaHash")));
            dto.put("senha", "Outra senha de teste 456");
            given().contentType("application/json").body(dto).put("/usuarios/" + id).then().statusCode(200);
            entityManager.clear();
            assertTrue(senhas.verificar((String) dto.get("senha"), repository.findById((long) id).senhaHash));
            given().get("/usuarios/" + id).then().statusCode(200).body("$", not(hasKey("senhaHash")));
        } finally {
            given().delete("/usuarios/" + id).then().statusCode(204);
        }
        given().get("/usuarios/" + id).then().statusCode(404);
    }

    @Test
    void deveValidarCamposEPerfis() {
        var dto = dados();
        dto.put("senha", "curta");
        given().contentType("application/json").body(dto).post("/usuarios").then().statusCode(400);
        dto = dados(); dto.put("senha", "                  ");
        given().contentType("application/json").body(dto).post("/usuarios").then().statusCode(400);
        dto = dados(); dto.put("perfis", List.of());
        given().contentType("application/json").body(dto).post("/usuarios").then().statusCode(400);
        dto = dados(); dto.put("perfis", List.of("PSICOLOGO"));
        given().contentType("application/json").body(dto).post("/usuarios").then().statusCode(400);
        dto = dados(); dto.put("email", "invalido");
        given().contentType("application/json").body(dto).post("/usuarios").then().statusCode(400);
        given().queryParam("page", -1).get("/usuarios").then().statusCode(400);
        given().get("/usuarios/perfis").then().statusCode(200)
                .body("$", containsInAnyOrder("ADMINISTRADOR", "ATENDENTE", "CLIENTE"));
        given().get("/").then().statusCode(200).body(is("AuraFit API em execucao."));
    }
}
