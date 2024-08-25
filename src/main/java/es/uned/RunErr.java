package es.uned;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

class RunErr extends OutputStream implements Runnable {
    private final RSyntaxTextArea terminal;
    private final Process process;

    RunErr(RSyntaxTextArea terminal, Process process) {
        this.terminal = terminal;
        this.process = process;
    }

    @Override
    public void run() {
        try {
            // Redirigir System.err a esta clase
            PrintStream err = new PrintStream(this, true, "UTF-8");
            System.setErr(err);
            terminal.setText("");  // Limpiar el área de texto antes de empezar
            try (BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = readerError.readLine()) != null) {
                    // Usar append para agregar líneas a la terminal
                    String finalLine = line;
                    SwingUtilities.invokeLater(() -> terminal.append(finalLine + "\n"));
                }
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> terminal.append("Error: " + e.getMessage() + "\n"));
        }
    }

    @Override
    public void write(int b) {
        // Escribir un solo byte en la terminal
        SwingUtilities.invokeLater(() -> terminal.append(String.valueOf((char) b)));
    }
}
