package br.puc.robobattle.ui;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Carrega um spritesheet em qualquer resolução e calcula automaticamente o tamanho
 * de cada frame a partir de (colunas x linhas). Faz também uma limpeza simples
 * de fundo "pintado" (xadrez branco/cinza) para transparência, se necessário.
 */
public class SpriteSheet {
    private final Image sheet;
    private final int frameW, frameH;
    private final int columns, rows;

    /**
     * @param resourcePath caminho no classpath (ex.: "/assets/robots/r1_idle.png")
     * @param columns número de colunas (ex.: 4 frames na horizontal → 4)
     * @param rows número de linhas (ex.: 1 linha → 1)
     */
    public SpriteSheet(String resourcePath, int columns, int rows) {
        this.columns = columns;
        this.rows = rows;

        InputStream is = SpriteSheet.class.getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IllegalArgumentException("Recurso não encontrado: " + resourcePath +
                    " (confira src/main/resources e o nome exato do arquivo)");
        }

        Image raw = new Image(is, 0, 0, false, false);
        if (raw.getWidth() <= 0 || raw.getHeight() <= 0) {
            throw new IllegalStateException("Falha ao carregar imagem: " + resourcePath);
        }

        // Remove fundo pintado (xadrez/cores sólidas) caso haja – vira alpha=0
        this.sheet = stripBackground(raw);

        // Calcula tamanho do frame automaticamente
        this.frameW = (int) Math.round(sheet.getWidth() / columns);
        this.frameH = (int) Math.round(sheet.getHeight() / rows);

        System.out.println("[SpriteSheet] " + resourcePath + " carregado "
                + (int)sheet.getWidth() + "x" + (int)sheet.getHeight()
                + " | frame=" + frameW + "x" + frameH
                + " | col=" + columns + " row=" + rows);
    }

    /** Retorna o frame correspondente ao índice (0-based). */
    public Image frame(int index) {
        int x = (index % columns) * frameW;
        int y = (index / columns) * frameH;
        return new WritableImage(sheet.getPixelReader(), x, y, frameW, frameH);
    }

    public int columns() { return columns; }
    public int frameW() { return frameW; }
    public int frameH() { return frameH; }

    // =====================================================================
    // Remoção de fundo pintado → torna transparente (chroma simples)
    // =====================================================================
    private static Image stripBackground(Image src) {
        int w = (int) src.getWidth();
        int h = (int) src.getHeight();
        PixelReader pr = src.getPixelReader();
        WritableImage out = new WritableImage(w, h);
        PixelWriter pw = out.getPixelWriter();

        List<int[]> bgColors = sampleBorderColors(pr, w, h);
        final int tol = 15; // tolerância para variações leves

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = pr.getArgb(x, y);
                int a = (argb >>> 24) & 0xFF;
                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = argb & 0xFF;

                if (a == 0) { pw.setArgb(x, y, argb); continue; }

                if (matchesAny(r, g, b, bgColors, tol)) {
                    // pixel de fundo → alpha 0
                    pw.setArgb(x, y, (0 << 24) | (r << 16) | (g << 8) | b);
                } else {
                    pw.setArgb(x, y, argb);
                }
            }
        }
        return out;
    }

    private static boolean matchesAny(int r, int g, int b, List<int[]> colors, int tol) {
        for (int[] c : colors) {
            if (Math.abs(r - c[0]) <= tol &&
                    Math.abs(g - c[1]) <= tol &&
                    Math.abs(b - c[2]) <= tol) {
                return true;
            }
        }
        return false;
    }

    private static List<int[]> sampleBorderColors(PixelReader pr, int w, int h) {
        List<int[]> list = new ArrayList<>();
        int[][] pts = {
                {0,0},{1,1},{w-1,0},{0,h-1},{w-1,h-1},
                {w/2,0},{0,h/2},{w-1,h/2},{w/2,h-1}
        };
        for (int[] p : pts) {
            int x = clamp(p[0], 0, w-1);
            int y = clamp(p[1], 0, h-1);
            int argb = pr.getArgb(x, y);
            int a = (argb >>> 24) & 0xFF;
            if (a < 240) continue; // já é transparente/semi
            int r = (argb >>> 16) & 0xFF;
            int g = (argb >>> 8) & 0xFF;
            int b = argb & 0xFF;
            boolean dup = false;
            for (int[] c : list) if (c[0]==r && c[1]==g && c[2]==b) { dup = true; break; }
            if (!dup) list.add(new int[]{r,g,b});
            if (list.size() >= 6) break;
        }
        return list;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
