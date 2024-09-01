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
    public void open() { // Este método se encarga de abrir los ficheros.
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle("Abrir Fichero");
        try {
            int x = jfc.showOpenDialog(null);
            if (flag == 1) {editor.setText("");}
            if (x == JFileChooser.APPROVE_OPTION) {
                editor.setText("");
                file = jfc.getSelectedFile();
                path = file.getPath();
                try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
                    String s;
                    boolean firstLine = true;
                    while ((s = reader.readLine()) != null) {
                        if (!firstLine) {editor.append("\n");}
                        editor.append(s);
                        firstLine = false;
                    }
                }
                jFrame.setTitle(file.getName());
                flag = 1;
                flagSave = 1;
                // Compilación en un hilo separado
                Compiler c = new Compiler(file, editor, terminal);
                Thread t = new Thread(c, "compilar");
                t.start();
            }
        } catch (Exception e) {logger.log(Level.SEVERE, "Error", e);}
    }
    public void save() { // Este método se encarga de guardar los ficheros.
        try {
            if (flagSave == 0) {saveAs();}
            else {
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(this.path))) {
                    for (int i = 0; i < editor.getLineCount(); i++) {
                        String[] text = editor.getText().split("\\n");
                        writer.write(text[i]);
                        writer.newLine();
                    }
                } catch (Exception e) {logger.log(Level.SEVERE, "Error durante la operación de  escritura.", e);}
            }
        } catch (Exception e) {logger.log(Level.SEVERE, "Error durante la operación de guardado.", e);}
    }
    public void saveAs() { // Este método se encarga de guardar los ficheros.
        JFileChooser jfc=new JFileChooser();
        jfc.setDialogTitle("Guardar Como");
        int x=jfc.showSaveDialog(null);
        if(x==JFileChooser.APPROVE_OPTION) {
            try {
                File file = jfc.getSelectedFile();
                String path = file.getPath();

                if (!path.toLowerCase().endsWith(".java")) {
                    path += ".java";
                    file = new File(path);
                }

                if (!file.exists()) {
                    boolean created = file.createNewFile();
                    if (!created) {
                        logger.log(Level.SEVERE, "No se pudo crear el archivo.");
                        return;
                    }
                }

                try (PrintStream ps = new PrintStream(Files.newOutputStream(file.toPath()))) {
                    String[] textLines = editor.getText().split("\\n");
                    for (String line : textLines) {
                        ps.println(line);
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error al escribir el archivo", e);
                }
                jFrame.setTitle(file.getName());
                flagSave = 1;

                // Compilación en un hilo separado
                Compiler c = new Compiler(file, editor, terminal);
                Thread t = new Thread(c, "compilar");
                t.start();
            } catch (Exception e) {logger.log(Level.SEVERE, "Error durante la operación de guardado.", e);}
        }
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

    public void run(JButton ok) { // Este método se encarga de recibir el nombre de la clase que se va a ejecutar.
        JLabel lab = new JLabel("Introduce el nombre de la clase que incluye el método main() ");
        run_f = new JDialog();
        run_f.setLayout(new BorderLayout());
        run_class = new JTextField(15);
        run_f.add(lab, BorderLayout.NORTH);
        run_f.add(run_class, BorderLayout.CENTER);
        run_f.add(ok, BorderLayout.SOUTH);
        run_f.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        run_f.pack();
        run_f.setVisible(true);
    }

    public void ok() { // Este método se encarga de llamar a la JVM para ejecutar el programa proporcionado por el usuario.
        try {
            String className = run_class.getText();
            if (className.isEmpty()) {JOptionPane.showMessageDialog(null, "El nombre de la clase no puede ser vacío.");}

            run_f.setVisible(false);
            ProcessBuilder processBuilder = new ProcessBuilder("java", className);
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
        // Compilación en un hilo separado
        Compiler c=new Compiler(file, editor, terminal);
        Thread t=new Thread(c,"compilar");
        t.start();
        status.setText("Ln : " + lineNum + "    Col : " + columnNum);
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
