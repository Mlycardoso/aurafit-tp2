package br.unitins.tp2;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AuraFitResourceTest {

    @Test
    void deveListarMarcas() {
        given()
                .when().get("/marcas")
                .then().statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(3)));
    }

    @Test
    void deveListarCategorias() {
        given()
                .when().get("/categorias")
                .then().statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(3)));
    }
}
