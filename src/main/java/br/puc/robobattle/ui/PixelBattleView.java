package br.puc.robobattle.ui;

import br.puc.robobattle.combat.Action;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * View de batalha com spritesheets (idle/attack/run/defend/hurt/death).
 * O atacante corre até o oponente, golpeia e retorna.
 * Prioridade: DEATH > HURT > DEFEND > RUN > ATTACK > IDLE
 *
 * Nesta versão:
 * - BG usa terrace.png
 * - Banner central superior “rpg/castelo” mostrando de quem é a vez
 * - Botões dentro da arena, em um painel de pedra translúcido
 * - Atalhos 1/2/3
 */
public class PixelBattleView {

    // ======== Config da cena ========
    private static final int BASE_W = 1280;
    private static final int BASE_H = 720;
    private static final boolean SMOOTHING = true;
    private static final double SPRITE_SCALE = 1.5;
    private static final int R_Y = 480;
    private static final int GAP = 80;

    private static final double RUN_SPEED = 900.0;  // px/s
    private static final int HIT_OVERLAP = 20;      // invade um pouco o centro

    private final UiBattleEngine engine;

    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer loop;

    // BG
    private Image arena;

    // Idles
    private SpriteSheet r1Idle, r2Idle;
    private SpriteAnimator anim1Idle, anim2Idle;

    // Ataques (3 variações por lado)
    private SpriteSheet[] r1Atks, r2Atks;
    private SpriteAnimator[] a1Atks, a2Atks;

    // RUN
    private SpriteSheet r1Run, r2Run;
    private SpriteAnimator anim1Run, anim2Run;

    // DEFENSE / HURT
    private SpriteSheet r1Defend, r2Defend;
    private SpriteAnimator anim1Defend, anim2Defend;
    private boolean p1DefendPlaying = false, p2DefendPlaying = false;
    private double tDef1 = 0, tDef2 = 0, durDef1 = 0, durDef2 = 0;

    private SpriteSheet r1Hurt, r2Hurt;
    private SpriteAnimator anim1Hurt, anim2Hurt;
    private boolean p1HurtPlaying = false, p2HurtPlaying = false;
    private double tHurt1 = 0, tHurt2 = 0, durHurt1 = 0, durHurt2 = 0;

    // DEATH
    private SpriteSheet r1Death, r2Death;
    private SpriteAnimator anim1Death, anim2Death;
    private boolean p1DeadStarted = false, p2DeadStarted = false;
    private boolean p1DeathDone = false, p2DeathDone = false;
    private double tDeath1 = 0, tDeath2 = 0;
    private double durDeath1 = 0, durDeath2 = 0;

    // Estado de ataque atual (animação do golpe)
    private boolean p1Attacking = false, p2Attacking = false;
    private int curAtk1 = -1, curAtk2 = -1;
    private double tAtk1 = 0, tAtk2 = 0;
    private double atkDur1 = 0, atkDur2 = 0;

    // FPS
    private final double idleFps1 = 6.0, idleFps2 = 6.0;
    private final double[] atkFps1 = {10.0, 12.0, 14.0};
    private final double[] atkFps2 = {12.0, 14.0, 16.0};
    private final double runFps1   = 14.0, runFps2   = 14.0;
    private final double defendFps1 = 10.0, defendFps2 = 10.0;
    private final double hurtFps1   = 12.0, hurtFps2   = 12.0;
    private final double deathFps1  = 8.0,  deathFps2  = 8.0;

    // Cinemática de aproximação
    private enum Phase { NONE, APPROACH, IMPACT, RETURN }
    private Phase phase = Phase.NONE;
    private boolean attackerIsP1 = true;          // baseado em before.currentName == "Jogador 1"
    private boolean inputLocked = false;
    private Action pendingAction = null;          // ATTACK ou SPECIAL
    private UiBattleEngine.Snapshot beforeSnap;   // snapshot antes do impacto
    private double approachTarget = 0;
    private double attackTimer = 0;

    // Deslocamentos horizontais (px)
    private double r1OffsetX = 0, r2OffsetX = 0;

