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
    // Este método redirige la salida de la clase RunOut a la terminal del IDE.
    @Override
    public void write(int b) {SwingUtilities.invokeLater(() -> terminal.append(String.valueOf((char) b)));}
    // Método principal de la clase. Se encarga de ejecutar el stdout y sus funciones.
    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String finalLine = line;
                SwingUtilities.invokeLater(() -> terminal.append(finalLine + "\n"));
            }
        } catch (IOException e) {SwingUtilities.invokeLater(() -> terminal.append("Error: " + e.getMessage() + "\n"));}
    }
}
