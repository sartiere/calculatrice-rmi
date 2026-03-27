import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ClientGUI extends Application {

    // ── RMI ──────────────────────────────────────────────────────────────────
    private CalculService calculService;
    private Label statusLabel;

    // ── COULEURS CLAIRES MODERNES ────────────────────────────────────────────
    private static final String BG_PRIMARY   = "#ffffff";
    private static final String BG_SECONDARY = "#f8fafc";
    private static final String BG_CARD      = "#f1f5f9";
    private static final String BG_HOVER     = "#e2e8f0";
    private static final String SIDEBAR      = "#f1f5f9";
    private static final String PRIMARY      = "#3b82f6";
    private static final String PRIMARY_DARK = "#2563eb";
    private static final String SECONDARY    = "#8b5cf6";
    private static final String ACCENT       = "#06b6d4";
    private static final String SUCCESS      = "#10b981";
    private static final String WARNING      = "#f59e0b";
    private static final String DANGER       = "#ef4444";
    private static final String TEXT_PRIMARY   = "#1e293b";
    private static final String TEXT_SECONDARY = "#475569";
    private static final String TEXT_MUTED     = "#64748b";
    private static final String BORDER         = "#e2e8f0";
    private static final String GRID_MAJOR     = "#cbd5e1";
    private static final String GRID_MINOR     = "#e2e8f0";

    // ── CALC STATE ───────────────────────────────────────────────────────────
    private String expression = "";
    private double memory     = 0;
    private boolean isDeg     = false;
    private Label  calcResult, calcExpr, calcHist, calcMem, angleBadge, normBadge, rmiBadge;

    // ── GRAPH ────────────────────────────────────────────────────────────────
    private TextField freeFunc, freeXMin, freeXMax;
    private Canvas    freeCanvas;
    private Label     freeInfo;

    // ── POLY ─────────────────────────────────────────────────────────────────
    private int         polyDeg = 2;
    private TextField[] polyCoeffs;
    private Label       polyDisplay, polyRootsLabel, polyInfoLabel;
    private Canvas      polyCanvas;
    private HBox        polyCoeffBox;

    // ── COMPARE ──────────────────────────────────────────────────────────────
    private TextField cmpF1, cmpF2, cmpF3, cmpXMin, cmpXMax;
    private Canvas    cmpCanvas;
    private Label     cmpIntersLabel;

    // ── STATS ────────────────────────────────────────────────────────────────
    private TextArea  statsInput;
    private GridPane  statsGrid;

    // ── UNITS ────────────────────────────────────────────────────────────────
    private ComboBox<String> unitFrom, unitTo;
    private TextField        unitValue;
    private Label            unitResult;
    private FlowPane         unitCatBox;
    private String           currentCat = "Longueur";

    private static final String[][] UNIT_CATS = {
            {"Longueur", "m","km","cm","mm","mi","ft","in","yd"},
            {"Masse",    "kg","g","mg","lb","oz","tonne"},
            {"Vitesse",  "m/s","km/h","mph","nœud"},
            {"Surface",  "m²","km²","cm²","ft²","acre","ha"},
            {"Données",  "bit","octet","Ko","Mo","Go","To"},
            {"Énergie",  "J","kJ","cal","kcal","kWh","eV"},
            {"Pression", "Pa","kPa","bar","atm","mmHg","psi"},
    };

    private static final double[][] UNIT_FACTORS = {
            {1, 1000, 0.01, 0.001, 1609.34, 0.3048, 0.0254, 0.9144},
            {1, 0.001, 1e-6, 0.453592, 0.0283495, 1000},
            {1, 0.277778, 0.44704, 0.514444},
            {1, 1e6, 0.0001, 0.092903, 4046.86, 10000},
            {1, 8, 8000, 8e6, 8e9, 8e12},
            {1, 1000, 4.184, 4184, 3.6e6, 1.602e-19},
            {1, 1000, 100000, 101325, 133.322, 6894.76},
    };

    // ── MATRIX (version dynamique) ───────────────────────────────────────────
    private int matrixSize = 2;
    private TextField[][] matA;
    private TextField[][] matB;
    private Label         matResult;
    private GridPane      matAGrid, matBGrid;
    private ComboBox<Integer> matrixSizeSelector;

    // ── NAVIGATION ───────────────────────────────────────────────────────────
    private StackPane contentArea;
    private ToggleGroup navGroup;

    // ═════════════════════════════════════════════════════════════════════════
    @Override
    public void start(Stage stage) {
        stage.setTitle("✨ Calculatrice Scientifique Pro — RMI ✨");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_PRIMARY + ";");

        VBox sidebar = createSidebar();
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: " + BG_PRIMARY + ";");
        contentArea.setPadding(new Insets(20));

        HBox statusBar = buildStatusBar();

        root.setLeft(sidebar);
        root.setCenter(contentArea);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.show();

        setView(0);
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(25, 15, 25, 15));
        sidebar.setStyle("-fx-background-color: " + SIDEBAR + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-width: 0 1 0 0;");
        sidebar.setPrefWidth(180);

        Label logo = new Label("📐 CalcPro");
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        logo.setTextFill(Color.web(PRIMARY));
        logo.setPadding(new Insets(0, 0, 20, 0));
        sidebar.getChildren().add(logo);

        navGroup = new ToggleGroup();

        String[][] navItems = {
                {"📱", "Calcul", "calc"},
                {"📈", "Courbe", "curve"},
                {"📐", "Polynôme", "poly"},
                {"🔄", "Comparer", "compare"},
                {"📊", "Stats", "stats"},
                {"⚖️", "Unités", "units"},
                {"🔢", "Matrice", "matrix"}
        };

        for (int i = 0; i < navItems.length; i++) {
            final int index = i;
            ToggleButton btn = new ToggleButton(navItems[i][0] + "  " + navItems[i][1]);
            btn.setToggleGroup(navGroup);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setFont(Font.font("Segoe UI", 13));
            btn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                            "-fx-padding: 12 15;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;" +
                            "-fx-cursor: hand;"
            );

            btn.setOnAction(e -> {
                setView(index);
                for (ToggleButton b : navGroup.getToggles().stream().map(t -> (ToggleButton) t).toList()) {
                    b.setStyle(
                            "-fx-background-color: transparent;" +
                                    "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                                    "-fx-padding: 12 15;" +
                                    "-fx-border-radius: 10;" +
                                    "-fx-background-radius: 10;" +
                                    "-fx-cursor: hand;"
                    );
                }
                btn.setStyle(
                        "-fx-background-color: " + BG_CARD + ";" +
                                "-fx-text-fill: " + PRIMARY + ";" +
                                "-fx-padding: 12 15;" +
                                "-fx-border-radius: 10;" +
                                "-fx-background-radius: 10;" +
                                "-fx-cursor: hand;" +
                                "-fx-border-color: " + PRIMARY + ";" +
                                "-fx-border-width: 0 0 0 3;"
                );
            });

            sidebar.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Label version = new Label("v2.0 | RMI");
        version.setFont(Font.font("Segoe UI", 10));
        version.setTextFill(Color.web(TEXT_MUTED));
        sidebar.getChildren().add(version);

        return sidebar;
    }

    private void setView(int index) {
        contentArea.getChildren().clear();
        switch (index) {
            case 0 -> contentArea.getChildren().add(buildCalcPanel());
            case 1 -> contentArea.getChildren().add(buildCurvePanel());
            case 2 -> contentArea.getChildren().add(buildPolyPanel());
            case 3 -> contentArea.getChildren().add(buildComparePanel());
            case 4 -> contentArea.getChildren().add(buildStatsPanel());
            case 5 -> contentArea.getChildren().add(buildUnitsPanel());
            case 6 -> contentArea.getChildren().add(buildMatrixPanel());
        }
    }

    private HBox buildStatusBar() {
        TextField hostField = new TextField("localhost");
        hostField.setPrefWidth(150);
        hostField.setStyle("-fx-background-color: " + BG_CARD + "; -fx-text-fill: " + TEXT_PRIMARY + ";" +
                "-fx-border-color: " + BORDER + "; -fx-border-radius: 8; -fx-background-radius: 8;" +
                "-fx-padding: 8; -fx-font-family: 'Segoe UI';");

        Button connectBtn = new Button("🔌 CONNECTER");
        connectBtn.setStyle("-fx-background-color: " + PRIMARY + "; -fx-text-fill: white;" +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;" +
                "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-padding: 8 16;");
        connectBtn.setOnAction(e -> connectRMI(hostField.getText().trim()));

        Label serveurLabel = new Label("🌐 SERVEUR :");
        serveurLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        statusLabel = new Label("⚫ NON CONNECTÉ");
        statusLabel.setTextFill(Color.web(DANGER));
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        HBox bar = new HBox(12, serveurLabel, hostField, connectBtn, statusLabel);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setStyle("-fx-background-color: " + BG_SECONDARY + "; -fx-border-color: " + BORDER + "; -fx-border-width: 1 0 0 0;");
        return bar;
    }

    private void connectRMI(String host) {
        new Thread(() -> {
            try {
                Registry reg = LocateRegistry.getRegistry(host, 1099);
                calculService = (CalculService) reg.lookup("CalculService");

                String clientName = "Client " + java.net.InetAddress.getLocalHost().getHostName();
                calculService.connect(clientName);

                Platform.runLater(() -> {
                    statusLabel.setText("🟢 CONNECTÉ À " + host);
                    statusLabel.setTextFill(Color.web(SUCCESS));
                    if (rmiBadge != null) {
                        rmiBadge.setText("🔗 RMI");
                        rmiBadge.setStyle("-fx-background-color: " + SUCCESS + "20; -fx-text-fill: " + SUCCESS + ";" +
                                "-fx-border-color: " + SUCCESS + "40; -fx-border-radius: 20; -fx-background-radius: 20;" +
                                "-fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: bold;");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("🔴 ÉCHEC CONNEXION");
                    statusLabel.setTextFill(Color.web(DANGER));
                    calculService = null;
                    if (rmiBadge != null) {
                        rmiBadge.setText("🔗 RMI");
                        rmiBadge.setStyle("-fx-background-color: " + DANGER + "20; -fx-text-fill: " + DANGER + ";" +
                                "-fx-border-color: " + DANGER + "40; -fx-border-radius: 20; -fx-background-radius: 20;" +
                                "-fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: bold;");
                    }
                });
                e.printStackTrace();
            }
        }).start();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ── CALC PANEL ───────────────────────────────────────────────────────────
    // ═════════════════════════════════════════════════════════════════════════
    private VBox buildCalcPanel() {
        calcHist = new Label("");
        calcHist.setFont(Font.font("Segoe UI", 12));
        calcHist.setTextFill(Color.web(TEXT_MUTED));
        calcHist.setAlignment(Pos.CENTER_RIGHT);
        calcHist.setMaxWidth(Double.MAX_VALUE);

        calcExpr = new Label("");
        calcExpr.setFont(Font.font("Segoe UI", 14));
        calcExpr.setTextFill(Color.web(ACCENT));
        calcExpr.setAlignment(Pos.CENTER_RIGHT);
        calcExpr.setMaxWidth(Double.MAX_VALUE);

        calcResult = new Label("0");
        calcResult.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 48));
        calcResult.setTextFill(Color.web(TEXT_PRIMARY));
        calcResult.setAlignment(Pos.CENTER_RIGHT);
        calcResult.setMaxWidth(Double.MAX_VALUE);

        calcMem = new Label("");
        calcMem.setFont(Font.font("Segoe UI", 11));
        calcMem.setTextFill(Color.web(WARNING));
        calcMem.setAlignment(Pos.CENTER_RIGHT);

        angleBadge = new Label("RAD");
        angleBadge.setStyle("-fx-background-color: " + SECONDARY + "20; -fx-text-fill: " + SECONDARY + ";" +
                "-fx-border-color: " + SECONDARY + "40; -fx-border-radius: 20; -fx-background-radius: 20;" +
                "-fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
        angleBadge.setOnMouseClicked(e -> toggleAngle());

        normBadge = new Label("NORM");
        normBadge.setStyle("-fx-background-color: " + ACCENT + "20; -fx-text-fill: " + ACCENT + ";" +
                "-fx-border-color: " + ACCENT + "40; -fx-border-radius: 20; -fx-background-radius: 20;" +
                "-fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        rmiBadge = new Label("🔗 RMI");
        rmiBadge.setStyle("-fx-background-color: #ef444420; -fx-text-fill: " + DANGER + ";" +
                "-fx-border-color: " + DANGER + "40; -fx-border-radius: 20; -fx-background-radius: 20;" +
                "-fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        HBox badges = new HBox(8, angleBadge, normBadge, rmiBadge);
        badges.setAlignment(Pos.CENTER_RIGHT);

        VBox screen = new VBox(8, badges, calcHist, calcExpr, calcResult, calcMem);
        screen.setPadding(new Insets(16, 20, 14, 20));
        screen.setStyle("-fx-background-color: " + BG_CARD + "; -fx-border-radius: 16; -fx-background-radius: 16;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        GridPane kp = new GridPane();
        kp.setHgap(6); kp.setVgap(6);
        kp.setPadding(new Insets(12));

        addKey(kp, "sin", 0, 0, "fn", () -> appendExpr("sin("));
        addKey(kp, "cos", 1, 0, "fn", () -> appendExpr("cos("));
        addKey(kp, "tan", 2, 0, "fn", () -> appendExpr("tan("));
        addKey(kp, "7", 3, 0, "num", () -> appendExpr("7"));
        addKey(kp, "8", 4, 0, "num", () -> appendExpr("8"));
        addKey(kp, "9", 5, 0, "num", () -> appendExpr("9"));
        addKey(kp, "%", 6, 0, "op", () -> appendExpr("%"));
        addKey(kp, "MS", 7, 0, "fn2", () -> memStore());
        addKey(kp, "MR", 8, 0, "fn2", () -> memRecall());

        addKey(kp, "log", 0, 1, "fn", () -> appendExpr("log10("));
        addKey(kp, "ln", 1, 1, "fn", () -> appendExpr("ln("));
        addKey(kp, "√x", 2, 1, "fn2", () -> appendExpr("sqrt("));
        addKey(kp, "4", 3, 1, "num", () -> appendExpr("4"));
        addKey(kp, "5", 4, 1, "num", () -> appendExpr("5"));
        addKey(kp, "6", 5, 1, "num", () -> appendExpr("6"));
        addKey(kp, "÷", 6, 1, "op", () -> appendExpr("/"));
        addKey(kp, "+", 7, 1, "op", () -> appendExpr("+"));
        addKey(kp, "-", 8, 1, "op", () -> appendExpr("-"));

        addKey(kp, "x²", 0, 2, "fn2", () -> appendExpr("^2"));
        addKey(kp, "x³", 1, 2, "fn2", () -> appendExpr("^3"));
        addKey(kp, "xʸ", 2, 2, "fn2", () -> appendExpr("^"));
        addKey(kp, "1", 3, 2, "num", () -> appendExpr("1"));
        addKey(kp, "2", 4, 2, "num", () -> appendExpr("2"));
        addKey(kp, "3", 5, 2, "num", () -> appendExpr("3"));
        addKey(kp, "×", 6, 2, "op", () -> appendExpr("*"));
        Button eqBtn = makeKey("=", "eq", this::calculate);
        eqBtn.setPrefHeight(104);
        GridPane.setRowSpan(eqBtn, 2);
        kp.add(eqBtn, 7, 2);
        addKey(kp, "⌫", 8, 2, "del", () -> backspace());

        addKey(kp, ".", 0, 3, "num", () -> appendExpr("."));
        addKey(kp, "0", 1, 3, "num", () -> appendExpr("0"));
        addKey(kp, "π", 2, 3, "fn2", () -> appendExpr("π"));
        addKey(kp, "e", 3, 3, "fn2", () -> appendExpr("e"));
        addKey(kp, "n!", 4, 3, "fn2", () -> appendExpr("!"));
        addKey(kp, "AC", 5, 3, "clr", () -> clearCalc());

        addKey(kp, "(", 0, 4, "fn2", () -> appendExpr("("));
        addKey(kp, ")", 1, 4, "fn2", () -> appendExpr(")"));
        addKey(kp, "|x|", 2, 4, "fn", () -> appendExpr("abs("));
        addKey(kp, "1/x", 3, 4, "fn2", () -> appendExpr("1/("));
        addKey(kp, "eˣ", 4, 4, "fn2", () -> appendExpr("e^"));
        addKey(kp, "10ˣ", 5, 4, "fn2", () -> appendExpr("10^"));
        addKey(kp, "DEG/RAD", 6, 4, "fn2", () -> toggleAngle());

        ScrollPane sp = new ScrollPane(kp);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        VBox panel = new VBox(12, screen, sp);
        panel.setStyle("-fx-background-color: " + BG_PRIMARY + ";");
        VBox.setVgrow(sp, Priority.ALWAYS);
        return panel;
    }

    private void addKey(GridPane gp, String txt, int col, int row, String type, Runnable action) {
        gp.add(makeKey(txt, type, action), col, row);
    }

    private Button makeKey(String txt, String type, Runnable action) {
        Button btn = new Button(txt);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(48);
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        String base = "-fx-cursor: hand; -fx-border-radius: 10; -fx-background-radius: 10;";
        applyKeyStyle(btn, type, base);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + BG_HOVER + "; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-border-color: " + PRIMARY + "40; " + base));
        btn.setOnMouseExited(e -> applyKeyStyle(btn, type, base));
        btn.setOnAction(e -> action.run());
        GridPane.setHgrow(btn, Priority.ALWAYS);
        return btn;
    }

    private void applyKeyStyle(Button btn, String type, String base) {
        switch (type) {
            case "fn"  -> btn.setStyle("-fx-background-color: #e6f7e6; -fx-text-fill: " + SUCCESS   + "; -fx-border-color: " + SUCCESS   + "30; " + base);
            case "fn2" -> btn.setStyle("-fx-background-color: #ede9fe; -fx-text-fill: " + SECONDARY + "; -fx-border-color: " + SECONDARY + "30; " + base);
            case "op"  -> btn.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: " + ACCENT    + "; -fx-border-color: " + ACCENT    + "30; " + base);
            case "clr" -> btn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: " + DANGER    + "; -fx-border-color: " + DANGER    + "30; " + base);
            case "del" -> btn.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: " + WARNING   + "; -fx-border-color: " + WARNING   + "30; " + base);
            case "eq"  -> btn.setStyle("-fx-background-color: linear-gradient(to bottom, " + PRIMARY + ", " + PRIMARY_DARK + "); -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; " + base);
            default    -> btn.setStyle("-fx-background-color: " + BG_CARD + "; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-border-color: " + BORDER + "; " + base);
        }
    }

    private void appendExpr(String s) { expression += s; calcExpr.setText(expression); }
    private void clearCalc()  { expression = ""; calcExpr.setText(""); calcResult.setText("0"); calcHist.setText(""); }
    private void backspace()  { if (!expression.isEmpty()) expression = expression.substring(0, expression.length()-1); calcExpr.setText(expression); }
    private void toggleAngle(){ isDeg = !isDeg; angleBadge.setText(isDeg ? "DEG" : "RAD"); }
    private void memStore()   { try { memory = Double.parseDouble(calcResult.getText()); calcMem.setText("M = " + memory); } catch (Exception ignored) {} }
    private void memRecall()  { appendExpr(String.valueOf(memory)); }

    private void calculate() {
        if (calculService == null) {
            calcResult.setText("❌ Non connecté");
            calcResult.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 32));
            return;
        }

        if (expression.isEmpty()) return;

        try {
            double result = calculService.evaluateExpression(expression, isDeg);
            String disp = formatNum(result);
            calcHist.setText(expression + " = " + disp);
            calcResult.setText(disp);
            calcResult.setFont(Font.font("Segoe UI", FontWeight.LIGHT, disp.length() > 14 ? 26 : 48));
            expression = disp;
            calcExpr.setText("");
        } catch (Exception e) {
            calcResult.setText("⚠️ Erreur RMI");
            calcResult.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 32));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ── COURBE AVEC PAPIER MILLIMÉTRÉ AMÉLIORÉ ───────────────────────────────
    // ═════════════════════════════════════════════════════════════════════════
    private VBox buildCurvePanel() {
        freeFunc = styledTextField("Math.sin(x)*x");
        freeXMin = styledTextField("-10"); freeXMin.setPrefWidth(80);
        freeXMax = styledTextField("10");  freeXMax.setPrefWidth(80);
        freeInfo = styledLabel("", 11, TEXT_MUTED);
        freeCanvas = new Canvas(800, 400);

        FlowPane presets = new FlowPane(6, 6);
        String[] pNames = {"sin(x)","cos(x)","tan(x)","x²","x³","eˣ","ln|x|","x·sin(x)","σ(x)","sinc","√|x|","sin+cos"};
        String[] pFuncs = {"Math.sin(x)","Math.cos(x)","Math.tan(x)","x*x","x*x*x",
                "Math.exp(x)","Math.log(Math.abs(x)+0.001)","Math.sin(x)*x",
                "1/(1+Math.exp(-x))","Math.sin(x)/(x+0.001)",
                "Math.sqrt(Math.abs(x))","Math.sin(3*x)+Math.cos(2*x)"};
        for (int i = 0; i < pNames.length; i++) {
            final String fn = pFuncs[i];
            Button b = presetBtn(pNames[i]);
            b.setOnAction(e -> { freeFunc.setText(fn); drawFreeGraph(); });
            presets.getChildren().add(b);
        }

        Button traceBtn = actionBtn("📈 TRACER LA COURBE");
        traceBtn.setOnAction(e -> drawFreeGraph());

        VBox panel = new VBox(12, panelLabel("📐 f(x) ="), freeFunc,
                new HBox(8, rangeLabel("x ∈ ["), freeXMin, rangeLabel(","), freeXMax, rangeLabel("]")),
                presets, traceBtn, freeCanvas, freeInfo);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + BG_PRIMARY + ";");
        return panel;
    }

    private void drawFreeGraph() {
        String func = freeFunc.getText();
        double xMin = parseDouble(freeXMin.getText(), -10);
        double xMax = parseDouble(freeXMax.getText(), 10);
        drawCurveOnCanvas(freeCanvas, new String[]{func}, xMin, xMax, null, null);
        freeInfo.setText("📌 f(x) = " + func + "   |   x ∈ [" + xMin + ", " + xMax + "]");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ── POLYNÔME ─────────────────────────────────────────────────────────────
    // ═════════════════════════════════════════════════════════════════════════
    private VBox buildPolyPanel() {
        polyDisplay = styledLabel("P(x) = —", 14, ACCENT);
        polyRootsLabel = styledLabel("Cliquez sur RÉSOUDRE", 11, TEXT_MUTED);
        polyInfoLabel = styledLabel("", 11, TEXT_MUTED);
        polyCanvas = new Canvas(800, 380);
        polyCoeffBox = new HBox(8);
        polyCoeffBox.setAlignment(Pos.CENTER);

        HBox degBox = new HBox(8);
        ToggleGroup tg = new ToggleGroup();
        for (int d = 1; d <= 5; d++) {
            final int deg = d;
            ToggleButton tb = new ToggleButton(d + (d==1?"er":"ème"));
            tb.setToggleGroup(tg);
            tb.setStyle(toggleStyle(false));
            if (d == 2) { tb.setSelected(true); tb.setStyle(toggleStyle(true)); }
            tb.setOnAction(e -> {
                degBox.getChildren().forEach(n -> ((ToggleButton)n).setStyle(toggleStyle(false)));
                tb.setStyle(toggleStyle(true));
                setPolyDeg(deg);
            });
            degBox.getChildren().add(tb);
        }

        TextField polyXMin = styledTextField("-10"); polyXMin.setPrefWidth(80);
        TextField polyXMax = styledTextField("10");  polyXMax.setPrefWidth(80);
        Button solveBtn = actionBtn("🔍 RÉSOUDRE & TRACER");
        solveBtn.setOnAction(e -> solveAndPlot(parseDouble(polyXMin.getText(),-10), parseDouble(polyXMax.getText(),10)));

        setPolyDeg(2);

        VBox panel = new VBox(12, panelLabel("🎯 DEGRÉ"), degBox,
                panelLabel("📊 COEFFICIENTS"), polyCoeffBox, polyDisplay,
                new HBox(8, rangeLabel("x ∈ ["), polyXMin, rangeLabel(","), polyXMax, rangeLabel("]")),
                solveBtn, polyCanvas, panelLabel("🌱 RACINES"), polyRootsLabel, polyInfoLabel);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + BG_PRIMARY + ";");
        return panel;
    }

    private void setPolyDeg(int d) {
        polyDeg = d;
        polyCoeffs = new TextField[d + 1];
        polyCoeffBox.getChildren().clear();
        double[][] defaults = {{},{1,-2},{1,-3,2},{1,-2,-1,2},{1,0,-5,0,4},{1,0,-5,0,4,0}};
        for (int i = d; i >= 0; i--) {
            String lbl = i == 0 ? "a₀" : i == 1 ? "a₁" : "a" + i;
            TextField tf = styledTextField(defaults[d].length > d-i ? String.valueOf((int)defaults[d][d-i]) : "0");
            tf.setPrefWidth(70);
            tf.textProperty().addListener((obs, o, n) -> updatePolyDisplay());
            polyCoeffs[i] = tf;
            Label coeffLbl = new Label(lbl);
            coeffLbl.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 11px;");
            VBox cb = new VBox(4, coeffLbl, tf);
            cb.setAlignment(Pos.CENTER);
            polyCoeffBox.getChildren().add(cb);
        }
        updatePolyDisplay();
    }

    private void updatePolyDisplay() { polyDisplay.setText("P(x) = " + polyToString(getPolyCoeffs())); }

    private double[] getPolyCoeffs() {
        double[] c = new double[polyDeg + 1];
        for (int i = polyDeg; i >= 0; i--)
            c[polyDeg - i] = parseDouble(polyCoeffs[i].getText(), 0);
        return c;
    }

    private void solveAndPlot(double xMin, double xMax) {
        double[] coeffs = getPolyCoeffs();
        drawCurveOnCanvas(polyCanvas, new String[]{"POLY"}, xMin, xMax, coeffs, null);
        List<Double> roots = findRoots(coeffs, xMin, xMax);
        if (roots.isEmpty()) {
            polyRootsLabel.setText("⚠️ Aucune racine réelle dans [" + xMin + ", " + xMax + "]");
            polyRootsLabel.setTextFill(Color.web(WARNING));
        } else {
            StringBuilder rs = new StringBuilder();
            String[] subs = {"₁","₂","₃","₄","₅"};
            for (int i = 0; i < roots.size(); i++)
                rs.append("x").append(i < subs.length ? subs[i] : (i+1)).append(" = ").append(formatNum(roots.get(i))).append("   ");
            polyRootsLabel.setText("🌿 " + rs);
            polyRootsLabel.setTextFill(Color.web(SUCCESS));
        }
        double[] derC = new double[polyDeg];
        for (int i = 0; i < polyDeg; i++) derC[i] = coeffs[i] * (polyDeg - i);
        int extrema = findRoots(derC, xMin, xMax).size();
        polyInfoLabel.setText("📌 Racines : " + roots.size() + "   |   📍 Origine : " + formatNum(evalPoly(coeffs, 0)) + "   |   📈 Extrema : " + extrema);
        polyInfoLabel.setTextFill(Color.web(TEXT_SECONDARY));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ── COMPARER ─────────────────────────────────────────────────────────────
    // ═════════════════════════════════════════════════════════════════════════
    private VBox buildComparePanel() {
        cmpF1 = styledTextField("x*x-4");
        cmpF2 = styledTextField("Math.sin(x)*3");
        cmpF3 = styledTextField("Math.abs(x)-2");
        cmpXMin = styledTextField("-8"); cmpXMin.setPrefWidth(80);
        cmpXMax = styledTextField("8");  cmpXMax.setPrefWidth(80);
        cmpCanvas = new Canvas(800, 400);
        cmpIntersLabel = styledLabel("Cliquez sur COMPARER", 11, TEXT_MUTED);

        HBox legend = new HBox(16,
                legendDot(PRIMARY, "f₁"), legendDot(ACCENT, "f₂"),
                legendDot(SECONDARY, "f₃"), legendDot(WARNING, "⚡ intersections"));

        Button cmpBtn = actionBtn("🔄 COMPARER LES COURBES");
        cmpBtn.setOnAction(e -> compareGraphs());

        VBox panel = new VBox(12, panelLabel("🎨 f₁(x) — bleu"), cmpF1,
                panelLabel("💙 f₂(x) — cyan"), cmpF2, panelLabel("💜 f₃(x) — violet"), cmpF3,
                new HBox(8, rangeLabel("x ∈ ["), cmpXMin, rangeLabel(","), cmpXMax, rangeLabel("]")),
                cmpBtn, cmpCanvas, legend, panelLabel("📍 POINTS D'INTERSECTION"), cmpIntersLabel);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + BG_PRIMARY + ";");
        return panel;
    }

    private void compareGraphs() {
        double xMin = parseDouble(cmpXMin.getText(), -8);
        double xMax = parseDouble(cmpXMax.getText(), 8);
        String[] funcs = {cmpF1.getText(), cmpF2.getText(), cmpF3.getText().trim()};
        List<double[]> inters = drawCurveOnCanvas(cmpCanvas, funcs, xMin, xMax, null, null);
        if (inters == null || inters.isEmpty()) {
            cmpIntersLabel.setText("⚠️ Aucune intersection trouvée");
            cmpIntersLabel.setTextFill(Color.web(TEXT_MUTED));
        } else {
            StringBuilder sb = new StringBuilder();
            for (double[] pt : inters)
                sb.append("(").append(formatNum(pt[0])).append(", ").append(formatNum(pt[1])).append(")  ");
            cmpIntersLabel.setText("🔗 " + sb);
            cmpIntersLabel.setTextFill(Color.web(WARNING));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ── STATS ────────────────────────────────────────────────────────────────
    // ═════════════════════════════════════════════════════════════════════════
    private VBox buildStatsPanel() {
        statsInput = new TextArea("2, 4, 4, 4, 5, 5, 7, 9, 12, 3");
        statsInput.setPrefHeight(80);
        statsInput.setStyle("-fx-control-inner-background: " + BG_CARD + "; -fx-text-fill: " + TEXT_PRIMARY + ";" +
                "-fx-border-color: " + BORDER + "; -fx-border-radius: 10;");
        statsGrid = new GridPane();
        statsGrid.setHgap(10); statsGrid.setVgap(10);
        Button analyzeBtn = actionBtn("📊 ANALYSER");
        analyzeBtn.setOnAction(e -> computeStats());
        VBox panel = new VBox(12, panelLabel("📋 VALEURS"), statsInput, analyzeBtn, statsGrid);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + BG_PRIMARY + ";");
        computeStats();
        return panel;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ── UNITÉS ───────────────────────────────────────────────────────────────
    // ═════════════════════════════════════════════════════════════════════════
    private VBox buildUnitsPanel() {
        unitFrom = new ComboBox<>(); unitFrom.setStyle(comboStyle()); unitFrom.setPrefWidth(240);
        unitTo = new ComboBox<>(); unitTo.setStyle(comboStyle()); unitTo.setPrefWidth(240);
        unitValue = styledTextField("1");
        unitResult = new Label("—");
        unitResult.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 32));
        unitResult.setTextFill(Color.web(ACCENT));
        unitResult.setAlignment(Pos.CENTER);
        unitResult.setMaxWidth(Double.MAX_VALUE);
        unitResult.setStyle("-fx-background-color: " + BG_CARD + "; -fx-padding: 16; -fx-border-radius: 12;");

        unitCatBox = new FlowPane(8, 8);
        for (String[] cat : UNIT_CATS) {
            final String category = cat[0];
            Button b = presetBtn(category);
            b.setOnAction(e -> {
                unitCatBox.getChildren().forEach(n -> n.setStyle(presetStyle(false)));
                b.setStyle(presetStyle(true));
                currentCat = category;
                updateUnitSelectors();
            });
            unitCatBox.getChildren().add(b);
        }
        unitFrom.setOnAction(e -> convertUnit());
        unitTo.setOnAction(e -> convertUnit());
        unitValue.textProperty().addListener((obs, o, n) -> convertUnit());
        updateUnitSelectors();

        VBox panel = new VBox(12, panelLabel("📁 CATÉGORIE"), unitCatBox,
                panelLabel("🔄 CONVERTIR DE"), unitFrom, panelLabel("🔢 VALEUR"), unitValue,
                panelLabel("🎯 VERS"), unitTo, unitResult);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + BG_PRIMARY + ";");
        return panel;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ── MATRICE DYNAMIQUE (2x2, 3x3, 4x4) ────────────────────────────────────
    // ═════════════════════════════════════════════════════════════════════════
    private VBox buildMatrixPanel() {
        matResult = styledLabel("Choisissez une opération", 13, TEXT_PRIMARY);
        matResult.setStyle("-fx-background-color: " + BG_CARD + "; -fx-padding: 14; -fx-border-radius: 12;");
        matResult.setWrapText(true);

        // Sélecteur de taille de matrice
        HBox sizeBox = new HBox(10);
        sizeBox.setAlignment(Pos.CENTER_LEFT);
        Label sizeLabel = new Label("Taille de la matrice:");
        sizeLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px;");
        matrixSizeSelector = new ComboBox<>();
        matrixSizeSelector.getItems().addAll(2, 3, 4);
        matrixSizeSelector.setValue(2);
        matrixSizeSelector.setStyle(comboStyle());
        matrixSizeSelector.setOnAction(e -> updateMatrixSize(matrixSizeSelector.getValue()));
        sizeBox.getChildren().addAll(sizeLabel, matrixSizeSelector);

        // Grilles pour les matrices
        matAGrid = new GridPane();
        matAGrid.setHgap(8);
        matAGrid.setVgap(8);
        matAGrid.setAlignment(Pos.CENTER);

        matBGrid = new GridPane();
        matBGrid.setHgap(8);
        matBGrid.setVgap(8);
        matBGrid.setAlignment(Pos.CENTER);

        // Initialiser avec taille 2x2
        initMatrices(2);

        VBox mAbox = new VBox(6, panelLabel("📌 MATRICE A"), matAGrid);
        VBox mBbox = new VBox(6, panelLabel("📌 MATRICE B"), matBGrid);
        HBox matrices = new HBox(30, mAbox, mBbox);
        matrices.setAlignment(Pos.CENTER);

        FlowPane ops = new FlowPane(8, 8);
        String[][] opList = {{"A+B","add"},{"A−B","sub"},{"A×B","mul"},
                {"det(A)","detA"},{"det(B)","detB"},{"A⁻¹","invA"},
                {"tr(A)","trA"},{"tr(B)","trB"},{"Aᵀ","transA"},{"Bᵀ","transB"}};
        for (String[] op : opList) {
            final String operation = op[1];
            Button b = presetBtn(op[0]);
            b.setOnAction(e -> matrixOp(operation));
            ops.getChildren().add(b);
        }

        VBox panel = new VBox(14, sizeBox, matrices, panelLabel("⚙️ OPÉRATION"), ops, panelLabel("📋 RÉSULTAT"), matResult);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + BG_PRIMARY + ";");
        matrixOp("add");
        return panel;
    }

    private void initMatrices(int size) {
        matA = new TextField[size][size];
        matB = new TextField[size][size];

        matAGrid.getChildren().clear();
        matBGrid.getChildren().clear();

        double defaultValue = 1;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matA[i][j] = styledTextField(String.valueOf((int)(defaultValue + i * size + j)));
                matA[i][j].setPrefWidth(70);
                matA[i][j].setAlignment(Pos.CENTER);
                matAGrid.add(matA[i][j], j, i);

                matB[i][j] = styledTextField(String.valueOf((int)(defaultValue + i * size + j + size * size)));
                matB[i][j].setPrefWidth(70);
                matB[i][j].setAlignment(Pos.CENTER);
                matBGrid.add(matB[i][j], j, i);
            }
        }
    }

    private void updateMatrixSize(int newSize) {
        matrixSize = newSize;
        initMatrices(newSize);
        matrixOp("add");
    }

    private double[][] getDynamicMatrix(TextField[][] m) {
        int size = m.length;
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = parseDouble(m[i][j].getText(), 0);
            }
        }
        return matrix;
    }

    private void matrixOp(String op) {
        double[][] A = getDynamicMatrix(matA);
        double[][] B = getDynamicMatrix(matB);
        int size = A.length;

        String res = switch (op) {
            case "add" -> "A + B =\n" + fmtMat(addMatrices(A, B));
            case "sub" -> "A − B =\n" + fmtMat(subMatrices(A, B));
            case "mul" -> "A × B =\n" + fmtMat(multiplyMatrices(A, B));
            case "detA" -> "det(A) = " + formatNum(determinant(A));
            case "detB" -> "det(B) = " + formatNum(determinant(B));
            case "invA" -> {
                double det = determinant(A);
                if (Math.abs(det) < 1e-12) yield "❌ A non inversible (det = 0)";
                yield "A⁻¹ =\n" + fmtMat(inverseMatrix(A));
            }
            case "trA" -> "tr(A) = " + formatNum(trace(A));
            case "trB" -> "tr(B) = " + formatNum(trace(B));
            case "transA" -> "Aᵀ =\n" + fmtMat(transposeMatrix(A));
            case "transB" -> "Bᵀ =\n" + fmtMat(transposeMatrix(B));
            default -> "?";
        };
        matResult.setText(res);
    }

    private double[][] addMatrices(double[][] A, double[][] B) {
        int n = A.length;
        double[][] C = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = A[i][j] + B[i][j];
        return C;
    }

    private double[][] subMatrices(double[][] A, double[][] B) {
        int n = A.length;
        double[][] C = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = A[i][j] - B[i][j];
        return C;
    }

    private double[][] multiplyMatrices(double[][] A, double[][] B) {
        int n = A.length;
        double[][] C = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return C;
    }

    private double determinant(double[][] matrix) {
        int n = matrix.length;
        if (n == 1) return matrix[0][0];
        if (n == 2) return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];

        double det = 0;
        for (int j = 0; j < n; j++) {
            det += matrix[0][j] * cofactor(matrix, 0, j);
        }
        return det;
    }

    private double cofactor(double[][] matrix, int row, int col) {
        return Math.pow(-1, row + col) * determinant(minor(matrix, row, col));
    }

    private double[][] minor(double[][] matrix, int row, int col) {
        int n = matrix.length;
        double[][] minor = new double[n - 1][n - 1];
        int r = 0, c;
        for (int i = 0; i < n; i++) {
            if (i == row) continue;
            c = 0;
            for (int j = 0; j < n; j++) {
                if (j == col) continue;
                minor[r][c] = matrix[i][j];
                c++;
            }
            r++;
        }
        return minor;
    }

    private double[][] inverseMatrix(double[][] matrix) {
        int n = matrix.length;
        double det = determinant(matrix);
        double[][] adj = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                adj[j][i] = cofactor(matrix, i, j);
            }
        }

        double[][] inv = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                inv[i][j] = adj[i][j] / det;
            }
        }
        return inv;
    }

    private double trace(double[][] matrix) {
        double tr = 0;
        for (int i = 0; i < matrix.length; i++) {
            tr += matrix[i][i];
        }
        return tr;
    }

    private double[][] transposeMatrix(double[][] matrix) {
        int n = matrix.length;
        double[][] trans = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                trans[j][i] = matrix[i][j];
            }
        }
        return trans;
    }

    private String fmtMat(double[][] m) {
        int n = m.length;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("[ ");
            for (int j = 0; j < n; j++) {
                sb.append(String.format("%8.4f", m[i][j]));
                if (j < n - 1) sb.append("  ");
            }
            sb.append(" ]\n");
        }
        return sb.toString();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ── MOTEUR DE TRACÉ AVEC PAPIER MILLIMÉTRÉ AMÉLIORÉ ──────────────────────
    // ═════════════════════════════════════════════════════════════════════════
    private List<double[]> drawCurveOnCanvas(Canvas canvas, String[] funcs,
                                             double xMin, double xMax,
                                             double[] coeffs, Object ignored) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double W = canvas.getWidth(), H = canvas.getHeight();
        gc.clearRect(0, 0, W, H);
        gc.setFill(Color.web(BG_CARD));
        gc.fillRect(0, 0, W, H);

        int N = (int) W;
        double[][] yValues = new double[funcs.length][N + 1];
        for (int fi = 0; fi < funcs.length; fi++)
            for (int i = 0; i <= N; i++) {
                double x = xMin + (i / (double) N) * (xMax - xMin);
                try { yValues[fi][i] = funcs[fi].equals("POLY") ? evalPoly(coeffs, x) : evalFunc(funcs[fi], x); }
                catch (Exception e) { yValues[fi][i] = Double.NaN; }
            }

        double yMin = Double.MAX_VALUE, yMax = -Double.MAX_VALUE;
        for (double[] row : yValues) for (double v : row)
            if (Double.isFinite(v) && Math.abs(v) < 1e10) { yMin = Math.min(yMin, v); yMax = Math.max(yMax, v); }
        if (yMin == Double.MAX_VALUE) return null;
        double pad = (yMax - yMin) * 0.12; if (pad == 0) pad = 1;
        double yn = yMin - pad, yx = yMax + pad;

        final double fyn = yn, fyx = yx;
        java.util.function.DoubleUnaryOperator toX = x -> ((x - xMin) / (xMax - xMin)) * W;
        java.util.function.DoubleUnaryOperator toY = y -> H - ((y - fyn) / (fyx - fyn)) * H;

        // ── PAPIER MILLIMÉTRÉ AMÉLIORÉ ──
        gc.setLineWidth(0.8);

        // Lignes fines (millimétré) - plus claires mais visibles
        gc.setStroke(Color.web(GRID_MINOR));
        gc.setLineWidth(0.5);
        int minorSteps = 10;
        double stepX = (xMax - xMin) / minorSteps;
        for (int i = 0; i <= minorSteps; i++) {
            double x = xMin + i * stepX;
            double px = toX.applyAsDouble(x);
            gc.strokeLine(px, 0, px, H);
        }

        int minorStepsY = 10;
        double stepY = (yx - yn) / minorStepsY;
        for (int i = 0; i <= minorStepsY; i++) {
            double y = yn + i * stepY;
            double py = toY.applyAsDouble(y);
            gc.strokeLine(0, py, W, py);
        }

        // Lignes principales (axes majeurs) - plus épaisses et plus foncées
        gc.setStroke(Color.web(GRID_MAJOR));
        gc.setLineWidth(1.2);
        int majorSteps = (int)((xMax - xMin) / 2);
        if (majorSteps < 1) majorSteps = 1;
        stepX = (xMax - xMin) / majorSteps;
        for (int i = 0; i <= majorSteps; i++) {
            double x = xMin + i * stepX;
            double px = toX.applyAsDouble(x);
            gc.strokeLine(px, 0, px, H);
        }

        int majorStepsY = (int)((yx - yn) / 2);
        if (majorStepsY < 1) majorStepsY = 1;
        stepY = (yx - yn) / majorStepsY;
        for (int i = 0; i <= majorStepsY; i++) {
            double y = yn + i * stepY;
            double py = toY.applyAsDouble(y);
            gc.strokeLine(0, py, W, py);
        }

        // Axes X et Y (plus épais et couleur plus prononcée)
        gc.setStroke(Color.web("#94a3b8"));
        gc.setLineWidth(2);
        if (yn < 0 && yx > 0) gc.strokeLine(0, toY.applyAsDouble(0), W, toY.applyAsDouble(0));
        if (xMin < 0 && xMax > 0) gc.strokeLine(toX.applyAsDouble(0), 0, toX.applyAsDouble(0), H);

        // Labels des axes
        gc.setFill(Color.web(TEXT_MUTED));
        gc.setFont(Font.font("Segoe UI", 10));
        int step = Math.max(1, (int)((xMax - xMin) / 6));
        for (int gx = (int)Math.ceil(xMin); gx <= xMax; gx += step)
            if (Math.abs(gx) > 0.01) gc.fillText(String.valueOf(gx), toX.applyAsDouble(gx) - 8, H - 5);

        for (int gy = (int)Math.ceil(yn); gy <= yx; gy += step)
            if (Math.abs(gy) > 0.01 && gy < yx && gy > yn)
                gc.fillText(String.valueOf(gy), 5, toY.applyAsDouble(gy) + 4);

        // Tracer les courbes
        String[] colors = {PRIMARY, ACCENT, SECONDARY, WARNING};
        for (int fi = 0; fi < funcs.length; fi++) {
            if (funcs[fi] == null || funcs[fi].isBlank()) continue;
            gc.setStroke(Color.web(colors[fi % colors.length]));
            gc.setLineWidth(2.5);
            gc.beginPath();
            boolean started = false;
            for (int i = 0; i <= N; i++) {
                double y = yValues[fi][i];
                if (!Double.isFinite(y) || Math.abs(y) > 1e10) { started = false; continue; }
                double px = i, py = toY.applyAsDouble(y);
                if (!started) { gc.moveTo(px, py); started = true; } else gc.lineTo(px, py);
            }
            gc.stroke();
        }

        List<double[]> intersections = new ArrayList<>();
        if (funcs[0].equals("POLY") && coeffs != null) {
            for (double rx : findRoots(coeffs, xMin, xMax)) {
                double px = toX.applyAsDouble(rx), py = toY.applyAsDouble(0);
                gc.setFill(Color.web(SUCCESS)); gc.fillOval(px-5, py-5, 10, 10);
                gc.setStroke(Color.web(BG_PRIMARY)); gc.setLineWidth(2); gc.strokeOval(px-5,py-5,10,10);
                gc.setFill(Color.web(SUCCESS)); gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
                gc.fillText("x=" + formatNum(rx), px + 8, py - 6);
            }
        }

        if (funcs.length >= 2 && !funcs[0].equals("POLY")) {
            double prevDiff = Double.NaN;
            for (int i = 1; i <= N; i++) {
                double d = yValues[0][i] - yValues[1][i];
                if (!Double.isNaN(prevDiff) && Double.isFinite(prevDiff) && Double.isFinite(d) && prevDiff * d <= 0) {
                    double lo = xMin + ((i-1)/(double)N)*(xMax-xMin);
                    double hi = xMin + (i/(double)N)*(xMax-xMin);
                    for (int j = 0; j < 50; j++) {
                        double mid = (lo + hi) / 2;
                        try {
                            double fm = evalFunc(funcs[0], mid) - evalFunc(funcs[1], mid);
                            double fl = evalFunc(funcs[0], lo)  - evalFunc(funcs[1], lo);
                            if (Math.abs(fm) < 1e-10) break;
                            if (fl * fm < 0) hi = mid; else lo = mid;
                        } catch (Exception e) { break; }
                    }
                    double xr = (lo + hi) / 2;
                    double yr; try { yr = evalFunc(funcs[0], xr); } catch (Exception e) { yr = 0; }
                    boolean dup = false;
                    for (double[] pt : intersections) if (Math.abs(pt[0]-xr) < 0.01) { dup = true; break; }
                    if (!dup) {
                        intersections.add(new double[]{xr, yr});
                        double px = toX.applyAsDouble(xr), py = toY.applyAsDouble(yr);
                        gc.setFill(Color.web(WARNING)); gc.fillOval(px-5, py-5, 10, 10);
                        gc.setStroke(Color.web(BG_PRIMARY)); gc.setLineWidth(2); gc.strokeOval(px-5,py-5,10,10);
                        gc.setFill(Color.web(WARNING)); gc.setFont(Font.font("Segoe UI", 9));
                        gc.fillText("(" + formatNum(xr) + "," + formatNum(yr) + ")", px + 7, py - 7);
                    }
                }
                prevDiff = d;
            }
        }
        return intersections;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ── FONCTIONS UTILITAIRES ────────────────────────────────────────────────
    // ═════════════════════════════════════════════════════════════════════════
    private void computeStats() {
        String raw = statsInput.getText();
        List<Double> numList = new ArrayList<>();
        for (String s : raw.split("[\\s,;]+"))
            if (!s.isBlank()) try { numList.add(Double.parseDouble(s.trim())); } catch (Exception ignored) {}
        if (numList.isEmpty()) return;
        double[] nums = numList.stream().mapToDouble(Double::doubleValue).toArray();
        double sum = 0; for (double v : nums) sum += v;
        double mean = sum / nums.length;
        double[] sorted = nums.clone(); Arrays.sort(sorted);
        double median = nums.length % 2 == 0 ? (sorted[nums.length/2-1] + sorted[nums.length/2]) / 2 : sorted[nums.length/2];
        double variance = 0; for (double v : nums) variance += (v - mean) * (v - mean);
        variance /= nums.length;
        double std = Math.sqrt(variance);
        double q1 = sorted[nums.length / 4], q3 = sorted[3 * nums.length / 4];
        Map<Double, Long> freq = new HashMap<>();
        for (double v : nums) freq.merge(v, 1L, Long::sum);
        double mode = freq.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
        String[][] items = {
                {"📊 Moyenne (μ)", formatNum(mean), "true"}, {"📈 Médiane", formatNum(median), "true"},
                {"📉 Écart-type (σ)", formatNum(std), "false"}, {"🎯 Variance (σ²)", formatNum(variance), "false"},
                {"⭐ Mode", formatNum(mode), "false"}, {"📏 Étendue", formatNum(sorted[sorted.length-1]-sorted[0]), "false"},
                {"⬇️ Min", formatNum(sorted[0]), "false"}, {"⬆️ Max", formatNum(sorted[sorted.length-1]), "false"},
                {"🔹 Q1", formatNum(q1), "false"}, {"🔸 Q3", formatNum(q3), "false"},
                {"📐 IQR", formatNum(q3 - q1), "false"}, {"🔢 N", String.valueOf(nums.length), "false"},
        };
        statsGrid.getChildren().clear();
        for (int i = 0; i < items.length; i++) {
            boolean accent = "true".equals(items[i][2]);
            VBox card = statCard(items[i][0], items[i][1], accent);
            statsGrid.add(card, i % 2, i / 2);
        }
    }

    private VBox statCard(String label, String value, boolean accent) {
        Label v = new Label(value);
        v.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        v.setTextFill(Color.web(accent ? PRIMARY : TEXT_PRIMARY));
        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", 11));
        l.setTextFill(Color.web(TEXT_MUTED));
        VBox card = new VBox(4, v, l);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12));
        card.setMinWidth(180);
        card.setStyle("-fx-background-color: " + BG_CARD + "; -fx-border-color: " + (accent ? PRIMARY + "40" : BORDER) + ";" +
                "-fx-border-radius: 12; -fx-background-radius: 12;");
        return card;
    }

    private void updateUnitSelectors() {
        for (String[] cat : UNIT_CATS) {
            if (cat[0].equals(currentCat)) {
                unitFrom.getItems().setAll(Arrays.copyOfRange(cat, 1, cat.length));
                unitTo.getItems().setAll(Arrays.copyOfRange(cat, 1, cat.length));
                if (unitTo.getItems().size() > 1) unitTo.getSelectionModel().select(1);
                unitFrom.getSelectionModel().selectFirst();
                convertUnit(); return;
            }
        }
    }

    private void convertUnit() {
        if (unitFrom.getValue() == null || unitTo.getValue() == null) return;
        double val = parseDouble(unitValue.getText(), 0);
        int fi = unitFrom.getSelectionModel().getSelectedIndex();
        int ti = unitTo.getSelectionModel().getSelectedIndex();
        if (fi < 0 || ti < 0) return;
        double result;
        if ("Température".equals(currentCat)) {
            String from = unitFrom.getValue(), to = unitTo.getValue();
            if ("°C".equals(from) && "°F".equals(to)) result = val*9.0/5+32;
            else if ("°F".equals(from) && "°C".equals(to)) result = (val-32)*5.0/9;
            else if ("°C".equals(from) && "K".equals(to)) result = val+273.15;
            else if ("K".equals(from) && "°C".equals(to)) result = val-273.15;
            else result = val;
        } else {
            double[] factors = null;
            for (int i = 0; i < UNIT_CATS.length; i++)
                if (UNIT_CATS[i][0].equals(currentCat)) { factors = UNIT_FACTORS[i]; break; }
            if (factors == null) return;
            result = val * factors[fi] / factors[ti];
        }
        unitResult.setText(formatNum(result) + " " + unitTo.getValue());
    }

    private double evalFunc(String expr, double x) {
        String e = expr.replaceAll("(?<![a-zA-Z])x(?![a-zA-Z])", "(" + x + ")");
        return new ExprParser(e, false).parse();
    }

    private List<Double> findRoots(double[] coeffs, double xMin, double xMax) {
        List<Double> roots = new ArrayList<>();
        int N = 8000; double dx = (xMax - xMin) / N;
        double prev = evalPoly(coeffs, xMin);
        for (int i = 1; i <= N; i++) {
            double x = xMin + i * dx, cur = evalPoly(coeffs, x);
            if (prev * cur <= 0) {
                double lo = xMin + (i-1)*dx, hi = x;
                for (int j = 0; j < 60; j++) {
                    double mid = (lo+hi)/2, fm = evalPoly(coeffs, mid);
                    if (Math.abs(fm) < 1e-12) break;
                    if (evalPoly(coeffs, lo) * fm < 0) hi = mid; else lo = mid;
                }
                double root = Math.round((lo+hi)/2 * 1e6) / 1e6;
                boolean dup = false;
                for (double r : roots) if (Math.abs(r - root) < 1e-5) { dup = true; break; }
                if (!dup) roots.add(root);
            }
            prev = cur;
        }
        return roots;
    }

    private double evalPoly(double[] coeffs, double x) {
        double r = 0; for (double c : coeffs) r = r * x + c; return r;
    }

    private String polyToString(double[] coeffs) {
        StringBuilder sb = new StringBuilder();
        int n = coeffs.length - 1;
        for (int i = 0; i < coeffs.length; i++) {
            double c = coeffs[i]; int pw = n - i;
            if (c == 0) continue;
            String sign = c < 0 ? (sb.length()==0?"−":" − ") : (sb.length()==0?"":" + ");
            double ab = Math.abs(c);
            sb.append(sign).append((ab==1&&pw>0)?"":formatNum(ab)).append(pw==0?"":(pw==1?"x":"x"+supScript(pw)));
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ── EXPRESSION PARSER ────────────────────────────────────────────────────
    // ═════════════════════════════════════════════════════════════════════════
    private static class ExprParser {
        private final String src; private int pos; private final boolean deg;
        ExprParser(String src, boolean deg) { this.src = src.replaceAll("\\s+",""); this.deg = deg; }
        double parse() { return parseExpr(); }
        private double parseExpr() {
            double v = parseTerm();
            while (pos < src.length()) { char c = src.charAt(pos);
                if(c=='+'){pos++;v+=parseTerm();}else if(c=='-'){pos++;v-=parseTerm();}else break; }
            return v;
        }
        private double parseTerm() {
            double v = parsePow();
            while (pos < src.length()) { char c = src.charAt(pos);
                if(c=='*'){pos++;v*=parsePow();}else if(c=='/'){pos++;double d=parsePow();v=d==0?Double.NaN:v/d;}else if(c=='%'){pos++;v%=parsePow();}else break; }
            return v;
        }
        private double parsePow() {
            double v = parseUnary();
            if(pos<src.length()&&src.charAt(pos)=='^'){pos++;v=Math.pow(v,parsePow());}
            while(pos<src.length()&&src.charAt(pos)=='!'){pos++;v=factorial((int)v);}
            return v;
        }
        private double parseUnary() {
            if(pos<src.length()&&src.charAt(pos)=='-'){pos++;return -parsePrimary();}
            if(pos<src.length()&&src.charAt(pos)=='+'){pos++;return parsePrimary();}
            return parsePrimary();
        }
        private double parsePrimary() {
            if(pos>=src.length())return 0;
            char c=src.charAt(pos);
            if(Character.isDigit(c)||c=='.')return parseNumber();
            if(c=='('){pos++;double v=parseExpr();if(pos<src.length()&&src.charAt(pos)==')')pos++;return v;}
            if(Character.isLetter(c))return parseNamedToken();
            return 0;
        }
        private double parseNumber() {
            int start=pos;
            while(pos<src.length()&&(Character.isDigit(src.charAt(pos))||src.charAt(pos)=='.'))pos++;
            if(pos<src.length()&&(src.charAt(pos)=='e'||src.charAt(pos)=='E')){pos++;
                if(pos<src.length()&&(src.charAt(pos)=='+'||src.charAt(pos)=='-'))pos++;
                while(pos<src.length()&&Character.isDigit(src.charAt(pos)))pos++;}
            try{return Double.parseDouble(src.substring(start,pos));}catch(Exception e){return 0;}
        }
        private double parseNamedToken() {
            int start=pos;
            while(pos<src.length()&&(Character.isLetter(src.charAt(pos))||src.charAt(pos)=='_'))pos++;
            String name = src.substring(start, pos);
            if(name.equals("PI")||name.equals("pi")||name.equals("π"))return Math.PI;
            if(name.equals("E")||name.equals("e"))return Math.E;
            double arg=0;
            if(pos<src.length()&&src.charAt(pos)=='('){pos++;arg=parseExpr();if(pos<src.length()&&src.charAt(pos)==')')pos++;}
            return switch(name.toLowerCase()){
                case "sin" -> Math.sin(deg?Math.toRadians(arg):arg);
                case "cos" -> Math.cos(deg?Math.toRadians(arg):arg);
                case "tan" -> Math.tan(deg?Math.toRadians(arg):arg);
                case "asin" -> { double v=Math.asin(arg); yield deg?Math.toDegrees(v):v; }
                case "acos" -> { double v=Math.acos(arg); yield deg?Math.toDegrees(v):v; }
                case "atan" -> { double v=Math.atan(arg); yield deg?Math.toDegrees(v):v; }
                case "sinh" -> Math.sinh(arg); case "cosh" -> Math.cosh(arg);
                case "tanh" -> Math.tanh(arg); case "sqrt" -> Math.sqrt(arg);
                case "cbrt" -> Math.cbrt(arg); case "abs" -> Math.abs(arg);
                case "floor" -> Math.floor(arg); case "ceil" -> Math.ceil(arg);
                case "log","log10" -> Math.log10(arg);
                case "log2" -> Math.log(arg)/Math.log(2);
                case "ln","log1p" -> Math.log(arg);
                case "exp" -> Math.exp(arg);
                default -> arg;
            };
        }
        private static double factorial(int n){
            n=Math.abs(n);if(n>170)return Double.POSITIVE_INFINITY;
            double r=1;for(int i=2;i<=n;i++)r*=i;return r;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ── HELPERS UI ───────────────────────────────────────────────────────────
    // ═════════════════════════════════════════════════════════════════════════
    private Label styledLabel(String txt, int size, String color) {
        Label l = new Label(txt); l.setFont(Font.font("Segoe UI", size)); l.setTextFill(Color.web(color)); return l;
    }
    private TextField styledTextField(String val) {
        TextField tf = new TextField(val); tf.setStyle("-fx-background-color: " + BG_CARD + "; -fx-text-fill: " + TEXT_PRIMARY + ";" +
                "-fx-border-color: " + BORDER + "; -fx-border-radius: 10; -fx-background-radius: 10;" +
                "-fx-padding: 10; -fx-font-family: 'Segoe UI';"); return tf;
    }
    private Label rangeLabel(String txt) {
        Label l = new Label(txt); l.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px;"); return l;
    }
    private Label panelLabel(String txt) {
        Label l = new Label(txt); l.setStyle("-fx-text-fill: " + PRIMARY + "; -fx-font-size: 13px; -fx-font-weight: bold;"); return l;
    }
    private String comboStyle() {
        return "-fx-background-color: " + BG_CARD + "; -fx-text-fill: " + TEXT_PRIMARY + ";" +
                "-fx-border-color: " + BORDER + "; -fx-border-radius: 10; -fx-background-radius: 10;" +
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;";
    }
    private String presetStyle(boolean active) {
        return active ? "-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; -fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 6 14;" :
                "-fx-background-color: " + BG_CARD + "; -fx-text-fill: " + TEXT_SECONDARY + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 6 14;";
    }
    private String toggleStyle(boolean active) { return presetStyle(active); }
    private Button presetBtn(String txt) {
        Button b = new Button(txt); b.setStyle(presetStyle(false)); b.setFont(Font.font("Segoe UI", 12)); return b;
    }
    private Button actionBtn(String txt) {
        Button b = new Button(txt); b.setMaxWidth(Double.MAX_VALUE); b.setPrefHeight(44);
        b.setStyle("-fx-background-color: linear-gradient(to right, " + PRIMARY + ", " + PRIMARY_DARK + "); -fx-text-fill: white;" +
                "-fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-weight: bold;");
        return b;
    }
    private HBox legendDot(String color, String label) {
        javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(16, 4);
        rect.setFill(Color.web(color)); rect.setArcWidth(4); rect.setArcHeight(4);
        Label l = new Label(label); l.setFont(Font.font("Segoe UI", 10)); l.setTextFill(Color.web(TEXT_SECONDARY));
        HBox box = new HBox(6, rect, l); box.setAlignment(Pos.CENTER_LEFT); return box;
    }
    private static String formatNum(double v) {
        if (Double.isNaN(v)) return "NaN";
        if (Double.isInfinite(v)) return v > 0 ? "+∞" : "−∞";
        if (v == Math.floor(v) && Math.abs(v) < 1e12) return String.valueOf((long) v);
        return String.format("%.6g", v).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
    private static double parseDouble(String s, double def) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }
    private static String supScript(int n) {
        return switch (n) { case 2->"²"; case 3->"³"; case 4->"⁴"; case 5->"⁵"; default->"^"+n; };
    }

    public static void main(String[] args) { launch(args); }
}