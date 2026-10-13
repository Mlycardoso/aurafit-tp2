package br.unitins.tp2.resource;

import br.unitins.tp2.dto.*;
import br.unitins.tp2.mapper.UsuarioResponseMapper;
import br.unitins.tp2.model.Perfil;
import br.unitins.tp2.service.UsuarioService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {
    @Inject UsuarioService service;

    @GET
    public PageResponse<UsuarioResponseDTO> buscarTodos(
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("pageSize") @DefaultValue("10") @Min(1) @Max(100) int pageSize) {
        return listar("", page, pageSize);
    }

    @GET @Path("/nome/{nome}")
    public PageResponse<UsuarioResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("pageSize") @DefaultValue("10") @Min(1) @Max(100) int pageSize) {
        return listar(nome, page, pageSize);
    }

    private PageResponse<UsuarioResponseDTO> listar(String nome, int page, int size) {
        return PageResponse.of(service.findByNome(nome, page, size), page, size, service.count(nome),
                UsuarioResponseMapper::toResponse);
    }

    @GET @Path("/perfis")
    public Perfil[] perfis() { return Perfil.values(); }

    @GET @Path("/{id}")
    public UsuarioResponseDTO buscarPorId(@PathParam("id") long id) {
        return UsuarioResponseMapper.toResponse(service.findById(id));
    }

    @POST
    public Response incluir(@NotNull @Valid UsuarioRequestDTO dto) {
        return Response.status(201).entity(UsuarioResponseMapper.toResponse(service.create(dto))).build();
    }

    @PUT @Path("/{id}")
    public UsuarioResponseDTO alterar(@PathParam("id") long id, @NotNull @Valid UsuarioUpdateDTO dto) {
        return UsuarioResponseMapper.toResponse(service.update(id, dto));
    }

    @DELETE @Path("/{id}")
    public Response apagar(@PathParam("id") long id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
