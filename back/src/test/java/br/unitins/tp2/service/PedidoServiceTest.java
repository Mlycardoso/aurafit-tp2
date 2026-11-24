package br.unitins.tp2.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.dto.PedidoDTOs;
import br.unitins.tp2.dto.ClienteRequestDTO;
import br.unitins.tp2.model.Cliente;
import br.unitins.tp2.model.ItemPedido;
import br.unitins.tp2.model.Pedido;
import br.unitins.tp2.model.Perfil;
import br.unitins.tp2.model.Produto;
import br.unitins.tp2.model.TipoEntrega;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.CategoriaRepository;
import br.unitins.tp2.repository.MunicipioRepository;
import br.unitins.tp2.repository.PedidoRepository;
import br.unitins.tp2.repository.ProdutoRepository;
import br.unitins.tp2.repository.UsuarioRepository;
import br.unitins.tp2.service.ClienteService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.SecurityContext;

@QuarkusTest
class PedidoServiceTest {
    @Inject PedidoService service;
    @Inject UsuarioRepository usuarios;
    @Inject ProdutoRepository produtos;
    @Inject PedidoRepository pedidos;
    @Inject CategoriaRepository categorias;
    @Inject MunicipioRepository municipios;
    @Inject EntityManager em;
    @Inject ClienteService clienteService;

    @Test @TestTransaction
    void criaPedidoComSnapshotsBaixaEstoqueEAtualizaStatusSemDuplicar() {
        Fixture fixture = fixture(10);
        var created = service.create(new PedidoDTOs.CreateRequest(TipoEntrega.RAPIDA,
                List.of(new PedidoDTOs.ItemRequest(fixture.produto.getId(), 2))), context(fixture.subject));
        assertEquals(new BigDecimal("29.90"), created.frete());
        assertEquals(new BigDecimal("229.90"), created.total());
        assertEquals(fixture.cliente.nome, created.nomeCliente());
        assertTrue(created.itens().getFirst().nomeProduto().startsWith("Chocolate snapshot"));
        assertEquals(8, fixture.produto.getEstoque());
        var sent = service.markSent(created.id());
        assertEquals("ENVIADO", sent.status().name());
        assertEquals(sent, service.markSent(created.id()));
    }

    @Test @TestTransaction
    void estoqueInsuficienteRejeitaPedido() {
        Fixture fixture = fixture(1);
        assertThrows(br.unitins.tp2.exception.UnprocessableException.class, () -> service.create(
                new PedidoDTOs.CreateRequest(TipoEntrega.NORMAL,
                        List.of(new PedidoDTOs.ItemRequest(fixture.produto.getId(), 2))), context(fixture.subject)));
    }

    @Test
    void pedidosConcorrentesNaoVendEmEstoqueEmDuplicidade() throws Exception {
        Fixture fixture = io.quarkus.narayana.jta.QuarkusTransaction.requiringNew().call(() -> fixture(1));
        var executor = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            var first = executor.submit(() -> placeOne(fixture, start));
            var second = executor.submit(() -> placeOne(fixture, start));
            start.countDown();
            int successful = (first.get(30, TimeUnit.SECONDS) ? 1 : 0) + (second.get(30, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, successful);
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean placeOne(Fixture fixture, CountDownLatch start) throws InterruptedException {
        start.await();
        try {
            service.create(new PedidoDTOs.CreateRequest(TipoEntrega.NORMAL,
                    List.of(new PedidoDTOs.ItemRequest(fixture.produto.getId(), 1))), context(fixture.subject));
            return true;
        } catch (br.unitins.tp2.exception.UnprocessableException exhausted) {
            return false;
        }
    }

    private Fixture fixture(int stock) {
        String subject = UUID.randomUUID().toString();
        Cliente client = clienteService.create(new ClienteRequestDTO("Cliente Snapshot", subject + "@test.local",
                "63999999999", "01001000", "Rua original", "Centro", "10", "Apto", null, null,
                true, List.of("63999999999")));
        Usuario user = new Usuario(); user.nome = client.nome; user.email = client.email; user.ativo = true;
        user.keycloakId = subject; user.cliente = client; user.perfis.add(Perfil.CLIENTE); em.persist(user);
        Produto product = new Produto(); product.setNome("Chocolate snapshot " + subject.substring(0, 8)); product.setSku("T-" + subject.substring(0, 8));
        product.setPreco(new BigDecimal("100.00")); product.setEstoque(stock); product.setAtivo(true);
        product.setCategoria(categorias.findAll().firstResult()); em.persist(product);
        em.flush();
        return new Fixture(subject, client, product);
    }

    private SecurityContext context(String subject) {
        return new SecurityContext() {
            private final Principal principal = () -> subject;
            @Override public Principal getUserPrincipal() { return principal; }
            @Override public boolean isUserInRole(String role) { return false; }
            @Override public boolean isSecure() { return true; }
            @Override public String getAuthenticationScheme() { return "Bearer"; }
        };
    }
    private record Fixture(String subject, Cliente cliente, Produto produto) {}
}
