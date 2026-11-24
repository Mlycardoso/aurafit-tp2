package br.unitins.tp2.resource;

import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.dto.PedidoDTOs;
import br.unitins.tp2.model.StatusPedido;
import br.unitins.tp2.service.PedidoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/pedidos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PedidoResource {
    @Inject PedidoService service;

    @POST @RolesAllowed("CLIENTE")
    public Response create(@NotNull @Valid PedidoDTOs.CreateRequest request, @Context SecurityContext context) {
        return Response.status(Response.Status.CREATED).entity(service.create(request, context)).build();
    }

    @GET @Path("/meus") @RolesAllowed("CLIENTE")
    public java.util.List<PedidoDTOs.Response> mine(@Context SecurityContext context) { return service.mine(context); }

    @GET @RolesAllowed("ADMIN")
    public PageResponse<PedidoDTOs.Response> list(@QueryParam("status") StatusPedido status,
            @QueryParam("nome") String name, @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("pageSize") @DefaultValue("10") @Min(1) @Max(100) int size) {
        return service.list(status, name, page, size);
    }

    @GET @Path("/{id}") @RolesAllowed({"ADMIN", "CLIENTE"})
    public PedidoDTOs.Response find(@PathParam("id") long id, @Context SecurityContext context) { return service.find(id, context); }

    @PATCH @Path("/{id}/enviar") @RolesAllowed("ADMIN")
    public PedidoDTOs.Response markSent(@PathParam("id") long id) { return service.markSent(id); }
}
