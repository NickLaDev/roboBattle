package br.puc.battledolls.ui;

import br.puc.battledolls.campaign.CampaignManager;
import br.puc.battledolls.items.Armor;
import br.puc.battledolls.items.Weapon;
import br.puc.battledolls.model.Player;
import br.puc.battledolls.model.Robot;
import br.puc.battledolls.model.CharacterClass;
import br.puc.battledolls.model.AbilityEffect;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;

public class GameFX extends Application {

    // ===== Login (mesma base que já rodava) =====
    private static final double DESIGN_W = 1344.0;
    private static final double DESIGN_H = 768.0;
    private static final double SLOT1_X = 490, SLOT1_Y = 402, SLOT1_W = 305, SLOT1_H = 52;
    private static final double SLOT2_X = 490, SLOT2_Y = 472, SLOT2_W = 305, SLOT2_H = 52;
    private static final double ENTER_X = 531, ENTER_Y = 550, ENTER_W = 230, ENTER_H = 54;
    private static final int FIELD_FONT_SIZE = 22;

    // ===== Recursos =====
    private static final String INTRO_BG = "/assets/ui/intro.png";
    private static final String SELECTION_BG = "/assets/ui/selection.png";
    private static final String SHOP_BG = "/assets/ui/shop.png";
    private static final String SWORD_FMT = "/assets/items/swords/sword%d.png";
    private static final String SHIELD_FMT = "/assets/items/shields/shield%d.png";

    // ===== Jogo =====
    private static final int START_CREDITS = 1000;

    // Modo de jogo: PvP (Player vs Player) ou PvC (Player vs CPU)
    private enum GameMode {
        PVP, PVC
    }

    private GameMode gameMode = GameMode.PVP;

    private Stage stage;
    private Player p1, p2;
    private Purchase pur1, pur2;
    private UiBattleEngine engine;
    private CampaignManager campaignManager; // Gerencia a campanha PvC

    public static class Purchase {
        public final Robot robot;
        public final int totalCost;
        public final CharacterClass character;

        public Purchase(CharacterClass character, Robot robot, int totalCost) {
            this.character = character;
            this.robot = robot;
            this.totalCost = totalCost;
        }
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("RoboBattle (FX)");
        showModeSelectionScreen();
        stage.show();
    }

