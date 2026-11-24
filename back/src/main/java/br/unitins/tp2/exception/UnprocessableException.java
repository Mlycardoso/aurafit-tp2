package br.unitins.tp2.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class UnprocessableException extends WebApplicationException {
    public UnprocessableException(String detail) { super(Response.status(422).entity(detail).build()); }
}
