package br.unitins.tp2.service;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class KeycloakException extends WebApplicationException {
    public KeycloakException(int status, String message) {
        super(Response.status(status).entity(message).build());
    }
}
