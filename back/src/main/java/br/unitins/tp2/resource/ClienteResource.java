package br.unitins.tp2.resource;
import br.unitins.tp2.dto.*;
import br.unitins.tp2.mapper.ClienteResponseMapper;
import br.unitins.tp2.service.*;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
@Path("clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {
    @Inject ClienteService service;
    @Inject CepService ceps;
    @GET
    public PageResponse<ClienteResponseDTO> buscarTodos(
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("pageSize") @DefaultValue("10") @Min(1) @Max(100) int size) {
        return PageResponse.of(service.findAll(page, size), page, size, service.count(),
                ClienteResponseMapper::toResponse);
    }
    @GET @Path("/nome/{nome}")
    public PageResponse<ClienteResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("pageSize") @DefaultValue("10") @Min(1) @Max(100) int size) {
        return PageResponse.of(service.findByNome(nome, page, size), page, size, service.count(nome),
                ClienteResponseMapper::toResponse);
    }
    @GET @Path("cep/{cep}")
    public CepDTO consultar(@PathParam("cep") String cep) { return ceps.consultar(cep); }
    @GET @Path("cpf/{cpf}")
    public ClienteResponseDTO buscarPorCpf(@PathParam("cpf") @Pattern(regexp="[0-9]{11}") String cpf) {
        return ClienteResponseMapper.toResponse(service.findByCpf(cpf));
    }
    @GET @Path("{id}")
    public ClienteResponseDTO buscarPorId(@PathParam("id") long id) {
        return ClienteResponseMapper.toResponse(service.findById(id));
    }
    @POST
    public ClienteResponseDTO incluir(@Valid ClienteRequestDTO dto) {
        return ClienteResponseMapper.toResponse(service.create(dto));
    }
    @PUT @Path("{id}")
    public void alterar(@PathParam("id") long id, @Valid ClienteRequestDTO dto) { service.update(id, dto); }
    @DELETE @Path("{id}")
    public void apagar(@PathParam("id") long id) {
        service.delete(id);
    }
}
