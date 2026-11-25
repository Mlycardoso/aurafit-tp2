package br.unitins.tp2;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import br.unitins.tp2.service.*;
import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import javax.imageio.ImageIO;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/** Integração real: PostgreSQL aurafit_test e bucket aurafit-produtos-test. */
@QuarkusTest
@io.quarkus.test.security.TestSecurity(user = "admin", roles = {"ADMIN"})
class ProdutoImagemResourceTest {
    @Inject ProdutoImagemReferencia referencias;
    @Inject ImagemStorage storage;

    @Test void uploadSubstituicaoCatalogoDownloadInativacaoEExclusao() throws Exception {
        int categoria = given().contentType("application/json").body(Map.of("nome", "Imagem " + UUID.randomUUID(), "ativo", true))
            .post("/categorias").then().statusCode(201).extract().path("id");
        Integer id = null;
        try {
            id = given().contentType("application/json").body(Map.of("nome", "Whey " + UUID.randomUUID(), "preco", 10,
                "estoque", 1, "ativo", true, "idCategoria", categoria)).post("/produtos").then().statusCode(201)
                .body("imagemUrl", nullValue()).extract().path("id");
            String endpoint = "/produtos/" + id + "/imagem";
            given().get(endpoint).then().statusCode(404);
            given().multiPart("arquivo", "texto.png", "não é imagem".getBytes(), "image/png").post(endpoint).then().statusCode(400);
            given().multiPart("arquivo", "foto.svg", "<svg/>".getBytes(), "image/svg+xml").post(endpoint).then().statusCode(400);
            given().multiPart("arquivo", "grande.png", new byte[5 * 1024 * 1024 + 1], "image/png").post(endpoint).then().statusCode(400);
            byte[] png = imagem("png");
            given().multiPart("arquivo", "foto.jpg", png, "image/jpeg").post(endpoint).then().statusCode(400);
            String url = given().multiPart("arquivo", "../../foto.png", png, "image/png").post(endpoint).then().statusCode(200)
                .body("aviso", nullValue()).extract().path("imagemUrl");
            String chaveAntiga = referencias.atual(id);
            given().get("/produtos/" + id).then().statusCode(200).body("imagemUrl", is(url));
            given().get("/produtos?pageSize=100").then().statusCode(200).body("items.imagemUrl", hasItem(url));
            byte[] downloaded = given().get(endpoint + "?download=true").then().statusCode(200)
                .header("Content-Disposition", containsString("attachment"))
                .header("X-Content-Type-Options", is("nosniff"))
                .contentType("image/png").extract().asByteArray();
            assertNotNull(ImageIO.read(new java.io.ByteArrayInputStream(downloaded)));
            given().multiPart("arquivo", "foto.jpg", imagem("jpg"), "image/jpeg").post(endpoint).then().statusCode(200);
            assertThrows(NoSuchKeyException.class, () -> storage.ler(chaveAntiga));
            given().patch("/produtos/" + id + "/inativar").then().statusCode(204);
            given().get(endpoint).then().statusCode(200).contentType("image/jpeg");
            String chave = referencias.atual(id);
            given().delete(endpoint).then().statusCode(204);
            assertThrows(NoSuchKeyException.class, () -> storage.ler(chave));
            given().get(endpoint).then().statusCode(404);
            given().get("/produtos/" + id).then().statusCode(200).body("imagemUrl", nullValue());
            given().delete(endpoint).then().statusCode(204);
        } finally {
            if (id != null) {
                String chave = referencias.atual(id);
                if (chave != null) storage.excluir(chave);
                ProdutoTestCleanup.remover(id);
            }
            given().delete("/categorias/" + categoria).then().statusCode(204);
        }
    }

    @Test void produtoInexistenteNaoRecebeArquivo() throws Exception {
        given().multiPart("arquivo", "foto.png", imagem("png"), "image/png")
            .post("/produtos/9223372036854775807/imagem").then().statusCode(404);
    }

    private static byte[] imagem(String formato) throws Exception {
        var out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB), formato, out);
        return out.toByteArray();
    }
}
