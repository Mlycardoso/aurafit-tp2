package br.unitins.tp2.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import org.jboss.logging.Logger;
import br.unitins.tp2.exception.ValidationException;

@ApplicationScoped
public class ProdutoImagemService {
    @Inject ProdutoImagemReferencia referencias;
    @Inject ImagemStorage storage;
    @Inject ImagemValidator validator;
    private static final Logger LOG = Logger.getLogger(ProdutoImagemService.class);
    public record Resultado(String imagemUrl, String aviso) {}

    public Resultado enviar(long id, byte[] bytes, String tipo) {
        String anterior = referencias.atual(id);
        var imagem = validator.validar(bytes, tipo);
        String chave = "produtos/" + id + "/" + UUID.randomUUID() + "." + imagem.extensao();
        storage.salvar(chave, imagem.bytes(), imagem.tipo());
        try {
            if (!referencias.trocar(id, anterior, chave))
                throw ValidationException.of("arquivo", "A imagem foi alterada em outra janela. Recarregue e tente novamente.");
        } catch (RuntimeException e) {
            limpar(chave);
            throw e;
        }
        boolean limpo = anterior == null || limpar(anterior);
        return new Resultado("/produtos/" + id + "/imagem?v=" + chave.substring(chave.lastIndexOf('/') + 1),
            limpo ? null : "Nova imagem salva. A remoção do arquivo anterior precisa ser concluída no armazenamento.");
    }

    public ImagemStorage.Arquivo ler(long id) {
        String chave = referencias.atual(id);
        if (chave == null) throw new NotFoundException();
        return storage.ler(chave);
    }

    public void excluir(long id) {
        String chave = referencias.atual(id);
        if (chave == null) return;
        // Se S3 falhar, a referência permanece para permitir nova tentativa.
        storage.excluir(chave);
        if (!referencias.trocar(id, chave, null))
            throw ValidationException.of("arquivo", "A imagem foi alterada em outra janela. Recarregue para ver a imagem atual.");
    }

    private boolean limpar(String chave) {
        try { storage.excluir(chave); return true; }
        catch (RuntimeException e) {
            LOG.warnf("Limpeza S3 pendente para chave %s (%s)", chave, e.getClass().getSimpleName());
            return false;
        }
    }
}
