package es.uned;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class Shutdown implements Runnable {
    private static final Logger logger = Logger.getLogger(Shutdown.class.getName());

    @Override
    public void run() {
        try {
            // Eliminar archivos .class
            executeCommand("cmd", "/c", "del *.class");

            // Eliminar el archivo Test.java
            executeCommand("cmd", "/c", "del Test.java");

        } catch (IOException e) {
            // Registrar la excepción con un mensaje
            logger.log(Level.SEVERE, "An error occurred while executing commands", e);
        }
    }

    private void executeCommand(String... command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO(); // Utiliza la entrada, salida y error del proceso actual
        Process process = processBuilder.start();

        try {
            // Espera a que el proceso termine antes de continuar
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restablecer el estado de interrupción
            logger.log(Level.WARNING, "Process interrupted", e);
        }
    }
}
