package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.dto.ProdutoDTO;
import br.unitins.tp2.dto.ProdutoResponseDTO;
import br.unitins.tp2.model.Produto;
import br.unitins.tp2.service.ProdutoService;
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

@Path("produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProdutoResource {

    @Inject
    ProdutoService service;

    @GET
    public PageResponse<ProdutoResponseDTO> findAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("5") int pageSize) {
        List<Produto> produtos = service.findAll(page, pageSize);
        return PageResponse.of(produtos, page, pageSize, service.count(), ProdutoResponseDTO::valueOf);
    }

    @GET
    @Path("/nome/{nome}")
    public PageResponse<ProdutoResponseDTO> findByNome(
            @PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("5") int pageSize) {
        List<Produto> produtos = service.findByNome(nome, page, pageSize);
        return PageResponse.of(produtos, page, pageSize, service.count(nome), ProdutoResponseDTO::valueOf);
    }

    @GET
    @Path("/{id}")
    public ProdutoResponseDTO findById(@PathParam("id") long id) {
        return ProdutoResponseDTO.valueOf(service.findById(id));
    }

    @POST
    public Response create(@Valid ProdutoDTO dto) {
        ProdutoResponseDTO response = ProdutoResponseDTO.valueOf(service.create(dto));
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    public ProdutoResponseDTO update(@PathParam("id") long id, @Valid ProdutoDTO dto) {
        return ProdutoResponseDTO.valueOf(service.update(id, dto));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") long id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
