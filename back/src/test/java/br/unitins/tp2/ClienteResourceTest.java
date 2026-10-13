package br.unitins.tp2;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
@QuarkusTest
class ClienteResourceTest {
    private Map<String,Object> dados() {
        return new HashMap<>(Map.of("nome","Cliente Teste","email","cliente@example.com",
            "telefone","63999999999","cep","01001000","logradouro","Praca da Se",
            "bairro","Se","numero","10","complemento","Sala 2"));
    }
    @Test void deveSalvarEditarEReutilizarMunicipio() {
        var first = given().contentType("application/json").body(dados()).post("/clientes")
            .then().statusCode(200).body("municipio.estado.sigla", is("SP"))
            .extract().jsonPath();
        int id = first.getInt("id");
        var second = given().contentType("application/json").body(dados()).post("/clientes")
            .then().statusCode(200).body("municipio.id", is(first.getInt("municipio.id")))
            .extract().jsonPath();
        try {
            var dto = dados(); dto.put("numero", "20");
            given().contentType("application/json").body(dto).put("/clientes/" + id)
                .then().statusCode(204);
            given().get("/clientes/" + id).then().statusCode(200)
                .body("municipio.codigoIbge", is("3550308")).body("numero", is("20"));
            given().queryParam("pageSize", 1).get("/clientes/nome/Cliente Teste")
                .then().statusCode(200).body("totalItems", is(2))
                .body("totalPages", is(2)).body("items.size()", is(1));
            given().queryParam("page", 1).queryParam("pageSize", 1).get("/clientes")
                .then().statusCode(200).body("items.size()", is(1));
            given().get("/clientes/nome/inexistente").then().statusCode(200).body("totalItems", is(0));
            given().queryParam("page", -1).get("/clientes").then().statusCode(400);
        } finally {
            given().delete("/clientes/" + id).then().statusCode(204);
            given().delete("/clientes/" + second.getInt("id")).then().statusCode(204);
        }
    }
    @Test void deveRejeitarDadosInvalidos() {
        var dto = dados(); dto.put("email", "invalido");
        given().contentType("application/json").body(dto).post("/clientes").then().statusCode(400);
        dto = dados(); dto.put("cep", "123");
        given().contentType("application/json").body(dto).post("/clientes").then().statusCode(400);
        dto = dados(); dto.put("numero", " ");
        given().contentType("application/json").body(dto).post("/clientes").then().statusCode(400);
        dto = dados(); dto.put("cep", "99999999");
        given().contentType("application/json").body(dto).post("/clientes").then().statusCode(400);
    }
}
