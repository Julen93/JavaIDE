package es.uned;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Logger;
import java.util.logging.Level;

class Compiler extends OutputStream implements Runnable {
    private final File file;
    private final RSyntaxTextArea editor, terminal;
    private static final Logger logger = Logger.getLogger(Compiler.class.getName());

    Compiler(File file, RSyntaxTextArea editor, RSyntaxTextArea terminal) {
        this.file = file;
        this.editor = editor;
        this.terminal = terminal;
    }

    public void run() {
        try {
            PrintStream out = new PrintStream(this);
            System.setOut(out);

            String tfText = editor.getText();
            String search = "public class ";
            int offsetP = tfText.indexOf(search);

            if (offsetP != -1) {
                // Buscar el final del nombre de la clase
                File javaFile = getFile(offsetP, search, tfText);

                // Verificar si el nombre del archivo coincide con el nombre de la clase
                if (javaFile == null)
                    System.out.println("Error: El nombre del archivo no coincide con el nombre de la clase");

                // Escribir el archivo .java con codificación UTF-8
                assert javaFile != null;
                try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(javaFile.toPath(), StandardCharsets.UTF_8))) {
                    writer.print(tfText);
                }

                // Compilar el archivo Java
                ProcessBuilder pb = new ProcessBuilder("javac", "-encoding", "UTF-8", javaFile.getName());
                pb.directory(new File(".")); // Directorio de trabajo actual
                pb.redirectErrorStream(true);
                Process p = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }

                int exitCode = p.waitFor();
                if (exitCode == 0) {
                    System.out.println("Compilation successful.\n");
                } else {
                    System.out.println("Compilation failed.\n");
                }
            } else {
                System.out.println("Error: No 'public class' definition found.\n");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred", e);
        }
    }

    private File getFile(int offsetP, String search, String tfText) {
        String fileNameWithoutExtension = file.getName().replace(".java", "");
        String className = getClassName(offsetP, search, tfText);
        if (fileNameWithoutExtension.equals(className)) {
            return new File(className + ".java");
        } else {
            return null;
        }
    }

    private String getClassName(int offsetP, String search, String tfText) {
        int start = offsetP + search.length();
        int end = tfText.indexOf(" ", start);

        if (end == -1 || tfText.indexOf("{", start) < end) {
            end = tfText.indexOf("{", start);
        }

        if (end != -1) {
            return tfText.substring(start, end).trim();
        } else {
            return "Test";
        }
    }

    @Override
    public void write(int b) {
        SwingUtilities.invokeLater(() -> terminal.append(String.valueOf((char) b)));
    }
}
