import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface du service de calcul distant.
 * Toute interface RMI doit étendre "Remote".
 * Toute méthode doit déclarer "throws RemoteException".
 */
public interface CalculService extends Remote {

    // Méthode de connexion (appelée par le client)
    String connect(String clientName) throws RemoteException;

    // Opérations arithmétiques de base
    int addition(int a, int b) throws RemoteException;
    int soustraction(int a, int b) throws RemoteException;
    int multiplication(int a, int b) throws RemoteException;
    double division(int a, int b) throws RemoteException;

    // Opérations avec des doubles
    double add(double a, double b) throws RemoteException;
    double sub(double a, double b) throws RemoteException;
    double mul(double a, double b) throws RemoteException;
    double div(double a, double b) throws RemoteException;

    // Fonctions scientifiques
    double sin(double x, boolean isDeg) throws RemoteException;
    double cos(double x, boolean isDeg) throws RemoteException;
    double tan(double x, boolean isDeg) throws RemoteException;
    double asin(double x, boolean isDeg) throws RemoteException;
    double acos(double x, boolean isDeg) throws RemoteException;
    double atan(double x, boolean isDeg) throws RemoteException;
    double sinh(double x) throws RemoteException;
    double cosh(double x) throws RemoteException;
    double tanh(double x) throws RemoteException;
    double sqrt(double x) throws RemoteException;
    double cbrt(double x) throws RemoteException;
    double abs(double x) throws RemoteException;
    double floor(double x) throws RemoteException;
    double ceil(double x) throws RemoteException;
    double log10(double x) throws RemoteException;
    double log2(double x) throws RemoteException;
    double ln(double x) throws RemoteException;
    double exp(double x) throws RemoteException;
    double pow(double base, double exponent) throws RemoteException;
    double factorial(int n) throws RemoteException;
    double percent(double value, double percent) throws RemoteException;

    // Constantes
    double getPi() throws RemoteException;
    double getE() throws RemoteException;

    // Évaluation d'expression
    double evaluateExpression(String expr, boolean isDeg) throws RemoteException;
}
