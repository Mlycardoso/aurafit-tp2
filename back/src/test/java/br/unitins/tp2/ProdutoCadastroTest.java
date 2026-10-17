package br.unitins.tp2;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ProdutoCadastroTest {
    @Test
    void devePreservarProdutoInativoValidarSkuEReativar() {
        int categoria = given().contentType("application/json").body(Map.of("nome", "Teste " + UUID.randomUUID(), "ativo", true))
            .post("/categorias").then().statusCode(201).extract().path("id");
        Integer id = null;
        try {
            String sku = "TEST-" + UUID.randomUUID().toString().replace("-", "");
            var dto = new HashMap<String,Object>(Map.of("nome", "Whey " + UUID.randomUUID(), "preco", 100,
                "estoque", 12, "ativo", true, "idCategoria", categoria, "sku", sku.toLowerCase(), "pesoGramas", 900, "sabor", "Chocolate"));
            id = given().contentType("application/json").body(dto).post("/produtos").then().statusCode(201)
                .body("sku", is(sku.toUpperCase())).body("pesoGramas", is(900)).body("sabor", is("Chocolate")).extract().path("id");
            dto.put("nome", "Outro " + UUID.randomUUID());
            given().contentType("application/json").body(dto).post("/produtos").then().statusCode(400);
            dto.put("pesoGramas", 0);
            given().contentType("application/json").body(dto).put("/produtos/" + id).then().statusCode(400);
            dto.put("pesoGramas", 900);
            dto.put("sku", "codigo invalido");
            given().contentType("application/json").body(dto).put("/produtos/" + id).then().statusCode(400);
            dto.put("sku", sku);
            given().patch("/produtos/" + id + "/inativar").then().statusCode(204);
            given().get("/produtos/" + id).then().statusCode(200).body("ativo", is(false)).body("estoque", is(12)).body("categoria.id", is(categoria));
            given().delete("/categorias/" + categoria).then().statusCode(400);
            given().contentType("application/json").body(dto).put("/produtos/" + id).then().statusCode(200).body("ativo", is(true));
            given().delete("/produtos/" + id).then().statusCode(204);
            given().get("/produtos/" + id).then().statusCode(200).body("ativo", is(false));
        } finally {
            if (id != null) ProdutoTestCleanup.remover(id);
            given().delete("/categorias/" + categoria).then().statusCode(204);
        }
    }
    @Test
    void deveGerarCodigoAoCriarSemSku() {
        int categoria = given().contentType("application/json").body(Map.of("nome", "Teste " + UUID.randomUUID(), "ativo", true))
            .post("/categorias").then().statusCode(201).extract().path("id");
        Integer id = null;
        try {
            var dto = Map.of("nome", "Produto " + UUID.randomUUID(), "preco", 10, "estoque", 1, "ativo", true, "idCategoria", categoria);
            var resposta = given().contentType("application/json").body(dto).post("/produtos").then().statusCode(201)
                .body("sku", startsWith("AF-")).extract();
            id = resposta.path("id");
            given().contentType("application/json").body(dto).put("/produtos/" + id).then().statusCode(200).body("sku", is((String) resposta.path("sku")));
        } finally {
            if (id != null) ProdutoTestCleanup.remover(id);
            given().delete("/categorias/" + categoria).then().statusCode(204);
        }
    }
}
