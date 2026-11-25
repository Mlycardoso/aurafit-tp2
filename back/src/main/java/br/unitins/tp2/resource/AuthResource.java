package br.unitins.tp2.resource;

import br.unitins.tp2.dto.AuthDTOs;
import br.unitins.tp2.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    @Inject AuthService service;

    @POST @Path("/login") @PermitAll
    public AuthDTOs.TokenResponse login(@NotNull @Valid AuthDTOs.LoginRequest request) {
        return service.login(request.email(), request.senha());
    }

    @POST @Path("/registro") @PermitAll
    public Response register(@NotNull @Valid AuthDTOs.RegistroRequest request) {
        return Response.status(Response.Status.CREATED).entity(service.register(request)).build();
    }

    @POST @Path("/refresh") @PermitAll
    public AuthDTOs.TokenResponse refresh(@NotNull @Valid AuthDTOs.RefreshRequest request) {
        return service.refresh(request.refreshToken());
    }

    @POST @Path("/logout") @RolesAllowed({"ADMIN", "CLIENTE"})
    public Response logout(@NotNull @Valid AuthDTOs.LogoutRequest request) {
        service.logout(request.refreshToken());
        return Response.noContent().build();
    }

    @GET @Path("/me") @RolesAllowed({"ADMIN", "CLIENTE"})
    public AuthDTOs.UsuarioAuth me(@Context SecurityContext context) { return service.me(context); }
}
