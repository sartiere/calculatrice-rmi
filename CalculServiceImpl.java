import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Implémentation concrète du service de calcul.
 * Doit étendre UnicastRemoteObject pour être accessible à distance.
 */
public class CalculServiceImpl extends UnicastRemoteObject implements CalculService {

    protected CalculServiceImpl() throws RemoteException {
        super();
    }

    // Méthode de connexion - appelée par le client lors de la connexion
    @Override
    public String connect(String clientName) throws RemoteException {
        String clientInfo = clientName + " (RMI)";
        System.out.println("[Serveur] 🔌 CLIENT CONNECTÉ : " + clientInfo);
        return "Connexion acceptée - Bienvenue " + clientName;
    }

    // Opérations de base (int)
    @Override
    public int addition(int a, int b) throws RemoteException {
        System.out.println("[Serveur] addition(" + a + ", " + b + ") appelée");
        return a + b;
    }

    @Override
    public int soustraction(int a, int b) throws RemoteException {
        System.out.println("[Serveur] soustraction(" + a + ", " + b + ") appelée");
        return a - b;
    }

    @Override
    public int multiplication(int a, int b) throws RemoteException {
        System.out.println("[Serveur] multiplication(" + a + ", " + b + ") appelée");
        return a * b;
    }

    @Override
    public double division(int a, int b) throws RemoteException {
        System.out.println("[Serveur] division(" + a + ", " + b + ") appelée");
        if (b == 0) {
            throw new RemoteException("Erreur : division par zéro !");
        }
        return (double) a / b;
    }

    // Opérations avec des doubles
    @Override
    public double add(double a, double b) throws RemoteException {
        double result = a + b;
        System.out.println("[Serveur] add(" + a + ", " + b + ") = " + result);
        return result;
    }

    @Override
    public double sub(double a, double b) throws RemoteException {
        double result = a - b;
        System.out.println("[Serveur] sub(" + a + ", " + b + ") = " + result);
        return result;
    }

    @Override
    public double mul(double a, double b) throws RemoteException {
        double result = a * b;
        System.out.println("[Serveur] mul(" + a + ", " + b + ") = " + result);
        return result;
    }

    @Override
    public double div(double a, double b) throws RemoteException {
        if (b == 0) {
            throw new RemoteException("Erreur : division par zéro !");
        }
        double result = a / b;
        System.out.println("[Serveur] div(" + a + ", " + b + ") = " + result);
        return result;
    }

    // Fonctions trigonométriques
    @Override
    public double sin(double x, boolean isDeg) throws RemoteException {
        double rad = isDeg ? Math.toRadians(x) : x;
        double result = Math.sin(rad);
        System.out.println("[Serveur] sin(" + x + (isDeg ? "°" : " rad") + ") = " + result);
        return result;
    }

    @Override
    public double cos(double x, boolean isDeg) throws RemoteException {
        double rad = isDeg ? Math.toRadians(x) : x;
        double result = Math.cos(rad);
        System.out.println("[Serveur] cos(" + x + (isDeg ? "°" : " rad") + ") = " + result);
        return result;
    }

    @Override
    public double tan(double x, boolean isDeg) throws RemoteException {
        double rad = isDeg ? Math.toRadians(x) : x;
        double result = Math.tan(rad);
        System.out.println("[Serveur] tan(" + x + (isDeg ? "°" : " rad") + ") = " + result);
        return result;
    }

    @Override
    public double asin(double x, boolean isDeg) throws RemoteException {
        double result = Math.asin(x);
        if (isDeg) result = Math.toDegrees(result);
        System.out.println("[Serveur] asin(" + x + ") = " + result + (isDeg ? "°" : " rad"));
        return result;
    }

    @Override
    public double acos(double x, boolean isDeg) throws RemoteException {
        double result = Math.acos(x);
        if (isDeg) result = Math.toDegrees(result);
        System.out.println("[Serveur] acos(" + x + ") = " + result + (isDeg ? "°" : " rad"));
        return result;
    }

    @Override
    public double atan(double x, boolean isDeg) throws RemoteException {
        double result = Math.atan(x);
        if (isDeg) result = Math.toDegrees(result);
        System.out.println("[Serveur] atan(" + x + ") = " + result + (isDeg ? "°" : " rad"));
        return result;
    }

