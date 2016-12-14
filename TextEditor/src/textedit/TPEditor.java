package textedit;		//se define el paquete donde debe estar este archivo

import java.awt.BorderLayout;    //importamos todo lo que se utilizar�
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoManager;
 
/**
 * Clase principal donde se construye la GUI del editor.
 * 
 * @author ecarrillo
 */

public class TPEditor {    //clase publica TPEditor
 
    private JFrame jFrame;            //instancia de JFrame (ventana principal)
    private JMenuBar jMenuBar;        //instancia de JMenuBar (barra de men�)
    private JToolBar jToolBar;        //instancia de JToolBar (barra de herramientas)
    private JTextArea jTextArea;      //instancia de JTextArea (area de edici�n)
    private JPopupMenu jPopupMenu;    //instancia de JPopupMenu (men� emergente)
    private JPanel statusBar;         //instancia de JPanel (barra de estado)
 
    private JCheckBoxMenuItem itemLineWrap;         //instancias de algunos items de men� que necesitan ser accesibles
    private JCheckBoxMenuItem itemShowToolBar;
    private JCheckBoxMenuItem itemFixedToolBar;
    private JCheckBoxMenuItem itemShowStatusBar;
    private JMenuItem mbItemUndo;
    private JMenuItem mbItemRedo;
    private JMenuItem mpItemUndo;
    private JMenuItem mpItemRedo;
 
    private JButton buttonUndo;    //instancias de algunos botones que necesitan ser accesibles
    private JButton buttonRedo;
 
    private JLabel sbFilePath;    //etiqueta que muestra la ubicaci�n del archivo actual
    private JLabel sbFileSize;    //etiqueta que muestra el tama�o del archivo actual
    private JLabel sbCaretPos;    //etiqueta que muestra la posici�n del cursor en el �rea de edici�n
 
    private boolean hasChanged = false;    //el estado del documento actual, no modificado por defecto
    private File currentFile = null;       //el archivo actual, ninguno por defecto
 
    private final EventHandler eventHandler;          //instancia de EventHandler (la clase que maneja eventos)
    private final ActionPerformer actionPerformer;    //instancia de ActionPerformer (la clase que ejecuta acciones)
    private final UndoManager undoManager;            //instancia de UndoManager (administrador de edici�n)
    
