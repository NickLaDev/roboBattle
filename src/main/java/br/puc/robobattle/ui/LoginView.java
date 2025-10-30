package br.puc.robobattle.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/** Tela de Login/Entrada no estilo neon-castelo, com fundo "intro.png". */
public class LoginView {

    public interface OnStart {
        void start(String player1, String player2);
    }

    private static final int W = 1280, H = 720;

    public Scene build(Stage stage, OnStart callback) {
        // === Fundo ===
        Image bg = new Image(getClass().getResourceAsStream("/assets/ui/intro.png"));
        ImageView bgView = new ImageView(bg);
        bgView.setPreserveRatio(true);
        bgView.setSmooth(true);

        StackPane bgLayer = new StackPane(bgView);
        bgLayer.setStyle("-fx-background-color: black;");

        // === Painel central ===
        Label title = new Label("ENTER GAME");
        title.setTextFill(Color.web("#e9ddff"));
        title.setStyle("""
            -fx-font-family: "Georgia","Times New Roman",serif;
            -fx-font-size: 26px; -fx-font-weight: bold;
        """);

        TextField p1 = new TextField();
        p1.setPromptText("Player 1");
        styleNeonField(p1);

        TextField p2 = new TextField();
        p2.setPromptText("Player 2");
        styleNeonField(p2);

        Button startBtn = new Button("ENTER GAME");
        styleNeonButton(startBtn);

        VBox form = new VBox(14, title, p1, p2, startBtn);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(24));
        form.setMaxWidth(480);

        StackPane panel = new StackPane(form);
        panel.setStyle(panelStyle("#bb86fc"));
        panel.setMaxWidth(560);
        panel.setMaxHeight(320);

        // Glow suave no painel
        DropShadow glow = new DropShadow(30, Color.web("#bb86fc"));
        glow.setSpread(0.15);
        panel.setEffect(glow);

        // === Overlay ===
        StackPane root = new StackPane(bgLayer, panel);
        StackPane.setAlignment(panel, Pos.CENTER);

        Scene scene = new Scene(root, W, H, Color.BLACK);

        // Faz o background preencher a janela (cover)
        scene.widthProperty().addListener((obs, o, w) -> {
            bgView.setFitWidth((double) w);
            bgView.setFitHeight(scene.getHeight());
        });
        scene.heightProperty().addListener((obs, o, h) -> {
            bgView.setFitWidth(scene.getWidth());
            bgView.setFitHeight((double) h);
        });

        // Ações
        Runnable submit = () -> {
            String n1 = p1.getText() == null || p1.getText().isBlank() ? "Jogador 1" : p1.getText().trim();
            String n2 = p2.getText() == null || p2.getText().isBlank() ? "Jogador 2" : p2.getText().trim();
            callback.start(n1, n2);
        };

        startBtn.setOnAction(e -> submit.run());
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) submit.run();
            if (e.getCode() == KeyCode.ESCAPE) stage.close();
        });

        // Focus inicial
        p1.requestFocus();

        return scene;
    }

    // ======= estilos helpers =======
    private static void styleNeonField(TextField tf) {
        tf.setStyle("""
            -fx-background-color: rgba(10,8,14,0.65);
            -fx-text-fill: #f5f3ff;
            -fx-prompt-text-fill: rgba(229, 231, 235, 0.6);
            -fx-font-size: 16px;
            -fx-background-insets: 0;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-border-color: rgba(233, 213, 255, 0.6);
            -fx-border-width: 1.5;
            -fx-padding: 10 14 10 14;
        """);
        DropShadow glow = new DropShadow(18, Color.web("#bb86fc"));
        glow.setSpread(0.10);
        tf.focusedProperty().addListener((o, was, is) -> tf.setEffect(is ? glow : null));
    }

    private static void styleNeonButton(Button b) {
        b.setTextFill(Color.web("#eee"));
        b.setStyle("""
            -fx-font-size: 16px; -fx-font-weight: bold;
            -fx-background-radius: 12; -fx-border-radius: 12;
            -fx-padding: 10 22 10 22;
            -fx-background-color: linear-gradient(#433260, #2a2140);
            -fx-border-color: rgba(233,213,255,0.7); -fx-border-width: 1.5;
        """);
        DropShadow glow = new DropShadow(24, Color.web("#bb86fc"));
        glow.setSpread(0.22);
        b.setOnMouseEntered(e -> { b.setEffect(glow); b.setScaleX(1.05); b.setScaleY(1.05); });
        b.setOnMouseExited (e -> { b.setEffect(null);  b.setScaleX(1.00); b.setScaleY(1.00); });
        b.setOnMousePressed(e -> { b.setScaleX(0.97); b.setScaleY(0.97); });
        b.setOnMouseReleased(e -> { b.setScaleX(1.05); b.setScaleY(1.05); });
    }

    private static String panelStyle(String accentHex) {
        String border = Color.web(accentHex, 0.85).toString().replace("0x", "#");
        return """
           -fx-background-color:
               linear-gradient(rgba(16,12,22,0.90), rgba(10,8,14,0.90)),
               radial-gradient(radius 100%%, rgba(255,255,255,0.06), rgba(0,0,0,0.06));
           -fx-background-radius: 18;
           -fx-border-radius: 18;
           -fx-border-color: %s;
           -fx-border-width: 2;
        """.formatted(border);
    }
}