    // Fonctions hyperboliques
    @Override
    public double sinh(double x) throws RemoteException {
        double result = Math.sinh(x);
        System.out.println("[Serveur] sinh(" + x + ") = " + result);
        return result;
    }

    @Override
    public double cosh(double x) throws RemoteException {
        double result = Math.cosh(x);
        System.out.println("[Serveur] cosh(" + x + ") = " + result);
        return result;
    }

    @Override
    public double tanh(double x) throws RemoteException {
        double result = Math.tanh(x);
        System.out.println("[Serveur] tanh(" + x + ") = " + result);
        return result;
    }

    // Racines
    @Override
    public double sqrt(double x) throws RemoteException {
        double result = Math.sqrt(x);
        System.out.println("[Serveur] sqrt(" + x + ") = " + result);
        return result;
    }

    @Override
    public double cbrt(double x) throws RemoteException {
        double result = Math.cbrt(x);
        System.out.println("[Serveur] cbrt(" + x + ") = " + result);
        return result;
    }

    // Valeur absolue et arrondis
    @Override
    public double abs(double x) throws RemoteException {
        double result = Math.abs(x);
        System.out.println("[Serveur] abs(" + x + ") = " + result);
        return result;
    }

    @Override
    public double floor(double x) throws RemoteException {
        double result = Math.floor(x);
        System.out.println("[Serveur] floor(" + x + ") = " + result);
        return result;
    }

    @Override
    public double ceil(double x) throws RemoteException {
        double result = Math.ceil(x);
        System.out.println("[Serveur] ceil(" + x + ") = " + result);
        return result;
    }

    // Logarithmes
    @Override
    public double log10(double x) throws RemoteException {
        double result = Math.log10(x);
        System.out.println("[Serveur] log10(" + x + ") = " + result);
        return result;
    }

    @Override
    public double log2(double x) throws RemoteException {
        double result = Math.log(x) / Math.log(2);
        System.out.println("[Serveur] log2(" + x + ") = " + result);
        return result;
    }

    @Override
    public double ln(double x) throws RemoteException {
        double result = Math.log(x);
        System.out.println("[Serveur] ln(" + x + ") = " + result);
        return result;
    }

    // Exponentielle
    @Override
    public double exp(double x) throws RemoteException {
        double result = Math.exp(x);
        System.out.println("[Serveur] exp(" + x + ") = " + result);
        return result;
    }

    // Puissance
    @Override
    public double pow(double base, double exponent) throws RemoteException {
        double result = Math.pow(base, exponent);
        System.out.println("[Serveur] pow(" + base + ", " + exponent + ") = " + result);
        return result;
    }

    // Factorielle
    @Override
    public double factorial(int n) throws RemoteException {
        n = Math.abs(n);
        if (n > 170) return Double.POSITIVE_INFINITY;
        double result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        System.out.println("[Serveur] factorial(" + n + ") = " + result);
        return result;
    }

    // Pourcentage
    @Override
    public double percent(double value, double percent) throws RemoteException {
        double result = value * percent / 100;
        System.out.println("[Serveur] percent(" + value + ", " + percent + "%) = " + result);
        return result;
    }

    // Constantes
    @Override
    public double getPi() throws RemoteException {
        System.out.println("[Serveur] getPi() = " + Math.PI);
        return Math.PI;
    }

    @Override
    public double getE() throws RemoteException {
        System.out.println("[Serveur] getE() = " + Math.E);
        return Math.E;
    }

    // Évaluation d'expression complète
    @Override
    public double evaluateExpression(String expr, boolean isDeg) throws RemoteException {
        System.out.println("[Serveur] evaluateExpression(\"" + expr + "\", deg=" + isDeg + ")");
        double result = evaluate(expr, isDeg);
        System.out.println("[Serveur] résultat = " + result);
        return result;
    }

    // Parser interne pour évaluer les expressions
    private double evaluate(String expr, boolean deg) {
        expr = expr.trim().replace("π", String.valueOf(Math.PI)).replace(" ", "");
        return new ExprEvaluator(expr, deg).parse();
    }

    // Parser d'expressions
    private static class ExprEvaluator {
        private final String src;
        private int pos;
        private final boolean deg;

