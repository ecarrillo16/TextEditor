package textedit;		//se define el paquete donde debe estar este archivo

import java.awt.Color;    //importamos todo lo que se utilizará
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
 * @author ecarrillo
 *
 */

public class ActionPerformer {    //clase publica ActionPerformer
 
    private final TPEditor tpEditor;    //instancia de TPEditor (la clase principal)
    private String lastSearch = "";     //la última búsqueda de texto realizada, por defecto no contiene nada
 
    public ActionPerformer(TPEditor tpEditor) {    //constructor de la clase ActionPerformer
    }
 
    public void actionNew() {    //opción seleccionada: "Nuevo"
    }
 
    public void actionOpen() {    //opción seleccionada: "Abrir"
    }
 
    public void actionSave() {    //opción seleccionada: "Guardar"
    }
 
    public void actionSaveAs() {    //opción seleccionada: "Guardar como"
    }
 
    public void actionPrint() {    //opción seleccionada: "Imprimir"
    }
 
    public void actionExit() {    //opción seleccionada: "Salir"
    }
 
    public void actionUndo() {    //opción seleccionada: "Deshacer"
    }
 
    public void actionRedo() {    //opción seleccionada: "Rehacer"
    }
 
    public void actionSearch() {    //opción seleccionada: "Buscar"
    }
 
    public void actionSearchNext() {    //opción seleccionada: "Buscar siguiente"
    }
 
    public void actionGoToLine() {    //opción seleccionada: "Ir a la línea..."
    }
 
    public void actionSelectFont() {   //opción seleccionada: "Fuente de letra"
    }
 
    public void actionSelectFontColor() {    //opción seleccionada: "Color de letra"
    }
 
    public void actionSelectBackgroundColor() {    //opción seleccionada: "Color de fondo"
    }
 
    private static JFileChooser getJFileChooser() {    //retorna un JFileChooser
    }
 
    //clase anónima interna que define un filtro de extensiones
    private static FileFilter textFileFilter = new FileFilter() {
        public boolean accept(File f) {
        }
 
        public String getDescription() {
        }
    };
 
    private static String shortPathName(String longpath) {    //comprime una ruta de archivo muy larga
    }
 
    private static String roundFileSize(long length) {    //retorna el tamaño de un archivo redondeado
    }
}