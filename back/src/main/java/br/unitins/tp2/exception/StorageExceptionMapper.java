package br.unitins.tp2.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Provider
public class StorageExceptionMapper implements ExceptionMapper<SdkException> {
    public Response toResponse(SdkException ex) {
        var problem = new Problem();
        problem.status = ex instanceof NoSuchKeyException ? 404 : 503;
        problem.title = problem.status == 404 ? "Imagem não encontrada" : "Armazenamento indisponível";
        problem.detail = problem.status == 404 ? "A imagem não está disponível." : "Não foi possível acessar as imagens. Tente novamente em instantes.";
        return Response.status(problem.status).type("application/problem+json").entity(problem).build();
    }
}
