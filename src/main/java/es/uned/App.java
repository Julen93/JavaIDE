package es.uned;

import org.fife.ui.rtextarea.CaretStyle;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This is an Integrated Development Environment for the Java programming language.
 *
 */

// Clase App: Contiene la interfaz principal del IDE.
public class App implements ActionListener, ListSelectionListener, CaretListener, KeyListener {


    // Campos de la clase App
    JButton ok;
    JFrame jFrame;
    JLabel status;
    DomainLogic dl ;
    JPanel contentPane;
    private int fromIndex;
    private JTextField tif;
    RSyntaxTextArea editor, terminal;
    private static final Logger logger = Logger.getLogger(App.class.getName());


    // Constructor de la clase App
    public App() {

        // Aplicar Look and Feel para configurar la apariencia de la aplicación
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Registrar la excepción con un mensaje
            logger.log(Level.SEVERE, "Error: setLookAndFeel.", e);
        }

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
        contentPane.setPreferredSize(new Dimension(800, 500));
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

        // Crear un JScrollPane (usando RTextScrollPane) para la terminal
        RTextScrollPane terminalScrollPane = new RTextScrollPane(terminal);
        terminalScrollPane.setLineNumbersEnabled(false);

        // Crear JSplitPane
        JSplitPane jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, contentPane, terminalScrollPane);

        // Configurar JMenuBar
        JMenuBar menuBar = new JMenuBar();
        String[] fileMenuItemName = {"Nuevo", "Abrir", "Guardar", "Guardar Como", "Salir"};
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
        dl = new DomainLogic(jFrame, contentPane, editor, terminal, status);

        // Crear botón de OK
        ok = new JButton("OK");
        ok.addActionListener(this);

        // Configurar JFrame
        jFrame.setJMenuBar(menuBar);
        jSplitPane.setDividerLocation(0.75);
        jFrame.add(jSplitPane, BorderLayout.CENTER);
        jFrame.add(status, BorderLayout.SOUTH);
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
        jFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);  // Maximizar el JFrame
        jFrame.setVisible(true);
    }


    // Métodos de la clase App
    @Override
    public void actionPerformed(ActionEvent event) { // Método encargado de recibir y manejar los eventos de acción.
        if (event.getActionCommand().equals("Nuevo")) {
            jFrame.setVisible(false);
            new App();
        }
        if (event.getActionCommand().equals("Abrir"))
            dl.open();
        if (event.getActionCommand().equals("Guardar"))
            dl.save();
        if (event.getActionCommand().equals("Guardar Como"))
            dl.saveAs();
        if (event.getActionCommand().equals("Salir"))
            System.exit(0);
        if (event.getActionCommand().equals("Deshacer"))
            dl.undo();
        if (event.getActionCommand().equals("rehacer"))
            dl.redo();
        if (event.getActionCommand().equals("Cortar"))
            dl.cut();
        if (event.getActionCommand().equals("Copiar"))
            dl.copy();
        if (event.getActionCommand().equals("Pegar"))
            dl.paste();
        if (event.getActionCommand().equals("Borrar"))
            dl.delete();
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
        if (event.getActionCommand().equals("Buscar siguiente"))
            fromIndex = dl.findNext(tif, fromIndex);
        if (event.getActionCommand().equals("Remplazar")) {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            JTextField findField = new JTextField(20);
            JTextField replaceField = new JTextField(20);
            panel.add(new JLabel("Buscar: "), BorderLayout.WEST);
            panel.add(findField, BorderLayout.CENTER);
            panel.add(new JLabel("Remplazar: "), BorderLayout.SOUTH);
            panel.add(replaceField, BorderLayout.EAST);
            int result = JOptionPane.showConfirmDialog(contentPane, panel, "Remplazar", JOptionPane.OK_CANCEL_OPTION);
            dl.replace(result, findField, replaceField);
        }
        if (event.getActionCommand().equals("Ir a")) {
            // Crear un panel para organizar la etiqueta y el campo de texto
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            // Crear la etiqueta
            JLabel label = new JLabel("Ir a linea: ");
            panel.add(label, BorderLayout.NORTH);
            // Crear el campo de texto
            JTextField findField = new JTextField(15);
            panel.add(findField, BorderLayout.CENTER);
            // Mostrar el diálogo de confirmación con el panel personalizado
            int result = JOptionPane.showConfirmDialog(jFrame, panel, "Ir a linea", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            dl.goToLine(result, findField);
        }
        if (event.getActionCommand().equals("Seleccionar Todo"))
            editor.selectAll();
        if (event.getActionCommand().equals("Propiedades de Fichero"))
            dl.fileProperties();
        if (event.getActionCommand().equals("Aumentar Tamaño"))
            dl.changeFontSize(editor.getFont().getSize() + 2);
        if (event.getActionCommand().equals("Reducir Tamaño"))
            dl.changeFontSize(editor.getFont().getSize() - 2);
        if (event.getActionCommand().equals("Ejecutar Programa"))
            dl.run(ok);
        if (event.getSource() == ok)
            dl.ok();
    }
    @Override
    public void keyTyped (KeyEvent event){}
    @Override
    public void keyPressed (KeyEvent event) { // Método encargado de ejecutarse cuando una tecla es presionada.
        if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE || event.getKeyCode() == KeyEvent.VK_DELETE)
            dl.keyPressed();
    }
    @Override
    public void keyReleased (KeyEvent event){}
    @Override
    public void caretUpdate (CaretEvent event){dl.caretUpdate();} // Método encargado de manejar eventos relacionados con el cursor.
    @Override
    public void valueChanged (ListSelectionEvent event) {}

    public static void main (String...args){ // Método principal de la clase.
        Runtime rt = Runtime.getRuntime();
        Shutdown sd = new Shutdown();
        rt.addShutdownHook(new Thread(sd));
        new App();
    }
}