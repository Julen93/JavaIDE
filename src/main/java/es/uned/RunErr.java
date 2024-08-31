package es.uned;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;


import java.io.*;
import java.awt.*;
import javax.swing.*;


// Clase RunErr: Contiene el stderr del proceso que ejecuta la JVM
public class RunErr extends OutputStream implements Runnable {


    // Campos de la clase RunErr
    private final Process process;
    private final RSyntaxTextArea terminal;


    // Constructor de la clase RunErr
    public RunErr(RSyntaxTextArea terminal, Process process) {
        this.process = process;
        this.terminal = terminal;
    }


    // Métodos de la clase RunErr
    // Este método redirige la salida de la clase RunErr a la terminal del IDE.
    @Override
    public void write(int b) {SwingUtilities.invokeLater(() -> terminal.append(String.valueOf((char) b)));}
    // Método principal de la clase. Se encarga de ejecutar el stderr y sus funciones.
    @Override
    public void run() {
        try {
            PrintStream err = new PrintStream(this, true);
            System.setErr(err);
            terminal.setText("");

            try (BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = readerError.readLine()) != null) {
                    String finalLine = line;
                    SwingUtilities.invokeLater(() -> {
                        terminal.setForeground(Color.RED);
                        terminal.append(finalLine + "\n");
                    });
                }
            }
        } catch (Exception e) {SwingUtilities.invokeLater(() -> terminal.append("Error: " + e.getMessage() + "\n"));}
    }
}
