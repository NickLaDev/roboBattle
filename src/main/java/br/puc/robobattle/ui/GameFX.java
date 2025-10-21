package br.puc.robobattle.ui;

import br.puc.robobattle.items.Armor;
import br.puc.robobattle.items.Module;
import br.puc.robobattle.items.Weapon;
import br.puc.robobattle.model.Player;
import br.puc.robobattle.model.Robot;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GameFX extends Application {

    // ====== CONFIG GERAL ======
    private static final int START_CREDITS = 1000;           // créditos iniciais
    private static int costLinear(int lvl) { return 100 * lvl; } // 0,100,200,300,400,500

    // ====== ESTADO DA APP ======
    private Stage stage;
    private Player p1, p2;
    private Purchase pur1, pur2; // compras confirmadas
    private UiBattleEngine engine;

    // ====== DTO de compra ======
    public static class Purchase {
        public final Robot robot;
        public final int totalCost;
        public Purchase(Robot robot, int totalCost) {
            this.robot = robot;
            this.totalCost = totalCost;
        }
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("RoboBattle (FX)");
        showNameScreen();
        stage.show();
    }

    // ==========================
    // 1) TELA DE NOMES
    // ==========================
    private void showNameScreen() {
        TextField tf1 = new TextField(); tf1.setPromptText("Nome do Jogador 1");
        TextField tf2 = new TextField(); tf2.setPromptText("Nome do Jogador 2");
        Button avancar = new Button("Avançar para a Loja");

        VBox box = new VBox(12,
                title("Configurar jogadores"),
                new Label("Informe os nomes:"),
                tf1, tf2,
                avancar);
        box.setPadding(new Insets(16));
        box.setAlignment(Pos.CENTER_LEFT);

        avancar.setOnAction(e -> {
            String n1 = tf1.getText().trim().isEmpty() ? "Jogador 1" : tf1.getText().trim();
            String n2 = tf2.getText().trim().isEmpty() ? "Jogador 2" : tf2.getText().trim();
            p1 = new Player(n1, START_CREDITS);
            p2 = new Player(n2, START_CREDITS);
            showShopScreen(p1, true);
        });

        stage.setScene(new Scene(box, 980, 640));
    }

    // ==========================
    // 2) TELA DA LOJA (por jogador)
    // ==========================
    private void showShopScreen(Player player, boolean isFirstPlayer) {
        final int credits = player.credits();

        // seleção
        ComboBox<Integer> cbW = comboLevels(); // arma
        ComboBox<Integer> cbA = comboLevels(); // armadura
        ComboBox<Integer> cbM = comboLevels(); // módulo
        ComboBox<Module.Type> cbT = new ComboBox<>();
        cbT.getItems().addAll(Module.Type.values());
        cbT.getSelectionModel().selectFirst();

        // tabelas de preço
        TextArea tabW = priceTable("ARMA", credits, 0);
        TextArea tabA = priceTable("ARMADURA", credits, 0);
        TextArea tabM = priceTable("MÓDULO", credits, 0);
        tabW.setPrefRowCount(8); tabA.setPrefRowCount(8); tabM.setPrefRowCount(8);

        // resumo dinâmico
        Label lblCred = new Label("Créditos: " + credits);
        Label lblSubtotal = new Label("Subtotal: 0");
        Label lblSaldo = new Label("Saldo pós-compra: " + credits);
        Label lblStats = new Label("Pré-visualização: —");
        Button btnPreview = new Button("Pré-visualizar");
        Button btnConfirm = new Button("Confirmar compra");

        // atualiza tabelas com base no subtotal (mostra saldo restante se comprar tal nível)
        Runnable refreshTables = () -> {
            int weaponCost = costLinear(get(cbW));
            int armorCost  = costLinear(get(cbA));
            int moduleCost = costLinear(get(cbM));
            int subtotal = weaponCost + armorCost + moduleCost;

            tabW.setText(buildPriceTable("ARMA", credits, 0));
            tabA.setText(buildPriceTable("ARMADURA", credits, weaponCost));
            tabM.setText(buildPriceTable("MÓDULO", credits, weaponCost + armorCost));

            lblSubtotal.setText("Subtotal: " + subtotal);
            lblSaldo.setText("Saldo pós-compra: " + (credits - subtotal));
        };

        cbW.setOnAction(e -> refreshTables.run());
        cbA.setOnAction(e -> refreshTables.run());
        cbM.setOnAction(e -> refreshTables.run());

        btnPreview.setOnAction(e -> {
            int wLvl = get(cbW), aLvl = get(cbA), mLvl = get(cbM);
            Module.Type type = cbT.getValue();
            Purchase p = buildPurchase(credits, wLvl, aLvl, mLvl, type);
            if (p == null) {
                alert("Créditos insuficientes para esta configuração.");
                return;
            }
            lblStats.setText(String.format(
                    "Prévia → HP=%d  ATK=%d  DEF=%d  CRIT=%.1f%%  EVADE=%.1f%%  SPECIAL=%s",
                    p.robot.stats().maxHp, p.robot.stats().atk, p.robot.stats().def,
                    p.robot.stats().crit * 100, p.robot.stats().evade * 100,
                    p.robot.isSpecialAvailable() ? "SIM (BATERIA)" : "NÃO"
            ));
        });

        btnConfirm.setOnAction(e -> {
            int wLvl = get(cbW), aLvl = get(cbA), mLvl = get(cbM);
            Module.Type type = cbT.getValue();
            Purchase p = buildPurchase(credits, wLvl, aLvl, mLvl, type);
            if (p == null) {
                alert("Créditos insuficientes para esta configuração.");
                return;
            }
            if (!player.buyAndEquip(p.robot, p.totalCost)) {
                alert("Falha ao equipar. Tente novamente.");
                return;
            }
            if (isFirstPlayer) {
                pur1 = p;
                showShopScreen(p2, false);  // passa para o jogador 2
            } else {
                pur2 = p;
                showBattleScreen();         // inicia a batalha em Pixel Art
            }
        });

        GridPane gridSel = new GridPane();
        gridSel.setHgap(12); gridSel.setVgap(8);
        gridSel.add(new Label("Arma (nível)"), 0, 0); gridSel.add(cbW, 1, 0);
        gridSel.add(new Label("Armadura (nível)"), 0, 1); gridSel.add(cbA, 1, 1);
        gridSel.add(new Label("Módulo (nível)"), 0, 2); gridSel.add(cbM, 1, 2);
        gridSel.add(new Label("Tipo de Módulo"), 0, 3); gridSel.add(cbT, 1, 3);
        gridSel.add(new Label(" "), 0, 4); gridSel.add(btnPreview, 1, 4);
        gridSel.add(lblStats, 0, 5, 2, 1);

        HBox priceTables = new HBox(12,
                card("Tabela ARMA", tabW),
                card("Tabela ARMADURA", tabA),
                card("Tabela MÓDULO", tabM));
        priceTables.setAlignment(Pos.CENTER_LEFT);

        HBox bottom = new HBox(16, lblCred, lblSubtotal, lblSaldo, btnConfirm);
        bottom.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(12,
                title("Loja de " + player.name() + " — Créditos: " + credits),
                priceTables,
                card("Seleção", gridSel),
                bottom
        );
        root.setPadding(new Insets(16));

        // estado inicial
        refreshTables.run();

        stage.setScene(new Scene(root, 980, 640));
    }

    private ComboBox<Integer> comboLevels() {
        ComboBox<Integer> cb = new ComboBox<>();
        cb.getItems().addAll(0,1,2,3,4,5);
        cb.getSelectionModel().select(0);
        cb.setPrefWidth(90);
        return cb;
    }

    private TextArea priceTable(String label, int credits, int subtotal) {
        TextArea ta = new TextArea(buildPriceTable(label, credits, subtotal));
        ta.setEditable(false); ta.setPrefColumnCount(18);
        return ta;
    }

    private String buildPriceTable(String label, int credits, int subtotal) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s\nNível | Preço | Saldo se comprar\n", label));
        sb.append("-------------------------------\n");
        for (int lvl = 0; lvl <= 5; lvl++) {
            int cost = costLinear(lvl);
            int remaining = credits - (subtotal + cost);
            sb.append(String.format("%5d | %5d | %d\n", lvl, cost, remaining));
        }
        return sb.toString();
    }

    private int get(ComboBox<Integer> cb) { return cb.getValue() == null ? 0 : cb.getValue(); }

    private Purchase buildPurchase(int credits, int wLvl, int aLvl, int mLvl, Module.Type mType) {
        int weaponCost = costLinear(wLvl);
        int armorCost  = costLinear(aLvl);
        int moduleCost = costLinear(mLvl);
        int total = weaponCost + armorCost + moduleCost;
        if (total > credits) return null;

        Weapon w = new Weapon("Arma N" + wLvl, wLvl);
        Armor  a = new Armor("Armadura N" + aLvl, aLvl);
        Module m = new Module(mType, mLvl);
        Robot preview = new Robot(w, a, m);
        return new Purchase(preview, total);
    }

    private VBox card(String title, javafx.scene.Node content) {
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: bold;");
        VBox box = new VBox(8, t, content);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #222; -fx-text-fill: white; -fx-background-radius: 12;");
        if (content instanceof Labeled l) l.setStyle("-fx-text-fill: #ddd;");
        return box;
    }

    private Label title(String t) {
        Label l = new Label(t);
        l.setFont(Font.font(18));
        return l;
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    // ==========================
    // 3) TELA DA BATALHA (PIXEL ART)
    // ==========================
    private void showBattleScreen() {
        // Robôs já equipados na loja; só inicializa o motor
        engine = new UiBattleEngine(p1, p2);

        // >>> NOVO: cena de batalha em Pixel Art
        PixelBattleView pixelView = new PixelBattleView(engine);
        Scene battleScene = pixelView.buildScene();

        // Alternativa de “reinício”: você pode voltar à tela de nomes a partir de um botão
        // dentro do PixelBattleView, ou adicionar um MenuBar aqui se quiser.

        stage.setScene(battleScene);
    }   

    public static void main(String[] args) { launch(args); }
}
