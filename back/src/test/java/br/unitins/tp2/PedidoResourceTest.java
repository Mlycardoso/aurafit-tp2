package br.unitins.tp2;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class PedidoResourceTest {
    @Test void listaPedidosExigeAutenticacao() { given().get("/pedidos").then().statusCode(401).contentType("application/problem+json"); }
    @Test @io.quarkus.test.security.TestSecurity(user = "cliente", roles = {"CLIENTE"})
    void clienteNaoPodeListarPedidosDeTodos() { given().get("/pedidos").then().statusCode(403).contentType("application/problem+json"); }
    @Test @io.quarkus.test.security.TestSecurity(user = "admin", roles = {"ADMIN"})
    void adminPodeListarPedidos() { given().get("/pedidos").then().statusCode(200); }
}
