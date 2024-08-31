package es.uned;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;


import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.logging.Logger;
import java.util.logging.Level;


// Clase Compiler: Contiene las principales funciones del compilador del IDE.
class Compiler extends OutputStream implements Runnable {

    // Campos de la clase Compiler
    private final File file;
    private final RSyntaxTextArea editor, terminal;
    private static final Logger logger = Logger.getLogger(Compiler.class.getName());


    // Constructor de la clase Compiler
    public Compiler(File file, RSyntaxTextArea editor, RSyntaxTextArea terminal) {
        this.file = file;
        this.editor = editor;
        this.terminal = terminal;
    }


    // Métodos de la clase Compiler
    // Este método se encarga de crear una copia del fichero en el path del programa si el nombre de este y el de la clase que contiene coinciden.
    private File getFile(int offsetP, String search, String tfText) {
        String fileNameWithoutExtension = file.getName().replace(".java", "");
        String className = getClassName(offsetP, search, tfText);
        if (fileNameWithoutExtension.equals(className)) {return new File(className + ".java");}
        else {return null;}
    }
    // Este método se encarga de obtener el nombre de la clase que contiene nuestro fichero.
    private String getClassName(int offsetP, String search, String tfText) {
        int start = offsetP + search.length();
        int end = tfText.indexOf(" ", start);
        if (end == -1 || tfText.indexOf("{", start) < end) {end = tfText.indexOf("{", start);}
        if (end != -1) {return tfText.substring(start, end).trim();}
        else {return "Test";}
    }
    // Este método redirige la salida de la clase Compiler a la terminal del IDE.
    @Override
    public void write(int b) {SwingUtilities.invokeLater(() -> terminal.append(String.valueOf((char) b)));}

    public void run() {
        try {
            PrintStream out = new PrintStream(this);
            System.setOut(out);

            String tfText = editor.getText();
            String search = "public class ";
            int offsetP = tfText.indexOf(search);

            if (offsetP != -1) {
                File javaFile = getFile(offsetP, search, tfText);

                if (javaFile == null) {System.out.println("Error: El nombre del archivo no coincide con el nombre de la clase");}

                assert javaFile != null;
                try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(javaFile.toPath()))) {writer.print(tfText);}

                ProcessBuilder pb = new ProcessBuilder("javac", javaFile.getName());
                pb.directory(new File("."));
                pb.redirectErrorStream(true);
                Process p = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {System.out.println(line);}
                }

                int exitCode = p.waitFor();
                if (exitCode == 0) {System.out.println("Compilacion completada con exito.\n");}
                else {System.out.println("Error de compilacion.\n");}
            } else {System.out.println("Error: No se ha encontrado una clase publica definida.\n");}
        } catch (Exception e) {logger.log(Level.SEVERE, "Error durante la compilacion.", e);}
    }
}
