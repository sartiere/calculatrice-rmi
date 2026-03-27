import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Client RMI.
 * Ce programme se connecte au registre RMI, récupère le service distant,
 * et appelle ses méthodes comme si elles étaient locales.
 *
 * UTILISATION :
 *   - Même machine    : java Client
 *   - Autre machine   : java Client 192.168.1.X   (remplacez par l'IP du serveur)
 */
public class Client {

    private static final int PORT = 1099;
    private static final String SERVICE_NAME = "CalculService";

    public static void main(String[] args) {

        // Si un argument est passé, utiliser comme adresse du serveur
        // Sinon, utiliser localhost (même machine)
        String host = (args.length > 0) ? args[0] : "localhost";

        try {
            System.out.println("========================================");
            System.out.println("  Client RMI");
            System.out.println("  Connexion au serveur : " + host + ":" + PORT);
            System.out.println("========================================");

            // 1. Obtenir le registre RMI sur l'adresse du serveur
            Registry registry = LocateRegistry.getRegistry(host, PORT);

            // 2. Rechercher le service distant par son nom
            CalculService calculService = (CalculService) registry.lookup(SERVICE_NAME);

            System.out.println("  Connexion réussie !\n");

            // 3. Appeler les méthodes distantes (comme des méthodes normales !)
            int a = 10, b = 3;

            System.out.println("  Valeurs utilisées : a = " + a + ", b = " + b);
            System.out.println("----------------------------------------");
            System.out.println("  Addition       : " + a + " + " + b + " = " + calculService.addition(a, b));
            System.out.println("  Soustraction   : " + a + " - " + b + " = " + calculService.soustraction(a, b));
            System.out.println("  Multiplication : " + a + " * " + b + " = " + calculService.multiplication(a, b));
            System.out.println("  Division       : " + a + " / " + b + " = " + calculService.division(a, b));
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("Erreur sur le client : " + e.getMessage());
            e.printStackTrace();
        }
    }
}