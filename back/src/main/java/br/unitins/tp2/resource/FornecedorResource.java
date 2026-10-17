package br.unitins.tp2.resource;
import br.unitins.tp2.dto.*;
import br.unitins.tp2.mapper.FornecedorResponseMapper;
import br.unitins.tp2.service.*;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
@Path("fornecedores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FornecedorResource {
    @Inject FornecedorService service;
    @Inject CepService ceps;
    @GET
    public PageResponse<FornecedorResponseDTO> buscarTodos(
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("pageSize") @DefaultValue("10") @Min(1) @Max(100) int size) {
        return PageResponse.of(service.findAll(page, size), page, size, service.count(),
                FornecedorResponseMapper::toResponse);
    }
    @GET @Path("/nome/{nome}")
    public PageResponse<FornecedorResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("pageSize") @DefaultValue("10") @Min(1) @Max(100) int size) {
        return PageResponse.of(service.findByNome(nome, page, size), page, size, service.count(nome),
                FornecedorResponseMapper::toResponse);
    }
    @GET @Path("cep/{cep}")
    public CepDTO consultar(@PathParam("cep") String cep) { return ceps.consultar(cep); }
    @GET @Path("cnpj/{cnpj}")
    public FornecedorResponseDTO buscarPorCnpj(@PathParam("cnpj") @Pattern(regexp="[A-Z0-9]{12}[0-9]{2}") String cnpj) {
        return FornecedorResponseMapper.toResponse(service.findByCnpj(cnpj));
    }
    @GET @Path("{id}")
    public FornecedorResponseDTO buscarPorId(@PathParam("id") long id) {
        return FornecedorResponseMapper.toResponse(service.findById(id));
    }
    @POST
    public FornecedorResponseDTO incluir(@Valid FornecedorRequestDTO dto) {
        return FornecedorResponseMapper.toResponse(service.create(dto));
    }
    @PUT @Path("{id}")
    public void alterar(@PathParam("id") long id, @Valid FornecedorRequestDTO dto) { service.update(id, dto); }
    @DELETE @Path("{id}")
    public void apagar(@PathParam("id") long id) {
        service.delete(id);
    }
}
