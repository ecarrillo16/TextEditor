package textedit;		//se define el paquete donde debe estar este archivo

import java.awt.Color;    //importamos todo lo que se utilizar�
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * Clase que ejecuta las operaciones solicitadas.
 * 
 * @author ecarrillo
 */

public class ActionPerformer {    //clase publica ActionPerformer
 
    private final TPEditor tpEditor;    //instancia de TPEditor (la clase principal)
    private String lastSearch = "";     //la �ltima b�squeda de texto realizada, por defecto no contiene nada
 
    /**
     * Constructor de la clase.
     * 
     * @param tpEditor clase principal
     */
    public ActionPerformer(TPEditor tpEditor) {    //constructor de la clase ActionPerformer
    	this.tpEditor = tpEditor;    //guarda la instancia de la clase TPEditor
    }
 
    /**
     * Opci�n seleccionada: "Nuevo".
     * 
     * Reemplaza el documento actual por uno nuevo vac�o.
     */
    public void actionNew() {    //opci�n seleccionada: "Nuevo"
    	if (tpEditor.documentHasChanged() == true) {    //si el documento esta marcado como modificado
            //se le ofrece al usuario guardar los cambios
            int option = JOptionPane.showConfirmDialog(tpEditor.getJFrame(), "�Desea guardar los cambios?");
     
            switch (option) {
                case JOptionPane.YES_OPTION:       //si elige que si
                    actionSave();              //se guarda el archivo
                    break;
                case JOptionPane.CANCEL_OPTION:    //si elige cancelar
                    return;                        //se cancela esta operaci�n
                //en otro caso se contin�a con la operaci�n y no se guarda el documento actual
            }
        }
     
        tpEditor.getJFrame().setTitle("TextPad Demo - Sin T�tulo");    //nuevo t�tulo de la ventana
     
        //limpia el contenido del area de edici�n
        tpEditor.getJTextArea().setText("");
        //limpia el contenido de las etiquetas en la barra de estado
        tpEditor.getJLabelFilePath().setText("");
        tpEditor.getJLabelFileSize().setText("");
     
        tpEditor.getUndoManager().die();    //limpia el buffer del administrador de edici�n
        tpEditor.updateControls();          //actualiza el estado de las opciones "Deshacer" y "Rehacer"
     
        //el archivo asociado al documento actual se establece como null
        tpEditor.setCurrentFile(null);
        //marca el estado del documento como no modificado
        tpEditor.setDocumentChanged(false);
    }
 