    /**
     * Punto de entrada del programa.
     *
     * Instanciamos esta clase para construir la GUI y hacerla visible.
     *
     * @param args argumentos de la l�nea de comandos.
     */
    public static void main(String[] args) {    //punto de entrada del programa
    	//construye la GUI en el EDT (Event Dispatch Thread)
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
     
            @Override
            public void run() {
                new TPEditor().jFrame.setVisible(true);    //hace visible la GUI creada por la clase TPEditor
            }
        });
    }
    
    /**
     * Constructor de la clase.
     *
     * Se construye la GUI del editor, y se instancian clases importantes.
     */
    public TPEditor() {    //constructor de la clase TPEditor
    	try {    //LookAndFeel nativo
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println(ex);
        }
     
        //construye un JFrame con t�tulo
        jFrame = new JFrame("TextPad Demo - Sin T�tulo");
        jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
     
        //asigna un manejador de eventos para el cierre del JFrame
        jFrame.addWindowListener(new WindowAdapter() {
     
            @Override
            public void windowClosing(WindowEvent we) {
                actionPerformer.actionExit();    //invoca el m�todo actionExit()
            }
        });
     
        eventHandler = new EventHandler();              //construye una instancia de EventHandler
        actionPerformer = new ActionPerformer(this);    //construye una instancia de ActionPerformer
        undoManager = new UndoManager();                //construye una instancia de UndoManager
        undoManager.setLimit(50);                       //le asigna un l�mite al buffer de ediciones
     
        buildTextArea();     //construye el �rea de edici�n, es importante que esta sea la primera parte en construirse
        buildMenuBar();      //construye la barra de men�
        buildToolBar();      //construye la barra de herramientas
        buildStatusBar();    //construye la barra de estado
        buildPopupMenu();    //construye el men� emergente
     
        jFrame.setJMenuBar(jMenuBar);                              //designa la barra de men� del JFrame
        Container c = jFrame.getContentPane();                     //obtiene el contenedor principal
        c.add(jToolBar, BorderLayout.NORTH);                       //a�ade la barra de herramientas, orientaci�n NORTE del contendor
        c.add(new JScrollPane(jTextArea), BorderLayout.CENTER);    //a�ade el �rea de edici�n en el CENTRO
        c.add(statusBar, BorderLayout.SOUTH);                      //a�ade la barra de estado, orientaci�n SUR
     
        //configura el JFrame con un tama�o inicial proporcionado con respecto a la pantalla
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        jFrame.setSize(pantalla.width / 2, pantalla.height / 2);
     
        //centra el JFrame en pantalla
        jFrame.setLocationRelativeTo(null);
    }
    
    /**
     * Construye la barra de men�.
     */
    private void buildMenuBar() {    //construye la barra de men�
    	jMenuBar = new JMenuBar();    //construye un JMenuBar
    	 
        //construye el men� "Archivo", a continuaci�n se construyen los items para este men�
        JMenu menuFile = new JMenu("Archivo");
     
        //construye el item "Nuevo"
        JMenuItem itemNew = new JMenuItem("Nuevo");
        //le asigna una combinaci�n de teclas
        itemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
        //le asigna un nombre de comando
        itemNew.setActionCommand("cmd_new");
     
        JMenuItem itemOpen = new JMenuItem("Abrir");
        itemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        itemOpen.setActionCommand("cmd_open");
     
        JMenuItem itemSave = new JMenuItem("Guardar");
        itemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
        itemSave.setActionCommand("cmd_save");
     
        JMenuItem itemSaveAs = new JMenuItem("Guardar como...");
        itemSaveAs.setActionCommand("cmd_saveas");
        itemSaveAs.addActionListener(eventHandler);
     
        JMenuItem itemPrint = new JMenuItem("Imprimir");
        itemPrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
        itemPrint.setActionCommand("cmd_print");
     
        JMenuItem itemExit = new JMenuItem("Salir");
        itemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
        itemExit.setActionCommand("cmd_exit");
     
        menuFile.add(itemNew);    //se a�aden los items al men� "Archivo"
        menuFile.add(itemOpen);
        menuFile.add(itemSave);
        menuFile.add(itemSaveAs);
        menuFile.addSeparator();
        menuFile.add(itemPrint);
        menuFile.addSeparator();
        menuFile.add(itemExit);
        //----------------------------------------------
     
        //construye el men� "Editar", a continuaci�n se construyen los items para este men�
        JMenu menuEdit = new JMenu("Editar");
     
        mbItemUndo = new JMenuItem("Deshacer");
        mbItemUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        mbItemUndo.setEnabled(false);
        mbItemUndo.setActionCommand("cmd_undo");
     
        mbItemRedo = new JMenuItem("Rehacer");
        mbItemRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        mbItemRedo.setEnabled(false);
        mbItemRedo.setActionCommand("cmd_redo");
     
        JMenuItem itemCut = new JMenuItem("Cortar");
        itemCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        itemCut.setActionCommand("cmd_cut");
     
        JMenuItem itemCopy = new JMenuItem("Copiar");
        itemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        itemCopy.setActionCommand("cmd_copy");
     
        JMenuItem itemPaste = new JMenuItem("Pegar");
        itemPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        itemPaste.setActionCommand("cmd_paste");
     
        JMenuItem itemSearch = new JMenuItem("Buscar");
        itemSearch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
        itemSearch.setActionCommand("cmd_search");
     
        JMenuItem itemSearchNext = new JMenuItem("Buscar siguiente");
        itemSearchNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        itemSearchNext.setActionCommand("cmd_searchnext");
     
        JMenuItem itemGotoLine = new JMenuItem("Ir a la l�nea...");
        itemGotoLine.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        itemGotoLine.setActionCommand("cmd_gotoline");
     
        JMenuItem itemSelectAll = new JMenuItem("Seleccionar todo");
        itemSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        itemSelectAll.setActionCommand("cmd_selectall");
     
        menuEdit.add(mbItemUndo);    //se a�aden los items al men� "Editar"
        menuEdit.add(mbItemRedo);
        menuEdit.addSeparator();     //a�ade separadores entre algunos items
        menuEdit.add(itemCut);
        menuEdit.add(itemCopy);
        menuEdit.add(itemPaste);
        menuEdit.addSeparator();
        menuEdit.add(itemSearch);
        menuEdit.add(itemSearchNext);
        menuEdit.add(itemGotoLine);
        menuEdit.addSeparator();
        menuEdit.add(itemSelectAll);
        //----------------------------------------------
     
        //construye el men� "Opciones", a continuaci�n se construyen los items para este men�
        JMenu menuTools = new JMenu("Opciones");
     
        itemLineWrap = new JCheckBoxMenuItem("Ajuste de l�nea");
        itemLineWrap.setSelected(true);
        itemLineWrap.setActionCommand("cmd_linewrap");
     
        itemShowToolBar = new JCheckBoxMenuItem("Ver barra de herramientas");
        itemShowToolBar.setSelected(true);
        itemShowToolBar.setActionCommand("cmd_showtoolbar");
     
        itemFixedToolBar = new JCheckBoxMenuItem("Fijar barra de herramientas");
        itemFixedToolBar.setSelected(true);
        itemFixedToolBar.setActionCommand("cmd_fixedtoolbar");
     
        itemShowStatusBar = new JCheckBoxMenuItem("Ver barra de estado");
        itemShowStatusBar.setSelected(true);
        itemShowStatusBar.setActionCommand("cmd_showstatusbar");
     
        JMenuItem itemFont = new JMenuItem("Fuente de letra");
        itemFont.setActionCommand("cmd_font");
     
        JMenuItem itemFontColor = new JMenuItem("Color de letra");
        itemFontColor.setActionCommand("cmd_fontcolor");
     
        JMenuItem itemBackgroundColor = new JMenuItem("Color de fondo");
        itemBackgroundColor.setActionCommand("cmd_backgroundcolor");
     
        menuTools.add(itemLineWrap);    //se a�aden los items al men� "Opciones"
        menuTools.add(itemShowToolBar);
        menuTools.add(itemFixedToolBar);
        menuTools.add(itemShowStatusBar);
        menuTools.addSeparator();
        menuTools.add(itemFont);
        menuTools.add(itemFontColor);
        menuTools.add(itemBackgroundColor);
     
        //construye el men� "Ayuda", a continuaci�n se construye el �nico item para este men�
        JMenu menuHelp = new JMenu("Ayuda");
     
        JMenuItem itemAbout = new JMenuItem("Acerca de");
        itemAbout.setActionCommand("cmd_about");
     
        menuHelp.add(itemAbout);     //se a�ade el �nico item al men� "Ayuda"
        //----------------------------------------------
     
        jMenuBar.add(menuFile);    //se a�aden los men�es construidos a la barra de men�
        jMenuBar.add(Box.createHorizontalStrut(5));    //a�ade espacios entre cada men�
        jMenuBar.add(menuEdit);
        jMenuBar.add(Box.createHorizontalStrut(5));
        jMenuBar.add(menuTools);
        jMenuBar.add(Box.createHorizontalStrut(5));
        jMenuBar.add(menuHelp);
     
        /* itera sobre todos los componentes de la barra de men�, se les asigna el mismo
        manejador de eventos a todos excepto a los separadores */
        for (Component c1 : jMenuBar.getComponents()) {
            //si el componente es un men�
            if (c1.getClass().equals(javax.swing.JMenu.class)) {
                //itera sobre los componentes del men�
                for (Component c2 : ((JMenu) c1).getMenuComponents()) {
                    //si el componente no es un separador
                    if (!c2.getClass().equals(javax.swing.JPopupMenu.Separator.class)) {
                        ((JMenuItem) c2).addActionListener(eventHandler);
                    }
                }
            }
        }
    }
 
    /**
     * Construye la barra de herramientas.
     */
    private void buildToolBar() {    //construye la barra de herramientas
    	jToolBar = new JToolBar();       //construye un JToolBar
        jToolBar.setFloatable(false);    //se configura por defecto como barra fija
     
        //construye el bot�n "Nuevo"
        JButton buttonNew = new JButton();
        //le asigna una etiqueta flotante
        buttonNew.setToolTipText("Nuevo");
        //le asigna una imagen ubicada en los recursos del proyecto
        buttonNew.setIcon(new ImageIcon(getClass().getResource("/res/tp_new.png")));
        //le asigna un nombre de comando
        buttonNew.setActionCommand("cmd_new");
     
        JButton buttonOpen = new JButton();
        buttonOpen.setToolTipText("Abrir");
        buttonOpen.setIcon(new ImageIcon(getClass().getResource("/res/tp_open.png")));
        buttonOpen.setActionCommand("cmd_open");
     
        JButton buttonSave = new JButton();
        buttonSave.setToolTipText("Guardar");
        buttonSave.setIcon(new ImageIcon(getClass().getResource("/res/tp_save.png")));
        buttonSave.setActionCommand("cmd_save");
     
        JButton buttonSaveAs = new JButton();
        buttonSaveAs.setToolTipText("Guardar como...");
        buttonSaveAs.setIcon(new ImageIcon(getClass().getResource("/res/tp_saveas.png")));
        buttonSaveAs.setActionCommand("cmd_saveas");
     
        JButton buttonPrint = new JButton();
        buttonPrint.setToolTipText("Imprimir");
        buttonPrint.setIcon(new ImageIcon(getClass().getResource("/res/tp_print.png")));
        buttonPrint.setActionCommand("cmd_print");
     
        buttonUndo = new JButton();
        buttonUndo.setEnabled(false);
        buttonUndo.setToolTipText("Deshacer");
        buttonUndo.setIcon(new ImageIcon(getClass().getResource("/res/tp_undo.png")));
        buttonUndo.setActionCommand("cmd_undo");
     
        buttonRedo = new JButton();
        buttonRedo.setEnabled(false);
        buttonRedo.setToolTipText("Rehacer");
        buttonRedo.setIcon(new ImageIcon(getClass().getResource("/res/tp_redo.png")));
        buttonRedo.setActionCommand("cmd_redo");
     
        JButton buttonCut = new JButton();
        buttonCut.setToolTipText("Cortar");
        buttonCut.setIcon(new ImageIcon(getClass().getResource("/res/tp_cut.png")));
        buttonCut.setActionCommand("cmd_cut");
     
        JButton buttonCopy = new JButton();
        buttonCopy.setToolTipText("Copiar");
        buttonCopy.setIcon(new ImageIcon(getClass().getResource("/res/tp_copy.png")));
        buttonCopy.setActionCommand("cmd_copy");
     
        JButton buttonPaste = new JButton();
        buttonPaste.setToolTipText("Pegar");
        buttonPaste.setIcon(new ImageIcon(getClass().getResource("/res/tp_paste.png")));
        buttonPaste.setActionCommand("cmd_paste");
     
        jToolBar.add(buttonNew);    //se a�aden los botones construidos a la barra de herramientas
        jToolBar.add(buttonOpen);
        jToolBar.add(buttonSave);
        jToolBar.add(buttonSaveAs);
        jToolBar.addSeparator();    //a�ade separadores entre algunos botones
        jToolBar.add(buttonPrint);
        jToolBar.addSeparator();
        jToolBar.add(buttonUndo);
        jToolBar.add(buttonRedo);
        jToolBar.addSeparator();
        jToolBar.add(buttonCut);
        jToolBar.add(buttonCopy);
        jToolBar.add(buttonPaste);
     
        /* itera sobre todos los componentes de la barra de herramientas, se les asigna el
        mismo margen y el mismo manejador de eventos unicamente a los botones */
        for (Component c : jToolBar.getComponents()) {
            //si el componente es un bot�n
            if (c.getClass().equals(javax.swing.JButton.class)) {
                JButton jb = (JButton) c;
                jb.setMargin(new Insets(0, 0, 0, 0));
                jb.addActionListener(eventHandler);
            }
        }
    }
 
    /**
     * Construye el �rea de edici�n.
     */
    private void buildTextArea() {    //construye el �rea de edici�n
    	jTextArea = new JTextArea();    //construye un JTextArea
    	 
        //se configura por defecto para que se ajusten las l�neas al tama�o del �rea de texto ...
        jTextArea.setLineWrap(true);
        //... y que se respete la integridad de las palabras en el ajuste
        jTextArea.setWrapStyleWord(true);
     
        //asigna el manejador de eventos para el cursor
        jTextArea.addCaretListener(eventHandler);
        //asigna el manejador de eventos para el rat�n
        jTextArea.addMouseListener(eventHandler);
        //asigna el manejador de eventos para registrar los cambios sobre el documento
        jTextArea.getDocument().addUndoableEditListener(eventHandler);
     
        //remueve las posibles combinaciones de teclas asociadas por defecto con el JTextArea
        jTextArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK), "none");    //remueve CTRL + X ("Cortar")
        jTextArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK), "none");    //remueve CTRL + C ("Copiar")
        jTextArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), "none");    //remueve CTRL + V ("Pegar")
    }
 
    /**
     * Construye la barra de estado.
     */
    private void buildStatusBar() {    //construye la barra de estado
    	statusBar = new JPanel();    //construye un JPanel
        //se configura con un BoxLayout
        statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.LINE_AXIS));
        //le a�ade un borde compuesto
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLoweredBevelBorder(),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
     
        //construye la etiqueta para mostrar la ubicaci�n del archivo actual
        sbFilePath = new JLabel("...");
        //construye la etiqueta para mostrar el tama�o del archivo actual
        sbFileSize = new JLabel("");
        //construye la etiqueta para mostrar la posici�n del cursor en el documento actual
        sbCaretPos = new JLabel("...");
     
        /* se a�aden las etiquetas construidas al JPanel, el resultado es un panel
        similar a una barra de estado */
        statusBar.add(sbFilePath);
        statusBar.add(Box.createRigidArea(new Dimension(10, 0)));
        statusBar.add(sbFileSize);
        statusBar.add(Box.createRigidArea(new Dimension(10, 0)));
        statusBar.add(Box.createHorizontalGlue());
        statusBar.add(sbCaretPos);
    }
 
    /**
     * Construye el men� emergente.
     */
    private void buildPopupMenu() {    //construye el men� emergente
    	jPopupMenu = new JPopupMenu();    //se construye un JPopupMenu
    	 
        //construye el item "Deshacer"
        mpItemUndo = new JMenuItem("Deshacer");
        //se configura desactivado por defecto
        mpItemUndo.setEnabled(false);
        //le asigna un nombre de comando
        mpItemUndo.setActionCommand("cmd_undo");
     
        mpItemRedo = new JMenuItem("Rehacer");
        mpItemRedo.setEnabled(false);
        mpItemRedo.setActionCommand("cmd_redo");
     
        JMenuItem itemCut = new JMenuItem("Cortar");
        itemCut.setActionCommand("cmd_cut");
     
        JMenuItem itemCopy = new JMenuItem("Copiar");
        itemCopy.setActionCommand("cmd_copy");
     
        JMenuItem itemPaste = new JMenuItem("Pegar");
        itemPaste.setActionCommand("cmd_paste");
     
        JMenuItem itemGoto = new JMenuItem("Ir a...");
        itemGoto.setActionCommand("cmd_gotoline");
     
        JMenuItem itemSearch = new JMenuItem("Buscar");
        itemSearch.setActionCommand("cmd_search");
     
        JMenuItem itemSearchNext = new JMenuItem("Buscar siguiente");
        itemSearchNext.setActionCommand("cmd_searchnext");
     
        JMenuItem itemSelectAll = new JMenuItem("Seleccionar todo");
        itemSelectAll.setActionCommand("cmd_selectall");
     
        jPopupMenu.add(mpItemUndo);    //se a�aden los items al men� emergente
        jPopupMenu.add(mpItemRedo);
        jPopupMenu.addSeparator();     //a�ade separadores entre algunos items
        jPopupMenu.add(itemCut);
        jPopupMenu.add(itemCopy);
        jPopupMenu.add(itemPaste);
        jPopupMenu.addSeparator();
        jPopupMenu.add(itemGoto);
        jPopupMenu.add(itemSearch);
        jPopupMenu.add(itemSearchNext);
        jPopupMenu.addSeparator();
        jPopupMenu.add(itemSelectAll);
     
        /* itera sobre todos los componentes del men� emergente, se les asigna el mismo
        manejador de eventos a todos excepto a los separadores */
        for (Component c : jPopupMenu.getComponents()) {
            //si el componente es un item
            if (c.getClass().equals(javax.swing.JMenuItem.class)) {
                ((JMenuItem) c).addActionListener(eventHandler);
            }
        }
    }
    
    /**
     * Hace visible el men� emergente.
     *
     * @param me evento del rat�n
     */
    private void showPopupMenu(MouseEvent me) {    //despliega el men� emergente
    	if (me.isPopupTrigger() == true) {    //si el evento es el desencadenador del men� emergente
            //hace visible el men� emergente en las coordenadas actuales del rat�n
            jPopupMenu.show(me.getComponent(), me.getX(), me.getY());
        }
    }
 
    /**
     * Actualiza el estado de las opciones "Deshacer" y "Rehacer".
     */
    void updateControls() {    //actualiza algunos componentes de la GUI
    	//averigua si se pueden deshacer los cambios en el documento actual
        boolean canUndo = undoManager.canUndo();
        //averigua si se pueden rehacer los cambios en el documento actual
        boolean canRedo = undoManager.canRedo();
     
        //activa o desactiva las opciones en la barra de men�
        mbItemUndo.setEnabled(canUndo);
        mbItemRedo.setEnabled(canRedo);
     
        //activa o desactiva las opciones en la barra de herramientas
        buttonUndo.setEnabled(canUndo);
        buttonRedo.setEnabled(canRedo);
     
        //activa o desactiva las opciones en el men� emergente
        mpItemUndo.setEnabled(canUndo);
        mpItemRedo.setEnabled(canRedo);
    }
 
    /**
     * Retorna la instancia de EventHandler, la clase interna que maneja eventos.
     *
     * @return el manejador de eventos.
     */
    EventHandler getEventHandler() {    //retorna la instancia de EventHandler (la clase interna que maneja eventos)
        return eventHandler;
    }
     
    /**
     * Retorna la instancia de UndoManager, la cual administra las ediciones sobre
     * el documento en el �rea de texto.
     *
     * @return el administrador de edici�n.
     */
    UndoManager getUndoManager() {    //retorna la instancia de UndoManager (administrador de edici�n)
        return undoManager;
    }
     
    /**
     * Retorna el estado del documento actual.
     *
     * @return true si ah sido modificado, false en caso contrario
     */
    boolean documentHasChanged() {    //retorna el estado del documento actual
        return hasChanged;
    }
     
    /**
     * Establece el estado del documento actual.
     *
     * @param hasChanged true si ah sido modificado, false en caso contrario
     */
    void setDocumentChanged(boolean hasChanged) {    //establece el estado del documento actual
        this.hasChanged = hasChanged;
    }
     
    /**
     * Retorna la instancia de JTextArea, el �rea de edici�n.
     *
     * @return retorna el �rea de edici�n.
     */
    JTextArea getJTextArea() {    //retorna la instancia de JTextArea (�rea de edici�n)
        return jTextArea;
    }
     
    /**
     * Retorna la instancia de JFrame, la ventana principal del editor.
     *
     * @return la ventana principal del editor.
     */
    JFrame getJFrame() {    //retorna la instancia de JFrame (ventana principal del editor)
        return jFrame;
    }
     
    /**
     * Retorna la instancia de File, el archivo actual.
     *
     * @return el archivo actual
     */
    File getCurrentFile() {    //retorna la instancia de File (el archivo actual)
        return currentFile;
    }
     
    /**
     * Establece el archivo actual.
     *
     * @param currentFile el archivo actual
     */
    void setCurrentFile(File currentFile) {    //establece el archivo actual
        this.currentFile = currentFile;
    }
     
    /**
     * Retorna la instancia de la etiqueta sbFilePath, donde se muestra la ubicaci�n
     * del archivo actual.
     *
     * @return la instancia de la etiqueta sbFilePath
     */
    JLabel getJLabelFilePath() {    //retorna la instancia de la etiqueta sbFilePath
        return sbFilePath;
    }
     
    /**
     * Retorna la instancia de la etiqueta sbFileSize, donde se muestra el tama�o
     * del archivo actual
     *
     * @return la instancia de la etiqueta sbFileSize
     */
    JLabel getJLabelFileSize() {    //retorna la instancia de la etiqueta sbFileSize
        return sbFileSize;
    }
 
    /* la clase EventHandler extiende e implementa las clases e interfaces necesarias para
    atender y manejar los eventos sobre la GUI principal del editor */
    class EventHandler extends MouseAdapter implements ActionListener,
                                                       CaretListener,
                                                       UndoableEditListener {
 
    	/**
         * Atiende y maneja los eventos de acci�n.
         *
         * @param ae evento de acci�n
         */
        @Override
        public void actionPerformed(ActionEvent ae) {    //implemento de la interface ActionListener
        	String ac = ae.getActionCommand();    //se obtiene el nombre del comando ejecutado
        	 
            if (ac.equals("cmd_new") == true) {    //opci�n seleccionada: "Nuevo"
                actionPerformer.actionNew();
            } else if (ac.equals("cmd_open") == true) {    //opci�n seleccionada: "Abrir"
                actionPerformer.actionOpen();
            } else if (ac.equals("cmd_save") == true) {    //opci�n seleccionada: "Guardar"
                actionPerformer.actionSave();
            } else if (ac.equals("cmd_saveas") == true) {    //opci�n seleccionada: "Guardar como"
                actionPerformer.actionSaveAs();
            } else if (ac.equals("cmd_print") == true) {    //opci�n seleccionada: "Imprimir"
                actionPerformer.actionPrint();
            } else if (ac.equals("cmd_exit") == true) {    //opci�n seleccionada: "Salir"
                actionPerformer.actionExit();
            } else if (ac.equals("cmd_undo") == true) {    //opci�n seleccionada: "Deshacer"
                actionPerformer.actionUndo();
            } else if (ac.equals("cmd_redo") == true) {    //opci�n seleccionada: "Rehacer"
                actionPerformer.actionRedo();
            } else if (ac.equals("cmd_cut") == true) {    //opci�n seleccionada: "Cortar"
                //corta el texto seleccionado en el documento
                jTextArea.cut();
            } else if (ac.equals("cmd_copy") == true) {    //opci�n seleccionada: "Copiar"
                //copia el texto seleccionado en el documento
                jTextArea.copy();
            } else if (ac.equals("cmd_paste") == true) {    //opci�n seleccionada: "Pegar"
                //pega en el documento el texto del portapapeles
                jTextArea.paste();
            } else if (ac.equals("cmd_gotoline") == true) {    //opci�n seleccionada: "Ir a la l�nea..."
                actionPerformer.actionGoToLine();
            } else if (ac.equals("cmd_search") == true) {    //opci�n seleccionada: "Buscar"
                actionPerformer.actionSearch();
            } else if (ac.equals("cmd_searchnext") == true) {    //opci�n seleccionada: "Buscar siguiente"
                actionPerformer.actionSearchNext();
            } else if (ac.equals("cmd_selectall") == true) {    //opci�n seleccionada: "Seleccionar todo"
                jTextArea.selectAll();
            } else if (ac.equals("cmd_linewrap") == true) {    //opci�n seleccionada: "Ajuste de l�nea"
                //si esta propiedad esta activada se desactiva, o lo inverso
                jTextArea.setLineWrap(!jTextArea.getLineWrap());
                jTextArea.setWrapStyleWord(!jTextArea.getWrapStyleWord());
            } else if (ac.equals("cmd_showtoolbar") == true) {    //opci�n seleccionada: "Ver barra de herramientas"
                //si la barra de herramientas esta visible se oculta, o lo inverso
                jToolBar.setVisible(!jToolBar.isVisible());
            } else if (ac.equals("cmd_fixedtoolbar") == true) {    //opci�n seleccionada: "Fijar barra de herramientas"
                //si esta propiedad esta activada se desactiva, o lo inverso
                jToolBar.setFloatable(!jToolBar.isFloatable());
            } else if (ac.equals("cmd_showstatusbar") == true) {    //opci�n seleccionada: "Ver barra de estado"
                //si la barra de estado esta visible se oculta, o lo inverso
                statusBar.setVisible(!statusBar.isVisible());
            } else if (ac.equals("cmd_font") == true) {    //opci�n seleccionada: "Fuente de letra"
                actionPerformer.actionSelectFont();
            } else if (ac.equals("cmd_fontcolor") == true) {    //opci�n seleccionada: "Color de letra"
                actionPerformer.actionSelectFontColor();
            } else if (ac.equals("cmd_backgroundcolor") == true) {    //opci�n seleccionada: "Color de fondo"
                actionPerformer.actionSelectBackgroundColor();
            } else if (ac.equals("cmd_about") == true) {    //opci�n seleccionada: "Acerca de"
                //presenta un dialogo modal con alguna informacion
                JOptionPane.showMessageDialog(jFrame,
                                              "TextPad Demo por Dark[byte]",
                                              "Acerca de",
                                              JOptionPane.INFORMATION_MESSAGE);
            }
        }
 
        /**
         * Atiende y maneja los eventos del cursor.
         *
         * @param ce evento del cursor
         */
        @Override
        public void caretUpdate(CaretEvent ce) {    //implemento de la interface CaretListener
        	final int caretPos;  //valor de la posici�n del cursor sin inicializar
            int y = 1;           //valor de la l�nea inicialmente en 1
            int x = 1;           //valor de la columna inicialmente en 1
         
            try {
                //obtiene la posici�n del cursor con respecto al inicio del JTextArea (�rea de edici�n)
                caretPos = jTextArea.getCaretPosition();
                //sabiendo lo anterior se obtiene el valor de la l�nea actual (se cuenta desde 0)
                y = jTextArea.getLineOfOffset(caretPos);
         
                /** a la posici�n del cursor se le resta la posici�n del inicio de la l�nea para
                   determinar el valor de la columna actual */
                x = caretPos - jTextArea.getLineStartOffset(y);
         
                //al valor de la l�nea actual se le suma 1 porque estas comienzan cont�ndose desde 0
                y += 1;
            } catch (BadLocationException ex) {    //en caso de que ocurra una excepci�n
                System.err.println(ex);
            }
         
            /** muestra la informaci�n recolectada en la etiqueta sbCaretPos de la
               barra de estado, tambi�n se incluye el n�mero total de lineas */
            sbCaretPos.setText("L�neas: " + jTextArea.getLineCount() + " - Y: " + y + " - X: " + x);
        }
 
        /**
         * Atiende y maneja los eventos sobre el documento en el �rea de edici�n.
         *
         * @param uee evento de edici�n
         */
        @Override
        public void undoableEditHappened(UndoableEditEvent uee) {    //implemento de la interface UndoableEditListener
        	/* el cambio realizado en el �rea de edici�n se guarda en el buffer
            del administrador de edici�n */
         undoManager.addEdit(uee.getEdit());
         updateControls();     //actualiza el estado de las opciones "Deshacer" y "Rehacer"
      
         hasChanged = true;    //marca el documento como modificado
        }
 
        /**
         * Atiende y maneja los eventos sobre el rat�n cuando este es presionado.
         *
         * @param me evento del rat�n
         */
        @Override
        public void mousePressed(MouseEvent me) {    //herencia de la clase MouseAdapter
        	showPopupMenu(me);
        }
 
        /**
         * Atiende y maneja los eventos sobre el rat�n cuando este es liberado.
         *
         * @param me evento del rat�n
         */
        @Override
        public void mouseReleased(MouseEvent me) {    //herencia de la clase MouseAdapter
        	showPopupMenu(me);
        }
    }
}