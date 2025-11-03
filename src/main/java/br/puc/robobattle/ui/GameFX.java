package br.puc.robobattle.ui;

import br.puc.robobattle.items.Armor;
import br.puc.robobattle.items.Weapon;
import br.puc.robobattle.model.Player;
import br.puc.robobattle.model.Robot;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    private static final String SHOP_BG  = "/assets/ui/shop.png";
    private static final String SWORD_FMT  = "/assets/items/swords/sword%d.png";
    private static final String SHIELD_FMT = "/assets/items/shields/shield%d.png";

    // ===== Jogo =====
    private static final int START_CREDITS = 1000;

    private Stage stage;
    private Player p1, p2;
    private Purchase pur1, pur2;
    private UiBattleEngine engine;

    public static class Purchase {
        public final Robot robot;
        public final int totalCost;
        public Purchase(Robot robot, int totalCost) { this.robot = robot; this.totalCost = totalCost; }
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("RoboBattle (FX)");
        showNameScreen();
        stage.show();
    }

    // =========================================================
    // 1) TELA DE NOMES (igual à que estava funcionando)
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
            String n2 = (tf2.getText() == null || tf2.getText().isBlank()) ? "Jogador 2" : tf2.getText().trim();
            p1 = new Player(n1, START_CREDITS);
            p2 = new Player(n2, START_CREDITS);
            showShopScreen(p1, true);
        };
        enterBtn.setOnAction(e -> submit.run());
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) submit.run();
            if (e.getCode() == KeyCode.ESCAPE) stage.close();
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
            -fx-prompt-text-fill: rgba(229,231,235,0.75);
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
        final int credits = player.credits();

        // seleção
        IntegerProperty weaponLvl = new SimpleIntegerProperty(0);
        IntegerProperty armorLvl  = new SimpleIntegerProperty(0);

        // custo real pelas classes
        IntUnaryOperator weaponCost = lvl -> (lvl <= 0) ? 0 : new Weapon("N"+lvl, lvl).getCost();
        IntUnaryOperator armorCost  = lvl -> (lvl <= 0) ? 0 : new Armor("N"+lvl, lvl).getCost();

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

        // ====== strips (com preço no tile) ======
        StripControl swords = itemStrip(
                "ESPADAS", SWORD_FMT, "Sem arma", weaponLvl,
                () -> credits - armorCost.applyAsInt(armorLvl.get()),
                weaponCost
        );
        StripControl shields = itemStrip(
                "ESCUDOS", SHIELD_FMT, "Sem escudo", armorLvl,
                () -> credits - weaponCost.applyAsInt(weaponLvl.get()),
                armorCost
        );
        // cross-refresh (quando um muda, recalcula acessibilidade do outro)
        weaponLvl.addListener((o,a,b) -> shields.refresh.run());
        armorLvl.addListener((o,a,b) -> swords.refresh.run());

        // ====== prévias ======
        ImageView swordPreview  = bigPreview();
        ImageView shieldPreview = bigPreview();
        bindPreview(weaponLvl, swordPreview, SWORD_FMT);
        bindPreview(armorLvl,  shieldPreview, SHIELD_FMT);

        HBox previews = new HBox(24,
                translucentCard("Arma escolhida", swordPreview),
                translucentCard("Escudo escolhido", shieldPreview)
        );
        previews.setAlignment(Pos.CENTER);

        // ====== barra inferior ======
        Label lblCred = bold("Créditos: " + credits);
        Label lblSubtotal = new Label("Subtotal: 0");
        Label lblSaldo = new Label("Saldo pós-compra: " + credits);
        lblCred.setTextFill(Color.web("#EDE9FE"));
        lblSubtotal.setTextFill(Color.web("#EDE9FE"));
        lblSaldo.setTextFill(Color.web("#EDE9FE"));

        Button btnPreview = primaryButton("Pré-visualizar");
        Button btnConfirm = primaryButton("Confirmar compra");

        HBox bottom = new HBox(16, btnPreview, new Region(), lblCred, lblSubtotal, lblSaldo, btnConfirm);
        HBox.setHgrow(bottom.getChildren().get(1), Priority.ALWAYS);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(10));
        bottom.setStyle("-fx-background-color: rgba(0,0,0,0.38); -fx-background-radius: 14;");

        VBox centerBox = new VBox(20, previews, bottom);
        centerBox.setAlignment(Pos.CENTER);

        // ====== adicionar no "board" e posicionar por porcentagem do fundo ======
        // regiões pensadas para casar com os nichos da sua arte:
        // esquerda:  x=6.5%  y=18%  w=30%  h=56%
        // direita:   x=63.5% y=18%  w=30%  h=56%
        // centro-inf: x=25%  y=76%  w=50%  h=18%
        board.getChildren().addAll(swords.node, shields.node, centerBox);
        placePct(swords.node,  0.065, 0.18, 0.30, 0.56, IMG_W, IMG_H);
        placePct(shields.node, 0.635, 0.18, 0.30, 0.56, IMG_W, IMG_H);
        placePct(centerBox,    0.250, 0.76, 0.50, 0.18, IMG_W, IMG_H);

        // ====== lógica do resumo ======
        Runnable refreshSummary = () -> {
            int sub = weaponCost.applyAsInt(weaponLvl.get()) + armorCost.applyAsInt(armorLvl.get());
            lblSubtotal.setText("Subtotal: " + sub);
            int saldo = credits - sub;
            lblSaldo.setText("Saldo pós-compra: " + saldo);
            lblSaldo.setTextFill(saldo < 0 ? Color.web("#F87171") : Color.web("#EDE9FE"));
        };
        weaponLvl.addListener((o,a,b) -> refreshSummary.run());
        armorLvl.addListener((o,a,b) -> refreshSummary.run());
        refreshSummary.run();

        btnPreview.setOnAction(e -> {
            Purchase p = buildPurchase(credits, weaponLvl.get(), armorLvl.get(), weaponCost, armorCost);
            if (p == null) { alert("Créditos insuficientes para esta configuração."); return; }
            alert(String.format(
                    "Prévia:\nHP=%d  ATK=%d  DEF=%d  CRIT=%.1f%%  EVADE=%.1f%%",
                    p.robot.stats().maxHp, p.robot.stats().atk, p.robot.stats().def,
                    p.robot.stats().crit*100, p.robot.stats().evade*100
            ));
        });

        btnConfirm.setOnAction(e -> {
            Purchase p = buildPurchase(credits, weaponLvl.get(), armorLvl.get(), weaponCost, armorCost);
            if (p == null) { alert("Créditos insuficientes para esta configuração."); return; }
            if (!player.buyAndEquip(p.robot, p.totalCost)) { alert("Falha ao equipar. Tente novamente."); return; }
            if (isFirstPlayer) { pur1 = p; showShopScreen(p2, false); } else { pur2 = p; showBattleScreen(); }
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
        scene.widthProperty().addListener((o,a,b) -> rescale.run());
        scene.heightProperty().addListener((o,a,b) -> rescale.run());
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
    // 3) BATALHA
    // =========================================================
    private void showBattleScreen() {
        engine = new UiBattleEngine(p1, p2);
        PixelBattleView pixelView = new PixelBattleView(engine);
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
        b.setStyle("""
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-background-color: linear-gradient(#6D28D9, #4C1D95);
            -fx-background-radius: 12;
            -fx-padding: 8 14 8 14;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 10, 0.3, 0, 2);
            -fx-cursor: hand;
        """);
        b.setOnMouseEntered(ev -> b.setStyle("""
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-background-color: linear-gradient(#7C3AED, #5B21B6);
            -fx-background-radius: 12;
            -fx-padding: 8 14 8 14;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 12, 0.35, 0, 3);
            -fx-cursor: hand;
        """));
        b.setOnMouseExited(ev -> b.setStyle("""
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-background-color: linear-gradient(#6D28D9, #4C1D95);
            -fx-background-radius: 12;
            -fx-padding: 8 14 8 14;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 10, 0.3, 0, 2);
            -fx-cursor: hand;
        """));
        return b;
    }

    private VBox translucentCard(String title, Node content) {
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: bold; -fx-text-fill: #EDE9FE;");
        VBox box = new VBox(8, t, content);
        box.setPadding(new Insets(12));
        box.setStyle("""
            -fx-background-color: rgba(0,0,0,0.38);
            -fx-background-radius: 14;
            -fx-text-fill: #EDE9FE;
        """);
        return box;
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
        if (is == null) throw new IllegalArgumentException("Recurso não encontrado: " + path +
                " (confira src/main/resources e o nome do arquivo).");
        return new Image(is, 0, 0, false, false);
    }

    // ---- strip de itens com badge de preço e seleção estilizada ----
    private static class StripControl { final VBox node; final Runnable refresh; StripControl(VBox n, Runnable r){node=n; refresh=r;} }
    private static class TileRef {
        final ToggleButton btn; final Label priceTag; final int level; final int price;
        TileRef(ToggleButton btn, Label priceTag, int level, int price){ this.btn=btn; this.priceTag=priceTag; this.level=level; this.price=price; }
        void applySelectedStyle(int selected){
            String bg = (level==selected) ? "rgba(88,28,135,0.55)" : "rgba(20,20,28,0.55)";
            String bd = (level==selected) ? "#A78BFA" : "#4B5563";
            btn.setStyle("""
                -fx-background-color: %s;
                -fx-background-radius: 14;
                -fx-border-color: %s;
                -fx-border-width: 2;
                -fx-border-radius: 14;
                -fx-cursor: hand;
            """.formatted(bg, bd));
        }
        void updateAffordability(int available){
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
        if (img != null) iv.setImage(img);

        Label priceTag = new Label(price == 0 ? "Grátis" : (price + " cr"));
        priceTag.setTextFill(Color.web("#A7F3D0"));

        StackPane box = new StackPane(iv);
        StackPane.setAlignment(priceTag, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(priceTag, new Insets(0,6,6,0));
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

    private Purchase buildPurchase(int credits, int wLvl, int aLvl,
                                   IntUnaryOperator weaponCost, IntUnaryOperator armorCost) {
        int total = weaponCost.applyAsInt(wLvl) + armorCost.applyAsInt(aLvl);
        if (total > credits) return null;
        Weapon w = (wLvl <= 0) ? null : new Weapon("Arma N" + wLvl, wLvl);
        Armor  a = (aLvl <= 0) ? null : new Armor("Armadura N" + aLvl, aLvl);
        Robot preview = new Robot(w, a, null); // sem módulo
        return new Purchase(preview, total);
    }

    private void alert(String msg) { new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait(); }

    public static void main(String[] args) { launch(args); }
}
