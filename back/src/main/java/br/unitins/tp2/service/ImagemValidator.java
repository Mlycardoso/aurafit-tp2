package br.unitins.tp2.service;

import br.unitins.tp2.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.*;
import javax.imageio.ImageIO;

@ApplicationScoped
public class ImagemValidator {
    public static final int MAX_BYTES = 5 * 1024 * 1024;
    public record Imagem(byte[] bytes, String tipo, String extensao) {}

    public Imagem validar(byte[] bytes, String tipo) {
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_BYTES)
            throw ValidationException.of("arquivo", "Envie uma imagem de até 5 MB, não vazia.");
        if (!"image/png".equals(tipo) && !"image/jpeg".equals(tipo))
            throw ValidationException.of("arquivo", "Use uma imagem PNG ou JPEG.");
        try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException();
            var reader = readers.next();
            try {
                reader.setInput(input);
                String formato = reader.getFormatName().toLowerCase();
                if (!(formato.equals("png") && tipo.equals("image/png")) &&
                    !(formato.equals("jpeg") && tipo.equals("image/jpeg"))) throw new IOException();
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > 6000 || height > 6000 || (long) width * height > 16000000)
                    throw ValidationException.of("arquivo", "A imagem deve ter até 6000 pixels por lado e 16 milhões de pixels.");
                var decoded = reader.read(0);
                var output = new ByteArrayOutputStream();
                // Reencode para descartar metadados e conteúdo anexado ao arquivo.
                if (!ImageIO.write(decoded, formato, output)) throw new IOException();
                if (output.size() > MAX_BYTES) throw ValidationException.of("arquivo", "A imagem processada excede 5 MB. Reduza suas dimensões.");
                return new Imagem(output.toByteArray(), tipo, formato.equals("jpeg") ? "jpg" : "png");
            } finally { reader.dispose(); }
        } catch (IOException | IllegalArgumentException ex) {
            throw ValidationException.of("arquivo", "O conteúdo não corresponde a uma imagem PNG ou JPEG válida.");
        }
    }
}
