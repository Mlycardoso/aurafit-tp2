package br.unitins.tp2.resource;
import br.unitins.tp2.dto.*;
import br.unitins.tp2.model.Cliente;
import br.unitins.tp2.repository.ClienteRepository;
import br.unitins.tp2.service.*;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
@Path("clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {
    @Inject ClienteRepository repository;
    @Inject ClienteService service;
    @Inject CepService ceps;
    @GET
    public PageResponse<Cliente> listar(@QueryParam("nome") @DefaultValue("") String nome,
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("pageSize") @DefaultValue("10") @Min(1) @Max(100) int size) {
        var query = repository.find("lower(nome) like ?1 order by nome, id", "%" + nome.trim().toLowerCase() + "%");
        long count = query.count();
        return PageResponse.of(query.page(page, size).list(), page, size, count, c -> c);
    }
    @GET @Path("cep/{cep}")
    public CepDTO consultar(@PathParam("cep") String cep) { return ceps.consultar(cep); }
    @GET @Path("{id}")
    public Cliente buscar(@PathParam("id") long id) {
        return repository.findByIdOptional(id).orElseThrow(NotFoundException::new);
    }
    @POST
    public Response criar(@Valid ClienteDTO dto) {
        return Response.status(201).entity(service.salvar(null, dto)).build();
    }
    @PUT @Path("{id}")
    public Cliente editar(@PathParam("id") long id, @Valid ClienteDTO dto) { return service.salvar(id, dto); }
    @DELETE @Path("{id}") @Transactional
    public Response excluir(@PathParam("id") long id) {
        if (!repository.deleteById(id)) throw new NotFoundException();
        return Response.noContent().build();
    }
}
