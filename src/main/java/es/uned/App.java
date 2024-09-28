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

            JOptionPane.showConfirmDialog(contentPane, panel, "", JOptionPane.OK_CANCEL_OPTION);

            currentProject = dl.createProject(Project, Org, App);
            fileExplorer.loadProject(currentProject);
            jFrame.setTitle("Nombre Proyecto: " + currentProject.getName());
        }
        if (event.getActionCommand().equals("Abrir Proyecto")) {
            currentProject = dl.openProject();
            fileExplorer.loadProject(currentProject);
            jFrame.setTitle("Nombre Proyecto: " + currentProject.getName());
        }
        if (event.getActionCommand().equals("Crear Clase")) {
            currentClass = dl.createClass();
            if (currentClass != null) {
                fileExplorer.loadProject(currentProject);
                jFrame.setTitle(jFrame.getTitle() + " / Nombre clase: " + currentClass.getName());
            }
        }
        if (event.getActionCommand().equals("Guardar")) {dl.save();}
        if (event.getActionCommand().equals("Salir")) {System.exit(0);}
        if (event.getActionCommand().equals("Deshacer")) {dl.undo();}
        if (event.getActionCommand().equals("rehacer")) {dl.redo();}
        if (event.getActionCommand().equals("Cortar")) {dl.cut();}
        if (event.getActionCommand().equals("Copiar")) {dl.copy();}
        if (event.getActionCommand().equals("Pegar")) {dl.paste();}
        if (event.getActionCommand().equals("Borrar")) {dl.delete();}
        if (event.getActionCommand().equals("Buscar")) {
            JDialog d=new JDialog(jFrame);
            d.setSize(300,100);
            d.setLayout(new FlowLayout());
            d.setVisible(true);
            JLabel lab=new JLabel("Buscar: ");
            tif=new JTextField(15);
            JButton b=new JButton("Buscar siguiente");
            b.addActionListener(this);
            d.add(lab);
            d.add(tif);
            d.add(b);
            fromIndex =0;
        }
        if (event.getActionCommand().equals("Buscar siguiente")) {fromIndex = dl.findNext(tif, fromIndex);}
        if (event.getActionCommand().equals("Remplazar")) {
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
        }

        if (event.getActionCommand().equals("Ir a")) {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            JLabel label = new JLabel("Ir a linea: ");
            panel.add(label, BorderLayout.NORTH);
            JTextField findField = new JTextField(15);
            panel.add(findField, BorderLayout.CENTER);
            int result = JOptionPane.showConfirmDialog(jFrame, panel, "", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            dl.goToLine(result, findField);
        }
        if (event.getActionCommand().equals("Seleccionar Todo")) {editor.selectAll();}
        if (event.getActionCommand().equals("Propiedades de Fichero")) {dl.fileProperties();}
        if (event.getActionCommand().equals("Aumentar Tamaño")) {dl.changeFontSize(editor.getFont().getSize() + 2);}
        if (event.getActionCommand().equals("Reducir Tamaño")) {dl.changeFontSize(editor.getFont().getSize() - 2);}
        if (event.getActionCommand().equals("Ejecutar Programa")) {dl.run(ok);}
        if (event.getSource() == ok) {dl.ok();}
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
    public void valueChanged(TreeSelectionEvent e) {
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
        Runtime rt = Runtime.getRuntime();
        Shutdown sd = new Shutdown();
        rt.addShutdownHook(new Thread(sd));
        new App();
    }
}