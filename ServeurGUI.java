import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ServeurGUI extends Application {

    private TextArea logArea;
    private Label statusLabel;
    private Button btnDemarrer;
    private Button btnArreter;
    private Registry registry;
    private CalculService calculService;
    private boolean running = false;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("🖥️ Serveur RMI — Service de Calcul");

        // ── HEADER ──────────────────────────────────────────
        Label titre = new Label("Serveur RMI");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        titre.setTextFill(Color.WHITE);

        Label sousTitre = new Label("Service de Calcul Réparti");
        sousTitre.setFont(Font.font("Arial", 13));
        sousTitre.setTextFill(Color.web("#a0c4ff"));

        VBox header = new VBox(4, titre, sousTitre);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(24, 20, 20, 20));
        header.setStyle("-fx-background-color: #1a1a2e;");

        // ── STATUS ──────────────────────────────────────────
        statusLabel = new Label("⚪  Serveur arrêté");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statusLabel.setTextFill(Color.web("#ff6b6b"));
        statusLabel.setPadding(new Insets(8, 16, 8, 16));
        statusLabel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 20;");

        HBox statusBox = new HBox(statusLabel);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(12, 20, 8, 20));
        statusBox.setStyle("-fx-background-color: #0f3460;");

        // ── INFO CARDS ──────────────────────────────────────
        VBox cardPort    = creerCard("Port", "1099");
        VBox cardService = creerCard("Service", "CalculService");
        VBox cardOps     = creerCard("Opérations", "Toutes fonctions scientifiques");

        HBox cards = new HBox(12, cardPort, cardService, cardOps);
        cards.setAlignment(Pos.CENTER);
        cards.setPadding(new Insets(16, 20, 8, 20));
        cards.setStyle("-fx-background-color: #0f3460;");

        // ── LOGS ────────────────────────────────────────────
        Label logTitre = new Label("📋  Journal d'activité");
        logTitre.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        logTitre.setTextFill(Color.web("#a0c4ff"));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setFont(Font.font("Consolas", 12));
        logArea.setStyle(
                "-fx-control-inner-background: #0d1117;" +
                        "-fx-text-fill: #58a6ff;" +
                        "-fx-border-color: #30363d;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );
        logArea.setPrefHeight(220);

        VBox logBox = new VBox(8, logTitre, logArea);
        logBox.setPadding(new Insets(8, 20, 12, 20));
        logBox.setStyle("-fx-background-color: #0f3460;");

        // ── BOUTONS ─────────────────────────────────────────
        btnDemarrer = new Button("▶   Démarrer le Serveur");
        btnDemarrer.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btnDemarrer.setPrefWidth(220);
        btnDemarrer.setPrefHeight(42);
        btnDemarrer.setStyle(
                "-fx-background-color: #00b894;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
        btnDemarrer.setOnAction(e -> demarrerServeur());

        btnArreter = new Button("⏹   Arrêter le Serveur");
        btnArreter.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btnArreter.setPrefWidth(220);
        btnArreter.setPrefHeight(42);
        btnArreter.setDisable(true);
        btnArreter.setStyle(
                "-fx-background-color: #d63031;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-opacity: 0.5;"
        );
        btnArreter.setOnAction(e -> arreterServeur());

        HBox boutons = new HBox(16, btnDemarrer, btnArreter);
        boutons.setAlignment(Pos.CENTER);
        boutons.setPadding(new Insets(12, 20, 24, 20));
        boutons.setStyle("-fx-background-color: #1a1a2e;");

        // ── ASSEMBLAGE ──────────────────────────────────────
        VBox root = new VBox(header, statusBox, cards, logBox, boutons);
        root.setStyle("-fx-background-color: #1a1a2e;");

        Scene scene = new Scene(root, 580, 560);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        log("✅ Prêt. Cliquez sur 'Démarrer' pour lancer le serveur.");
    }

    private VBox creerCard(String label, String valeur) {
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", 11));
        lbl.setTextFill(Color.web("#a0c4ff"));

        Label val = new Label(valeur);
        val.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        val.setTextFill(Color.WHITE);

        VBox card = new VBox(3, lbl, val);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10, 20, 10, 20));
        card.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-background-radius: 8;"
        );
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private void demarrerServeur() {
        new Thread(() -> {
            try {
                // Créer l'implémentation avec logs pour toutes les méthodes
                calculService = new CalculServiceImpl() {
                    @Override
                    public String connect(String clientName) throws java.rmi.RemoteException {
                        String result = super.connect(clientName);
                        Platform.runLater(() -> log("🔌 NOUVEAU CLIENT : " + clientName));
                        return result;
                    }

                    @Override
                    public double evaluateExpression(String expr, boolean isDeg) throws java.rmi.RemoteException {
                        double result = super.evaluateExpression(expr, isDeg);
                        Platform.runLater(() -> log("📐 eval(\"" + expr + "\") = " + result));
                        return result;
                    }

                    @Override
                    public double add(double a, double b) throws java.rmi.RemoteException {
                        double result = super.add(a, b);
                        Platform.runLater(() -> log("➕ " + a + " + " + b + " = " + result));
                        return result;
                    }

                    @Override
                    public double sub(double a, double b) throws java.rmi.RemoteException {
                        double result = super.sub(a, b);
                        Platform.runLater(() -> log("➖ " + a + " - " + b + " = " + result));
                        return result;
                    }

                    @Override
                    public double mul(double a, double b) throws java.rmi.RemoteException {
                        double result = super.mul(a, b);
                        Platform.runLater(() -> log("✖️ " + a + " × " + b + " = " + result));
                        return result;
                    }

                    @Override
                    public double div(double a, double b) throws java.rmi.RemoteException {
                        double result = super.div(a, b);
                        Platform.runLater(() -> log("➗ " + a + " ÷ " + b + " = " + result));
                        return result;
                    }

                    @Override
                    public double sin(double x, boolean isDeg) throws java.rmi.RemoteException {
                        double result = super.sin(x, isDeg);
                        Platform.runLater(() -> log("📐 sin(" + x + (isDeg ? "°" : " rad") + ") = " + result));
                        return result;
                    }

                    @Override
                    public double cos(double x, boolean isDeg) throws java.rmi.RemoteException {
                        double result = super.cos(x, isDeg);
                        Platform.runLater(() -> log("📐 cos(" + x + (isDeg ? "°" : " rad") + ") = " + result));
                        return result;
                    }

                    @Override
                    public double tan(double x, boolean isDeg) throws java.rmi.RemoteException {
                        double result = super.tan(x, isDeg);
                        Platform.runLater(() -> log("📐 tan(" + x + (isDeg ? "°" : " rad") + ") = " + result));
                        return result;
                    }

                    @Override
                    public double sqrt(double x) throws java.rmi.RemoteException {
                        double result = super.sqrt(x);
                        Platform.runLater(() -> log("√ " + x + " = " + result));
                        return result;
                    }

                    @Override
                    public double ln(double x) throws java.rmi.RemoteException {
                        double result = super.ln(x);
                        Platform.runLater(() -> log("ln(" + x + ") = " + result));
                        return result;
                    }

                    @Override
                    public double log10(double x) throws java.rmi.RemoteException {
                        double result = super.log10(x);
                        Platform.runLater(() -> log("log₁₀(" + x + ") = " + result));
                        return result;
                    }

                    @Override
                    public double pow(double base, double exponent) throws java.rmi.RemoteException {
                        double result = super.pow(base, exponent);
                        Platform.runLater(() -> log(base + "^" + exponent + " = " + result));
                        return result;
                    }

                    @Override
                    public double factorial(int n) throws java.rmi.RemoteException {
                        double result = super.factorial(n);
                        Platform.runLater(() -> log(n + "! = " + result));
                        return result;
                    }
                };

                registry = LocateRegistry.createRegistry(1099);
                registry.bind("CalculService", calculService);
                running = true;

                Platform.runLater(() -> {
                    statusLabel.setText("🟢  Serveur actif — Port 1099");
                    statusLabel.setTextFill(Color.web("#00b894"));
                    btnDemarrer.setDisable(true);
                    btnDemarrer.setStyle(
                            "-fx-background-color: #00b894;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-opacity: 0.5;"
                    );
                    btnArreter.setDisable(false);
                    btnArreter.setStyle(
                            "-fx-background-color: #d63031;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-cursor: hand;"
                    );
                    log("✅ Serveur démarré avec succès !");
                    log("⏳ En attente de connexions clients...");
                });

            } catch (Exception e) {
                Platform.runLater(() -> log("❌ Erreur : " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    private void arreterServeur() {
        try {
            if (registry != null) {
                registry.unbind("CalculService");
            }
            running = false;
            Platform.runLater(() -> {
                statusLabel.setText("⚪  Serveur arrêté");
                statusLabel.setTextFill(Color.web("#ff6b6b"));
                btnDemarrer.setDisable(false);
                btnDemarrer.setStyle(
                        "-fx-background-color: #00b894;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;"
                );
                btnArreter.setDisable(true);
                btnArreter.setStyle(
                        "-fx-background-color: #d63031;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8;" +
                                "-fx-opacity: 0.5;"
                );
                log("⏹ Serveur arrêté.");
            });
        } catch (Exception e) {
            Platform.runLater(() -> log("❌ Erreur lors de l'arrêt : " + e.getMessage()));
        }
    }

    private void log(String message) {
        Platform.runLater(() -> {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.appendText("[" + time + "]  " + message + "\n");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}