package br.unitins.tp2;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class FornecedorResourceTest {
    private Map<String, Object> dados(String cnpj) {
        return new HashMap<>(Map.of("nome", "Fornecedor Teste", "cnpj", cnpj,
                "email", "fornecedor@example.com", "telefone", "63999999999",
                "cep", "01001000", "logradouro", "Praca da Se", "bairro", "Se",
                "numero", "10", "ativo", true));
    }

    @Test
    void deveCadastrarEditarPesquisarPaginarEExcluir() {
        var dto = dados("12345678000195");
        var primeiro = given().contentType("application/json").body(dto).post("/fornecedores")
                .then().statusCode(200).body("municipio.estado.sigla", is("SP")).extract().jsonPath();
        int id = primeiro.getInt("id");
        Integer segundo = null;
        try {
            given().contentType("application/json").body(dto).post("/fornecedores").then().statusCode(400);
            segundo = given().contentType("application/json").body(dados("12ABC34501DE35"))
                    .post("/fornecedores").then().statusCode(200)
                    .body("municipio.id", is(primeiro.getInt("municipio.id"))).extract().path("id");
            dto.put("numero", "25");
            dto.put("ativo", false);
            given().contentType("application/json").body(dto).put("/fornecedores/" + id).then().statusCode(204);
            given().get("/fornecedores/" + id).then().statusCode(200)
                    .body("numero", is("25")).body("ativo", is(false)).body("cnpj", is("12345678000195"));
            given().queryParam("pageSize", 1).get("/fornecedores/nome/Fornecedor Teste")
                    .then().statusCode(200).body("totalItems", is(2)).body("totalPages", is(2));
            given().queryParam("page", 1).queryParam("pageSize", 1).get("/fornecedores")
                    .then().statusCode(200).body("items.size()", is(1));
            given().get("/fornecedores/nome/inexistente").then().statusCode(200).body("totalItems", is(0));
        } finally {
            given().delete("/fornecedores/" + id).then().statusCode(204);
            if (segundo != null) given().delete("/fornecedores/" + segundo).then().statusCode(204);
        }
        given().get("/fornecedores/" + id).then().statusCode(404);
    }

    @Test
    void deveRejeitarDadosInvalidos() {
        for (var entry : Map.of("cnpj", "123", "email", "invalido", "nome", " ",
                "telefone", "12", "cep", "99999999", "numero", " ").entrySet()) {
            var dto = dados("12345678000195");
            dto.put(entry.getKey(), entry.getValue());
            given().contentType("application/json").body(dto).post("/fornecedores").then().statusCode(400);
        }
        given().queryParam("page", -1).get("/fornecedores").then().statusCode(400);
        given().get("/fornecedores/cep/01001000").then().statusCode(200).body("uf", is("SP"));
    }

    @Test
    void deveVincularFornecedoresEProtegerExclusao() {
        int fornecedor = given().contentType("application/json").body(dados("12345678000195"))
                .post("/fornecedores").then().statusCode(200).extract().path("id");
        int outro = given().contentType("application/json").body(dados("12ABC34501DE35"))
                .post("/fornecedores").then().statusCode(200).extract().path("id");
        int categoria = given().contentType("application/json")
                .body(Map.of("nome", "Categoria " + java.util.UUID.randomUUID(), "ativo", true))
                .post("/categorias").then().statusCode(201).extract().path("id");
        Integer produto = null;
        try {
            var dto = new HashMap<String, Object>(Map.of("nome", "Whey " + java.util.UUID.randomUUID(),
                    "preco", 99.90, "estoque", 10, "ativo", true, "idCategoria", categoria,
                    "idsFornecedores", java.util.List.of(fornecedor, outro)));
            produto = given().contentType("application/json").body(dto).post("/produtos")
                    .then().statusCode(201).body("fornecedores.id", containsInAnyOrder(fornecedor, outro))
                    .extract().path("id");
            given().get("/produtos/" + produto).then().statusCode(200).body("fornecedores.size()", is(2));
            given().delete("/fornecedores/" + fornecedor).then().statusCode(400);
            dto.put("idsFornecedores", java.util.List.of(99999999));
            given().contentType("application/json").body(dto).put("/produtos/" + produto).then().statusCode(400);
            dto.put("idsFornecedores", java.util.List.of(fornecedor, fornecedor));
            given().contentType("application/json").body(dto).put("/produtos/" + produto).then().statusCode(400);
            dto.put("idsFornecedores", java.util.List.of(outro));
            given().contentType("application/json").body(dto).put("/produtos/" + produto)
                    .then().statusCode(200).body("fornecedores.id", contains(outro));
            dto.remove("idsFornecedores");
            given().contentType("application/json").body(dto).put("/produtos/" + produto)
                    .then().statusCode(200).body("fornecedores.id", contains(outro));
            dto.put("idsFornecedores", java.util.List.of());
            given().contentType("application/json").body(dto).put("/produtos/" + produto)
                    .then().statusCode(200).body("fornecedores", empty());
        } finally {
            if (produto != null) given().delete("/produtos/" + produto).then().statusCode(204);
            given().delete("/fornecedores/" + fornecedor).then().statusCode(204);
            given().delete("/fornecedores/" + outro).then().statusCode(204);
            given().delete("/categorias/" + categoria).then().statusCode(204);
        }
    }
}
