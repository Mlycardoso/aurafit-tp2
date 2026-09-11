package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.CategoriaDTO;
import br.unitins.tp2.dto.CategoriaResponseDTO;
import br.unitins.tp2.service.CategoriaService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("categorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriaResource {

    @Inject
    CategoriaService service;

    @GET
    public List<CategoriaResponseDTO> findAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("100") int pageSize) {
        return service.findAll(page, pageSize).stream().map(CategoriaResponseDTO::valueOf).toList();
    }

    @GET
    @Path("/nome/{nome}")
    public List<CategoriaResponseDTO> findByNome(
            @PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("100") int pageSize) {
        return service.findByNome(nome, page, pageSize).stream().map(CategoriaResponseDTO::valueOf).toList();
    }

    @GET
    @Path("/count")
    public long count() {
        return service.count();
    }

    @GET
    @Path("/{id}")
    public CategoriaResponseDTO findById(@PathParam("id") long id) {
        return CategoriaResponseDTO.valueOf(service.findById(id));
    }

    @POST
    public Response create(@Valid CategoriaDTO dto) {
        CategoriaResponseDTO response = CategoriaResponseDTO.valueOf(service.create(dto));
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    public CategoriaResponseDTO update(@PathParam("id") long id, @Valid CategoriaDTO dto) {
        return CategoriaResponseDTO.valueOf(service.update(id, dto));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") long id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