    // ====== HUD / Botões (overlay) ======
    private Label lblTurn;
    private Button btnAtk, btnDef, btnSpc;
    private HBox buttons;
    private String lastAccent = "";

    private final Random rng = new Random();

    public PixelBattleView(UiBattleEngine engine) { this.engine = engine; }

    public Scene buildScene() {
        canvas = new Canvas(BASE_W, BASE_H);
        gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(SMOOTHING);

        // ===== BG: usa o terrace.png que você mandou =====
        arena = new Image(getClass().getResourceAsStream("/assets/bg/terrace.png"), 0, 0, false, false);

        // ===== Sprites =====
        r1Idle = new SpriteSheet("/assets/robots/r1_idle.png", 6, 1);
        r2Idle = new SpriteSheet("/assets/robots/r2_idle.png", 6, 1);
        anim1Idle = new SpriteAnimator(r1Idle.columns(), idleFps1);
        anim2Idle = new SpriteAnimator(r2Idle.columns(), idleFps2);

        r1Atks = new SpriteSheet[] {
                new SpriteSheet("/assets/robots/r1_attack1.png", 4, 1),
                new SpriteSheet("/assets/robots/r1_attack2.png", 4, 1),
                new SpriteSheet("/assets/robots/r1_attack3.png", 4, 1)
        };
        r2Atks = new SpriteSheet[] {
                new SpriteSheet("/assets/robots/r2_attack1.png", 4, 1),
                new SpriteSheet("/assets/robots/r2_attack2.png", 4, 1),
                new SpriteSheet("/assets/robots/r2_attack3.png", 4, 1)
        };
        a1Atks = new SpriteAnimator[] {
                new SpriteAnimator(r1Atks[0].columns(), atkFps1[0]),
                new SpriteAnimator(r1Atks[1].columns(), atkFps1[1]),
                new SpriteAnimator(r1Atks[2].columns(), atkFps1[2])
        };
        a2Atks = new SpriteAnimator[] {
                new SpriteAnimator(r2Atks[0].columns(), atkFps2[0]),
                new SpriteAnimator(r2Atks[1].columns(), atkFps2[1]),
                new SpriteAnimator(r2Atks[2].columns(), atkFps2[2])
        };

        r1Run = new SpriteSheet("/assets/robots/r1_run.png", 8, 1);
        r2Run = new SpriteSheet("/assets/robots/r2_run.png", 8, 1);
        anim1Run = new SpriteAnimator(r1Run.columns(), runFps1);
        anim2Run = new SpriteAnimator(r2Run.columns(), runFps2);

        r1Defend = new SpriteSheet("/assets/robots/r1_defend.png", 2, 1);
        r2Defend = new SpriteSheet("/assets/robots/r2_defend.png", 2, 1);
        anim1Defend = new SpriteAnimator(r1Defend.columns(), defendFps1);
        anim2Defend = new SpriteAnimator(r2Defend.columns(), defendFps2);
        durDef1 = r1Defend.columns() / defendFps1;
        durDef2 = r2Defend.columns() / defendFps2;

        r1Hurt = new SpriteSheet("/assets/robots/r1_hurt.png", 3, 1);
        r2Hurt = new SpriteSheet("/assets/robots/r2_hurt.png", 3, 1);
        anim1Hurt = new SpriteAnimator(r1Hurt.columns(), hurtFps1);
        anim2Hurt = new SpriteAnimator(r2Hurt.columns(), hurtFps2);
        durHurt1 = r1Hurt.columns() / hurtFps1;
        durHurt2 = r2Hurt.columns() / hurtFps2;

        r1Death = new SpriteSheet("/assets/robots/r1_death.png", 3, 1);
        r2Death = new SpriteSheet("/assets/robots/r2_death.png", 3, 1);
        anim1Death = new SpriteAnimator(r1Death.columns(), deathFps1);
        anim2Death = new SpriteAnimator(r2Death.columns(), deathFps2);
        durDeath1 = r1Death.columns() / deathFps1;
        durDeath2 = r2Death.columns() / deathFps2;

        // ===== Overlay dentro da arena =====
        StackPane root = new StackPane(canvas);

        // Banner central superior (tema castelo)
        lblTurn = new Label();
        lblTurn.setAlignment(Pos.CENTER);
        lblTurn.setTextFill(Color.web("#ffe9b6")); // dourado claro
        lblTurn.setStyle("""
            -fx-font-family: "Georgia", "Times New Roman", serif;
            -fx-font-size: 22px;
            -fx-font-weight: bold;
            -fx-padding: 8 20 8 20;
            -fx-background-color: linear-gradient(rgba(35,22,45,0.84), rgba(25,16,32,0.84));
            -fx-background-radius: 18;
            -fx-border-radius: 18;
            -fx-border-color: rgba(252, 211, 77, 0.85); /* dourado */
            -fx-border-width: 2;
        """);
        DropShadow bannerGlow = new DropShadow(26, Color.web("#f59e0b"));
        bannerGlow.setSpread(0.15);
        lblTurn.setEffect(bannerGlow);

        HBox bannerBox = new HBox(lblTurn);
        bannerBox.setAlignment(Pos.TOP_CENTER);
        bannerBox.setPadding(new Insets(14, 0, 0, 0));
        bannerBox.setMouseTransparent(true);

        // Botões em “pedra” translúcida
        btnAtk = makeStoneButton("⚔ ATTACK");
        btnDef = makeStoneButton("🛡 DEFEND");
        btnSpc = makeStoneButton("⚡ SPECIAL");

        btnAtk.setOnAction(e -> perform(Action.ATTACK));
        btnDef.setOnAction(e -> perform(Action.DEFEND));
        btnSpc.setOnAction(e -> perform(Action.SPECIAL));

        buttons = new HBox(16, btnAtk, btnDef, btnSpc);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10));
        buttons.setMaxWidth(560);
        buttons.setStyle(pedestalStyle("#9a7bd1")); // borda violeta que combina com o BG

        VBox bottomBox = new VBox(buttons);
        bottomBox.setAlignment(Pos.BOTTOM_CENTER);
        bottomBox.setPadding(new Insets(0, 0, 20, 0));

        BorderPane overlay = new BorderPane();
        overlay.setPickOnBounds(false);
        overlay.setTop(bannerBox);
        overlay.setBottom(bottomBox);

        root.getChildren().add(overlay);

        Scene scene = new Scene(root, BASE_W, BASE_H, Color.BLACK);

        // Atalhos 1/2/3
        scene.setOnKeyPressed(ev -> {
            if (inputLocked || engine.snapshot().finished) return;
            if (ev.getCode() == KeyCode.DIGIT1) btnAtk.fire();
            else if (ev.getCode() == KeyCode.DIGIT2) btnDef.fire();
            else if (ev.getCode() == KeyCode.DIGIT3 && !btnSpc.isDisabled()) btnSpc.fire();
        });

        // ==== GAME LOOP ====
        loop = new AnimationTimer() {
            long last = 0;
            @Override public void handle(long now) {
                if (last == 0) last = now;
                double dt = (now - last) / 1_000_000_000.0;
                last = now;
                update(dt);
                render();

                var s = engine.snapshot();

                // Texto centralizado “de quem é a vez”
                lblTurn.setText("Vez de: " + s.currentName + "  —  Round " + s.round);

                // Accent do banner e do pedestal conforme lado (só quando muda)
                String accent = s.currentName.equals("Jogador 1") ? "#60a5fa" : "#fb7185"; // azul x rosa
                if (!accent.equals(lastAccent)) {
                    lastAccent = accent;
                    bannerGlow.setColor(Color.web(accent));
                    lblTurn.setStyle(bannerStyle(accent));
                    buttons.setStyle(pedestalStyle(accent));
                }

                boolean finished = s.finished;
                btnAtk.setDisable(finished || inputLocked);
                btnDef.setDisable(finished || inputLocked);
                btnSpc.setDisable(finished || inputLocked || !s.currentSpecial);
            }
        };
        loop.start();
        return scene;
    }

    // ======== Estilos ========
    private String bannerStyle(String accentHex) {
        return """
            -fx-font-family: "Georgia", "Times New Roman", serif;
            -fx-font-size: 22px;
            -fx-font-weight: bold;
            -fx-padding: 8 20 8 20;
            -fx-background-color: linear-gradient(rgba(35,22,45,0.84), rgba(25,16,32,0.84));
            -fx-background-radius: 18;
            -fx-border-radius: 18;
            -fx-border-color: %s;
            -fx-border-width: 2;
        """.formatted(Color.web(accentHex, 0.9).toString().replace("0x", "#"));
    }

    private String pedestalStyle(String accentHex) {
        // painel de pedra translúcido
        String border = Color.web(accentHex, 0.75).toString().replace("0x", "#");
        return """
            -fx-background-color:
              linear-gradient(rgba(24,18,30,0.85), rgba(18,12,24,0.85)),
              radial-gradient(radius 100%%, rgba(255,255,255,0.06), rgba(0,0,0,0.06));
            -fx-background-radius: 16;
            -fx-border-radius: 16;
            -fx-border-color: %s;
            -fx-border-width: 2;
        """.formatted(border);
    }

    private Button makeStoneButton(String text) {
        Button b = new Button(text);
        b.setFocusTraversable(false);
        b.setTextFill(Color.web("#f5f3ff"));
        b.setStyle("""
           -fx-font-size: 16px;
           -fx-font-weight: bold;
           -fx-background-radius: 12;
           -fx-border-radius: 12;
           -fx-padding: 8 20 8 20;
           -fx-background-color: linear-gradient(#3a2f47, #1f182a);
           -fx-border-color: rgba(255,255,255,0.15);
           -fx-border-width: 1.2;
        """);
        DropShadow glow = new DropShadow(20, Color.web("#c084fc"));
        glow.setSpread(0.18);
        b.setOnMouseEntered(e -> { b.setEffect(glow); b.setScaleX(1.05); b.setScaleY(1.05); });
        b.setOnMouseExited (e -> { b.setEffect(null); b.setScaleX(1.0); b.setScaleY(1.0); });
        b.setOnMousePressed(e -> { b.setScaleX(0.96); b.setScaleY(0.96); });
        b.setOnMouseReleased(e -> { b.setScaleX(1.05); b.setScaleY(1.05); });
        return b;
    }

    // =================== GAME LOGIC (inalterada) ===================
    private void update(double dt) {
        var snap = engine.snapshot();

        // Fim de luta: só death
        if (snap.finished) {
            startDeathIfNeeded(snap);
            if (p1DeadStarted && !p1DeathDone) { anim1Death.update(dt); tDeath1 += dt; if (tDeath1 >= durDeath1) p1DeathDone = true; }
            if (p2DeadStarted && !p2DeathDone) { anim2Death.update(dt); tDeath2 += dt; if (tDeath2 >= durDeath2) p2DeathDone = true; }
            return;
        }

        // Cinemática de corrida
        switch (phase) {
            case APPROACH -> {
                if (attackerIsP1) {
                    anim1Run.update(dt);
                    r1OffsetX = clamp(r1OffsetX + RUN_SPEED * dt, 0, approachTarget);
                    if (r1OffsetX >= approachTarget - 1e-3) startImpact();
                } else {
                    anim2Run.update(dt);
                    r2OffsetX = clamp(r2OffsetX - RUN_SPEED * dt, -approachTarget, 0);
                    if (Math.abs(r2OffsetX + approachTarget) <= 1e-3) startImpact();
                }
                return;
            }
            case IMPACT -> {
                if (attackerIsP1 && p1Attacking) { a1Atks[curAtk1].update(dt); tAtk1 += dt; }
                if (!attackerIsP1 && p2Attacking) { a2Atks[curAtk2].update(dt); tAtk2 += dt; }
                attackTimer += dt;
                if (attackTimer >= (attackerIsP1 ? atkDur1 : atkDur2)) {
                    if (attackerIsP1) { p1Attacking = false; } else { p2Attacking = false; }
                    phase = Phase.RETURN;
                }
                tickDefendHurt(dt);
                return;
            }
            case RETURN -> {
                if (attackerIsP1) {
                    anim1Run.update(dt);
                    r1OffsetX = clamp(r1OffsetX - RUN_SPEED * dt, 0, approachTarget);
                    if (r1OffsetX <= 0) finishCinematic();
                } else {
                    anim2Run.update(dt);
                    r2OffsetX = clamp(r2OffsetX + RUN_SPEED * dt, -approachTarget, 0);
                    if (r2OffsetX >= 0) finishCinematic();
                }
                tickDefendHurt(dt);
                return;
            }
            case NONE -> { /* segue fluxo normal */ }
        }

        // Fluxo normal
        tickDefendHurt(dt);

        if (p1Attacking) {
            a1Atks[curAtk1].update(dt);
            tAtk1 += dt;
            if (tAtk1 >= atkDur1) { p1Attacking = false; a1Atks[curAtk1].reset(); curAtk1 = -1; }
        } else {
            anim1Idle.update(dt);
        }

        if (p2Attacking) {
            a2Atks[curAtk2].update(dt);
            tAtk2 += dt;
            if (tAtk2 >= atkDur2) { p2Attacking = false; a2Atks[curAtk2].reset(); curAtk2 = -1; }
        } else {
            anim2Idle.update(dt);
        }
    }

    private void tickDefendHurt(double dt) {
        if (p1DefendPlaying) { anim1Defend.update(dt); tDef1 += dt; if (tDef1 >= durDef1) p1DefendPlaying = false; }
        if (p2DefendPlaying) { anim2Defend.update(dt); tDef2 += dt; if (tDef2 >= durDef2) p2DefendPlaying = false; }
        if (p1HurtPlaying)   { anim1Hurt.update(dt);   tHurt1 += dt; if (tHurt1 >= durHurt1) p1HurtPlaying = false; }
        if (p2HurtPlaying)   { anim2Hurt.update(dt);   tHurt2 += dt; if (tHurt2 >= durHurt2) p2HurtPlaying = false; }
    }

    /** Executa o golpe no IMPACT, decide DEFEND/HURT e liga a anim de ataque. */
    private void startImpact() {
        engine.perform(pendingAction); // aplica dano, troca turno, etc.

        // Se acabou a luta, a sequência encerra
        if (engine.snapshot().finished) {
            phase = Phase.NONE;
            inputLocked = false;
            return;
        }

        // Liga animação de ataque do atacante correto
        if (attackerIsP1) {
            curAtk1 = rng.nextInt(r1Atks.length);
            p1Attacking = true; tAtk1 = 0;
            atkDur1 = (double) r1Atks[curAtk1].columns() / fpsAtk1(curAtk1);
            a1Atks[curAtk1].reset();
        } else {
            curAtk2 = rng.nextInt(r2Atks.length);
            p2Attacking = true; tAtk2 = 0;
            atkDur2 = (double) r2Atks[curAtk2].columns() / fpsAtk2(curAtk2);
            a2Atks[curAtk2].reset();
        }

        // Decide DEFEND/HURT (com base no snapshot antes/depois)
        var after = engine.snapshot();
        int enemyHpBefore = beforeSnap.enemyHp;
        int enemyHpAfter  = after.enemyHp;
        boolean enemyWasGuarding = beforeSnap.enemyGuarding;
        boolean tookDamage = enemyHpAfter < enemyHpBefore;

        if (attackerIsP1) {
            if (enemyWasGuarding) { p2DefendPlaying = true; p2HurtPlaying = false; tDef2 = 0; anim2Defend.reset(); }
            else if (tookDamage)  { p2HurtPlaying = true;   p2DefendPlaying = false; tHurt2 = 0; anim2Hurt.reset(); }
        } else {
            if (enemyWasGuarding) { p1DefendPlaying = true; p1HurtPlaying = false; tDef1 = 0; anim1Defend.reset(); }
            else if (tookDamage)  { p1HurtPlaying = true;   p1DefendPlaying = false; tHurt1 = 0; anim1Hurt.reset(); }
        }

        attackTimer = 0;
        phase = Phase.IMPACT;
    }

    private void finishCinematic() {
        inputLocked = false;
        phase = Phase.NONE;
        pendingAction = null;
        beforeSnap = null;
        r1OffsetX = 0; r2OffsetX = 0;
    }

    /** Quem perdeu? (usa nomes padrão "Jogador 1"/"Jogador 2" usados nesta view) */
    private void startDeathIfNeeded(UiBattleEngine.Snapshot s) {
        if (p1DeadStarted || p2DeadStarted) return;
        boolean enemyLost = s.winner != null && s.winner.equals(s.currentName);
        String loserName = enemyLost ? s.enemyName : s.currentName;
        if ("Jogador 1".equals(loserName)) {
            p1DeadStarted = true; p1DeathDone = false; tDeath1 = 0; anim1Death.reset();
            p1Attacking = false; p1DefendPlaying = false; p1HurtPlaying = false;
        } else {
            p2DeadStarted = true; p2DeathDone = false; tDeath2 = 0; anim2Death.reset();
            p2Attacking = false; p2DefendPlaying = false; p2HurtPlaying = false;
        }
    }

    private void render() {
        gc.clearRect(0,0,BASE_W,BASE_H);
        drawBackgroundCover(arena);

        var snap = engine.snapshot();

        Image img1, img2;
        boolean p1Running = (phase == Phase.APPROACH || phase == Phase.RETURN) && attackerIsP1;
        boolean p2Running = (phase == Phase.APPROACH || phase == Phase.RETURN) && !attackerIsP1;

        // P1
        if (snap.finished && p1DeadStarted) {
            int idx = p1DeathDone ? (r1Death.columns() - 1) : anim1Death.currentIndex();
            img1 = r1Death.frame(idx);
        } else if (p1HurtPlaying) {
            img1 = r1Hurt.frame(anim1Hurt.currentIndex());
        } else if (p1DefendPlaying) {
            img1 = r1Defend.frame(anim1Defend.currentIndex());
        } else if (p1Running) {
            img1 = r1Run.frame(anim1Run.currentIndex());
        } else if (p1Attacking) {
            img1 = r1Atks[curAtk1].frame(a1Atks[curAtk1].currentIndex());
        } else {
            img1 = r1Idle.frame(anim1Idle.currentIndex());
        }

        // P2
        if (snap.finished && p2DeadStarted) {
            int idx = p2DeathDone ? (r2Death.columns() - 1) : anim2Death.currentIndex();
            img2 = r2Death.frame(idx);
        } else if (p2HurtPlaying) {
            img2 = r2Hurt.frame(anim2Hurt.currentIndex());
        } else if (p2DefendPlaying) {
            img2 = r2Defend.frame(anim2Defend.currentIndex());
        } else if (p2Running) {
            img2 = r2Run.frame(anim2Run.currentIndex());
        } else if (p2Attacking) {
            img2 = r2Atks[curAtk2].frame(a2Atks[curAtk2].currentIndex());
        } else {
            img2 = r2Idle.frame(anim2Idle.currentIndex());
        }

        // Tamanhos
        int drawW1 = (int)Math.round(img1.getWidth()  * SPRITE_SCALE);
        int drawH1 = (int)Math.round(img1.getHeight() * SPRITE_SCALE);
        int drawW2 = (int)Math.round(img2.getWidth()  * SPRITE_SCALE);
        int drawH2 = (int)Math.round(img2.getHeight() * SPRITE_SCALE);

        // Posições base
        int centerX = BASE_W / 2;
        int r1x = centerX - GAP - drawW1;
        int r2x = centerX + GAP;
        int r1y = R_Y - drawH1;
        int r2y = R_Y - drawH2;

        // Aplica deslocamentos de corrida (px)
        int x1 = (int)Math.round(r1x + r1OffsetX);
        int x2 = (int)Math.round(r2x + r2OffsetX);

        // Desenho
        drawSprite(img1, x1, r1y, drawW1, drawH1, false);
        drawSprite(img2, x2, r2y, drawW2, drawH2, true);

        drawHpBarsHD();

        if (snap.finished) {
            gc.setFill(Color.color(0, 0, 0, 0.55));
            gc.fillRect(0, 0, BASE_W, BASE_H);
            gc.setFill(Color.WHITE);
            String msg = "*** VENCEDOR: " + snap.winner + " ***";
            gc.fillText(msg, BASE_W / 2.0 - 120, 64);
        }
    }

    /** Um frame; se flipX=true, espelha (P2). */
    private void drawSprite(Image frame, int x, int y, int drawW, int drawH, boolean flipX) {
        if (!flipX) {
            gc.drawImage(frame, 0, 0, frame.getWidth(), frame.getHeight(), x, y, drawW, drawH);
        } else {
            gc.save();
            gc.translate(x + drawW, y);
            gc.scale(-1, 1);
            gc.drawImage(frame, 0, 0, frame.getWidth(), frame.getHeight(), 0, 0, drawW, drawH);
            gc.restore();
        }
    }

    private void drawHpBarsHD() {
        var s = engine.snapshot();

        int barW = 260, barH = 16;

        // bordas
        gc.setFill(Color.color(0,0,0,0.6));
        gc.fillRect(24, 24, barW+4, barH+4);
        gc.fillRect(BASE_W - barW - 28, 24, barW+4, barH+4);

        // preenchimentos (heurística por nomes padrão)
        int p1hp = s.currentName.equals("Jogador 1") ? s.currentHp : s.enemyHp;
        int p1max= s.currentName.equals("Jogador 1") ? s.currentMaxHp : s.enemyMaxHp;
        int p2hp = s.currentName.equals("Jogador 2") ? s.currentHp : s.enemyHp;
        int p2max= s.currentName.equals("Jogador 2") ? s.currentMaxHp : s.enemyMaxHp;

        double r1 = Math.max(0, (double)p1hp / Math.max(1, p1max));
        double r2 = Math.max(0, (double)p2hp / Math.max(1, p2max));
        gc.setFill(Color.web("#2aff2a"));
        gc.fillRect(26, 26, (int)(barW * r1), barH);
        gc.setFill(Color.web("#ff3b3b"));
        gc.fillRect(BASE_W - barW - 26, 26, (int)(barW * r2), barH);
    }

    private void perform(Action a) {
        var before = engine.snapshot();

        if (a == Action.DEFEND) {
            engine.perform(a);
            return;
        }

        // ATTACK/SPECIAL: começa IMEDIATAMENTE a corrida
        inputLocked = true;
        phase = Phase.APPROACH;
        pendingAction = a;
        beforeSnap = before;
        attackerIsP1 = "Jogador 1".equals(before.currentName);

        approachTarget = Math.max(0, 2.0 * GAP - HIT_OVERLAP);

        // resets
        if (attackerIsP1) { r1OffsetX = 0; anim1Run.reset(); }
        else              { r2OffsetX = 0; anim2Run.reset(); }

        p1Attacking = false; p2Attacking = false;
        curAtk1 = -1; curAtk2 = -1;
        tAtk1 = tAtk2 = 0;
    }

    private double fpsAtk1(int idx) { return atkFps1[Math.max(0, Math.min(idx, atkFps1.length-1))]; }
    private double fpsAtk2(int idx) { return atkFps2[Math.max(0, Math.min(idx, atkFps2.length-1))]; }

    private void drawBackgroundCover(Image img) {
        double sw = img.getWidth(), sh = img.getHeight();
        double scale = Math.max(BASE_W / sw, BASE_H / sh);
        double dw = sw * scale, dh = sh * scale;
        double dx = (BASE_W - dw) / 2.0, dy = (BASE_H - dh) / 2.0;
        gc.drawImage(img, 0, 0, sw, sh, dx, dy, dw, dh);
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
