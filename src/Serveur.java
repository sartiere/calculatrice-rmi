import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Serveur RMI.
 * Ce programme crée le registre RMI, instancie le service,
 * et l'enregistre pour que les clients puissent le trouver.
 *
 * EXÉCUTION :
 *   java Serveur
 *   (ou avec une IP spécifique) : java -Djava.rmi.server.hostname=TON_IP Serveur
 */
public class Serveur {

    // Port du registre RMI (1099 est le port par défaut)
    private static final int PORT = 1099;

    // Nom sous lequel le service sera enregistré dans le registre
    private static final String SERVICE_NAME = "CalculService";

    public static void main(String[] args) {
        try {
            // 1. Créer une instance du service
            CalculService calculService = new CalculServiceImpl();

            // 2. Créer le registre RMI sur le port défini
            Registry registry = LocateRegistry.createRegistry(PORT);

            // 3. Enregistrer le service dans le registre avec un nom
            registry.bind(SERVICE_NAME, calculService);

            System.out.println("========================================");
            System.out.println("  Serveur RMI démarré avec succès !");
            System.out.println("  Port    : " + PORT);
            System.out.println("  Service : " + SERVICE_NAME);
            System.out.println("  En attente de clients...");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("Erreur sur le serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
