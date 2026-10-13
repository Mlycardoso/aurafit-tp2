package br.unitins.tp2;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AdministrativoResourceTest {
    private Map<String, Object> cadastro(String nome) {
        return new HashMap<>(Map.of("nome", nome, "descricao", "Teste administrativo", "ativo", true));
    }

    @Test
    void deveCadastrarEditarFiltrarEExcluirMarca() {
        String nome = "Marca " + UUID.randomUUID();
        var dto = cadastro(nome);
        int id = given().contentType("application/json").body(dto).post("/marcas")
                .then().statusCode(201).extract().path("id");
        try {
            given().contentType("application/json").body(dto).post("/marcas")
                    .then().statusCode(400);
            dto.put("ativo", false);
            dto.put("nome", nome + " editada");
            given().contentType("application/json").body(dto).put("/marcas/" + id)
                    .then().statusCode(200).body("ativo", is(false));
            given().get("/marcas/nome/" + nome).then().statusCode(200).body("size()", is(1));
            given().get("/marcas/" + id).then().statusCode(200).body("nome", is(nome + " editada"));
        } finally {
            given().delete("/marcas/" + id).then().statusCode(204);
        }
        given().get("/marcas/" + id).then().statusCode(404);
    }

    @Test
    void deveProtegerCategoriaVinculadaEEditarProduto() {
        String nome = "Categoria " + UUID.randomUUID();
        var categoria = cadastro(nome);
        int categoriaId = given().contentType("application/json").body(categoria).post("/categorias")
                .then().statusCode(201).extract().path("id");
        Integer produtoId = null;
        try {
            var produto = cadastro("Produto " + UUID.randomUUID());
            produto.put("preco", 99.90);
            produto.put("estoque", 10);
            produto.put("idCategoria", categoriaId);
            produtoId = given().contentType("application/json").body(produto).post("/produtos")
                    .then().statusCode(201).body("categoria.id", is(categoriaId)).extract().path("id");
            given().delete("/categorias/" + categoriaId).then().statusCode(400)
                    .body("errors[0].message", containsString("produtos vinculados"));
            categoria.put("ativo", false);
            given().contentType("application/json").body(categoria).put("/categorias/" + categoriaId)
                    .then().statusCode(200).body("ativo", is(false));
            given().get("/categorias/nome/" + nome).then().statusCode(200).body("size()", is(1));
            produto.put("estoque", 20);
            given().contentType("application/json").body(produto).put("/produtos/" + produtoId)
                    .then().statusCode(200).body("estoque", is(20));
            given().get("/produtos/" + produtoId).then().statusCode(200)
                    .body("categoria.ativo", is(false));
            produto.put("preco", -1);
            given().contentType("application/json").body(produto).post("/produtos").then().statusCode(400);
            produto.put("preco", 10);
            produto.put("idCategoria", 99999999);
            given().contentType("application/json").body(produto).post("/produtos").then().statusCode(400);
        } finally {
            if (produtoId != null) given().delete("/produtos/" + produtoId).then().statusCode(204);
            given().delete("/categorias/" + categoriaId).then().statusCode(204);
        }
        given().get("/categorias/" + categoriaId).then().statusCode(404);
    }

    @Test
    void deveRejeitarNomesVazios() {
        for (String recurso : new String[] {"marcas", "categorias"}) {
            given().contentType("application/json").body(cadastro("   ")).post("/" + recurso)
                    .then().statusCode(400);
        }
    }
}
