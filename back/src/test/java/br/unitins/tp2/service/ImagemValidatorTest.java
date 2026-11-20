package br.unitins.tp2.service;

import static org.junit.jupiter.api.Assertions.*;
import br.unitins.tp2.exception.ValidationException;
import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

class ImagemValidatorTest {
    private final ImagemValidator validator = new ImagemValidator();
    @Test void rejeitaVazioTipoETamanho() {
        assertThrows(ValidationException.class, () -> validator.validar(new byte[0], "image/png"));
        assertThrows(ValidationException.class, () -> validator.validar(new byte[10], "image/svg+xml"));
        assertThrows(ValidationException.class, () -> validator.validar(new byte[5*1024*1024+1], "image/png"));
        assertThrows(ValidationException.class, () -> validator.validar("texto".getBytes(), "image/png"));
    }
    @Test void rejeitaDimensoesExcessivas() throws Exception {
        var out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(6001, 1, BufferedImage.TYPE_INT_RGB), "png", out);
        assertThrows(ValidationException.class, () -> validator.validar(out.toByteArray(), "image/png"));
    }
    @Test void preservaImagemERemoveConteudoAnexado() throws Exception {
        var out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB), "png", out);
        byte[] original = out.toByteArray();
        out.write("<script>alert(1)</script>".getBytes());
        var result = validator.validar(out.toByteArray(), "image/png");
        assertArrayEquals(original, result.bytes());
        assertEquals("png", result.extensao());
    }
}
