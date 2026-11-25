package br.unitins.tp2.service;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class UsuarioSchemaMigration {
    private static final Logger LOG = Logger.getLogger(UsuarioSchemaMigration.class);
    @Inject DataSource dataSource;

    void removeLegacyPasswordColumn(@Observes StartupEvent event) {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("ALTER TABLE usuario DROP COLUMN IF EXISTS senhahash");
        } catch (SQLException exception) {
            LOG.error("Não foi possível remover a coluna antiga de senhaHash da tabela usuario.");
            throw new IllegalStateException("Migração de segurança da tabela de usuários falhou.", exception);
        }
    }
}
