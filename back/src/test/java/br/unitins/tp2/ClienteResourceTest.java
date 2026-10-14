package br.unitins.tp2;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
@QuarkusTest
class ClienteResourceTest {
    @Test void deveVincularUsuarioEManterDadosPessoais() {
        var dto = dados();
        dto.put("cpf", "52998224725");
        dto.put("dataNascimento", "2000-01-10");
        dto.put("ativo", false);
        dto.put("telefones", java.util.List.of("63999999999", "1133334444"));
        int cliente = given().contentType("application/json").body(dto).post("/clientes")
            .then().statusCode(200).body("ativo", is(false)).body("telefones.size()", is(2)).extract().path("id");
        Integer usuario = null;
        try {
            given().contentType("application/json").body(dto).post("/clientes").then().statusCode(400);
            given().get("/clientes/cpf/52998224725").then().statusCode(200).body("id", is(cliente));
            var conta = new HashMap<String,Object>(Map.of("nome", "Conta Teste", "email", "vinculo@example.com",
                "senha", "Senha exemplo 123456", "ativo", true, "perfis", java.util.List.of("CLIENTE"), "idCliente", cliente));
            usuario = given().contentType("application/json").body(conta).post("/usuarios")
                .then().statusCode(201).body("cliente.cpf", is("52998224725")).extract().path("id");
            conta.put("email", "outra@example.com");
            given().contentType("application/json").body(conta).post("/usuarios").then().statusCode(400);
            given().delete("/clientes/" + cliente).then().statusCode(400);
            dto.put("ativo", true);
            dto.put("telefones", java.util.List.of("11988887777"));
            given().contentType("application/json").body(dto).put("/clientes/" + cliente).then().statusCode(204);
            given().get("/usuarios/" + usuario).then().statusCode(200)
                .body("cliente.ativo", is(true)).body("cliente.telefones", contains("11988887777"));
            conta.remove("senha");
            conta.remove("idCliente");
            given().contentType("application/json").body(conta).put("/usuarios/" + usuario)
                .then().statusCode(200).body("cliente", nullValue());
        } finally {
            if (usuario != null) given().delete("/usuarios/" + usuario).then().statusCode(204);
            given().delete("/clientes/" + cliente).then().statusCode(204);
        }
    }

    @Test void deveValidarDadosPessoais() {
        var dto = dados(); dto.put("cpf", "123");
        given().contentType("application/json").body(dto).post("/clientes").then().statusCode(400);
        dto = dados(); dto.put("dataNascimento", "2999-01-01");
        given().contentType("application/json").body(dto).post("/clientes").then().statusCode(400);
        dto = dados(); dto.put("telefones", java.util.List.of("63999999999", "63999999999"));
        given().contentType("application/json").body(dto).post("/clientes").then().statusCode(400);
        dto = dados(); dto.put("telefones", java.util.List.of());
        given().contentType("application/json").body(dto).post("/clientes").then().statusCode(400);
    }
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