    // =========================================================
    // 1) TELA DE SELEÇÃO DE MODO (nova tela com selection.png)
    // =========================================================
    private void showModeSelectionScreen() {
        Image bg = load(SELECTION_BG);
        final double IMG_W = bg.getWidth();
        final double IMG_H = bg.getHeight();

        Pane board = new Pane();
        board.setPrefSize(IMG_W, IMG_H);
        board.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        board.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        board.setBackground(new Background(new BackgroundImage(
                bg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, new BackgroundSize(IMG_W, IMG_H, false, false, false, false))));

        // ===== CONFIGURAÇÃO DE POSIÇÃO DOS BOTÕES =====
        // Ajuste estes valores para alinhar os botões com sua imagem

        // Dimensões dos botões
        final double BUTTON_WIDTH = 400;
        final double BUTTON_HEIGHT = 60;
        final double BUTTON_SPACING = 20; // Espaço entre os botões

        // Posição do botão PvP (Player vs Player)
        final double PVP_BUTTON_X = 520; // ← AJUSTE AQUI: posição X do botão PvP
        final double PVP_BUTTON_Y = 560; // ← AJUSTE AQUI: posição Y do botão PvP

        // Posição do botão PvC (Player vs CPU)
        final double PVC_BUTTON_X = 520; // ← AJUSTE AQUI: posição X do botão PvC
        final double PVC_BUTTON_Y = 480; // ← AJUSTE AQUI: posição Y do botão PvC

        // Posição do título
        final double TITLE_X = 460; // ← AJUSTE AQUI: posição X do título
        final double TITLE_Y = 350; // ← AJUSTE AQUI: posição Y do título

        // ===============================================

        // Título
        Label title = new Label("SELECIONE O MODO DE JOGO");
        title.setTextFill(Color.web("#BB86FC"));
        title.setStyle("""
                    -fx-font-family: "Georgia", "Times New Roman", serif;
                    -fx-font-size: 32px;
                    -fx-font-weight: bold;
                """);
        DropShadow titleGlow = new DropShadow(30, Color.web("#BB86FC"));
        titleGlow.setSpread(0.3);
        title.setEffect(titleGlow);
        title.setLayoutX(TITLE_X);
        title.setLayoutY(TITLE_Y);

        // Botão Player vs Player
        Button btnPvP = createModeButton("PLAYER vs PLAYER", "#60a5fa");
        btnPvP.setPrefWidth(BUTTON_WIDTH);
        btnPvP.setPrefHeight(BUTTON_HEIGHT);
        btnPvP.setLayoutX(PVP_BUTTON_X);
        btnPvP.setLayoutY(PVP_BUTTON_Y);
        btnPvP.setOnAction(e -> {
            gameMode = GameMode.PVP;
            showNameScreen();
        });

        // Botão Player vs CPU
        Button btnPvC = createModeButton("PLAYER vs CPU", "#fb7185");
        btnPvC.setPrefWidth(BUTTON_WIDTH);
        btnPvC.setPrefHeight(BUTTON_HEIGHT);
        btnPvC.setLayoutX(PVC_BUTTON_X);
        btnPvC.setLayoutY(PVC_BUTTON_Y);
        btnPvC.setOnAction(e -> {
            gameMode = GameMode.PVC;
            showNameScreen();
        });

        board.getChildren().addAll(title, btnPvP, btnPvC);

        StackPane root = new StackPane(board);
        root.setStyle("-fx-background-color: black;");
        Scene scene = new Scene(root, 1280, 720);

        Runnable rescale = () -> {
            double s = Math.min(scene.getWidth() / IMG_W, scene.getHeight() / IMG_H);
            board.setScaleX(s);
            board.setScaleY(s);
        };
        scene.widthProperty().addListener((o, a, b) -> rescale.run());
        scene.heightProperty().addListener((o, a, b) -> rescale.run());
        rescale.run();

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE)
                stage.close();
        });

        stage.setScene(scene);
        stage.centerOnScreen();
    }

    /**
     * Cria um botão estilizado no tema gótico/neon purple.
     */
    private Button createModeButton(String text, String accentColor) {
        Button btn = new Button(text);
        btn.setTextFill(Color.web("#F5F3FF"));
        btn.setStyle(String.format("""
                    -fx-font-family: "Georgia", "Times New Roman", serif;
                    -fx-font-size: 20px;
                    -fx-font-weight: bold;
                    -fx-background-color: linear-gradient(rgba(35,22,45,0.85), rgba(25,16,32,0.85));
                    -fx-background-radius: 12;
                    -fx-border-color: %s;
                    -fx-border-width: 2;
                    -fx-border-radius: 12;
                    -fx-padding: 16 32 16 32;
                    -fx-cursor: hand;
                """, accentColor));

        DropShadow glow = new DropShadow(25, Color.web(accentColor));
        glow.setSpread(0.25);

        btn.setOnMouseEntered(e -> {
            btn.setEffect(glow);
            btn.setScaleX(1.05);
            btn.setScaleY(1.05);
            btn.setStyle(String.format("""
                        -fx-font-family: "Georgia", "Times New Roman", serif;
                        -fx-font-size: 20px;
                        -fx-font-weight: bold;
                        -fx-background-color: linear-gradient(rgba(45,30,55,0.95), rgba(35,24,42,0.95));
                        -fx-background-radius: 12;
                        -fx-border-color: %s;
                        -fx-border-width: 3;
                        -fx-border-radius: 12;
                        -fx-padding: 16 32 16 32;
                        -fx-cursor: hand;
                    """, accentColor));
        });

        btn.setOnMouseExited(e -> {
            btn.setEffect(null);
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
            btn.setStyle(String.format("""
                        -fx-font-family: "Georgia", "Times New Roman", serif;
                        -fx-font-size: 20px;
                        -fx-font-weight: bold;
                        -fx-background-color: linear-gradient(rgba(35,22,45,0.85), rgba(25,16,32,0.85));
                        -fx-background-radius: 12;
                        -fx-border-color: %s;
                        -fx-border-width: 2;
                        -fx-border-radius: 12;
                        -fx-padding: 16 32 16 32;
                        -fx-cursor: hand;
                    """, accentColor));
        });

        btn.setOnMousePressed(e -> {
            btn.setScaleX(0.98);
            btn.setScaleY(0.98);
        });

        btn.setOnMouseReleased(e -> {
            btn.setScaleX(1.05);
            btn.setScaleY(1.05);
        });

        return btn;
    }

    // =========================================================
    // 2) TELA DE NOMES (removida seleção de modo daqui)
    // =========================================================
    private void showNameScreen() {
        Image bg = load(INTRO_BG);
        final double IMG_W = bg.getWidth();
        final double IMG_H = bg.getHeight();

        Pane board = new Pane();
        board.setPrefSize(IMG_W, IMG_H);
        board.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        board.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        board.setBackground(new Background(new BackgroundImage(
                bg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, new BackgroundSize(IMG_W, IMG_H, false, false, false, false))));

        TextField tf1 = buildLoginField();
        TextField tf2 = buildLoginField();
        placePx(tf1, SLOT1_X, SLOT1_Y, SLOT1_W, SLOT1_H, IMG_W, IMG_H);
        placePx(tf2, SLOT2_X, SLOT2_Y, SLOT2_W, SLOT2_H, IMG_W, IMG_H);

        // No modo PvC, desabilita o campo do jogador 2
        if (gameMode == GameMode.PVC) {
            tf2.setDisable(true);
            tf2.setOpacity(0.5);
            tf2.setText("CPU");
        }

        Button enterBtn = new Button();
        enterBtn.setOpacity(0.0);
        enterBtn.setBackground(Background.EMPTY);
        enterBtn.setBorder(Border.EMPTY);
        placePx(enterBtn, ENTER_X, ENTER_Y, ENTER_W, ENTER_H, IMG_W, IMG_H);

        board.getChildren().addAll(tf1, tf2, enterBtn);

        StackPane root = new StackPane(board);
        root.setStyle("-fx-background-color: black;");
        Scene scene = new Scene(root, 1280, 720);

        Runnable rescale = () -> {
            double s = Math.min(scene.getWidth() / IMG_W, scene.getHeight() / IMG_H);
            board.setScaleX(s);
            board.setScaleY(s);
        };
        scene.widthProperty().addListener((o, a, b) -> rescale.run());
        scene.heightProperty().addListener((o, a, b) -> rescale.run());
        rescale.run();

        Runnable submit = () -> {
            String n1 = (tf1.getText() == null || tf1.getText().isBlank()) ? "Jogador 1" : tf1.getText().trim();
            String n2 = (gameMode == GameMode.PVC) ? "CPU"
                    : ((tf2.getText() == null || tf2.getText().isBlank()) ? "Jogador 2" : tf2.getText().trim());
            p1 = new Player(n1, START_CREDITS);
            p2 = new Player(n2, START_CREDITS);
            showShopScreen(p1, true);
        };
        enterBtn.setOnAction(e -> submit.run());
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)
                submit.run();
            if (e.getCode() == KeyCode.ESCAPE)
                stage.close();
        });

        tf1.requestFocus();
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    private TextField buildLoginField() {
        TextField tf = new TextField();
        tf.setBackground(Background.EMPTY);
        tf.setBorder(Border.EMPTY);
        tf.setAlignment(Pos.CENTER_LEFT);
        tf.setPadding(new Insets(0, 10, 0, 10));
        tf.setFont(Font.font(FIELD_FONT_SIZE));
        tf.setStyle("""
                    -fx-background-color: transparent;
                    -fx-background-insets: 0;
                    -fx-background-radius: 0;
                    -fx-border-color: transparent;
                    -fx-text-fill: #EDE9FE;
                    -fx-prompt-text-fill: rgba(229,231,235,0.75);n
                    -fx-caret-color: #BB86FC;
                    -fx-highlight-fill: rgba(187,134,252,0.35);
                    -fx-highlight-text-fill: white;
                """);
        return tf;
    }

    private static void placePx(Region node, double x, double y, double w, double h, double imgW, double imgH) {
        double sx = imgW / DESIGN_W;
        double sy = imgH / DESIGN_H;
        node.setLayoutX(x * sx);
        node.setLayoutY(y * sy);
        node.setPrefSize(w * sx, h * sy);
    }

    // =========================================================
    // 2) LOJA (ancrada no fundo + responsiva + UI polida)
    // =========================================================
    private void showShopScreen(Player player, boolean isFirstPlayer) {
        showShopScreen(player, isFirstPlayer, false);
    }

    private void showShopScreen(Player player, boolean isFirstPlayer, boolean isUpgrade) {
        final int credits = player.credits();

        // seleção
        ObjectProperty<CharacterClass> characterPick = new SimpleObjectProperty<>(CharacterClass.BEATRIZ);
        // Se for upgrade, inicializa com os níveis atuais do jogador
        int currentWeaponLvl = 0;
        int currentArmorLvl = 0;
        if (isUpgrade && player.robot() != null) {
            // Estima os níveis baseado nas stats (aproximação)
            var stats = player.robot().stats();
            // Arma: ATK base é 10, cada nível adiciona 6, então (atk - 10) / 6
            currentWeaponLvl = Math.max(0, Math.min(5, (stats.atk - 10) / 6));
            // Armadura: DEF base é 5, cada nível adiciona 4, então (def - 5) / 4
            currentArmorLvl = Math.max(0, Math.min(5, (stats.def - 5) / 4));
            characterPick.set(player.robot().characterClass());
        }
        IntegerProperty weaponLvl = new SimpleIntegerProperty(currentWeaponLvl);
        IntegerProperty armorLvl = new SimpleIntegerProperty(currentArmorLvl);

        // custo real pelas classes
        IntUnaryOperator weaponCost = lvl -> (lvl <= 0) ? 0 : new Weapon("N" + lvl, lvl).getCost();
        IntUnaryOperator armorCost = lvl -> (lvl <= 0) ? 0 : new Armor("N" + lvl, lvl).getCost();

        // ====== fundo ======
        Image bgImg = load(SHOP_BG);
        final double IMG_W = bgImg.getWidth();
        final double IMG_H = bgImg.getHeight();

        Pane board = new Pane();
        board.setPrefSize(IMG_W, IMG_H);
        board.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        board.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        board.setBackground(new Background(new BackgroundImage(
                bgImg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, new BackgroundSize(IMG_W, IMG_H, false, false, false, false))));

        // ====== seleção de personagem ======
        // Se for upgrade, usa o personagem atual do jogador
        if (isUpgrade && player.robot() != null) {
            characterPick.set(player.robot().characterClass());
        }
        VBox characterCard = buildCharacterSelector(characterPick);
        // Se for upgrade, desabilita a seleção de personagem
        if (isUpgrade) {
            characterCard.setDisable(true);
            characterCard.setOpacity(0.7);
        }

        // ====== strips (com preço no tile) ======
        StripControl swords = itemStrip(
                "ESPADAS", SWORD_FMT, "Sem arma", weaponLvl,
                () -> credits - armorCost.applyAsInt(armorLvl.get()),
                weaponCost);
        StripControl shields = itemStrip(
                "ESCUDOS", SHIELD_FMT, "Sem escudo", armorLvl,
                () -> credits - weaponCost.applyAsInt(weaponLvl.get()),
                armorCost);
        // cross-refresh (quando um muda, recalcula acessibilidade do outro)
        weaponLvl.addListener((o, a, b) -> shields.refresh.run());
        armorLvl.addListener((o, a, b) -> swords.refresh.run());

        // ====== prévias ======
        ImageView swordPreview = bigPreview();
        ImageView shieldPreview = bigPreview();
        bindPreview(weaponLvl, swordPreview, SWORD_FMT);
        bindPreview(armorLvl, shieldPreview, SHIELD_FMT);

        Label abilityName = new Label();
        abilityName.setTextFill(Color.web("#EDE9FE"));
        abilityName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        abilityName.setWrapText(true);
        abilityName.setMaxWidth(180);

        Label abilityDesc = new Label();
        abilityDesc.setTextFill(Color.web("#EDE9FE"));
        abilityDesc.setWrapText(true);
        abilityDesc.setMaxWidth(180);
        abilityDesc.setStyle("-fx-font-size: 12px;");

        VBox abilityBox = new VBox(4, abilityName, abilityDesc);
        abilityBox.setAlignment(Pos.TOP_LEFT);
        abilityBox.setPrefWidth(180);
        VBox abilityCard = translucentCard("Habilidade especial", abilityBox);
        abilityCard.setPrefWidth(200);
        abilityCard.setMinWidth(200);

        VBox swordCard = translucentCard("Arma escolhida", swordPreview);
        swordCard.setPrefWidth(140);
        swordCard.setMinWidth(140);

        VBox shieldCard = translucentCard("Escudo escolhido", shieldPreview);
        shieldCard.setPrefWidth(150);
        shieldCard.setMinWidth(150);

        HBox previews = new HBox(20, swordCard, shieldCard, abilityCard);
        previews.setAlignment(Pos.CENTER);

        // ====== barra inferior ======
        Label lblCred = bold("Créditos: " + credits);
        lblCred.setMinWidth(120);
        lblCred.setTextFill(Color.web("#EDE9FE"));

        Label lblSubtotal = new Label("Subtotal: 0");
        lblSubtotal.setMinWidth(120);
        lblSubtotal.setTextFill(Color.web("#EDE9FE"));

        Label lblSaldo = new Label("Saldo pós-compra: " + credits);
        lblSaldo.setMinWidth(180);
        lblSaldo.setTextFill(Color.web("#EDE9FE"));

        Button btnPreview = primaryButton("Pré-visualizar");
        Button btnConfirm = primaryButton(isUpgrade ? "Confirmar Upgrade" : "Confirmar compra");

        // Adiciona label mostrando fase atual se for upgrade
        Label phaseLabel = null;
        if (isUpgrade && campaignManager != null) {
            var currentPhase = campaignManager.getCurrentPhase();
            if (currentPhase != null) {
                phaseLabel = new Label(String.format("Fase %d/%d - Próximo: %s",
                        campaignManager.getCurrentPhaseNumber(),
                        campaignManager.getTotalPhases(),
                        currentPhase.cpuCharacter().name()));
                phaseLabel.setTextFill(Color.web("#BB86FC"));
                phaseLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                phaseLabel.setMinWidth(250);
            }
        }

        Label lblAbility = new Label();
        lblAbility.setTextFill(Color.web("#EDE9FE"));
        lblAbility.setMinWidth(200);

        HBox bottom = new HBox(16, btnPreview, new Region(), lblCred, lblSubtotal, lblSaldo,
                phaseLabel != null ? phaseLabel : lblAbility, btnConfirm);
        HBox.setHgrow(bottom.getChildren().get(1), Priority.ALWAYS);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(10));
        bottom.setStyle("-fx-background-color: rgba(0,0,0,0.38); -fx-background-radius: 14;");

        VBox centerBox = new VBox(20, previews, bottom);
        centerBox.setAlignment(Pos.CENTER);

        // ====== adicionar no "board" e posicionar por porcentagem do fundo ======
        // Layout reorganizado:
        // Personagem no topo centro: x=30% y=3% w=40% (mais espaço)
        // Espadas esquerda: x=3% y=26% w=26% h=48%
        // Escudos direita: x=71% y=26% w=26% h=48%
        // Centro inferior (previews + bottom): x=20% y=76% w=60% h=20%
        board.getChildren().addAll(characterCard, swords.node, shields.node, centerBox);
        placePct(characterCard, 0.30, 0.03, 0.40, 0.20, IMG_W, IMG_H);
        placePct(swords.node, 0.03, 0.26, 0.26, 0.48, IMG_W, IMG_H);
        placePct(shields.node, 0.71, 0.26, 0.26, 0.48, IMG_W, IMG_H);
        placePct(centerBox, 0.20, 0.76, 0.60, 0.20, IMG_W, IMG_H);

        // ====== lógica do resumo ======
        Runnable refreshSummary = () -> {
            int sub = weaponCost.applyAsInt(weaponLvl.get()) + armorCost.applyAsInt(armorLvl.get());
            lblSubtotal.setText("Subtotal: " + sub);
            int saldo = credits - sub;
            lblSaldo.setText("Saldo pós-compra: " + saldo);
            lblSaldo.setTextFill(saldo < 0 ? Color.web("#F87171") : Color.web("#EDE9FE"));
            CharacterClass cc = characterPick.get();
            AbilityEffect ability = cc.ability();
            abilityName.setText(ability.name());
            abilityDesc.setText(ability.description());
            lblAbility.setText("Especial: " + ability.name());
        };
        weaponLvl.addListener((o, a, b) -> refreshSummary.run());
        armorLvl.addListener((o, a, b) -> refreshSummary.run());
        characterPick.addListener((o, a, b) -> refreshSummary.run());
        refreshSummary.run();

        btnPreview.setOnAction(e -> {
            Purchase p = buildPurchase(credits, characterPick.get(), weaponLvl.get(), armorLvl.get(), weaponCost,
                    armorCost);
            if (p == null) {
                alert("Créditos insuficientes para esta configuração.");
                return;
            }
            alert(String.format(
                    "Personagem: %s\nHabilidade: %s\nHP=%d  ATK=%d  DEF=%d  CRIT=%.1f%%  EVADE=%.1f%%  Cargas de especial=%d",
                    p.character.displayName(),
                    p.character.ability().name(),
                    p.robot.stats().maxHp, p.robot.stats().atk, p.robot.stats().def,
                    p.robot.stats().crit * 100, p.robot.stats().evade * 100,
                    p.robot.specialCharges()));
        });

        btnConfirm.setOnAction(e -> {
            Purchase p = buildPurchase(credits, characterPick.get(), weaponLvl.get(), armorLvl.get(), weaponCost,
                    armorCost);
            if (p == null) {
                alert("Créditos insuficientes para esta configuração.");
                return;
            }
            if (!player.buyAndEquip(p.robot, p.totalCost)) {
                alert("Falha ao equipar. Tente novamente.");
                return;
            }

            // Se for upgrade, vai para a próxima fase
            if (isUpgrade) {
                startCampaignPhase();
                return;
            }

            if (isFirstPlayer) {
                pur1 = p;
                // No modo PvC, inicia a campanha
                if (gameMode == GameMode.PVC) {
                    campaignManager = new CampaignManager(p1);
                    startCampaignPhase();
                } else {
                    showShopScreen(p2, false);
                }
            } else {
                pur2 = p;
                showBattleScreen();
            }
        });

        // ====== cena com escalonamento automático do tabuleiro ======
        StackPane root = new StackPane(board);
        root.setStyle("-fx-background-color: black;");
        Scene scene = new Scene(root, 1160, 700);

        Runnable rescale = () -> {
            double s = Math.min(scene.getWidth() / IMG_W, scene.getHeight() / IMG_H);
            board.setScaleX(s);
            board.setScaleY(s);
        };
        scene.widthProperty().addListener((o, a, b) -> rescale.run());
        scene.heightProperty().addListener((o, a, b) -> rescale.run());
        rescale.run();

        stage.setScene(scene);
    }

    // ---- helpers de posicionamento por porcentagem do fundo ----
    private static void placePct(Region node, double px, double py, double pw, double ph,
            double imgW, double imgH) {
        node.setLayoutX(px * imgW);
        node.setLayoutY(py * imgH);
        node.setPrefSize(pw * imgW, ph * imgH);
        node.setMaxSize(pw * imgW, ph * imgH);
        node.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    // =========================================================
    // 3) CAMPANHA PvC
    // =========================================================
    /**
     * Inicia uma fase da campanha.
     */
    private void startCampaignPhase() {
        if (campaignManager == null || campaignManager.isCampaignComplete()) {
            // Campanha completa - mostrar tela de vitória
            showCampaignCompleteScreen();
            return;
        }

        // Cria o jogador CPU para esta fase
        p2 = campaignManager.createCPUPlayer();
        if (p2 == null) {
            showCampaignCompleteScreen();
            return;
        }

        // Mostra a batalha
        showBattleScreen(true);
    }

    /**
     * Chamado quando uma batalha da campanha termina.
     */
    public void onCampaignBattleFinished(boolean playerWon) {
        campaignManager.advanceToNextPhase(playerWon);

        if (!playerWon) {
            // Jogador perdeu - mostrar tela de game over
            showGameOverScreen();
        } else if (campaignManager.isCampaignComplete()) {
            // Campanha completa
            showCampaignCompleteScreen();
        } else {
            // Próxima fase - mostrar tela de upgrade
            showUpgradeScreen();
        }
    }

    /**
     * Tela de upgrade entre fases.
     */
    private void showUpgradeScreen() {
        // Reutiliza a tela de loja, mas com título diferente e sem permitir trocar
        // personagem
        showShopScreen(p1, false, true);
    }

    /**
     * Tela de vitória da campanha.
     */
    private void showCampaignCompleteScreen() {
        Image bg = load(SELECTION_BG);
        final double IMG_W = bg.getWidth();
        final double IMG_H = bg.getHeight();

        Pane board = new Pane();
        board.setPrefSize(IMG_W, IMG_H);
        board.setBackground(new Background(new BackgroundImage(
                bg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, new BackgroundSize(IMG_W, IMG_H, false, false, false, false))));

        Label title = new Label("CAMPANHA COMPLETA!");
        title.setTextFill(Color.web("#BB86FC"));
        title.setStyle("""
                    -fx-font-family: "Georgia", "Times New Roman", serif;
                    -fx-font-size: 48px;
                    -fx-font-weight: bold;
                """);
        DropShadow titleGlow = new DropShadow(40, Color.web("#BB86FC"));
        titleGlow.setSpread(0.3);
        title.setEffect(titleGlow);
        title.setLayoutX((IMG_W - 600) / 2);
        title.setLayoutY(IMG_H * 0.3);

        Label creditsLabel = new Label("Créditos Totais Ganhos: " + campaignManager.getTotalCreditsEarned());
        creditsLabel.setTextFill(Color.web("#EDE9FE"));
        creditsLabel.setStyle("-fx-font-size: 24px;");
        creditsLabel.setLayoutX((IMG_W - 500) / 2);
        creditsLabel.setLayoutY(IMG_H * 0.5);

        Button btnMenu = createModeButton("VOLTAR AO MENU", "#60a5fa");
        btnMenu.setPrefWidth(300);
        btnMenu.setPrefHeight(50);
        btnMenu.setLayoutX((IMG_W - 300) / 2);
        btnMenu.setLayoutY(IMG_H * 0.7);
        btnMenu.setOnAction(e -> showModeSelectionScreen());

        board.getChildren().addAll(title, creditsLabel, btnMenu);

        StackPane root = new StackPane(board);
        root.setStyle("-fx-background-color: black;");
        Scene scene = new Scene(root, 1280, 720);
        stage.setScene(scene);
    }

    /**
     * Tela de game over.
     */
    private void showGameOverScreen() {
        Image bg = load(SELECTION_BG);
        final double IMG_W = bg.getWidth();
        final double IMG_H = bg.getHeight();

        Pane board = new Pane();
        board.setPrefSize(IMG_W, IMG_H);
        board.setBackground(new Background(new BackgroundImage(
                bg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, new BackgroundSize(IMG_W, IMG_H, false, false, false, false))));

        Label title = new Label("GAME OVER");
        title.setTextFill(Color.web("#F87171"));
        title.setStyle("""
                    -fx-font-family: "Georgia", "Times New Roman", serif;
                    -fx-font-size: 48px;
                    -fx-font-weight: bold;
                """);
        DropShadow titleGlow = new DropShadow(40, Color.web("#F87171"));
        titleGlow.setSpread(0.3);
        title.setEffect(titleGlow);
        title.setLayoutX((IMG_W - 400) / 2);
        title.setLayoutY(IMG_H * 0.4);

        Button btnMenu = createModeButton("VOLTAR AO MENU", "#60a5fa");
        btnMenu.setPrefWidth(300);
        btnMenu.setPrefHeight(50);
        btnMenu.setLayoutX((IMG_W - 300) / 2);
        btnMenu.setLayoutY(IMG_H * 0.6);
        btnMenu.setOnAction(e -> showModeSelectionScreen());

        board.getChildren().addAll(title, btnMenu);

        StackPane root = new StackPane(board);
        root.setStyle("-fx-background-color: black;");
        Scene scene = new Scene(root, 1280, 720);
        stage.setScene(scene);
    }

    // =========================================================
    // 4) BATALHA (PvP ou PvC)
    // =========================================================
    private void showBattleScreen() {
        showBattleScreen(false);
    }

    private void showBattleScreen(boolean isCampaign) {
        engine = new UiBattleEngine(p1, p2, gameMode == GameMode.PVC);
        // Se for campanha, passa o caminho dos sprites da CPU e a configuração de frames
        String cpuSpritePath = null;
        br.puc.battledolls.campaign.CPUCharacter.SpriteFrameConfig cpuFrameConfig = null;
        if (isCampaign && campaignManager != null) {
            var phase = campaignManager.getCurrentPhase();
            if (phase != null) {
                cpuSpritePath = phase.cpuCharacter().spritePath();
                cpuFrameConfig = phase.cpuCharacter().frameConfig();
            }
        }
        PixelBattleView pixelView = new PixelBattleView(engine, gameMode == GameMode.PVC,
                isCampaign ? this : null, cpuSpritePath, cpuFrameConfig);
        Scene battleScene = pixelView.buildScene();
        stage.setScene(battleScene);
    }

    // =========================================================
    // Helpers visuais / itens
    // =========================================================
    private Label bold(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private Button primaryButton(String text) {
        Button b = new Button(text);
        b.setMinWidth(170);
        b.setPrefWidth(Region.USE_COMPUTED_SIZE);
        b.setWrapText(true);
        b.setStyle("""
                    -fx-font-weight: bold;
                    -fx-text-fill: white;
                    -fx-background-color: linear-gradient(#6D28D9, #4C1D95);
                    -fx-background-radius: 12;
                    -fx-padding: 8 16 8 16;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 10, 0.3, 0, 2);
                    -fx-cursor: hand;
                    -fx-font-size: 13px;
                """);
        b.setOnMouseEntered(ev -> b.setStyle("""
                    -fx-font-weight: bold;
                    -fx-text-fill: white;
                    -fx-background-color: linear-gradient(#7C3AED, #5B21B6);
                    -fx-background-radius: 12;
                    -fx-padding: 8 16 8 16;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 12, 0.35, 0, 3);
                    -fx-cursor: hand;
                    -fx-font-size: 13px;
                """));
        b.setOnMouseExited(ev -> b.setStyle("""
                    -fx-font-weight: bold;
                    -fx-text-fill: white;
                    -fx-background-color: linear-gradient(#6D28D9, #4C1D95);
                    -fx-background-radius: 12;
                    -fx-padding: 8 16 8 16;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 10, 0.3, 0, 2);
                    -fx-cursor: hand;
                    -fx-font-size: 13px;
                """));
        return b;
    }

    private VBox translucentCard(String title, Node content) {
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: bold; -fx-text-fill: #EDE9FE; -fx-font-size: 13px;");
        t.setWrapText(true);
        t.setMaxWidth(Double.MAX_VALUE);
        VBox box = new VBox(8, t, content);
        box.setPadding(new Insets(12));
        box.setAlignment(Pos.TOP_CENTER);
        box.setStyle("""
                    -fx-background-color: rgba(0,0,0,0.38);
                    -fx-background-radius: 14;
                    -fx-text-fill: #EDE9FE;
                """);
        return box;
    }

    private VBox buildCharacterSelector(ObjectProperty<CharacterClass> selected) {
        // Título da seção
        Label titleLabel = new Label("ESCOLHA SEU PERSONAGEM");
        titleLabel.setTextFill(Color.web("#BB86FC"));
        titleLabel.setStyle("""
                    -fx-font-family: "Georgia", "Times New Roman", serif;
                    -fx-font-size: 24px;
                    -fx-font-weight: bold;
                    -fx-padding: 0 0 15 0;
                """);
        DropShadow titleGlow = new DropShadow(25, Color.web("#BB86FC"));
        titleGlow.setSpread(0.4);
        titleLabel.setEffect(titleGlow);

        // ComboBox estilizado
        ComboBox<CharacterClass> combo = new ComboBox<>();
        combo.getItems().addAll(CharacterClass.values());
        combo.setPrefWidth(400);
        combo.setStyle("""
                    -fx-background-color: linear-gradient(to bottom, rgba(88,28,135,0.6), rgba(55,16,85,0.6));
                    -fx-background-radius: 12;
                    -fx-border-color: #A78BFA;
                    -fx-border-width: 2;
                    -fx-border-radius: 12;
                    -fx-font-size: 16px;
                    -fx-text-fill: #EDE9FE;
                    -fx-prompt-text-fill: #EDE9FE;
                """);

        combo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(CharacterClass item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.displayName() + " — " + item.ability().name());
                    setStyle("""
                            -fx-background-color: rgba(30,30,40,0.95);
                            -fx-text-fill: #EDE9FE;
                            -fx-padding: 8 12 8 12;
                            -fx-font-size: 14px;
                        """);
                }
            }
        });

        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CharacterClass item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.displayName());
                    setStyle("-fx-text-fill: #EDE9FE; -fx-font-size: 16px; -fx-font-weight: bold;");
                }
            }
        });

        if (selected.get() == null && !combo.getItems().isEmpty()) {
            selected.set(combo.getItems().get(0));
        }
        combo.setValue(selected.get());

        // Imagem do personagem - MUITO MAIOR e com efeitos
        ImageView portrait = new ImageView();
        portrait.setFitWidth(320);
        portrait.setFitHeight(320);
        portrait.setPreserveRatio(true);

        // Efeito de brilho na imagem
        DropShadow portraitGlow = new DropShadow(35, Color.web("#A78BFA"));
        portraitGlow.setSpread(0.3);
        portrait.setEffect(portraitGlow);

        // Container para a imagem com borda brilhante
        StackPane portraitContainer = new StackPane(portrait);
        portraitContainer.setStyle("""
                    -fx-background-color: linear-gradient(to bottom right, rgba(167,139,250,0.15), rgba(88,28,135,0.15));
                    -fx-background-radius: 20;
                    -fx-border-color: linear-gradient(to bottom right, #BB86FC, #A78BFA);
                    -fx-border-width: 3;
                    -fx-border-radius: 20;
                    -fx-padding: 20;
                """);
        portraitContainer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // Nome da habilidade - destacado
        Label abilityLabel = new Label();
        abilityLabel.setTextFill(Color.web("#BB86FC"));
        abilityLabel.setAlignment(Pos.CENTER);
        abilityLabel.setStyle("""
                    -fx-font-size: 18px;
                    -fx-font-weight: bold;
                    -fx-padding: 10 0 5 0;
                """);

        // Descrição do personagem - melhorada
        Label desc = new Label();
        desc.setWrapText(true);
        desc.setTextFill(Color.web("#EDE9FE"));
        desc.setAlignment(Pos.CENTER);
        desc.setMaxWidth(Double.MAX_VALUE);
        desc.setPrefWidth(380);
        desc.setStyle("""
                    -fx-font-size: 13px;
                    -fx-line-spacing: 2;
                    -fx-padding: 0 15 0 15;
                """);

        Runnable refresh = () -> {
            CharacterClass cc = selected.get();
            if (cc == null)
                return;
            if (combo.getValue() != cc)
                combo.setValue(cc);
            abilityLabel.setText("Habilidade: " + cc.ability().name());
            desc.setText(cc.description());
            try {
                portrait.setImage(load(cc.portraitPath()));
            } catch (IllegalArgumentException e) {
                portrait.setImage(null);
            }
        };

        combo.valueProperty().addListener((obs, old, val) -> {
            if (val != null && val != selected.get())
                selected.set(val);
        });
        selected.addListener((obs, old, val) -> refresh.run());
        refresh.run();

        // Layout vertical melhorado
        VBox content = new VBox(15, titleLabel, combo, portraitContainer, abilityLabel, desc);
        content.setAlignment(Pos.TOP_CENTER);
        content.setFillWidth(true);
        content.setStyle("""
                    -fx-background-color: linear-gradient(to bottom, rgba(0,0,0,0.5), rgba(30,20,40,0.6));
                    -fx-background-radius: 20;
                    -fx-border-color: rgba(187,134,252,0.4);
                    -fx-border-width: 2;
                    -fx-border-radius: 20;
                    -fx-padding: 25;
                """);

        // Efeito de sombra externa no card inteiro
        DropShadow cardShadow = new DropShadow(40, Color.web("#000000"));
        cardShadow.setSpread(0.2);
        content.setEffect(cardShadow);

        VBox wrapper = new VBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        return wrapper;
    }

    private ImageView bigPreview() {
        ImageView iv = new ImageView();
        iv.setFitWidth(96);
        iv.setFitHeight(96);
        iv.setPreserveRatio(true);
        return iv;
    }

    private void bindPreview(IntegerProperty lvlProp, ImageView target, String fmt) {
        lvlProp.addListener((o, oldV, newV) -> {
            int lvl = newV == null ? 0 : newV.intValue();
            target.setImage(lvl <= 0 ? null : load(fmt.formatted(lvl)));
        });
    }

    private Image load(String path) {
        InputStream is = getClass().getResourceAsStream(path);
        if (is == null)
            throw new IllegalArgumentException("Recurso não encontrado: " + path +
                    " (confira src/main/resources e o nome do arquivo).");
        return new Image(is, 0, 0, false, false);
    }

    // ---- strip de itens com badge de preço e seleção estilizada ----
    private static class StripControl {
        final VBox node;
        final Runnable refresh;

        StripControl(VBox n, Runnable r) {
            node = n;
            refresh = r;
        }
    }

    private static class TileRef {
        final ToggleButton btn;
        final Label priceTag;
        final int level;
        final int price;

        TileRef(ToggleButton btn, Label priceTag, int level, int price) {
            this.btn = btn;
            this.priceTag = priceTag;
            this.level = level;
            this.price = price;
        }

        void applySelectedStyle(int selected) {
            String bg = (level == selected) ? "rgba(88,28,135,0.55)" : "rgba(20,20,28,0.55)";
            String bd = (level == selected) ? "#A78BFA" : "#4B5563";
            btn.setStyle("""
                        -fx-background-color: %s;
                        -fx-background-radius: 14;
                        -fx-border-color: %s;
                        -fx-border-width: 2;
                        -fx-border-radius: 14;
                        -fx-cursor: hand;
                    """.formatted(bg, bd));
        }

        void updateAffordability(int available) {
            boolean ok = price <= available;
            priceTag.setTextFill(ok ? Color.web("#A7F3D0") : Color.web("#FCA5A5"));
            priceTag.setStyle("""
                        -fx-font-size: 11px;
                        -fx-background-color: rgba(0,0,0,0.55);
                        -fx-background-radius: 9;
                        -fx-padding: 2 6 2 6;
                    """);
        }
    }

    private StripControl itemStrip(
            String title, String fmtPath, String noneLabel, IntegerProperty bindTo,
            IntSupplier availableCreditsSupplier, IntUnaryOperator costFunc) {

        VBox titleBox = new VBox();
        Label t = new Label(title);
        t.setStyle("-fx-text-fill: #EDE9FE; -fx-font-weight: bold; -fx-font-size: 14px;");
        titleBox.getChildren().add(t);

        FlowPane row = new FlowPane();
        row.setHgap(10);
        row.setVgap(10);
        row.setAlignment(Pos.TOP_LEFT);

        ToggleGroup group = new ToggleGroup();
        List<TileRef> tiles = new ArrayList<>();

        tiles.add(addTile(row, null, noneLabel, 0, group, costFunc.applyAsInt(0)));
        for (int lvl = 1; lvl <= 5; lvl++) {
            tiles.add(addTile(row, load(fmtPath.formatted(lvl)), "Nível " + lvl, lvl, group, costFunc.applyAsInt(lvl)));
        }

        group.selectedToggleProperty().addListener((obs, old, sel) -> {
            int lvl = (sel == null) ? 0 : (int) sel.getUserData();
            bindTo.set(lvl);
            tiles.forEach(tl -> tl.applySelectedStyle(lvl));
        });

        Runnable refreshAfford = () -> {
            int available = availableCreditsSupplier.getAsInt();
            tiles.forEach(tl -> tl.updateAffordability(available));
        };

        tiles.get(0).btn.setSelected(true);
        bindTo.set(0);
        refreshAfford.run();

        VBox node = new VBox(8, titleBox, row);
        node.setPadding(new Insets(12));
        node.setStyle("-fx-background-color: rgba(0,0,0,0.38); -fx-background-radius: 14;");
        return new StripControl(node, refreshAfford);
    }

    private TileRef addTile(FlowPane row, Image img, String tooltip, int level, ToggleGroup group, int price) {
        ImageView iv = new ImageView();
        iv.setPreserveRatio(true);
        iv.setFitWidth(72);
        iv.setFitHeight(72);
        if (img != null)
            iv.setImage(img);

        Label priceTag = new Label(price == 0 ? "Grátis" : (price + " cr"));
        priceTag.setTextFill(Color.web("#A7F3D0"));

        StackPane box = new StackPane(iv);
        StackPane.setAlignment(priceTag, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(priceTag, new Insets(0, 6, 6, 0));
        box.getChildren().add(priceTag);
        box.setPrefSize(84, 84);

        ToggleButton b = new ToggleButton();
        b.setGraphic(box);
        b.setPrefSize(96, 96);
        b.setFocusTraversable(false);
        Tooltip.install(b, new Tooltip(tooltip));
        b.setUserData(level);
        b.setToggleGroup(group);

        TileRef ref = new TileRef(b, priceTag, level, price);
        ref.applySelectedStyle(-1);

        row.getChildren().add(b);
        return ref;
    }

    private Purchase buildPurchase(int credits, CharacterClass character, int wLvl, int aLvl,
            IntUnaryOperator weaponCost, IntUnaryOperator armorCost) {
        int total = weaponCost.applyAsInt(wLvl) + armorCost.applyAsInt(aLvl);
        if (total > credits)
            return null;
        Weapon w = (wLvl <= 0) ? null : new Weapon("Arma N" + wLvl, wLvl);
        Armor a = (aLvl <= 0) ? null : new Armor("Armadura N" + aLvl, aLvl);
        Robot preview = new Robot(character, w, a, null); // sem módulo
        return new Purchase(character, preview, total);
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
