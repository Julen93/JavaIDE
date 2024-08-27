package es.uned;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.io.*;

// stdout
class RunOut extends OutputStream implements Runnable {
    private final RSyntaxTextArea terminal;
    private final Process process;

    RunOut(RSyntaxTextArea terminal, Process process) {
        this.terminal = terminal;
        this.process = process;
    }

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
