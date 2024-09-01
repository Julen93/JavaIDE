package es.uned;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


// Clase Shutdown: Contiene el código que se va a ejecutar a modo de hook cuando la aplicación finalice.
public class Shutdown implements Runnable {


    // Campos de la clase Shutdown
    private static final Logger logger = Logger.getLogger(Shutdown.class.getName());


    // Métodos de la clase Shutdown
    // Este método se encarga de ejecutar comandos de terminal dentro de la clase Shutdown.
    private void executeCommand(String... command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();
        Process process = processBuilder.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.WARNING, "Process interrupted", e);
        }
    }
    // Método principal de la clase. Se encarga de ejecutar el shutdown del IDE y sus funciones.
    @Override
    public void run() {
        try {
            executeCommand("cmd", "/c", "del *.class");
            executeCommand("cmd", "/c", "del Test.java");
        } catch (IOException e) {logger.log(Level.SEVERE, "An error occurred while executing commands", e);}
    }
}
