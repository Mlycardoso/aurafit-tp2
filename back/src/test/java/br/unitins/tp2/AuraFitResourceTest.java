package br.unitins.tp2;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

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

    @Test
    void deveListarProdutosComCategoriaEPaginacao() {
        given()
                .queryParam("page", 0)
                .queryParam("pageSize", 5)
                .when().get("/produtos")
                .then().statusCode(200)
                .body("items", hasSize(5))
                .body("items[0].categoria.nome", is("Creatina"))
                .body("page", is(0))
                .body("pageSize", is(5))
                .body("totalItems", greaterThanOrEqualTo(7))
                .body("totalPages", is(2));
    }

    @Test
    void deveFiltrarProdutosPorNome() {
        given()
                .when().get("/produtos/nome/Whey")
                .then().statusCode(200)
                .body("items", hasSize(2))
                .body("totalItems", is(2));
    }
}
