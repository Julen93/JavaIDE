package es.uned;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.CaretStyle;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App implements ActionListener, ListSelectionListener, CaretListener, KeyListener {
    JFrame jFrame;
    JPanel contentPane;
    RSyntaxTextArea editor, terminal;
    private static final Logger logger = Logger.getLogger(App.class.getName());
    JLabel status;
    JButton ok;
    DomainLogic dl;
    private JTextField tif;
    private int fromIndex;

    public App() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Registrar la excepción con un mensaje
            logger.log(Level.SEVERE, "An error occurred", e);
        }

        // Crear JFrame
        jFrame = new JFrame("App");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Crear JPanel
        contentPane = new JPanel(new BorderLayout());
        contentPane.setPreferredSize(new Dimension(800, 500));
        editor = new RSyntaxTextArea(20, 60);
        editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        editor.setCodeFoldingEnabled(true);
        editor.addCaretListener(this);
        editor.addKeyListener(this);
        editor.setHighlightCurrentLine(false);  // Deshabilita el resaltado de la línea actual
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            theme.apply(editor);
        } catch (IOException e) {
            //e.printStackTrace();
        }

        contentPane.add(new RTextScrollPane(editor));
        //Configurar terminal
        terminal = new RSyntaxTextArea();
        terminal.setFont(new Font("Monospaced", Font.PLAIN, 14));  // Fuente monospaced
        terminal.setBackground(Color.BLACK);  // Fondo negro
        terminal.setForeground(Color.GREEN);  // Texto verde
        terminal.setCaretColor(Color.WHITE);  // Color del cursor
        //terminal.setEditable(false);  // Deshabilitar edición directa

        // Configurar el caret (cursor) como bloque, si es posible
        terminal.getCaret().setVisible(true);
        terminal.setCaretColor(Color.GREEN);  // Cambia el color del cursor a verde
        terminal.getCaret().setBlinkRate(500); // Rate del parpadeo del cursor
        terminal.setCaretStyle(RSyntaxTextArea.INSERT_MODE, CaretStyle.BLOCK_STYLE); // Cursor como bloque
        terminal.setHighlightCurrentLine(false);  // Deshabilita el resaltado de la línea actual

        // Autoscroll: Cada vez que se añade texto, hacer scroll hasta el final
        terminal.append("\n");  // Añadir nueva línea si es necesario
        terminal.setCaretPosition(terminal.getDocument().getLength());  // Posicionar el caret al final

        // Desactivar envoltura de línea (opcional, depende de la terminal)
        terminal.setLineWrap(false);

        // Desactivar opciones contextuales que no se usarán en una terminal
        terminal.setPopupMenu(null);

        // Crear un JScrollPane (usando RTextScrollPane) para la terminal
        RTextScrollPane terminalScrollPane = new RTextScrollPane(terminal);

        // Desactivar el marcador de línea en la terminal
        terminalScrollPane.setLineNumbersEnabled(false);

        // Crear JSplitPane
        JSplitPane jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, contentPane, terminalScrollPane);
        ok = new JButton("OK");
        ok.addActionListener(this);

        // Crear JMenuBar
        JMenuBar menuBar = new JMenuBar();

        String[] fileMenuItemName = {"New", "Open", "Save", "Save As", "Exit"};
        String[] editMenuItemName = {"Undo", "Redo", "Cut", "Copy", "Paste", "Delete", "Find", "Replace", "Go To", "Select All", "File Properties"};
        String[] fontMenuItemName = {"Increase Size", "Decrease Size"};
        String[] toolsMenuItemName = {"Run"};
        int[] fileKeyEvent = {KeyEvent.VK_N, KeyEvent.VK_O, KeyEvent.VK_S, KeyEvent.VK_W, KeyEvent.VK_Q};
        int[] fontKeyEvent = {KeyEvent.VK_I, KeyEvent.VK_D};
        int[] editKeyEvent = {KeyEvent.VK_Z, KeyEvent.VK_Y, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_V, KeyEvent.VK_DELETE, KeyEvent.VK_F, KeyEvent.VK_R, KeyEvent.VK_G, KeyEvent.VK_A, KeyEvent.VK_P};
        int[] toolsKeyEvent = {KeyEvent.VK_F9};
        int accShortcut = InputEvent.CTRL_DOWN_MASK; // Usar la máscara correcta
        JMenu file = new JMenu("File");
        JMenu edit = new JMenu("Edit");
        JMenu font = new JMenu("Font");
        JMenu tools = new JMenu("Tools");
        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(font);
        menuBar.add(tools);

        editor.setEditable(true);
        JMenuItem[] fileMenuItem = new JMenuItem[fileMenuItemName.length];
        file.setMnemonic(KeyEvent.VK_F);

        JMenuItem[] editMenuItem = new JMenuItem[editMenuItemName.length];
        edit.setMnemonic(KeyEvent.VK_E);

        JMenuItem[] fontMenuItem = new JMenuItem[fontMenuItemName.length];
        font.setMnemonic(KeyEvent.VK_O);

        JMenuItem[] toolsMenuItem = new JMenuItem[toolsMenuItemName.length];
        tools.setMnemonic(KeyEvent.VK_T);

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

        jFrame.setJMenuBar(menuBar);
        jSplitPane.setDividerLocation(0.75);
        jFrame.add(jSplitPane, BorderLayout.CENTER);
        status = new JLabel("Ln : 1    Col : 1");
        status.setFont(new Font("Verdana", Font.PLAIN, 14));
        jFrame.add(status, BorderLayout.SOUTH);
        // Inicializar DomainLogic aquí
        dl = new DomainLogic(jFrame, contentPane, editor, terminal, status);
        dl.changeFontSize(editor.getFont().getSize() + 2);
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
        jFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);  // Maximizar el JFrame
        jFrame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("New")) {
            jFrame.setVisible(false);
            new App();
        }
        if (event.getActionCommand().equals("Open"))
            dl.open();
        if (event.getActionCommand().equals("Save"))
            dl.save();
        if (event.getActionCommand().equals("Save As"))
            dl.saveAs();
        if (event.getActionCommand().equals("Exit"))
            System.exit(0);
        if (event.getActionCommand().equals("Undo"))
            dl.undo();
        if (event.getActionCommand().equals("Redo"))
            dl.redo();
        if (event.getActionCommand().equals("Cut"))
            dl.cut();
        if (event.getActionCommand().equals("Copy"))
            dl.copy();
        if (event.getActionCommand().equals("Paste")) {
            dl.paste();
        }
        if (event.getActionCommand().equals("Delete"))
            dl.delete();
        if (event.getActionCommand().equals("Find")) {
            JDialog d=new JDialog(jFrame,"find");
            d.setSize(300,100);
            d.setLayout(new FlowLayout());
            d.setVisible(true);
            JLabel lab=new JLabel("Find what");
            tif=new JTextField(15);
            JButton b=new JButton("Find next");
            b.addActionListener(this);
            d.add(lab);
            d.add(tif);
            d.add(b);
            fromIndex =0;
        }
        if (event.getActionCommand().equals("Find next"))
            fromIndex = dl.findNext(tif, fromIndex);
        if (event.getActionCommand().equals("Replace")) {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            JTextField findField = new JTextField(20);
            JTextField replaceField = new JTextField(20);
            panel.add(new JLabel("Find:"), BorderLayout.WEST);
            panel.add(findField, BorderLayout.CENTER);
            panel.add(new JLabel("Replace:"), BorderLayout.SOUTH);
            panel.add(replaceField, BorderLayout.EAST);
            int result = JOptionPane.showConfirmDialog(contentPane, panel, "Replace", JOptionPane.OK_CANCEL_OPTION);
            dl.replace(result, findField, replaceField);
        }
        if (event.getActionCommand().equals("Go To")) {
            // Crear un panel para organizar la etiqueta y el campo de texto
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            // Crear la etiqueta
            JLabel label = new JLabel("Go to Line:");
            panel.add(label, BorderLayout.NORTH);
            // Crear el campo de texto
            JTextField findField = new JTextField(15);
            panel.add(findField, BorderLayout.CENTER);
            // Mostrar el diálogo de confirmación con el panel personalizado
            int result = JOptionPane.showConfirmDialog(jFrame, panel, "Go to Line", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            dl.goToLine(result, findField);
        }
        if (event.getActionCommand().equals("Select All"))
            editor.selectAll();
        if (event.getActionCommand().equals("File Properties"))
            dl.fileProperties();
        if (event.getActionCommand().equals("Increase Size"))
            dl.changeFontSize(editor.getFont().getSize() + 2);
        if (event.getActionCommand().equals("Decrease Size"))
            dl.changeFontSize(editor.getFont().getSize() - 2);
        if (event.getActionCommand().equals("Run"))
            dl.run(ok);
        if (event.getSource() == ok)
            dl.ok();
    }
    @Override
    public void keyTyped (KeyEvent event){}
    @Override
    public void keyPressed (KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE || event.getKeyCode() == KeyEvent.VK_DELETE)
            dl.keyPressed();
    }
    @Override
    public void keyReleased (KeyEvent event){}
    @Override
    public void caretUpdate (CaretEvent event){dl.caretUpdate();}
    @Override
    public void valueChanged (ListSelectionEvent event) {}

    public static void main (String...args){
        // Obtener la instancia de Runtime
        Runtime rt = Runtime.getRuntime();
        // Crear una instancia de Shutdown (nuestro hook)
        Shutdown sd = new Shutdown();
        // Registrar el hook de cierre para que se ejecute cuando el proceso esté cerrando
        rt.addShutdownHook(new Thread(sd));
        // Ejecutar la aplicación principal (en este caso, una instancia de Note)
        new App();
    }
}