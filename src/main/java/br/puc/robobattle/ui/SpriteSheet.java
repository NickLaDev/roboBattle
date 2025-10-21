package br.puc.robobattle.ui;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SpriteSheet {
    private final Image sheet;
    private final int frameW, frameH;
    private final int columns;

    public SpriteSheet(String resourcePath, int frameW, int frameH) {
        InputStream is = SpriteSheet.class.getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IllegalArgumentException("Recurso não encontrado: " + resourcePath +
                    " (confira src/main/resources e o nome exato do arquivo)");
        }
        Image raw = new Image(is, 0, 0, false, false);
        if (raw.getWidth() <= 0 || raw.getHeight() <= 0) {
            throw new IllegalStateException("Falha ao carregar imagem: " + resourcePath);
        }

        // Remove “xadrez”/fundo sólido: pega cores de borda e zera alpha dessas cores (com tolerância).
        this.sheet = stripBackground(raw, 14); // tolerância (0–255). Aumente se ainda sobrar halo.

        this.frameW = frameW;
        this.frameH = frameH;

        if (((int) sheet.getWidth()) % frameW != 0 || ((int) sheet.getHeight()) % frameH != 0) {
            throw new IllegalStateException("Dimensões incompatíveis para frames " + frameW + "x" + frameH +
                    " em " + resourcePath + " (width=" + (int) sheet.getWidth() + ", height=" + (int) sheet.getHeight() + "). " +
                    "Esperado múltiplos exatos do tamanho do frame.");
        }
        this.columns = (int) (sheet.getWidth() / frameW);

        System.out.println("[SpriteSheet] " + resourcePath +
                " carregado: " + (int) sheet.getWidth() + "x" + (int) sheet.getHeight() +
                " | frames=" + columns);
    }

    public Image frame(int index) {
        int x = (index % columns) * frameW;
        int y = (index / columns) * frameH;
        return new WritableImage(sheet.getPixelReader(), x, y, frameW, frameH);
    }

    public int columns() { return columns; }
    public int frameW() { return frameW; }
    public int frameH() { return frameH; }

    // --- Utilitário: remove cores de borda (checkerboard/chapadas) tornando-as transparentes
    private static Image stripBackground(Image src, int tol) {
        int w = (int) src.getWidth();
        int h = (int) src.getHeight();
        PixelReader pr = src.getPixelReader();
        WritableImage out = new WritableImage(w, h);
        PixelWriter pw = out.getPixelWriter();

        // Coleta cores de borda (quatro cantos + centro das bordas)
        List<Color> bg = new ArrayList<>();
        int[][] samples = {
                {0, 0}, {w - 1, 0}, {0, h - 1}, {w - 1, h - 1},
                {w / 2, 0}, {w / 2, h - 1}, {0, h / 2}, {w - 1, h / 2}
        };
        for (int[] s : samples) {
            Color c = pr.getColor(s[0], s[1]);
            addDistinct(bg, c, 3.0 / 255.0); // evita duplicar cores quase iguais
        }

        // Para cada pixel: se for “parecido” com alguma cor de fundo, zera alpha
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = pr.getColor(x, y);
                if (matchesAny(c, bg, tol / 255.0)) {
                    pw.setColor(x, y, new Color(0, 0, 0, 0));
                } else {
                    pw.setColor(x, y, c);
                }
            }
        }
        return out;
    }

    private static boolean matchesAny(Color c, List<Color> keys, double tol) {
        for (Color k : keys) {
            if (close(c.getRed(),   k.getRed(),   tol) &&
                    close(c.getGreen(), k.getGreen(), tol) &&
                    close(c.getBlue(),  k.getBlue(),  tol)) {
                return true;
            }
        }
        return false;
    }

    private static boolean close(double a, double b, double tol) {
        return Math.abs(a - b) <= tol;
    }

    private static void addDistinct(List<Color> list, Color c, double tol) {
        for (Color k : list) {
            if (close(c.getRed(), k.getRed(), tol) &&
                    close(c.getGreen(), k.getGreen(), tol) &&
                    close(c.getBlue(), k.getBlue(), tol)) {
                return;
            }
        }
        list.add(c);
    }
}
