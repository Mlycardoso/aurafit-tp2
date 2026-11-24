package br.unitins.tp2;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AutorizacaoResourceTest {
    @Test
    void semTokenRecebe401ComProblem() {
        given().get("/usuarios").then().statusCode(401)
                .contentType("application/problem+json").body("title", org.hamcrest.Matchers.is("Não autenticado"));
    }

    @Test
    @io.quarkus.test.security.TestSecurity(user = "cliente", roles = {"CLIENTE"})
    void roleIncorretaRecebe403ComProblem() {
        given().get("/usuarios").then().statusCode(403)
                .contentType("application/problem+json").body("title", org.hamcrest.Matchers.is("Acesso negado"));
    }

    @Test
    @io.quarkus.test.security.TestSecurity(user = "admin", roles = {"ADMIN"})
    void roleCorretaAcessaEndpointAdministrativo() {
        given().get("/usuarios").then().statusCode(200);
    }
}
