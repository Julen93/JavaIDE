package es.uned;

import org.fife.ui.rtextarea.CaretStyle;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.*;


/**
 * This is an Integrated Development Environment for the Java programming language.
 *
 */

// Clase App: Contiene la interfaz principal del IDE.
public class App implements ActionListener, ListSelectionListener, CaretListener, KeyListener, TreeSelectionListener {


    // Campos de la clase App
    JButton ok;
    JFrame jFrame;
    JLabel status;
    DomainLogic dl ;
    JPanel contentPane;
    private int fromIndex;
    private JDialog run_f;
    private JTextField run_class;
    private JTextField tif;
    private File currentClass;
    private File currentProject;
    RSyntaxTextArea editor, terminal;
    private final FileExplorer fileExplorer;
    private static final Logger logger = Logger.getLogger(App.class.getName());


    // Constructor de la clase App
    public App() {

        // Aplicar Look and Feel para configurar la apariencia de la aplicación
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {logger.log(Level.SEVERE, "Error: setLookAndFeel.", e);}

        // Crear JFrame
        jFrame = new JFrame("Java IDE");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Configurar editor de texto.
        editor = new RSyntaxTextArea(20, 60);
        editor.addCaretListener(this);
        editor.addKeyListener(this);
        editor.setCodeFoldingEnabled(true);
        editor.setHighlightCurrentLine(false);  // Deshabilita el resaltado de la línea actual
        editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        editor.setEditable(true);
        try { // Aplicar tema oscuro al editor.
            Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            theme.apply(editor);
        } catch (IOException e) {
            // Registrar la excepción con un mensaje
            logger.log(Level.SEVERE, "Error: Aplicar tema oscuro.", e);
        }

        // Configurar JPanel que va a contener el editor de texto.
        contentPane = new JPanel(new BorderLayout());
        contentPane.setPreferredSize(new Dimension(800, 650)); //PARA CAMBIAR EL TAMAÑO DE LA TERMINAL!!!!!!
        contentPane.add(new RTextScrollPane(editor));

        // Configurar terminal
        terminal = new RSyntaxTextArea();
        terminal.setBackground(Color.BLACK);
        terminal.setForeground(Color.GREEN);
        terminal.setCaretColor(Color.WHITE);
        terminal.setFont(new Font("Monospaced", Font.PLAIN, 14));
        terminal.getCaret().setVisible(true);
        terminal.setCaretColor(Color.GREEN);
        terminal.getCaret().setBlinkRate(500);
        terminal.setCaretStyle(RSyntaxTextArea.INSERT_MODE, CaretStyle.BLOCK_STYLE);
        terminal.setHighlightCurrentLine(false);
        terminal.append("\n");
        terminal.setCaretPosition(terminal.getDocument().getLength());
        terminal.setPopupMenu(null);
        terminal.setText("¡Bienvenido al auténtico IDE para programar en Java!\nEmpecemos por crear un proyecto nuevo o abrir uno ya creado.");

        // Crear un JScrollPane (usando RTextScrollPane) para la terminal
        RTextScrollPane terminalScrollPane = new RTextScrollPane(terminal);
        terminalScrollPane.setLineNumbersEnabled(false);

        // Instancia de FileExplorer con el directorio raíz deseado
        fileExplorer = new FileExplorer();
        fileExplorer.setPreferredSize(new Dimension(200, 550));
        fileExplorer.getFileTree().addTreeSelectionListener(this);

        // Crear JSplitPane
        JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileExplorer, contentPane);
        JSplitPane jSplitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jSplitPane, terminalScrollPane);
        jSplitPane.setDividerLocation(0.75);
        jSplitPane2.setDividerLocation(0.25);


        // Configurar JMenuBar
        JMenuBar menuBar = new JMenuBar();
        String[] fileMenuItemName = {"Nuevo Proyecto", "Abrir Proyecto", "Crear Clase", "Guardar", "Salir"};
        String[] editMenuItemName = {"Deshacer", "Rehacer", "Cortar", "Copiar", "Pegar", "Borrar", "Buscar", "Remplazar", "Ir a", "Seleccionar Todo", "Propiedades de Fichero"};
        String[] fontMenuItemName = {"Aumentar Tamaño", "Reducir Tamaño"};
        String[] toolsMenuItemName = {"Ejecutar Programa"};
        int[] fileKeyEvent = {KeyEvent.VK_N, KeyEvent.VK_O, KeyEvent.VK_S, KeyEvent.VK_W, KeyEvent.VK_Q};
        int[] fontKeyEvent = {KeyEvent.VK_I, KeyEvent.VK_D};
        int[] editKeyEvent = {KeyEvent.VK_Z, KeyEvent.VK_Y, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_V, KeyEvent.VK_DELETE, KeyEvent.VK_F, KeyEvent.VK_R, KeyEvent.VK_G, KeyEvent.VK_A, KeyEvent.VK_P};
        int[] toolsKeyEvent = {KeyEvent.VK_F9};
        int accShortcut = InputEvent.CTRL_DOWN_MASK; // Usar la máscara correcta
        JMenu file = new JMenu("Fichero");
        JMenu edit = new JMenu("Edición");
        JMenu font = new JMenu("Fuente");
        JMenu tools = new JMenu("Herramientas");
        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(font);
        menuBar.add(tools);

        JLabel messageLabel = new JLabel("Pulsa la tecla F5 para refrescar el explorador del proyecto.  ");
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(messageLabel);

        JMenuItem[] fileMenuItem = new JMenuItem[fileMenuItemName.length];
        file.setMnemonic(KeyEvent.VK_F);
        JMenuItem[] editMenuItem = new JMenuItem[editMenuItemName.length];
        edit.setMnemonic(KeyEvent.VK_E);
        JMenuItem[] fontMenuItem = new JMenuItem[fontMenuItemName.length];
        font.setMnemonic(KeyEvent.VK_O);
        JMenuItem[] toolsMenuItem = new JMenuItem[toolsMenuItemName.length];
        tools.setMnemonic(KeyEvent.VK_H);

        // Agregar ítems del menú File
        for (int i = 0; i < fileMenuItemName.length; i++) {
            fileMenuItem[i] = new JMenuItem(fileMenuItemName[i], fileKeyEvent[i]);
            fileMenuItem[i].addActionListener(this);
            fileMenuItem[i].setAccelerator(KeyStroke.getKeyStroke(fileKeyEvent[i], accShortcut));
            file.add(fileMenuItem[i]);
        }

        // Agregar ítems del menú Edit
        for (int i = 0; i < editMenuItemName.length; i++) {
            editMenuItem[i] = new JMenuItem(editMenuItemName[i], editKeyEvent[i]);
            editMenuItem[i].addActionListener(this);
            editMenuItem[i].setAccelerator(KeyStroke.getKeyStroke(editKeyEvent[i], accShortcut));
            edit.add(editMenuItem[i]);
        }

        // Agregar ítems del menú Font
        for (int i = 0; i < fontMenuItemName.length; i++) {
            fontMenuItem[i] = new JMenuItem(fontMenuItemName[i], fontKeyEvent[i]);
            fontMenuItem[i].addActionListener(this);
            fontMenuItem[i].setAccelerator(KeyStroke.getKeyStroke(fontKeyEvent[i], accShortcut));
            font.add(fontMenuItem[i]);
        }

        // Agregar ítems del menú Tools
        for (int i = 0; i < toolsMenuItemName.length; i++) {
            toolsMenuItem[i] = new JMenuItem(toolsMenuItemName[i], toolsKeyEvent[0]);
            toolsMenuItem[i].addActionListener(this);
            toolsMenuItem[i].setAccelerator(KeyStroke.getKeyStroke(toolsKeyEvent[0], accShortcut)); // Usa CTRL como modificador
            tools.add(toolsMenuItem[i]);
        }

        // Configurar marcador de líneas y columnas
        status = new JLabel("Ln : 1    Col : 1");
        status.setFont(new Font("Verdana", Font.PLAIN, 14));

        // Inicializar DomainLogic
        dl = new DomainLogic(contentPane, editor, terminal, status);
        dl.printFile(new File("C:/Program Files (x86)/JavaIDE/ascii-art.txt"));
        // Crear botón de OK
        ok = new JButton("OK");
        ok.addActionListener(this);

        String userHome = System.getProperty("user.home");
        File javaIDEProjectsDir = new File(userHome, "JavaIDEProjects");
        if (!javaIDEProjectsDir.exists()) {
            if (javaIDEProjectsDir.mkdir()) {
                System.out.println("Directorio 'JavaIDEProjects' creado en: " + javaIDEProjectsDir.getAbsolutePath());
            } else {
                System.out.println("No se pudo crear el directorio 'JavaIDEProjects'.");
            }
        } else {
            System.out.println("El directorio 'JavaIDEProjects' ya existe en: " + javaIDEProjectsDir.getAbsolutePath());
        }

        // Configurar JFrame
        jFrame.setJMenuBar(menuBar);
        jFrame.add(jSplitPane2, BorderLayout.CENTER);
        jFrame.add(status, BorderLayout.SOUTH);
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
        jFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);  // Maximizar el JFrame \\
        ImageIcon image_icon = new ImageIcon("C:/Program Files (x86)/JavaIDE/LogoUNED.jpg");
        Image image=image_icon.getImage();
        jFrame.setIconImage(image);
        jFrame.setVisible(true);
    }


    // Métodos de la clase App
    @Override
    public void actionPerformed(ActionEvent event) { // Método encargado de recibir y manejar los eventos de acción.
        if (event.getActionCommand().equals("Nuevo Proyecto")) {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            JPanel labels = new JPanel(new GridLayout(3, 1, 5, 5));
            JPanel fields = new JPanel(new GridLayout(3, 1, 5, 5));

            JTextField Project = new JTextField(20);
            JTextField Org = new JTextField(20);
            JTextField App = new JTextField(20);

            labels.add(new JLabel("Proyecto: "));
            labels.add(new JLabel("Organización: "));
            labels.add(new JLabel("Aplicación: "));
            fields.add(Project);
            fields.add(Org);
            fields.add(App);

            panel.add(labels, BorderLayout.WEST);
            panel.add(fields, BorderLayout.CENTER);

            int result = JOptionPane.showConfirmDialog(contentPane, panel, "", JOptionPane.OK_CANCEL_OPTION);

            // Si el usuario hace clic en "OK" (botón OK)
            if (result == JOptionPane.OK_OPTION) {
                // Comprobar si algún campo está vacío
                if (Project.getText().trim().isEmpty() || Org.getText().trim().isEmpty() || App.getText().trim().isEmpty()) {
                    // Mostrar mensaje de error
                    JOptionPane.showMessageDialog(contentPane, "Todos los campos son obligatorios. Por favor, rellene todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Si todos los campos están completos, crear el proyecto
                    currentProject = dl.createProject(Project, Org, App);
                    fileExplorer.loadProject(currentProject);
                    jFrame.setTitle("Nombre Proyecto: " + currentProject.getName());
                }
            }
        }
        if (event.getActionCommand().equals("Abrir Proyecto")) {
            currentProject = dl.openProject();
            fileExplorer.loadProject(currentProject);
            jFrame.setTitle("Nombre Proyecto: " + currentProject.getName());
        }
        if (event.getActionCommand().equals("Crear Clase")) {
            dl.createClass();
            if (currentClass != null && currentClass.getName().endsWith(".java")) {
                dl.save();
                currentClass = null;
            }
            fileExplorer.loadProject(currentProject);

        }
        if (event.getActionCommand().equals("Guardar")) {
            if (currentProject != null) {
                if (currentClass != null) {
                    if (currentClass.getName().endsWith(".java")) {
                        dl.save();
                        JOptionPane.showMessageDialog(contentPane, "Cambios guardados correctamente.");
                    }
                } else {
                    JOptionPane.showMessageDialog(contentPane, "Error. No hay una clase cargada en el editor.");
                }
            } else {
                JOptionPane.showMessageDialog(contentPane, "Debes crear o abrir un proyecto antes de intentar guardar un programa.");
            }
        }
        if (event.getActionCommand().equals("Salir")) {
            System.exit(0);
        }
        if (event.getActionCommand().equals("Deshacer")) {
            if (currentClass != null) {
                dl.undo();
            } else {
                JOptionPane.showMessageDialog(contentPane, "Debes crear o cargar una clase.");
            }
        }
        if (event.getActionCommand().equals("rehacer")) {
            if (currentClass != null) {
                dl.redo();
            } else {
                JOptionPane.showMessageDialog(contentPane, "Debes crear o cargar una clase.");
            }
        }
        if (event.getActionCommand().equals("Cortar")) {
            if (currentClass != null) {
                dl.cut();
            } else {
                JOptionPane.showMessageDialog(contentPane, "Debes crear o cargar una clase.");
            }
        }
        if (event.getActionCommand().equals("Copiar")) {
            if (currentClass != null) {
                dl.copy();
            } else {
                JOptionPane.showMessageDialog(contentPane, "Debes crear o cargar una clase.");
            }
        }
        if (event.getActionCommand().equals("Pegar")) {
            if (currentClass != null) {
                dl.paste();
            } else {
                JOptionPane.showMessageDialog(contentPane, "Debes crear o cargar una clase.");
            }
        }
        if (event.getActionCommand().equals("Borrar")) {
            if (currentClass != null) {
                dl.delete();
            } else {
                JOptionPane.showMessageDialog(contentPane, "Debes crear o cargar una clase.");
            }
        }
        if (event.getActionCommand().equals("Buscar")) {
            if (currentClass != null) {
                JDialog d = new JDialog(jFrame);
                d.setSize(300, 100);
                d.setLayout(new FlowLayout());
                d.setVisible(true);
                JLabel lab = new JLabel("Buscar: ");
                tif = new JTextField(15);
                JButton b = new JButton("Buscar siguiente");
                b.addActionListener(this);
                d.add(lab);
                d.add(tif);
                d.add(b);
                fromIndex = 0;
            } else {
                JOptionPane.showMessageDialog(contentPane, "Debes crear o cargar una clase.");
            }
        }
        if (event.getActionCommand().equals("Buscar siguiente")) {
            fromIndex = dl.findNext(tif, fromIndex);
        }
        if (event.getActionCommand().equals("Remplazar")) {
            if (currentClass != null) {
                JPanel panel = new JPanel(new BorderLayout(5, 5));
                JPanel labels = new JPanel(new GridLayout(2, 1, 5, 5));
                JPanel fields = new JPanel(new GridLayout(2, 1, 5, 5));

                JTextField findField = new JTextField(20);
                JTextField replaceField = new JTextField(20);

                labels.add(new JLabel("Buscar: "));
                labels.add(new JLabel("Reemplazar: "));
                fields.add(findField);
                fields.add(replaceField);

                panel.add(labels, BorderLayout.WEST);
                panel.add(fields, BorderLayout.CENTER);

                int result = JOptionPane.showConfirmDialog(contentPane, panel, "", JOptionPane.OK_CANCEL_OPTION);

                dl.replace(result, findField, replaceField);
            } else {
                JOptionPane.showMessageDialog(contentPane, "Debes crear o cargar una clase.");
            }
        }

        if (event.getActionCommand().equals("Ir a")) {
            if (currentClass != null) {
                JPanel panel = new JPanel(new BorderLayout(5, 5));
                JLabel label = new JLabel("Ir a linea: ");
                panel.add(label, BorderLayout.NORTH);
                JTextField findField = new JTextField(15);
                panel.add(findField, BorderLayout.CENTER);
                int result = JOptionPane.showConfirmDialog(jFrame, panel, "", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                dl.goToLine(result, findField);
            } else {JOptionPane.showMessageDialog(contentPane, "Debes crear o cargar una clase.");}
        }
        if (event.getActionCommand().equals("Seleccionar Todo")) {
            if (currentClass != null) {
                editor.selectAll();
            } else {JOptionPane.showMessageDialog(contentPane, "Debes crear o cargar una clase.");}
        }
        if (event.getActionCommand().equals("Propiedades de Fichero")) {
            if (currentClass != null) {
                dl.fileProperties();
            } else {JOptionPane.showMessageDialog(contentPane, "Debes crear o cargar una clase.");}
        }
        if (event.getActionCommand().equals("Aumentar Tamaño")) {dl.changeFontSize(editor.getFont().getSize() + 2);}
        if (event.getActionCommand().equals("Reducir Tamaño")) {dl.changeFontSize(editor.getFont().getSize() - 2);}
        if (event.getActionCommand().equals("Ejecutar Programa")) {
            if (currentProject != null) {
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
            } else {JOptionPane.showMessageDialog(contentPane, "Debes abrir un proyecto antes de intentar ejecutar un programa.");}
        }
        if (event.getSource() == ok) {dl.run(run_f, run_class);}
    }
    @Override
    public void keyTyped (KeyEvent event){}
    @Override
    public void keyPressed (KeyEvent event) { // Método encargado de ejecutarse cuando una tecla es presionada.
        if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE || event.getKeyCode() == KeyEvent.VK_DELETE)
            dl.keyPressed();
        if (event.getKeyCode() == KeyEvent.VK_F5) {
            System.out.println("Tecla F5 presionada");
            if (currentProject != null)
                fileExplorer.loadProject(currentProject);
        }
    }
    @Override
    public void keyReleased (KeyEvent event){}
    @Override
    public void caretUpdate (CaretEvent event){
        if (currentClass != null)
            dl.caretUpdate();
    }
    @Override
    public void valueChanged (ListSelectionEvent event) {}
    @Override
    public void valueChanged(TreeSelectionEvent event) {
        File file = fileExplorer.getFile();
        if (file != null && file.isFile()) {
            jFrame.setTitle("Nombre Proyecto: " + currentProject.getName() + " / Nombre Fichero: " + file.getName());
            currentClass = file;
            dl.valueChanged(currentClass);
        } else {
            jFrame.setTitle("");
            editor.setText("");
            terminal.setText("");
            System.out.println("Carpeta seleccionada: " + (currentClass != null ? currentClass.getAbsolutePath() : "No file selected"));
            currentClass = null;
        }
    }
    public static void main (String...args){ // Método principal de la clase.
        new App();
    }
}