    /**
     * Opci�n seleccionada: "Abrir".
     * 
     * Le permite al usuario elegir un archivo para cargar en el �rea de edici�n.
     */
    public void actionOpen() {    //opci�n seleccionada: "Abrir"
    	if (tpEditor.documentHasChanged() == true) {    //si el documento esta marcado como modificado
            //le ofrece al usuario guardar los cambios
            int option = JOptionPane.showConfirmDialog(tpEditor.getJFrame(), "�Desea guardar los cambios?");
     
            switch (option) {
                case JOptionPane.YES_OPTION:     //si elige que si
                    actionSave();            //se guarda el archivo
                    break;
                case JOptionPane.CANCEL_OPTION:  //si elige cancelar
                    return;                      //se cancela esta operaci�n
                //en otro caso se contin�a con la operaci�n y no se guarda el documento actual
            }
        }
     
        JFileChooser fc = getJFileChooser();    //obtiene un JFileChooser
     
        //se presenta un dialogo modal para que el usuario seleccione un archivo
        int state = fc.showOpenDialog(tpEditor.getJFrame());
     
        if (state == JFileChooser.APPROVE_OPTION) {    //si elige abrir el archivo
            File f = fc.getSelectedFile();    //obtiene el archivo seleccionado
     
            try {
                //abre un flujo de datos desde el archivo seleccionado
                BufferedReader br = new BufferedReader(new FileReader(f));
                tpEditor.getJTextArea().read(br, null);    //lee desde el flujo de datos hacia el �rea de edici�n
                br.close();    //cierra el flujo de datos
     
                //asigna el manejador de eventos para registrar los cambios en el nuevo documento actual
                tpEditor.getJTextArea().getDocument().addUndoableEditListener(tpEditor.getEventHandler());
     
                tpEditor.getUndoManager().die();    //limpia el buffer del administrador de edici�n
                tpEditor.updateControls();          //actualiza el estado de las opciones "Deshacer" y "Rehacer"
     
                //nuevo t�tulo de la ventana con el nombre del archivo abierto
                tpEditor.getJFrame().setTitle("TextPad Demo - " + f.getName());
     
                //muestra la ubicaci�n del archivo abierto
                tpEditor.getJLabelFilePath().setText(shortPathName(f.getAbsolutePath()));
                //muestra el tama�o del archivo abierto
                tpEditor.getJLabelFileSize().setText(roundFileSize(f.length()));
     
                //establece el archivo abierto como el archivo actual
                tpEditor.setCurrentFile(f);
                //marca el estado del documento como no modificado
                tpEditor.setDocumentChanged(false);
            } catch (IOException ex) {    //en caso de que ocurra una excepci�n
                //presenta un dialogo modal con alguna informaci�n de la excepci�n
                JOptionPane.showMessageDialog(tpEditor.getJFrame(),
                                              ex.getMessage(),
                                              ex.toString(),
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }
 
    /**
     * Opci�n seleccionada: "Guardar".
     * 
     * Guarda el documento actual en el archivo asociado actualmente.
     */
    public void actionSave() {    //opci�n seleccionada: "Guardar"
    	if (tpEditor.getCurrentFile() == null) {    //si no hay un archivo asociado al documento actual
            actionSaveAs();    //invoca el m�todo actionSaveAs()
        } else if (tpEditor.documentHasChanged() == true) {    //si el documento esta marcado como modificado
            try {
                //abre un flujo de datos hacia el archivo asociado al documento actual
                BufferedWriter bw = new BufferedWriter(new FileWriter(tpEditor.getCurrentFile()));
                //escribe desde el flujo de datos hacia el archivo
                tpEditor.getJTextArea().write(bw);
                bw.close();    //cierra el flujo
     
                //marca el estado del documento como no modificado
                tpEditor.setDocumentChanged(false);
            } catch (IOException ex) {    //en caso de que ocurra una excepci�n
                //presenta un dialogo modal con alguna informaci�n de la excepci�n
                JOptionPane.showMessageDialog(tpEditor.getJFrame(),
                                              ex.getMessage(),
                                              ex.toString(),
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }
 
    /**
     * Opci�n seleccionada: "Guardar como".
     * 
     * Le permite al usuario elegir la ubicaci�n donde se guardar� el documento actual.
     */
    public void actionSaveAs() {    //opci�n seleccionada: "Guardar como"
    	JFileChooser fc = getJFileChooser();    //se obtiene un JFileChooser
    	 
        //presenta un dialogo modal para que el usuario seleccione un archivo
        int state = fc.showSaveDialog(tpEditor.getJFrame());
        if (state == JFileChooser.APPROVE_OPTION) {    //si elige guardar en el archivo
            File f = fc.getSelectedFile();    //se obtiene el archivo seleccionado
     
            try {
                //abre un flujo de datos hacia el archivo asociado seleccionado
                BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                //escribe desde el flujo de datos hacia el archivo
                tpEditor.getJTextArea().write(bw);
                bw.close();    //cierra el flujo
     
                //nuevo t�tulo de la ventana con el nombre del archivo guardado
                tpEditor.getJFrame().setTitle("TextPad Demo - " + f.getName());
     
                //muestra la ubicaci�n del archivo guardado
                tpEditor.getJLabelFilePath().setText(shortPathName(f.getAbsolutePath()));
                //muestra el tama�o del archivo guardado
                tpEditor.getJLabelFileSize().setText(roundFileSize(f.length()));
     
                //establece el archivo guardado como el archivo actual
                tpEditor.setCurrentFile(f);
                //marca el estado del documento como no modificado
                tpEditor.setDocumentChanged(false);
            } catch (IOException ex) {    //en caso de que ocurra una excepci�n
                //presenta un dialogo modal con alguna informaci�n de la excepci�n
                JOptionPane.showMessageDialog(tpEditor.getJFrame(),
                                              ex.getMessage(),
                                              ex.toString(),
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }
 
    /**
     * Opci�n seleccionada: "Imprimir".
     * 
     * Imprime el documento actual.
     */
    public void actionPrint() {    //opci�n seleccionada: "Imprimir"
    	boolean result = false;    //resultado de la impresi�n, por defecto es false
    	 
        //si el documento actual no esta vac�o
        if (tpEditor.getJTextArea().getText().trim().equals("") == false) {
            //invoca nuestra la clase PrintAction para presentar el dialogo de impresi�n
            result = PrintAction.print(tpEditor.getJTextArea(), tpEditor.getJFrame());
        }
    }
 
    /**
     * Opci�n seleccionada: "Salir".
     * 
     * Finaliza el programa.
     */
    public void actionExit() {    //opci�n seleccionada: "Salir"
    	if (tpEditor.documentHasChanged() == true) {    //si el documento esta marcado como modificado
            //se le ofrece al usuario guardar los cambios
            int option = JOptionPane.showConfirmDialog(tpEditor.getJFrame(), "�Desea guardar los cambios?");
     
            switch (option) {
                case JOptionPane.YES_OPTION:     //si elige que si
                    actionSave();                //se guarda el archivo
                    break;
                case JOptionPane.CANCEL_OPTION:  //si elige cancelar
                    return;                      //se cancela esta operaci�n
                //en otro caso se contin�a con la operaci�n y no se guarda el documento actual
            }
        }
     
        System.exit(0);    //se finaliza el programa con el c�digo 0 (sin error)
    }
 
    /**
     * Opci�n seleccionada: "Deshacer".
     * 
     * Deshace el �ltimo cambio realizado en el documento actual.
     */
    public void actionUndo() {    //opci�n seleccionada: "Deshacer"
    	try {
            //deshace el �ltimo cambio realizado sobre el documento en el �rea de edici�n
            tpEditor.getUndoManager().undo();
        } catch (CannotUndoException ex) {    //en caso de que ocurra una excepci�n
            System.err.println(ex);
        }
     
        //actualiza el estado de las opciones "Deshacer" y "Rehacer"
        tpEditor.updateControls();
    }
 
    /**
     * Opci�n seleccionada: "Rehacer".
     * 
     * Rehace el �ltimo cambio realizado en el documento actual.
     */
    public void actionRedo() {    //opci�n seleccionada: "Rehacer"
    	try {
            //rehace el �ltimo cambio realizado sobre el documento en el �rea de edici�n
            tpEditor.getUndoManager().redo();
        } catch (CannotRedoException ex) {    //en caso de que ocurra una excepci�n
            System.err.println(ex);
        }
     
        //actualiza el estado de las opciones "Deshacer" y "Rehacer"
        tpEditor.updateControls();
    }
 
    /**
     * Opci�n seleccionada: "Buscar".
     * 
     * Busca un texto especificado por el usuario en el documento actual. El texto queda 
     * guardado para b�squedas siguientes.
     */
    public void actionSearch() {    //opci�n seleccionada: "Buscar"
    	//solicita al usuario que introduzca el texto a buscar
        String text = JOptionPane.showInputDialog(
                tpEditor.getJFrame(),
                "Texto:",
                "TextPad Demo - Buscar",
                JOptionPane.QUESTION_MESSAGE);
     
        if (text != null) {    //si se introdujo texto (puede ser una cadena vac�a)
            String textAreaContent = tpEditor.getJTextArea().getText();    //obtiene todo el contenido del �rea de edici�n
            int pos = textAreaContent.indexOf(text);    //obtiene la posici�n de la primera ocurrencia del texto
     
            if (pos > -1) {    //si la posici�n es mayor a -1 significa que la b�squeda fue positiva
                //selecciona el texto en el �rea de edici�n para resaltarlo
                tpEditor.getJTextArea().select(pos, pos + text.length());
            }
     
            //establece el texto buscado como el texto de la �ltima b�squeda realizada
            lastSearch = text;
        }
    }
 
    /**
     * Opci�n seleccionada: "Buscar siguiente".
     * 
     * Busca el texto de la �ltima b�squeda en el documento actual.
     */
    public void actionSearchNext() {    //opci�n seleccionada: "Buscar siguiente"
    	if (lastSearch.isEmpty() == false) {    //si la �ltima b�squeda contiene texto
            String textAreaContent = tpEditor.getJTextArea().getText();    //se obtiene todo el contenido del �rea de edici�n
            int pos = tpEditor.getJTextArea().getCaretPosition();    //se obtiene la posici�n del cursor sobre el �rea de edici�n
            //buscando a partir desde la posici�n del cursor, se obtiene la posici�n de la primera ocurrencia del texto
            pos = textAreaContent.indexOf(lastSearch, pos);
     
            if (pos > -1) {    //si la posici�n es mayor a -1 significa que la b�squeda fue positiva
                //selecciona el texto en el �rea de edici�n para resaltarlo
                tpEditor.getJTextArea().select(pos, pos + lastSearch.length());
            }
        } else {    //si la �ltima b�squeda no contiene nada
            actionSearch();    //invoca el m�todo actionSearch()
        }
    }
 
    /**
     * Opci�n seleccionada: "Ir a la l�nea...".
     * 
     * Posiciona el cursor en el inicio de una l�nea especificada por el usuario.
     */
    public void actionGoToLine() {    //opci�n seleccionada: "Ir a la l�nea..."
    	//solicita al usuario que introduzca el n�mero de l�nea
        String line = JOptionPane.showInputDialog(
                tpEditor.getJFrame(),
                "N�mero:",
                "TextPad Demo - Ir a la l�nea...",
                JOptionPane.QUESTION_MESSAGE);
     
        if (line != null && line.length() > 0) {    //si se introdujo un dato
            try {
                int pos = Integer.parseInt(line);    //el dato introducido se convierte en entero
     
                //si el n�mero de l�nea esta dentro de los l�mites del �rea de texto
                if (pos >= 0 && pos <= tpEditor.getJTextArea().getLineCount()) {
                    //posiciona el cursor en el inicio de la l�nea
                    tpEditor.getJTextArea().setCaretPosition(tpEditor.getJTextArea().getLineStartOffset(pos));
                }
            } catch (NumberFormatException ex) {    //en caso de que ocurran excepciones
                System.err.println(ex);
            } catch (BadLocationException ex) {
                System.err.println(ex);
            }
        }
    }
 
    /**
     * Opci�n seleccionada: "Fuente de letra".
     * 
     * Le permite al usuario elegir la fuente para la letra en el �rea de edici�n.
     */
    public void actionSelectFont() {   //opci�n seleccionada: "Fuente de letra"
    	//presenta el dialogo de selecci�n de fuentes
        Font font = JFontChooser.showDialog(tpEditor.getJFrame(),
                                            "TextPad Demo - Fuente de letra:",
                                            tpEditor.getJTextArea().getFont());
        if (font != null) {    //si un fuente fue seleccionado
            //se establece como fuente del �rea de edici�n
            tpEditor.getJTextArea().setFont(font);
        }
    }
    /**
     * Opci�n seleccionada: "Color de letra".
     * 
     * Le permite al usuario elegir el color para la letra en el �rea de edici�n.
     */
    public void actionSelectFontColor() {    //opci�n seleccionada: "Color de letra"
    	//presenta el dialogo de selecci�n de colores
        Color color = JColorChooser.showDialog(tpEditor.getJFrame(),
                                               "TextPad Demo - Color de letra:",
                                               tpEditor.getJTextArea().getForeground());
        if (color != null) {    //si un color fue seleccionado
            //se establece como color del fuente y cursor
            tpEditor.getJTextArea().setForeground(color);
            tpEditor.getJTextArea().setCaretColor(color);
        }
    }
 
    /**
     * Opci�n seleccionada: "Color de fondo".
     * 
     * Le permite al usuario elegir el color para el fondo del �rea de edici�n.
     */
    public void actionSelectBackgroundColor() {    //opci�n seleccionada: "Color de fondo"
    	//presenta el dialogo de selecci�n de colores
        Color color = JColorChooser.showDialog(tpEditor.getJFrame(),
                                               "TextPad Demo - Color de fondo:",
                                               tpEditor.getJTextArea().getForeground());
        if (color != null) {    //si un color fue seleccionado
            //se establece como color de fondo
            tpEditor.getJTextArea().setBackground(color);
        }
    }
 
    /**
     * Retorna la instancia de un JFileChooser, con el cual se muestra un dialogo que permite
     * seleccionar un archivo.
     * 
     * @return un dialogo para seleccionar un archivo.
     */
    private static JFileChooser getJFileChooser() {    //retorna un JFileChooser
    	JFileChooser fc = new JFileChooser();                     //construye un JFileChooser
        fc.setDialogTitle("TextPad Demo - Elige un archivo:");    //se le establece un t�tulo
        fc.setMultiSelectionEnabled(false);                       //desactiva la multi-selecci�n
        fc.setFileFilter(textFileFilter);                         //aplica un filtro de extensiones
        return fc;    //retorna el JFileChooser
    }
 
    /**
     * Clase an�nima interna que extiende la clase javax.swing.filechooser.FileFilter para 
     * establecer un filtro de archivos en el JFileChooser.
     */
    private static FileFilter textFileFilter = new FileFilter() {
    	public boolean accept(File f) {
            //acepta directorios y archivos de extensi�n .txt
            return f.isDirectory() || f.getName().toLowerCase().endsWith("txt");
        }
     
        public String getDescription() {
            //la descripci�n del tipo de archivo aceptado
            return "Text Files";
        }
    };
 
    /**
     * Retorna la ruta de la ubicaci�n de un archivo en forma reducida.
     * 
     * @param longpath la ruta de un archivo
     * @return la ruta reducida del archivo
     */
    private static String shortPathName(String longPath) {    //comprime una ruta de archivo muy larga
    	//construye un arreglo de cadenas, donde cada una es un nombre de directorio
        String[] tokens = longPath.split(Pattern.quote(File.separator));
     
        //construye un StringBuilder donde se a�adira el resultado
        StringBuilder shortpath = new StringBuilder();
     
        //itera sobre el arreglo de cadenas
        for (int i = 0 ; i < tokens.length ; i++) {
            if (i == tokens.length - 1) {             //si la cadena actual es la �ltima, es el nombre del archivo
                shortpath.append(tokens[i]);    //se a�ade al resultado sin separador
                break;                          //se termina el bucle
            } else if (tokens[i].length() >= 10) {    //si la cadena actual tiene 10 o m�s caracteres
                //se toman los primeros 3 caracteres y se a�ade al resultado con un separador
                shortpath.append(tokens[i].substring(0, 3)).append("...").append(File.separator);
            } else {                                  //si la cadena actual tiene menos de 10 caracteres
                //se a�ade al resultado con un separador
                shortpath.append(tokens[i]).append(File.separator);
            }
        }
     
        return shortpath.toString();    //retorna la cadena resultante
    }
 
    /**
     * Redondea la longitud de un archivo en KiloBytes si es necesario.
     * 
     * @param length longitud de un archivo
     * @return el tama�o redondeado  
     */
    private static String roundFileSize(long length) {    //retorna el tama�o de un archivo redondeado
    	//retorna el tama�o del archivo redondeado
        return (length < 1024) ? length + " bytes" : (length / 1024) + " Kbytes";
    }
}