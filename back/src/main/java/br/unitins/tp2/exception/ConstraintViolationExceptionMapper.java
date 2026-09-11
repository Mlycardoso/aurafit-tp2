package br.unitins.tp2.exception;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jspecify.annotations.NonNull;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Context
    UriInfo uriInfo;

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public Response toResponse(ConstraintViolationException e) {
        // List<Problem.FieldError> fieldErrors = e.getConstraintViolations()
        //         .stream()
        //         .map(this::toFieldError)
        //         .collect(Collectors.toList());

        List<Problem.@NonNull FieldError> fieldErrors = e.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        v -> lastProperty(v.getPropertyPath()),
                        v -> new Problem.FieldError(
                                lastProperty(v.getPropertyPath()),
                                v.getMessage()
                        ),
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .toList();

        Problem p = new Problem();
        p.type = baseUrl + "/errors/validation-error";
        p.title = "Erro de validação";
        p.status = Response.Status.BAD_REQUEST.getStatusCode();
        p.detail = "Um ou mais campos não passaram na validação.";
        p.instance = (uriInfo != null ? uriInfo.getRequestUri().getPath() : null);
        p.timestamp = OffsetDateTime.now();
        p.errors = fieldErrors;

        return Response.status(p.status)
                .type("application/problem+json")
                .entity(p)
                .build();
    }

    private Problem.FieldError toFieldError(ConstraintViolation<?> v) {
        String field = lastProperty(v.getPropertyPath());
        String message = v.getMessage();
        return new Problem.FieldError(field, message);
    }

    private String lastProperty(Path path) {
        String last = null;
        for (Path.Node node : path) {
            ElementKind kind = node.getKind();
            if (kind == ElementKind.PROPERTY || kind == ElementKind.CONTAINER_ELEMENT) {
                last = node.getName();
            }
        }
        return (last != null) ? last : "unknown";
    }
}
