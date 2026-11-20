package br.unitins.tp2.service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import br.unitins.tp2.exception.ValidationException;

class ProdutoImagemServiceTest {
    static class Referencia extends ProdutoImagemReferencia {
        String chave = "anterior.png";
        boolean conflito;
        public String atual(long id) { return chave; }
        public boolean trocar(long id, String esperada, String nova) {
            if (conflito) return false;
            chave = nova; return true;
        }
    }
    static class Storage implements ImagemStorage {
        Map<String, byte[]> objetos = new HashMap<>(Map.of("anterior.png", new byte[]{1}));
        boolean falharUpload, falharDelete;
        public void salvar(String chave, byte[] bytes, String tipo) {
            if (falharUpload) throw new IllegalStateException("indisponível");
            objetos.put(chave, bytes);
        }
        public Arquivo ler(String chave) { return new Arquivo(objetos.get(chave), "image/png"); }
        public void excluir(String chave) {
            if (falharDelete) throw new IllegalStateException("indisponível");
            objetos.remove(chave);
        }
    }
    private ProdutoImagemService criar(Referencia ref, Storage storage) {
        var service = new ProdutoImagemService();
        service.referencias = ref; service.storage = storage;
        service.validator = new ImagemValidator() {
            public Imagem validar(byte[] bytes, String tipo) { return new Imagem(bytes, tipo, "png"); }
        };
        return service;
    }
    @Test void falhaDeUploadPreservaReferenciaEImagemAnterior() {
        var ref = new Referencia(); var storage = new Storage(); storage.falharUpload = true;
        assertThrows(IllegalStateException.class, () -> criar(ref, storage).enviar(1, new byte[]{2}, "image/png"));
        assertEquals("anterior.png", ref.chave); assertEquals(1, storage.objetos.size());
    }
    @Test void falhaDeDeletePreservaReferenciaParaRepeticao() {
        var ref = new Referencia(); var storage = new Storage(); storage.falharDelete = true;
        assertThrows(IllegalStateException.class, () -> criar(ref, storage).excluir(1));
        assertEquals("anterior.png", ref.chave); assertEquals(1, storage.objetos.size());
    }
    @Test void conflitoDeReferenciaLimpaApenasNovoArquivo() {
        var ref = new Referencia(); ref.conflito = true; var storage = new Storage();
        assertThrows(ValidationException.class, () -> criar(ref, storage).enviar(1, new byte[]{2}, "image/png"));
        assertEquals(Set.of("anterior.png"), storage.objetos.keySet());
    }
    @Test void falhaDeLimpezaInformaPendenciaSemPerderImagemNova() {
        var ref = new Referencia(); var storage = new Storage(); storage.falharDelete = true;
        var resultado = criar(ref, storage).enviar(1, new byte[]{2}, "image/png");
        assertNotNull(resultado.aviso()); assertNotEquals("anterior.png", ref.chave);
        assertArrayEquals(new byte[]{2}, storage.objetos.get(ref.chave));
    }
}
