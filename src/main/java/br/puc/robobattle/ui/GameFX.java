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

    // ===== Config de layout da tela de nomes (igual à versão que rodava) =====
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

    // Carrinho da compra daquele jogador
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

    // ==========================
    // 1) Tela de nomes (mesma base)
    // ==========================
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
        enterBtn.setOpacity(0.0); // invisível sobre a arte
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

    private Label bold(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }



    private static void placePx(Region node, double x, double y, double w, double h, double imgW, double imgH) {
        double sx = imgW / DESIGN_W;
        double sy = imgH / DESIGN_H;
        node.setLayoutX(x * sx);
        node.setLayoutY(y * sy);
        node.setPrefSize(w * sx, h * sy);
    }

    // ==========================
    // 2) Loja (com background novo e sem tabelas)
    // ==========================
    private void showShopScreen(Player player, boolean isFirstPlayer) {
        final int credits = player.credits();

        // seleção
        IntegerProperty weaponLvl = new SimpleIntegerProperty(0);
        IntegerProperty armorLvl  = new SimpleIntegerProperty(0);

        // custo real pelas classes
        IntUnaryOperator weaponCost = lvl -> (lvl <= 0) ? 0 : new Weapon("N"+lvl, lvl).getCost();
        IntUnaryOperator armorCost  = lvl -> (lvl <= 0) ? 0 : new Armor("N"+lvl, lvl).getCost();

        // ===== fundo =====
        ImageView bg = new ImageView(load(SHOP_BG));
        bg.setPreserveRatio(true);
        bg.setSmooth(true);
        bg.setCache(true);

        // ===== overlays =====
        BorderPane overlay = new BorderPane();
        overlay.setPickOnBounds(false);
        overlay.setPadding(new Insets(16));

        // tiras de seleção (com badge de preço apenas)
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

        // quando um lado muda, o outro recalcula a acessibilidade (cores dos badges)
        weaponLvl.addListener((o,a,b) -> shields.refresh.run());
        armorLvl.addListener((o,a,b) -> swords.refresh.run());

        // prévias grandes
        ImageView swordPreview  = bigPreview();
        ImageView shieldPreview = bigPreview();
        bindPreview(weaponLvl, swordPreview, SWORD_FMT);
        bindPreview(armorLvl,  shieldPreview, SHIELD_FMT);

        HBox previews = new HBox(24,
                translucentCard("Arma escolhida", swordPreview),
                translucentCard("Escudo escolhido", shieldPreview)
        );
        previews.setAlignment(Pos.CENTER);

        // barra inferior
        Label lblCred = bold("Créditos: " + credits);
        Label lblSubtotal = new Label("Subtotal: 0");
        Label lblSaldo = new Label("Saldo pós-compra: " + credits);
        lblCred.setTextFill(Color.web("#EDE9FE"));
        lblSubtotal.setTextFill(Color.web("#EDE9FE"));
        lblSaldo.setTextFill(Color.web("#EDE9FE"));

        Button btnPreview = new Button("Pré-visualizar");
        Button btnConfirm = new Button("Confirmar compra");

        HBox bottom = new HBox(16, btnPreview, new Region(), lblCred, lblSubtotal, lblSaldo, btnConfirm);
        HBox.setHgrow(bottom.getChildren().get(1), Priority.ALWAYS);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(8));
        bottom.setStyle("-fx-background-color: rgba(0,0,0,0.35); -fx-background-radius: 10;");

        VBox center = new VBox(24, previews, bottom);
        center.setAlignment(Pos.BOTTOM_CENTER);
        center.setPadding(new Insets(0, 32, 24, 32));

        VBox left  = new VBox(8, header("ESPADAS"), swords.node);
        VBox right = new VBox(8, header("ESCUDOS"), shields.node);
        left.setAlignment(Pos.TOP_LEFT);
        right.setAlignment(Pos.TOP_RIGHT);

        overlay.setLeft(left);
        overlay.setRight(right);
        overlay.setCenter(center);
        BorderPane.setMargin(left, new Insets(40, 0, 0, 40));
        BorderPane.setMargin(right, new Insets(40, 40, 0, 0));

        // root com background
        StackPane root = new StackPane(bg, overlay);
        Scene scene = new Scene(root, 1160, 700);
        bg.setPreserveRatio(true);
        bg.setSmooth(true);

        scene.widthProperty().addListener((o,ov,nv) -> cover(bg, scene));
        scene.heightProperty().addListener((o,ov,nv) -> cover(bg, scene));
        cover(bg, scene);


        // resumo
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
            if (isFirstPlayer) { pur1 = p; showShopScreen(p2, false); }
            else { pur2 = p; showBattleScreen(); }
        });

        stage.setScene(scene);
    }

    // ===== Helpers visuais e de item =====
    private Label header(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #EDE9FE; -fx-font-size: 14px;");
        l.setPadding(new Insets(0,0,4,6));
        l.setBackground(new Background(new BackgroundFill(Color.rgb(0,0,0,0.35), new CornerRadii(8), Insets.EMPTY)));
        return l;
    }

    private VBox translucentCard(String title, Node content) {
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: bold; -fx-text-fill: #EDE9FE;");
        VBox box = new VBox(8, t, content);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: rgba(0,0,0,0.35); -fx-background-radius: 12; -fx-text-fill: #EDE9FE;");
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

    // strip de itens com badge de preço
    private static class StripControl { final VBox node; final Runnable refresh; StripControl(VBox n, Runnable r){node=n; refresh=r;} }
    private static class TileRef {
        final ToggleButton btn; final Label priceTag; final int level; final int price;
        TileRef(ToggleButton btn, Label priceTag, int level, int price){ this.btn=btn; this.priceTag=priceTag; this.level=level; this.price=price; }
        void applySelectedStyle(int selected){
            String bg = (level==selected) ? "#2A2144" : "#1E1E1E";
            String bd = (level==selected) ? "#BB86FC" : "#4A4A4A";
            btn.setStyle("""
                -fx-background-color: %s;
                -fx-background-radius: 12;
                -fx-border-color: %s;
                -fx-border-width: 2;
                -fx-border-radius: 12;
            """.formatted(bg, bd));
        }
        void updateAffordability(int available){
            boolean ok = price <= available;
            priceTag.setTextFill(ok ? Color.web("#A7F3D0") : Color.web("#FCA5A5"));
            priceTag.setStyle("-fx-font-size: 11px; -fx-background-color: rgba(20,20,20,0.75); -fx-background-radius: 8; -fx-padding: 2 6 2 6;" +
                    (ok ? "" : " -fx-background-color: rgba(255,80,80,0.15);"));
        }
    }

    private StripControl itemStrip(
            String title, String fmtPath, String noneLabel, IntegerProperty bindTo,
            IntSupplier availableCreditsSupplier, IntUnaryOperator costFunc) {

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

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

        VBox node = new VBox(6, row);
        node.setPadding(new Insets(8));
        node.setStyle("-fx-background-color: rgba(0,0,0,0.35); -fx-background-radius: 12;");
        return new StripControl(node, refreshAfford);
    }

    private TileRef addTile(HBox row, Image img, String tooltip, int level, ToggleGroup group, int price) {
        ImageView iv = new ImageView();
        iv.setPreserveRatio(true);
        iv.setFitWidth(64);
        iv.setFitHeight(64);
        if (img != null) iv.setImage(img);

        Label priceTag = new Label(price == 0 ? "Grátis" : (price + " cr"));
        priceTag.setTextFill(Color.web("#A7F3D0"));

        StackPane box = new StackPane(iv);
        StackPane.setAlignment(priceTag, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(priceTag, new Insets(0,4,4,0));
        box.getChildren().add(priceTag);
        box.setPrefSize(76, 76);

        ToggleButton b = new ToggleButton();
        b.setGraphic(box);
        b.setPrefSize(84, 84);
        b.setFocusTraversable(false);
        Tooltip.install(b, new Tooltip(tooltip));
        b.setUserData(level);
        b.setToggleGroup(group);

        TileRef ref = new TileRef(b, priceTag, level, price);
        ref.applySelectedStyle(-1);

        row.getChildren().add(b);
        return ref;
    }

    // ==========================
    // 3) Batalha
    // ==========================
    private void showBattleScreen() {
        engine = new UiBattleEngine(p1, p2);
        PixelBattleView pixelView = new PixelBattleView(engine);
        Scene battleScene = pixelView.buildScene();
        stage.setScene(battleScene);
    }

    private Purchase buildPurchase(int credits, int wLvl, int aLvl,
                                   IntUnaryOperator weaponCost, IntUnaryOperator armorCost) {
        int total = weaponCost.applyAsInt(wLvl) + armorCost.applyAsInt(aLvl);
        if (total > credits) return null;
        Weapon w = (wLvl <= 0) ? null : new Weapon("Arma N" + wLvl, wLvl);
        Armor  a = (aLvl <= 0) ? null : new Armor("Armadura N" + aLvl, aLvl);
        Robot preview = new Robot(w, a, null); // sem módulo por enquanto
        return new Purchase(preview, total);
    }

    private void alert(String msg) { new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait(); }

    public static void main(String[] args) { launch(args); }

    private void cover(ImageView bg, Scene scene) {
        var img = bg.getImage();
        if (img == null) return;

        double iw = img.getWidth();
        double ih = img.getHeight();
        double vw = scene.getWidth();
        double vh = scene.getHeight();

        double scale = Math.max(vw / iw, vh / ih); // cobre a tela

        double w = iw * scale;
        double h = ih * scale;

        bg.setFitWidth(w);
        bg.setFitHeight(h);

        // centraliza
        bg.setTranslateX((vw - w) / 2);
        bg.setTranslateY((vh - h) / 2);
    }

}
