package br.unitins.tp2.service;

import br.unitins.tp2.repository.ProdutoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class ProdutoImagemReferencia {
    @Inject ProdutoRepository produtos;

    public String atual(long id) {
        var produto = produtos.findById(id);
        if (produto == null) throw new NotFoundException();
        return produto.getImagemChave();
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public boolean trocar(long id, String esperada, String nova) {
        // Compare-and-set: uma resposta atrasada não sobrescreve outra edição de imagem.
        long updated = esperada == null
            ? produtos.update("imagemChave = ?1 where id = ?2 and imagemChave is null", nova, id)
            : produtos.update("imagemChave = ?1 where id = ?2 and imagemChave = ?3", nova, id, esperada);
        return updated == 1;
    }
}
