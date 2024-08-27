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
    private File file;
    private int flag=0;
    private String path;
    private JDialog run_f;
    private int flagSave=0;
    private final JFrame jFrame;
    private final JLabel status;
    private JTextField run_class;
    private final JPanel contentPane;
    private final RSyntaxTextArea editor, terminal;
    private final LinkedList<String> undoStack = new LinkedList<>();
    private final LinkedList<String> redoStack = new LinkedList<>();
    private static final Logger logger = Logger.getLogger(DomainLogic.class.getName());


    // Constructor de la clase DomainLogic
    public DomainLogic(JFrame jFrame, JPanel contentPane, RSyntaxTextArea rSTextArea, RSyntaxTextArea terminal, JLabel status) {
        this.jFrame = jFrame;
        this.status = status;
        this.editor = rSTextArea;
        this.terminal = terminal;
        this.contentPane = contentPane;
    }


    // Métodos de la clase DomainLogic
    public void open() {
        JFileChooser jfc = new JFileChooser();
        try {
            int x = jfc.showOpenDialog(null);
            if (flag == 1) {
                editor.setText("");
            }
            if (x == JFileChooser.APPROVE_OPTION) {
                editor.setText("");
                file = jfc.getSelectedFile();
                path = file.getPath();
                try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
                    String s;
                    boolean firstLine = true;
                    while ((s = reader.readLine()) != null) {
                        if (!firstLine) {
                            editor.append("\n"); // Agrega el salto de línea antes de agregar la siguiente línea
                        }
                        editor.append(s);
                        firstLine = false;
                    }
                }
                jFrame.setTitle(file.getName());
                flag = 1;
                flagSave = 1;
                // Compilación en un hilo separado
                Compiler c = new Compiler(file, editor, terminal);
                Thread t = new Thread(c, "compile");
                t.start();
            }
        } catch (Exception e) {
            // Registrar la excepción con un mensaje
            logger.log(Level.SEVERE, "An error occurred", e);
        }
    }
    public void save() {
        try {
            if (flagSave == 0) {
                JFileChooser jfc = new JFileChooser();
                int x = jfc.showSaveDialog(null);
                if (x == JFileChooser.APPROVE_OPTION) {
                    try {
                        save2(jfc);
                    } catch (Exception e) {
                        // Registrar la excepción con un mensaje
                        logger.log(Level.SEVERE, "An error occurred during save", e);
                    }
                }
            } else {
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(this.path))) {
                    for (int i = 0; i < editor.getLineCount(); i++) {
                        String[] text = editor.getText().split("\\n");
                        writer.write(text[i]);
                        writer.newLine(); // Añadir una nueva línea después de cada línea escrita
                    }
                } catch (Exception e) {
                    // Registrar la excepción con un mensaje
                    logger.log(Level.SEVERE, "An error occurred during file write", e);
                }
            }
        } catch (Exception e) {
            // Registrar la excepción con un mensaje
            logger.log(Level.SEVERE, "An error occurred during save", e);
        }
    }
    public void saveAs() {
        JFileChooser jfc=new JFileChooser();
        jfc.setDialogTitle("Save as");
        int x=jfc.showSaveDialog(null);
        if(x==JFileChooser.APPROVE_OPTION)
        {
            try {
                save2(jfc);
            }catch(Exception er){System.out.println();}
        }
    }
    private void save2(JFileChooser jfc) {
        try {
            // Obtener el archivo seleccionado
            File file = jfc.getSelectedFile();
            String path = file.getPath();

            // Verificar si el archivo tiene la extensión .java, y agregarla si es necesario
            if (!path.toLowerCase().endsWith(".java")) {
                path += ".java";
                file = new File(path);
            }

            // Crear el archivo si no existe
            if (!file.exists()) {
                boolean created = file.createNewFile(); // Crear el archivo si no existe
                if (!created) {
                    logger.log(Level.SEVERE, "No se pudo crear el archivo.");
                    return; // Terminar si no se pudo crear el archivo
                }
            }

            // Preparar el flujo de salida para el archivo
            try (PrintStream ps = new PrintStream(Files.newOutputStream(file.toPath()))) {
                // Obtener el texto del editor y dividirlo en líneas
                String[] textLines = editor.getText().split("\\n");

                // Imprimir las líneas al flujo de salida
                for (String line : textLines) {
                    ps.println(line);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error al escribir el archivo", e);
            }

            // Actualizar el título del JFrame
            jFrame.setTitle(file.getName());

            // Actualizar la bandera de guardado
            flagSave = 1;

            // Crear y lanzar el hilo del compilador
            Compiler c = new Compiler(file, editor, terminal);
            Thread t = new Thread(c, "compile");
            t.start();
        } catch (Exception e) {
            // Registrar cualquier excepción no manejada
            logger.log(Level.SEVERE, "Error en el método", e);
        }
    }
    public void undo () {
        if (!undoStack.isEmpty()) {
            String previousText = undoStack.pop(); // Recupera el último estado guardado
            if (redoStack.size() == 5) {
                redoStack.removeFirst();
                redoStack.push(previousText); // Guarda el estado actual para poder rehacerlo
            } else
                redoStack.push(previousText); // Guarda el estado actual para poder rehacerlo
            editor.setText(previousText);
        }
    }
    public void redo () {
        if (!redoStack.isEmpty()) {
            String nextText = redoStack.pop(); // Recupera el último estado "rehacer"
            undoStack.push(nextText); // Guarda el estado actual para poder deshacerlo de nuevo
            editor.setText(nextText);
        }
    }
    public void cut () {
        if (undoStack.size() == 50) {
            undoStack.removeFirst();
            undoStack.push(editor.getText()); // Guarda el estado actual para poder deshacerlo de nuevo
        } else
            undoStack.push(editor.getText()); // Guarda el estado actual para poder deshacerlo de nuevo
        editor.cut();
    }
    public void copy () {
        editor.copy();
    }
    public void paste() {
        if (undoStack.size() == 50) {
            undoStack.removeFirst();
            undoStack.push(editor.getText()); // Guarda el estado actual para poder deshacerlo de nuevo
        } else
            undoStack.push(editor.getText()); // Guarda el estado actual para poder deshacerlo de nuevo
        editor.paste();
    }
    public void delete() {
        if (undoStack.size() == 5) {
            undoStack.removeFirst();
            undoStack.push(editor.getText()); // Guarda el estado actual para poder deshacerlo de nuevo
        } else
            undoStack.push(editor.getText()); // Guarda el estado actual para poder deshacerlo de nuevo
        editor.replaceSelection("");
    }
    public int findNext(JTextField tif, int fromIndex) {
        try{
            String search=tif.getText();
            int offset=editor.getText().indexOf(search,fromIndex);
            String str=editor.getText().substring(offset,offset+search.length());
            if(search.equals(str))
                editor.select(offset,offset+search.length());  // One less. ie.select(0,4) will select from 0 to 3.
            fromIndex = offset+search.length();
        }catch(Exception ignored){}
        return fromIndex;
    }
    public void replace(int result,  JTextField findField,  JTextField replaceField) {
        if (result == JOptionPane.OK_OPTION) {
            SearchContext context = new SearchContext();
            context.setSearchFor(findField.getText());
            context.setReplaceWith(replaceField.getText());
            context.setMatchCase(false);
            context.setRegularExpression(false);
            context.setSearchForward(true);
            SearchResult res = SearchEngine.replaceAll(editor, context);
            JOptionPane.showMessageDialog(contentPane, res.getCount() + " occurrences replaced.");
        }
    }
    public void goToLine(int result, JTextField findField) {
        // Si el usuario presionó OK, obtener el valor del campo de texto
        if (result == JOptionPane.OK_OPTION) {
            String lineStr = findField.getText();
            if (lineStr != null) {
                try {
                    int line = Integer.parseInt(lineStr);
                    int totalLines = editor.getDocument().getDefaultRootElement().getElementCount();

                    if (line > 0 && line <= totalLines) {
                        editor.setCaretPosition(editor.getDocument().getDefaultRootElement().getElement(line - 1).getStartOffset());
                    } else {
                        JOptionPane.showMessageDialog(contentPane, "Invalid line number. Please enter a number between 1 and " + totalLines + ".");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(contentPane, "Invalid input. Please enter a valid line number.");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(contentPane, "An error occurred. Please try again.");
                }
            }
        }
    }
    public void fileProperties() {
        int lineCount = editor.getLineCount();
        int wordCount = editor.getText().split("\\s+").length;
        int charCount = editor.getText().length();
        String message = String.format("Lines: %d\nWords: %d\nCharacters: %d", lineCount, wordCount, charCount);
        JOptionPane.showMessageDialog(contentPane, message, "File Properties", JOptionPane.INFORMATION_MESSAGE);
    }
    public void changeFontSize(int newSize) {
        Font currentFont = editor.getFont();
        Font newFont = new Font(currentFont.getFontName(), currentFont.getStyle(), newSize);
        editor.setFont(newFont);
        terminal.setFont(newFont);
    }

    public void run(JButton ok) {
        try {
            //Compiler c=new Compiler(file, editor, terminal);
            //Thread t1=new Thread(c,"compile");
            //t1.start();
            // Configurar la interfaz de usuario para ingresar el nombre de la clase
            JLabel lab = new JLabel("Enter the name of class containing main() ");
            run_f = new JDialog();
            run_f.setLayout(new BorderLayout());
            run_class = new JTextField(15);
            run_f.add(lab, BorderLayout.NORTH);
            run_f.add(run_class, BorderLayout.CENTER);
            run_f.add(ok, BorderLayout.SOUTH);
            run_f.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            run_f.pack();
            run_f.setVisible(true);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in run method", e);
        }
    }

    public void ok() {
        try {
            // Obtener el nombre de la clase desde el campo de texto
            String className = run_class.getText();
            if (className.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Class name cannot be empty.");
                return;
            }

            // Configurar el comando para ejecutar el programa Java
            run_f.setVisible(false);
            ProcessBuilder processBuilder = new ProcessBuilder("java", className);
            Process p = processBuilder.start();

            // Inicializar los manejadores de entrada y salida
            RunOut runOut = new RunOut(terminal, p);
            RunErr runErr = new RunErr(terminal, p);
            RunIn runIn = new RunIn(terminal, p);

            // Iniciar hilos para manejar la entrada, salida y errores
            Thread tr = new Thread(runOut, "runA");
            Thread tr1 = new Thread(runErr, "runB");
            Thread tr2 = new Thread(runIn, "runC");
            tr.start();
            tr1.start();
            tr2.start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error executing the Java process", e);
        }
    }
    public void caretUpdate (){
        int caretPos;
        int lineNum=1;
        int columnNum=1;
        try{
            caretPos = editor.getCaretPosition();
            lineNum = editor.getLineOfOffset(caretPos);
            columnNum = caretPos - editor.getLineStartOffset(lineNum);
        }catch(Exception e){
            // Registrar la excepción con un mensaje
            logger.log(Level.SEVERE, "An error occurred", e);
        }
        terminal.setText("");
        terminal.setForeground(Color.GREEN);
        Compiler c=new Compiler(file, editor, terminal);
        Thread t=new Thread(c,"compile");
        t.start();
        status.setText("Ln : " + lineNum + "    Col : " + columnNum);
    }
    public void keyPressed() {
        if (unf == 0) {
            if (undoStack.size() == 5) {
                undoStack.removeFirst();
                undoStack.push(editor.getText()); // Guarda el estado actual para poder deshacerlo de nuevo
            } else
                undoStack.push(editor.getText()); // Guarda el estado actual para poder deshacerlo de nuevo
            unf = 1;
        } else
            unf = 0;
    }
}
