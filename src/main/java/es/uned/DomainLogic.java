package es.uned;

import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;


import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;


// Clase DomainLogic: Contiene las principales funcionalidades del IDE.
public class DomainLogic {


    // Campos de la clase DomainLogic
    private int unf;
    private File currentClass;
    private File currentProject;
    private final JLabel status;
    private String currentPackage;
    private final JPanel contentPane;
    private final RSyntaxTextArea editor, terminal;
    private final LinkedList<String> undoStack = new LinkedList<>();
    private final LinkedList<String> redoStack = new LinkedList<>();
    private static final Logger logger = Logger.getLogger(DomainLogic.class.getName());
    private final File javaIDEProjectsDir = new File(System.getProperty("user.home"), "JavaIDEProjects");





    // Constructor de la clase DomainLogic
    public DomainLogic(JPanel contentPane, RSyntaxTextArea rSTextArea, RSyntaxTextArea terminal, JLabel status) {
        this.status = status;
        this.editor = rSTextArea;
        this.terminal = terminal;
        this.contentPane = contentPane;
    }


    // Métodos de la clase DomainLogic
    public File createProject(JTextField project, JTextField org, JTextField app) {
        // Obtener los valores de los JTextField
        String projectName = project.getText();
        String organizationName = org.getText();
        currentPackage = organizationName;
        String appName = app.getText();

        // Raíz del proyecto
        String rootPath = new File(javaIDEProjectsDir, projectName).getAbsolutePath();
        File projectDir = new File(rootPath);
        currentProject = projectDir;

        // Crear directorio raíz
        if (projectDir.mkdir()) {
            System.out.println("Directorio raíz del proyecto creado: " + projectDir.getAbsolutePath());

            // Crear la estructura "src/main/java/<nombre_org>"
            String packagePath = rootPath + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + organizationName.replace('.', File.separatorChar);
            File javaPackageDir = new File(packagePath);

            // Crear directorio de paquetes
            if (javaPackageDir.mkdirs()) {
                System.out.println("Directorio de código fuente creado: " + javaPackageDir.getAbsolutePath());
            }

            // Crear el archivo .java con el nombre de la aplicación
            String javaFilePath = packagePath + File.separator + appName + ".java";
            File javaFile = new File(javaFilePath);

            // Crear archivo .java
            try {
                if (javaFile.createNewFile()) {
                    System.out.println("Archivo " + appName + ".java creado en: " + javaFile.getAbsolutePath());

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(javaFile))) {
                        writer.write("package " + organizationName + ";\n\n");
                        writer.write("public class " + appName + " {\n");
                        writer.write("    public static void main(String[] args) {\n");
                        writer.write("        System.out.println(\"Hello, World!\");\n");
                        writer.write("    }\n");
                        writer.write("}\n");
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error.", e);
            }

            // Crear el directorio "src/main/resources"
            String resourcesPath = rootPath + File.separator + "src" + File.separator + "main" + File.separator + "resources";
            File resourcesDir = new File(resourcesPath);
            if (resourcesDir.mkdirs()) {
                System.out.println("Directorio de recursos creado: " + resourcesDir.getAbsolutePath());
            }

            // Crear el directorio "src/test/java" para los tests
            String testPath = rootPath + File.separator + "src" + File.separator + "test" + File.separator + "java";
            File testDir = new File(testPath);
            if (testDir.mkdirs()) {
                System.out.println("Directorio de pruebas creado: " + testDir.getAbsolutePath());
            }

            // Crear el directorio "target/classes" para los archivos compilados (.class)
            String classesPath = rootPath + File.separator + "target" + File.separator + "classes" + File.separator + organizationName.replace('.', File.separatorChar);
            File classesDir = new File(classesPath);

            // Crear directorios de clases compiladas con la misma estructura que el paquete de Java
            if (classesDir.mkdirs()) {
                System.out.println("Directorio de clases compiladas creado con la misma estructura: " + classesDir.getAbsolutePath());
            }

            // Crear el archivo pom.xml (siempre en un proyecto Maven)
            File pomFile = new File(rootPath + File.separator + "pom.xml");
            try {
                if (pomFile.createNewFile()) {
                    System.out.println("Archivo pom.xml creado en: " + pomFile.getAbsolutePath());

                    // Escribir contenido básico en el pom.xml
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(pomFile))) {
                        writer.write("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" ");
                        writer.write("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
                        writer.write("xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 ");
                        writer.write("http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
                        writer.write("    <modelVersion>4.0.0</modelVersion>\n");
                        writer.write("    <groupId>" + organizationName + "</groupId>\n");
                        writer.write("    <artifactId>" + projectName + "</artifactId>\n");
                        writer.write("    <version>1.0-SNAPSHOT</version>\n");
                        writer.write("</project>");
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error.", e);
            }
        } else {
            System.out.println("No se pudo crear el directorio raíz del proyecto.");
        }
        editor.setText("");
        terminal.setText("");
        return projectDir;
    }



    public File openProject() { // Este método se encarga de abrir los ficheros.
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle("Abrir Proyecto");
        jfc.setCurrentDirectory(javaIDEProjectsDir);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        // Deshabilitar el comportamiento predeterminado de abrir directorios con doble clic
        jfc.setAcceptAllFileFilterUsed(false);
        try {
            int x = jfc.showOpenDialog(null);
            if (x == JFileChooser.APPROVE_OPTION) {
                editor.setText("");
                terminal.setText("");
                currentProject = jfc.getSelectedFile();
            }
        } catch (Exception e) {logger.log(Level.SEVERE, "Error", e);}
        currentPackage = getGroupIdFromPom(String.valueOf(currentProject));
        return currentProject;
    }
    public static String getGroupIdFromPom(String projectPath) {
        // Ruta del archivo pom.xml dentro del directorio del proyecto
        String pomFilePath = projectPath + File.separator + "pom.xml";
        File pomFile = new File(pomFilePath);

        if (!pomFile.exists()) {
            System.out.println("No se encontró el archivo pom.xml en la ruta: " + pomFilePath);
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(pomFile))) {
            String line;
            boolean isGroupId = false;

            // Leer línea por línea el archivo
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Cuando encontramos la etiqueta <groupId>, nos preparamos para capturar el valor en la siguiente línea
                if (line.startsWith("<groupId>")) {
                    isGroupId = true;
                }

                // Capturamos el valor de groupId cuando lo encontramos
                if (isGroupId && line.endsWith("</groupId>")) {
                    return line.replace("<groupId>", "").replace("</groupId>", "").trim();
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error.", e);
        }

        return null;
    }
    public File createClass() {
        // Verificar que el proyecto actual esté establecido
        if (currentProject == null) {
            JOptionPane.showMessageDialog(null, "No hay un proyecto cargado. Cargue o cree un proyecto primero.");
            return null;
        } else {

            // Solicitar el nombre de la clase al usuario
            String className = JOptionPane.showInputDialog("Introduce el nombre de la nueva clase (sin extensión):");
            if (className == null || className.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "El nombre de la clase no puede estar vacío.");
                return null;
            }

            // Directorio donde se guardará la clase
            File javaFile = getFile(className);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(javaFile))) {
                // Escribir una plantilla básica para la clase Java
                writer.write("package " + currentPackage + ";");
                writer.newLine();
                writer.newLine();
                writer.write("public class " + className + " {");
                writer.newLine();
                writer.write("    public static void main(String[] args) {");
                writer.newLine();
                writer.write("        System.out.println(\"Hello, World!\");");
                writer.newLine();
                writer.write("    }");
                writer.newLine();
                writer.write("}");
                writer.newLine();

                JOptionPane.showMessageDialog(null, "Clase '" + className + ".java' creada en: " + javaFile.getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error al crear el archivo de la clase: " + e.getMessage());
            }
            currentClass = javaFile;
            return currentClass;
        }
    }

    private File getFile(String className) {
        String packagePath = "src" + File.separator + "main" + File.separator + "java";

        // Obtener el paquete de la clase para replicar la estructura del paquete dentro de "target/classes"
        String packageDir = packagePath + File.separator + currentPackage.replace('.', File.separatorChar);

        File srcDir = new File(currentProject, packageDir);


        // Crear el archivo .java
        return new File(srcDir, className + ".java");
    }

    public void printFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            editor.setText(""); // Limpiar el JTextArea antes de cargar contenido nuevo
            String line;
            while ((line = reader.readLine()) != null) {
                editor.append(line + "\n"); // Añadir cada línea al JTextArea
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error.", e);
        }
    }
    public void save() { // Este método se encarga de guardar los ficheros.
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(currentClass.getAbsolutePath()))) {
                for (int i = 0; i < editor.getLineCount(); i++) {
                    String[] text = editor.getText().split("\\n");
                    writer.write(text[i]);
                    writer.newLine();
                }
            } catch (Exception e) {logger.log(Level.SEVERE, "Error durante la operación de  escritura.", e);}
        } catch (Exception e) {logger.log(Level.SEVERE, "Error durante la operación de guardado.", e);}
    }
    public void undo () { // Este método se encarga de deshacer los cambios en los ficheros.
        if (!undoStack.isEmpty()) {
            String previousText = undoStack.pop();
            if (redoStack.size() == 20) {
                redoStack.removeFirst();
                redoStack.push(previousText);
            } else {redoStack.push(previousText);}
            editor.setText(previousText);
        }
    }
    public void redo () { // Este método se encarga de rehacer los cambios en los ficheros.
        if (!redoStack.isEmpty()) {
            String nextText = redoStack.pop();
            undoStack.push(nextText);
            editor.setText(nextText);
        }
    }
    public void cut () { // Este método se encarga de cortar la cadena de texto seleccionada del editor.
        if (undoStack.size() == 20) {
            undoStack.removeFirst();
            undoStack.push(editor.getText());
        } else {undoStack.push(editor.getText());}
        editor.cut();
    }
    // Este método se encarga de copiar la cadena de texto seleccionada del editor.
    public void copy () {editor.copy();}
    public void paste() { // Este método se encarga de pegar la cadena de texto seleccionada en el editor.
        if (undoStack.size() == 20) {
            undoStack.removeFirst();
            undoStack.push(editor.getText());
        } else {undoStack.push(editor.getText());}
        editor.paste();
    }
    public void delete() { // Este método se encarga de borrar la cadena de texto seleccionada del editor.
        if (undoStack.size() == 20) {
            undoStack.removeFirst();
            undoStack.push(editor.getText());
        } else {undoStack.push(editor.getText());}
        editor.replaceSelection("");
    }
    // Este método se encarga de buscar la siguiente cadena dentro del editor que coincida con la cadena de texto introducida por el usuario.
    public int findNext(JTextField tif, int fromIndex) {
        try {
            String search=tif.getText();
            int offset=editor.getText().indexOf(search,fromIndex);
            String str=editor.getText().substring(offset,offset+search.length());
            if(search.equals(str)) {editor.select(offset,offset+search.length());}
            fromIndex = offset+search.length();
        } catch (Exception ignored) {}
        return fromIndex;
    }
    // Este método se encarga de buscar y reemplazar todas las cadenas del editor que coincidan con la cadena de texto introducida por el usuario.
    public void replace(int result,  JTextField findField,  JTextField replaceField) {
        if (undoStack.size() == 20) {
            undoStack.removeFirst();
            undoStack.push(editor.getText());
        } else {undoStack.push(editor.getText());}
        if (result == JOptionPane.OK_OPTION) {
            SearchContext context = new SearchContext();
            context.setSearchFor(findField.getText());
            context.setReplaceWith(replaceField.getText());
            context.setMatchCase(false);
            context.setRegularExpression(false);
            context.setSearchForward(true);
            SearchResult res = SearchEngine.replaceAll(editor, context);
            JOptionPane.showMessageDialog(contentPane, res.getCount() + " coincidencias reemplazadas.");
        }
    }
    public void goToLine(int result, JTextField findField) { // Este método se encarga de desplazar al usuario a la línea deseada.
        if (result == JOptionPane.OK_OPTION) {
            String lineStr = findField.getText();
            if (lineStr != null) {
                try {
                    int line = Integer.parseInt(lineStr);
                    int totalLines = editor.getDocument().getDefaultRootElement().getElementCount();

                    if (line > 0 && line <= totalLines) {editor.setCaretPosition(editor.getDocument().getDefaultRootElement().getElement(line - 1).getStartOffset());}
                    else {JOptionPane.showMessageDialog(contentPane, "Invalid line number. Please enter a number between 1 and " + totalLines + ".");}
                } catch (NumberFormatException e) {JOptionPane.showMessageDialog(contentPane, "Invalid input. Please enter a valid line number.");
                } catch (Exception e) {JOptionPane.showMessageDialog(contentPane, "An error occurred. Please try again.");}
            }
        }
    }
    public void fileProperties() { // Este método muestra algunas propiedades del fichero abierto en el editor.
        int lineCount = editor.getLineCount();
        int wordCount = editor.getText().split("\\s+").length;
        int charCount = editor.getText().length();
        String message = String.format("Líneas: %d\nPalabras: %d\nCaracteres: %d", lineCount, wordCount, charCount);
        JOptionPane.showMessageDialog(contentPane, message, "Propiedades de Fichero", JOptionPane.INFORMATION_MESSAGE);
    }
    public void changeFontSize(int newSize) {
        Font currentFont = editor.getFont();
        Font newFont = new Font(currentFont.getFontName(), currentFont.getStyle(), newSize);
        editor.setFont(newFont);
        terminal.setFont(newFont);
    }
    public void run(JDialog run_f, JTextField run_class) { // Este método se encarga de llamar a la JVM para ejecutar el programa proporcionado por el usuario.
        try {
            String className = run_class.getText();
            if (className.isEmpty()) {JOptionPane.showMessageDialog(null, "El nombre de la clase no puede ser vacío.");}

            run_f.setVisible(false);

            // Directorio de salida para los archivos compilados dentro de "target/classes"
            File classesDir = new File(currentProject, "target/classes");
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", classesDir.getAbsolutePath(), currentPackage+"."+className);

            Process p = processBuilder.start();

            RunOut runOut = new RunOut(terminal, p);
            RunErr runErr = new RunErr(terminal, p);
            RunIn runIn = new RunIn(terminal, p);

            Thread tr = new Thread(runOut, "runOut");
            Thread tr1 = new Thread(runErr, "runErr");
            Thread tr2 = new Thread(runIn, "runIn");

            tr.start();
            tr1.start();
            tr2.start();
        } catch (IOException e) {logger.log(Level.SEVERE, "Error en la ejecución del programa.", e);}
    }
    public void caretUpdate (){ // Método encargado de manejar eventos relacionados con el cursor.
        int caretPos;
        int lineNum=1;
        int columnNum=1;
        try{
            caretPos = editor.getCaretPosition();
            lineNum = editor.getLineOfOffset(caretPos);
            columnNum = caretPos - editor.getLineStartOffset(lineNum);
        }catch(Exception e) {logger.log(Level.SEVERE, "Error.", e);}
        terminal.setText("");
        terminal.setForeground(Color.GREEN);
        if (currentClass.getName().endsWith(".java")) {
            // Compilación en un hilo separado
            Compiler c = new Compiler(currentProject, currentPackage, currentClass, editor, terminal);
            Thread t = new Thread(c, "compilar");
            t.start();
        }
        status.setText("Ln : " + lineNum + "    Col : " + columnNum);
    }
    public void valueChanged(File currentClass) {
        this.currentClass = currentClass;
        // Verificar si es un archivo .java
        if (currentClass.getName().endsWith(".java")) {
            System.out.println("Archivo .java seleccionado: " + currentClass.getAbsolutePath());
            printFile(currentClass);
            // Compilación en un hilo separado
            Compiler c = new Compiler(currentProject, currentPackage, currentClass, editor, terminal);
            Thread t = new Thread(c, "compilar");
            t.start();
        } else {
            printFile(currentClass);
        }
    }
    public void keyPressed() { // Método encargado de ejecutarse cuando las teclas suprimir o backspace son presionadas.
        if (unf == 0) {
            if (undoStack.size() == 20) {
                undoStack.removeFirst();
                undoStack.push(editor.getText());
            } else {undoStack.push(editor.getText());}
            unf = 1;
        } else {unf = 0;}
    }
}
