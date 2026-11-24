package br.unitins.tp2.exception;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.Priorities;
import jakarta.annotation.Priority;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

final class SecurityProblemResponse {
    private SecurityProblemResponse() {}

    static Response create(int status, String title, String detail, String baseUrl, UriInfo uri) {
        var problem = new Problem();
        problem.type = baseUrl + "/errors/" + status;
        problem.title = title;
        problem.status = status;
        problem.detail = detail;
        problem.instance = uri == null ? null : uri.getRequestUri().getPath();
        problem.timestamp = OffsetDateTime.now();
        return Response.status(status).type("application/problem+json").entity(problem).build();
    }
}

@Provider
@ApplicationScoped
class UnauthorizedProblemMapper implements ExceptionMapper<NotAuthorizedException> {
    @ConfigProperty(name = "problem.base-url") String baseUrl;
    @Context UriInfo uri;

    @Override public Response toResponse(NotAuthorizedException exception) {
        return SecurityProblemResponse.create(401, "Não autenticado", "É necessário autenticar para acessar este recurso.", baseUrl, uri);
    }
}

@Provider
@ApplicationScoped
class ForbiddenProblemMapper implements ExceptionMapper<ForbiddenException> {
    @ConfigProperty(name = "problem.base-url") String baseUrl;
    @Context UriInfo uri;

    @Override public Response toResponse(ForbiddenException exception) {
        return SecurityProblemResponse.create(403, "Acesso negado", "Você não tem permissão para acessar este recurso.", baseUrl, uri);
    }
}

@Provider
@ApplicationScoped
class NotFoundProblemMapper implements ExceptionMapper<NotFoundException> {
    @ConfigProperty(name = "problem.base-url") String baseUrl;
    @Context UriInfo uri;

    @Override public Response toResponse(NotFoundException exception) {
        return SecurityProblemResponse.create(404, "Não encontrado", "O recurso solicitado não foi encontrado.", baseUrl, uri);
    }
}

@Provider
@ApplicationScoped
@Priority(Priorities.ENTITY_CODER)
class ProblemResponseFilter implements ContainerResponseFilter {
    @ConfigProperty(name = "problem.base-url") String baseUrl;
    @Context UriInfo uri;

    @Override public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        int status = response.getStatus();
        if ((status == 401 || status == 403 || status == 404) && !(response.getEntity() instanceof Problem)) {
            String title = switch (status) {
                case 401 -> "Não autenticado";
                case 403 -> "Acesso negado";
                default -> "Não encontrado";
            };
            String detail = switch (status) {
                case 401 -> "É necessário autenticar para acessar este recurso.";
                case 403 -> "Você não tem permissão para acessar este recurso.";
                default -> "O recurso solicitado não foi encontrado.";
            };
            response.setEntity(SecurityProblemResponse.create(status, title, detail, baseUrl, uri).getEntity());
            response.getHeaders().putSingle("Content-Type", "application/problem+json");
        }
    }
}
