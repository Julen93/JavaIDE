package es.uned;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.awt.*;
import java.io.*;

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
            PrintStream err = new PrintStream(this, true);
            System.setErr(err);
            terminal.setText("");  // Limpiar el área de texto al inicio

            try (BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = readerError.readLine()) != null) {
                    String finalLine = line;
                    SwingUtilities.invokeLater(() -> {
                        // Guardar el color actual del texto
                        //Color originalColor = terminal.getForeground();
                        // Cambiar el color a rojo solo para el texto de error
                        terminal.setForeground(Color.RED);

                        terminal.append(finalLine + "\n");
                        // Restaurar el color original para el siguiente texto
                        //terminal.setForeground(originalColor);
                    });
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
