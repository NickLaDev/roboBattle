package br.puc.robobattle.ui;

import br.puc.robobattle.combat.Action;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class PixelBattleView {

    private static final int BASE_W = 320;   // resolução base
    private static final int BASE_H = 180;
    private static final int SCALE  = 4;     // tamanho da janela: 320x180 * 4 = 1280x720

    private final UiBattleEngine engine;

    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer loop;

    // assets
    private Image bg;
    private SpriteSheet r1Idle, r1Atk, r2Idle, r2Atk;
    private SpriteAnimator anim1Idle, anim1Atk, anim2Idle, anim2Atk;

    // estado de anim
    private boolean p1Attacking = false;
    private boolean p2Attacking = false;
    private double elapsed;

    public PixelBattleView(UiBattleEngine engine) {
        this.engine = engine;
    }

    public Scene buildScene() {
        // Canvas base
        canvas = new Canvas(BASE_W, BASE_H);
        gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);

        // Carrega assets
        bg     = new Image(getClass().getResourceAsStream("/assets/bg/arena_320x180.png"), 0, 0, false, false);
        r1Idle = new SpriteSheet("/assets/robots/r1_idle.png",   32, 32);
        r1Atk  = new SpriteSheet("/assets/robots/r1_attack.png", 32, 32);
        r2Idle = new SpriteSheet("/assets/robots/r2_idle.png",   32, 32);
        r2Atk  = new SpriteSheet("/assets/robots/r2_attack.png", 32, 32);

        // Animadores (usa número de colunas detectado na imagem)
        anim1Idle = new SpriteAnimator(r1Idle.columns(), 6);
        anim1Atk  = new SpriteAnimator(r1Atk.columns(),  10);
        anim2Idle = new SpriteAnimator(r2Idle.columns(), 6);
        anim2Atk  = new SpriteAnimator(r2Atk.columns(),  10);

        // UI de ações
        Button btnAtk = new Button("ATTACK");
        Button btnDef = new Button("DEFEND");
        Button btnSpc = new Button("SPECIAL");
        Label  lblTop = new Label();

        btnAtk.setOnAction(e -> perform(Action.ATTACK));
        btnDef.setOnAction(e -> perform(Action.DEFEND));
        btnSpc.setOnAction(e -> perform(Action.SPECIAL));

        HBox controls = new HBox(8, btnAtk, btnDef, btnSpc);
        controls.setPadding(new Insets(8));

        // Escala inteira da cena
        Group gameRoot = new Group(canvas);
        gameRoot.setScaleX(SCALE);
        gameRoot.setScaleY(SCALE);

        BorderPane root = new BorderPane();
        root.setTop(lblTop);
        BorderPane.setMargin(lblTop, new Insets(6,8,6,8));
        root.setCenter(gameRoot);
        root.setBottom(controls);

        Scene scene = new Scene(root, BASE_W * SCALE, BASE_H * SCALE + 60, Color.BLACK);

        // Game loop: pixel render
        loop = new AnimationTimer() {
            long last = 0;
            @Override public void handle(long now) {
                if (last == 0) last = now;
                double dt = (now - last) / 1_000_000_000.0;
                last = now;
                update(dt);
                render();
                var s = engine.snapshot();
                lblTop.setText(String.format("Vez de: %s — Round %d", s.currentName, s.round));
                btnSpc.setDisable(!s.currentSpecial);
                boolean finished = s.finished;
                btnAtk.setDisable(finished);
                btnDef.setDisable(finished);
                btnSpc.setDisable(finished || !s.currentSpecial);
            }
        };
        loop.start();

        return scene;
    }

    private void update(double dt) {
        elapsed += dt;

        // avança animações
        if (p1Attacking) anim1Atk.update(dt); else anim1Idle.update(dt);
        if (p2Attacking) anim2Atk.update(dt); else anim2Idle.update(dt);

        // encerra anima de ataque após ~0.35s
        if (p1Attacking && elapsed > 0.35) { p1Attacking = false; anim1Atk.reset(); }
        if (p2Attacking && elapsed > 0.35) { p2Attacking = false; anim2Atk.reset(); }
    }

    private void render() {
        gc.clearRect(0,0,BASE_W,BASE_H);
        gc.drawImage(bg, 0, 0);

        // posições base
        int r1x = 70,  r1y = 110;
        int r2x = 220, r2y = 110;

        // desenha R1
        var img1 = p1Attacking ? r1Atk.frame(anim1Atk.currentIndex()) : r1Idle.frame(anim1Idle.currentIndex());
        drawSprite(img1, r1x, r1y, 2); // 2× (64×64)

        // desenha R2
        var img2 = p2Attacking ? r2Atk.frame(anim2Atk.currentIndex()) : r2Idle.frame(anim2Idle.currentIndex());
        drawSprite(img2, r2x, r2y, 2);

        // barras de HP “pixeladas”
        drawHpBarsPixel();
    }

    private void drawSprite(Image frame, int x, int y, int scale) {
        int w = (int)frame.getWidth();
        int h = (int)frame.getHeight();
        gc.drawImage(frame, x, y, w * scale, h * scale);
    }

    private void drawHpBarsPixel() {
        var s = engine.snapshot();
        // P1
        int p1hp = s.currentName.equals("Jogador 1") ? s.currentHp : s.enemyHp;
        int p1max= s.currentName.equals("Jogador 1") ? s.currentMaxHp : s.enemyMaxHp;
        // P2
        int p2hp = s.currentName.equals("Jogador 2") ? s.currentHp : s.enemyHp;
        int p2max= s.currentName.equals("Jogador 2") ? s.currentMaxHp : s.enemyMaxHp;

        int barW = 80, barH = 6;

        // borda
        gc.setFill(Color.BLACK);
        gc.fillRect(12, 12, barW+2, barH+2);
        gc.fillRect(BASE_W - barW - 14, 12, barW+2, barH+2);

        // preenchimento
        double r1 = Math.max(0, (double)p1hp / Math.max(1, p1max));
        double r2 = Math.max(0, (double)p2hp / Math.max(1, p2max));
        gc.setFill(Color.web("#2aff2a"));
        gc.fillRect(13, 13, (int)(barW * r1), barH);
        gc.setFill(Color.web("#ff3b3b"));
        gc.fillRect(BASE_W - barW - 13, 13, (int)(barW * r2), barH);
    }

    private void perform(Action a) {
        var before = engine.snapshot();
        var res = engine.perform(a);
        if (a == Action.ATTACK || a == Action.SPECIAL) {
            if (before.currentName.equals("Jogador 1")) p1Attacking = true; else p2Attacking = true;
            elapsed = 0;
        }
        // res.logs disponível se quiser exibir texto também
    }
}
