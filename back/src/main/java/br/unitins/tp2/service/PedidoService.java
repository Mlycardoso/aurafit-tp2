package br.unitins.tp2.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.dto.PedidoDTOs;
import br.unitins.tp2.model.Cliente;
import br.unitins.tp2.model.ItemPedido;
import br.unitins.tp2.model.Pedido;
import br.unitins.tp2.model.Perfil;
import br.unitins.tp2.model.Produto;
import br.unitins.tp2.model.StatusPedido;
import br.unitins.tp2.model.TipoEntrega;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.exception.UnprocessableException;
import br.unitins.tp2.repository.PedidoRepository;
import br.unitins.tp2.repository.ProdutoRepository;
import br.unitins.tp2.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.SecurityContext;

@ApplicationScoped
public class PedidoService {
    private static final BigDecimal FRETE_NORMAL = new BigDecimal("14.90");
    private static final BigDecimal FRETE_RAPIDO = new BigDecimal("29.90");
    @Inject PedidoRepository pedidos;
    @Inject ProdutoRepository produtos;
    @Inject UsuarioRepository usuarios;
    @Inject EntityManager entityManager;

    @Transactional
    public PedidoDTOs.Response create(PedidoDTOs.CreateRequest request, SecurityContext context) {
        Usuario usuario = usuarioAutenticado(context);
        Cliente cliente = usuario.cliente;
        if (cliente == null || !cliente.ativo) throw new NotFoundException("Cadastro de cliente não encontrado.");
        Pedido pedido = new Pedido();
        pedido.cliente = cliente;
        pedido.dataHora = OffsetDateTime.now();
        pedido.status = StatusPedido.PENDENTE;
        pedido.tipoEntrega = request.tipoEntrega();
        pedido.frete = request.tipoEntrega() == TipoEntrega.RAPIDA ? FRETE_RAPIDO : FRETE_NORMAL;
        pedido.prazoEntrega = prazo(LocalDate.now(), request.tipoEntrega() == TipoEntrega.RAPIDA ? 2 : 7);
        pedido.nomeCliente = cliente.nome;
        pedido.cep = cliente.cep;
        pedido.logradouro = cliente.logradouro;
        pedido.numero = cliente.numero;
        pedido.complemento = cliente.complemento;
        pedido.bairro = cliente.bairro;
        pedido.municipio = cliente.municipio.nome;
        pedido.estado = cliente.municipio.estado.sigla;
        BigDecimal subtotal = BigDecimal.ZERO;
        for (PedidoDTOs.ItemRequest requestItem : request.itens()) {
            Produto produto = entityManager.find(Produto.class, requestItem.produtoId(), LockModeType.PESSIMISTIC_WRITE);
            if (produto == null || !Boolean.TRUE.equals(produto.getAtivo()))
                throw new UnprocessableException("Um ou mais produtos não estão disponíveis para compra.");
            if (requestItem.quantidade() > produto.getEstoque())
                throw new UnprocessableException("Estoque insuficiente para " + produto.getNome() + ". Disponível: " + produto.getEstoque() + ".");
            produto.setEstoque(produto.getEstoque() - requestItem.quantidade());
            ItemPedido item = new ItemPedido();
            item.pedido = pedido;
            item.produto = produto;
            item.nomeProduto = produto.getNome();
            item.sku = produto.getSku();
            item.quantidade = requestItem.quantidade();
            item.precoUnitario = produto.getPreco();
            pedido.itens.add(item);
            subtotal = subtotal.add(item.precoUnitario.multiply(BigDecimal.valueOf(item.quantidade)));
        }
        pedido.total = subtotal.add(pedido.frete);
        pedidos.persistAndFlush(pedido);
        return response(pedido);
    }

    @Transactional
    public List<PedidoDTOs.Response> mine(SecurityContext context) {
        Usuario usuario = usuarioAutenticado(context);
        if (usuario.cliente == null) return List.of();
        return pedidos.find("cliente.id = ?1 order by dataHora desc", usuario.cliente.getId()).list().stream().map(PedidoService::response).toList();
    }

    @Transactional
    public PageResponse<PedidoDTOs.Response> list(StatusPedido status, String name, int page, int size) {
        StringBuilder query = new StringBuilder("1 = 1");
        Map<String, Object> params = new HashMap<>();
        if (status != null) { query.append(" and status = :status"); params.put("status", status); }
        if (name != null && !name.isBlank()) { query.append(" and lower(nomeCliente) like :name"); params.put("name", "%" + name.strip().toLowerCase(Locale.ROOT) + "%"); }
        var result = pedidos.find(query + " order by dataHora desc", params);
        long total = pedidos.count(query.toString(), params);
        return PageResponse.of(result.page(page, size).list(), page, size, total, PedidoService::response);
    }

    @Transactional
    public PedidoDTOs.Response find(long id, SecurityContext context) {
        Pedido pedido = pedidos.findByIdOptional(id).orElseThrow(() -> new NotFoundException("Pedido não encontrado."));
        if (!context.isUserInRole("ADMIN")) {
            Usuario usuario = usuarioAutenticado(context);
            if (usuario.cliente == null || !usuario.cliente.getId().equals(pedido.cliente.getId()))
                throw new NotFoundException("Pedido não encontrado.");
        }
        return response(pedido);
    }

    @Transactional
    public PedidoDTOs.Response markSent(long id) {
        Pedido pedido = pedidos.findByIdOptional(id).orElseThrow(() -> new NotFoundException("Pedido não encontrado."));
        if (pedido.status == StatusPedido.PENDENTE) pedido.status = StatusPedido.ENVIADO;
        return response(pedido);
    }

    private Usuario usuarioAutenticado(SecurityContext context) {
        String subject = context.getUserPrincipal() == null ? null : context.getUserPrincipal().getName();
        return usuarios.find("keycloakId", subject).firstResultOptional().orElseThrow(() -> new NotFoundException("Usuário AuraFit não encontrado."));
    }

    private static LocalDate prazo(LocalDate start, int workingDays) {
        LocalDate date = start;
        int days = 0;
        while (days < workingDays) {
            date = date.plusDays(1);
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) days++;
        }
        return date;
    }

    private static PedidoDTOs.Response response(Pedido pedido) {
        List<PedidoDTOs.ItemResponse> items = pedido.itens.stream().map(item -> new PedidoDTOs.ItemResponse(
                item.produto == null ? null : item.produto.getId(), item.nomeProduto, item.sku,
                item.quantidade, item.precoUnitario)).toList();
        return new PedidoDTOs.Response(pedido.getId(), pedido.dataHora, pedido.status, pedido.tipoEntrega,
                pedido.frete, pedido.prazoEntrega, pedido.nomeCliente, pedido.cep, pedido.logradouro,
                pedido.numero, pedido.complemento, pedido.bairro, pedido.municipio, pedido.estado, pedido.total, items);
    }
}
