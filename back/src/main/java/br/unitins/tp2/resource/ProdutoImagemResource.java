package br.unitins.tp2.resource;

import br.unitins.tp2.service.ProdutoImagemService;
import br.unitins.tp2.service.ImagemValidator;
import br.unitins.tp2.exception.ValidationException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import java.io.IOException;
import java.nio.file.Files;

@Path("/produtos/{id}/imagem")
public class ProdutoImagemResource {
    @Inject ProdutoImagemService service;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public ProdutoImagemService.Resultado upload(@PathParam("id") long id, @RestForm("arquivo") FileUpload arquivo) throws IOException {
        if (arquivo == null || arquivo.size() == 0 || arquivo.size() > ImagemValidator.MAX_BYTES)
            throw ValidationException.of("arquivo", "Selecione uma imagem PNG ou JPEG de até 5 MB.");
        return service.enviar(id, Files.readAllBytes(arquivo.uploadedFile()), arquivo.contentType());
    }

    @GET
    @Produces({"image/png", "image/jpeg"})
    public Response visualizar(@PathParam("id") long id, @QueryParam("download") @DefaultValue("false") boolean download) {
        var arquivo = service.ler(id);
        String extensao = "image/png".equals(arquivo.tipo()) ? "png" : "jpg";
        return Response.ok(arquivo.bytes(), arquivo.tipo())
            .header("Content-Disposition", (download ? "attachment" : "inline") + "; filename=\"produto-" + id + "." + extensao + "\"")
            .header("X-Content-Type-Options", "nosniff")
            .header("Cache-Control", "no-cache")
            .build();
    }

    @DELETE
    public Response excluir(@PathParam("id") long id) {
        service.excluir(id);
        return Response.noContent().build();
    }
}
