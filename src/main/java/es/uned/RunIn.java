package es.uned;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

// stdin
class RunIn implements Runnable, KeyListener {
    private String input = "";
    private final RSyntaxTextArea terminal;
    private final Process process;

    RunIn(RSyntaxTextArea terminal, Process process) {
        this.terminal = terminal;
        this.process = process;
    }

    public void run() {
        try {
            terminal.setText("");
            terminal.addKeyListener(this);
        } catch (Exception e2) {
            SwingUtilities.invokeLater(() -> terminal.append("Error: " + e2.getMessage() + "\n"));
        }
    }

    public void keyReleased(KeyEvent e) {
        try {
            char chr = e.getKeyChar();
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    pw.println(input);
                    pw.flush();
                    input = "";
                } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && !input.isEmpty()) {
                    input = input.substring(0, input.length() - 1);
                } else if (e.getKeyCode() >= 32 && e.getKeyCode() <= 126) {  // Solo caracteres imprimibles
                    if (!(e.getKeyCode() >= 37 && e.getKeyCode() <= 40)) {  // Ignorar flechas de dirección
                        input += chr;
                    }
                }
            }
        } catch (Exception e12) {
            SwingUtilities.invokeLater(() -> terminal.append("Input is not allowed!!\n"));
        }
    }

    public void keyTyped(KeyEvent e) {
        // No implementation needed
    }

    public void keyPressed(KeyEvent e) {
        // No implementation needed
    }
}
