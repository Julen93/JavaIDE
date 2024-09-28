package es.uned;


import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.io.File;

public class FileExplorer extends JPanel {

    private final JTree fileTree;
    private final DefaultTreeModel treeModel;
    private File rootDirectory;
    private final DefaultMutableTreeNode rootNode;

    public FileExplorer() {
        setLayout(new BorderLayout());

        // Inicializar el rootDirectory con un valor nulo o un directorio predeterminado
        this.rootDirectory = null;
        rootNode = new DefaultMutableTreeNode();

        // Crear el árbol de directorios vacío
        treeModel = new DefaultTreeModel(rootNode);
        fileTree = new JTree(treeModel);

        // Añadir el árbol a un scroll pane
        JScrollPane treeScrollPane = new JScrollPane(fileTree);
        add(treeScrollPane, BorderLayout.CENTER);
    }
    public JTree getFileTree(){
        return fileTree;
    }

    public void loadProject(File rootDirectory) {
        // Actualiza el directorio raíz con el nuevo que recibe como parámetro
        this.rootDirectory = rootDirectory;

        // Limpiar el nodo raíz
        rootNode.removeAllChildren();

        // Llenar el árbol de nuevo con el nuevo directorio raíz
        createTree(rootNode, this.rootDirectory);

        // Actualizar el modelo del árbol
        treeModel.reload();
    }

    // Método para llenar el árbol con los archivos y carpetas
    private void createTree(DefaultMutableTreeNode node, File file) {
        File[] files = file.listFiles();
        if (files == null) return;

        for (File f : files) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNode(f));
            node.add(childNode);
            if (f.isDirectory()) {
                createTree(childNode, f); // Recursivamente llenar los subdirectorios
            }
        }
    }

    public File getFile() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
        if (node == null) return null;

        FileNode fileNode = (FileNode) node.getUserObject();
        return fileNode.getFile();
    }

    // Clase auxiliar para representar nodos de archivos en el JTree
    private static class FileNode {
        private final File file;

        public FileNode(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        @Override
        public String toString() {
            return file.getName().isEmpty() ? file.getPath() : file.getName();
        }
    }
}