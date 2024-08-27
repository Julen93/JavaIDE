package es.uned;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;


import javax.swing.*;
import java.io.PrintWriter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.OutputStreamWriter;


// Clase RunIn: Contiene el stdin del proceso que ejecuta la JVM
public class RunIn implements Runnable, KeyListener {


    // Campos de la clase RunIn
    private PrintWriter pw;
    private final Process process;
    private final RSyntaxTextArea terminal;
    private final StringBuilder input = new StringBuilder();  // Usar StringBuilder para mejorar el rendimiento


    // Constructor de la clase RunIn
    public RunIn(RSyntaxTextArea terminal, Process process) {
        this.process = process;
        this.terminal = terminal;
    }


    // Métodos de la clase RunIn
    @Override
    public void run() {
        try {
            terminal.setText("");
            terminal.addKeyListener(this);
            pw = new PrintWriter(new OutputStreamWriter(process.getOutputStream()), true);
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> terminal.append("Error: " + e.getMessage() + "\n"));
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        try {
            char chr = event.getKeyChar();
            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                pw.println(input);  // Enviar la entrada completa
                pw.flush();
                input.setLength(0);  // Limpiar el StringBuilder
            } else if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE && input.length() > 0) {
                input.setLength(input.length() - 1);  // Eliminar el último carácter
            } else if (event.getKeyCode() >= 32 && event.getKeyCode() <= 126) {  // Solo caracteres imprimibles
                if (!(event.getKeyCode() >= 37 && event.getKeyCode() <= 40)) {  // Ignorar flechas de dirección
                    input.append(chr);
                }
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> terminal.append("Input is not allowed!!\n"));
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // No implementation needed
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // No implementation needed
    }
}