package br.unitins.tp2;

// Limpeza exclusiva do banco de testes; a API nunca apaga produtos.
final class ProdutoTestCleanup {
    static void remover(long id) {
        io.quarkus.narayana.jta.QuarkusTransaction.requiringNew().run(() ->
            jakarta.enterprise.inject.spi.CDI.current().select(br.unitins.tp2.repository.ProdutoRepository.class)
                .get().deleteById(id));
    }
}
