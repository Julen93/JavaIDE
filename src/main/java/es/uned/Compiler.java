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
    private final File currentClass;
    private final RSyntaxTextArea editor, terminal;
    private static final Logger logger = Logger.getLogger(Compiler.class.getName());
    private final File currentProject;
    private final String currentPackage;

    // Constructor de la clase Compiler
    public Compiler(File currentProject, String currentPackage, File currentClass, RSyntaxTextArea editor, RSyntaxTextArea terminal) {
        this.currentClass = currentClass;
        this.editor = editor;
        this.terminal = terminal;
        this.currentProject = currentProject;
        this.currentPackage = currentPackage;
    }


    // Obtener el nombre del paquete desde el código fuente
    private String getPackageName(int offsetP, String search, String tfText) {
        int start = offsetP + search.length();
        int end = tfText.indexOf(" ", start);
        if (end == -1 || tfText.indexOf(";", start) < end) {
            end = tfText.indexOf(";", start);
        }
        if (end != -1) {
            return tfText.substring(start, end).trim();
        } else {
            return ""; // Retornar una cadena vacía si no se encuentra el nombre del paquete
        }
    }
    // Este método se encarga de obtener el nombre de la clase que contiene nuestro fichero.
    private String getClassName(int offsetP, String search, String tfText) {
        int start = offsetP + search.length();
        int end = tfText.indexOf(" ", start);
        if (end == -1 || tfText.indexOf("{", start) < end) {
            end = tfText.indexOf("{", start);
        }
        if (end != -1) {
            return tfText.substring(start, end).trim();
        } else {
            return "";
        }
    }
    // Este método se encarga de comprobar si el nombre del fichero y el del paquete en el que se encuentra aparecen en la clase.
    private File checkFile(int offsetP1, int offsetP2, String search1, String search2, String tfText) {
        String packageName = getPackageName(offsetP1, search1, tfText);
        String fileNameWithoutExtension = currentClass.getName().replace(".java", "");
        String className = getClassName(offsetP2, search2, tfText);

        if (currentPackage.equals(packageName)) {
            if (fileNameWithoutExtension.equals(className)) {
                return currentClass;
            } else {
                System.out.println("Error: El nombre del archivo no coincide con el nombre de la clase");
                return null;
            }
        } else {
            System.out.println("Error: El nombre del paquete en tu archivo Java no coincide con la estructura de directorios en la que esta ubicado el archivo.");
            return null;
        }
    }
    public void run() {
        try {
            PrintStream out = new PrintStream(this);
            System.setOut(out);

            String tfText = editor.getText();
            String search1 = "package ";
            int offsetP1 = tfText.indexOf(search1);
            String search2 = "public class ";
            int offsetP2 = tfText.indexOf(search2);
            if (offsetP1 != -1) {
                if (offsetP2 != -1) {
                    File javaFile = checkFile(offsetP1, offsetP2, search1, search2, tfText);

                    assert javaFile != null;
                    try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(javaFile.toPath()))) {writer.print(tfText);}

                    // Directorio de salida para los archivos compilados dentro de "target/classes"
                    Process p = getProcess(javaFile);

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        }
                    }

                    int exitCode = p.waitFor();
                    if (exitCode == 0) {
                        System.out.println("Compilacion de "+currentClass.getName()+" completada con exito.\n");
                    } else {
                        System.out.println("Error de compilacion.\n");
                        // **Eliminación del archivo class previo debido a una compilación fallida**
                        File classFile = getDotClass();
                        if (classFile.exists()) {
                            boolean delete = classFile.delete();
                            if (delete) {System.out.println("Archivo eliminado exitosamente.");
                            } else {System.err.println("Error: no se pudo eliminar el archivo.");}
                        }
                    }
                } else {
                    System.out.println("Error: No se ha encontrado una clase publica definida.\n");
                }
            } else {
                System.out.println("Error: No se ha encontrado un paquete definido.\n");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error durante la compilacion.", e);
        }
    }
    private Process getProcess(File javaFile) throws IOException {
        File classesDir = new File(currentProject, "target/classes");

        // Proceso de compilación que almacena los archivos compilados en la carpeta de paquetes dentro de "target/classes"
        ProcessBuilder pb = new ProcessBuilder("javac", "-d", classesDir.getAbsolutePath(), currentClass.getName());
        pb.directory(javaFile.getParentFile()); // Directorio del archivo .java
        pb.redirectErrorStream(true);
        return pb.start();
    }
    // Este método redirige la salida de la clase Compiler a la terminal del IDE.
    @Override
    public void write(int b) {
        SwingUtilities.invokeLater(() -> terminal.append(String.valueOf((char) b)));
    }
    private File getDotClass() {
        File classesDir = new File(currentProject, "target/classes");
        // Convertir el nombre del paquete en una ruta de directorios
        String packagePath = currentPackage.replace('.', File.separatorChar);
        // Nombre de la clase sin la extensión .java
        String className = currentClass.getName().replace(".java", "");
        // Ruta completa al archivo .class
        return new File(classesDir, packagePath + File.separator + className + ".class");
    }
}
