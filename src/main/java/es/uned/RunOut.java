package es.uned;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;


import java.io.*;
import javax.swing.*;


// Clase RunOut: Contiene el stdout del proceso que ejecuta la JVM
public class RunOut extends OutputStream implements Runnable {


    // Campos de la clase RunOut
    private final Process process;
    private final RSyntaxTextArea terminal;


    // Constructor de la clase RunOut
    public RunOut(RSyntaxTextArea terminal, Process process) {
        this.process = process;
        this.terminal = terminal;
    }


    // Métodos de la clase RunOut
    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Enviar la salida al terminal
                String finalLine = line;
                SwingUtilities.invokeLater(() -> terminal.append(finalLine + "\n"));
            }
        } catch (IOException e) {
            // Registrar la excepción o mostrar un mensaje en la interfaz
            SwingUtilities.invokeLater(() -> terminal.append("Error: " + e.getMessage() + "\n"));
        }
    }

    @Override
    public void write(int b) {
        // Redirigir el carácter al RSyntaxTextArea
        SwingUtilities.invokeLater(() -> terminal.append(String.valueOf((char) b)));
    }
}