        ExprEvaluator(String src, boolean deg) {
            this.src = src;
            this.deg = deg;
            this.pos = 0;
        }

        double parse() { return parseExpr(); }

        private double parseExpr() {
            double v = parseTerm();
            while (pos < src.length()) {
                char c = src.charAt(pos);
                if (c == '+') { pos++; v += parseTerm(); }
                else if (c == '-') { pos++; v -= parseTerm(); }
                else break;
            }
            return v;
        }

        private double parseTerm() {
            double v = parsePow();
            while (pos < src.length()) {
                char c = src.charAt(pos);
                if (c == '*') { pos++; v *= parsePow(); }
                else if (c == '/') { pos++; double d = parsePow(); v = d == 0 ? Double.NaN : v / d; }
                else if (c == '%') { pos++; v %= parsePow(); }
                else break;
            }
            return v;
        }

        private double parsePow() {
            double v = parseUnary();
            if (pos < src.length() && src.charAt(pos) == '^') { pos++; v = Math.pow(v, parsePow()); }
            while (pos < src.length() && src.charAt(pos) == '!') { pos++; v = factorial((int)v); }
            return v;
        }

        private double parseUnary() {
            if (pos < src.length() && src.charAt(pos) == '-') { pos++; return -parsePrimary(); }
            if (pos < src.length() && src.charAt(pos) == '+') { pos++; return parsePrimary(); }
            return parsePrimary();
        }

        private double parsePrimary() {
            if (pos >= src.length()) return 0;
            char c = src.charAt(pos);

            if (Character.isDigit(c) || c == '.') return parseNumber();
            if (c == '(') { pos++; double v = parseExpr(); if (pos < src.length() && src.charAt(pos) == ')') pos++; return v; }
            if (Character.isLetter(c)) return parseNamedToken();
            return 0;
        }

        private double parseNumber() {
            int start = pos;
            while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) pos++;
            if (pos < src.length() && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
                pos++;
                if (pos < src.length() && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) pos++;
                while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
            }
            try { return Double.parseDouble(src.substring(start, pos)); } catch (Exception e) { return 0; }
        }

        private double parseNamedToken() {
            int start = pos;
            while (pos < src.length() && (Character.isLetter(src.charAt(pos)) || src.charAt(pos) == '_')) pos++;
            String name = src.substring(start, pos);

            if (name.equals("PI") || name.equals("pi") || name.equals("π")) return Math.PI;
            if (name.equals("E") || name.equals("e")) return Math.E;

            double arg = 0;
            if (pos < src.length() && src.charAt(pos) == '(') {
                pos++; arg = parseExpr();
                if (pos < src.length() && src.charAt(pos) == ')') pos++;
            }

            return switch (name.toLowerCase()) {
                case "sin"   -> Math.sin(deg ? Math.toRadians(arg) : arg);
                case "cos"   -> Math.cos(deg ? Math.toRadians(arg) : arg);
                case "tan"   -> Math.tan(deg ? Math.toRadians(arg) : arg);
                case "asin"  -> { double v = Math.asin(arg); yield deg ? Math.toDegrees(v) : v; }
                case "acos"  -> { double v = Math.acos(arg); yield deg ? Math.toDegrees(v) : v; }
                case "atan"  -> { double v = Math.atan(arg); yield deg ? Math.toDegrees(v) : v; }
                case "sinh"  -> Math.sinh(arg);
                case "cosh"  -> Math.cosh(arg);
                case "tanh"  -> Math.tanh(arg);
                case "sqrt"  -> Math.sqrt(arg);
                case "cbrt"  -> Math.cbrt(arg);
                case "abs"   -> Math.abs(arg);
                case "floor" -> Math.floor(arg);
                case "ceil"  -> Math.ceil(arg);
                case "log", "log10" -> Math.log10(arg);
                case "log2"  -> Math.log(arg) / Math.log(2);
                case "ln", "log1p" -> Math.log(arg);
                case "exp"   -> Math.exp(arg);
                default      -> arg;
            };
        }

        private static double factorial(int n) {
            n = Math.abs(n);
            if (n > 170) return Double.POSITIVE_INFINITY;
            double r = 1;
            for (int i = 2; i <= n; i++) r *= i;
            return r;
        }
    }
}