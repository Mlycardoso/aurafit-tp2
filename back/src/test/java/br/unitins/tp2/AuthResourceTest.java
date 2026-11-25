package br.unitins.tp2;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AuthResourceTest {
    private Map<String, Object> registration(String email) {
        return Map.of("senha", "Senha segura de teste 123!", "cliente", Map.of(
                "nome", "Cliente " + UUID.randomUUID(), "email", email, "telefone", "63999999999",
                "cep", "01001000", "logradouro", "Praça da Sé", "bairro", "Sé", "numero", "10",
                "telefones", java.util.List.of("63999999999")));
    }

    @Test void loginERefreshDelegamAoClienteFalso() {
        given().contentType("application/json").body(Map.of("email", "teste@example.com", "senha", "wrong"))
                .post("/auth/login").then().statusCode(401).contentType("application/problem+json");
        given().contentType("application/json").body(Map.of("email", "teste@example.com", "senha", "valida"))
                .post("/auth/login").then().statusCode(200).body("accessToken", is("test-access-token"))
                .body("refreshToken", is("test-refresh-token")).body("expiresIn", is(300))
                .body("usuario.email", is("teste@example.com"));
        given().contentType("application/json").body(Map.of("refreshToken", "test-refresh-token"))
                .post("/auth/refresh").then().statusCode(200).body("accessToken", is("test-access-token-renovado"));
    }

    @Test void registroCriaContaLocalEPerfilCliente() {
        String email = UUID.randomUUID() + "@example.com";
        given().contentType("application/json").body(registration(email)).post("/auth/registro")
                .then().statusCode(201).body("usuario.email", is(email)).body("usuario.roles", contains("CLIENTE"))
                .body("usuario.cliente.cep", is("01001000")).body("$", not(hasKey("senha")));
        given().contentType("application/json").body(registration(email)).post("/auth/registro")
                .then().statusCode(409).contentType("application/problem+json");
    }

    @Test
    @io.quarkus.test.security.TestSecurity(user = "cliente", roles = {"CLIENTE"})
    void meExigeAutenticacaoEDevolveRoles() {
        given().get("/auth/me").then().statusCode(200).body("roles", contains("CLIENTE"));
    }
}
