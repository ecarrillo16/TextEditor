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
 * @author ecarrillo
 *
 */

public class ActionPerformer {    //clase publica ActionPerformer
 
    private final TPEditor tpEditor;    //instancia de TPEditor (la clase principal)
    private String lastSearch = "";     //la �ltima b�squeda de texto realizada, por defecto no contiene nada
 
    public ActionPerformer(TPEditor tpEditor) {    //constructor de la clase ActionPerformer
    }
 
    public void actionNew() {    //opci�n seleccionada: "Nuevo"
    }
 
    public void actionOpen() {    //opci�n seleccionada: "Abrir"
    }
 
    public void actionSave() {    //opci�n seleccionada: "Guardar"
    }
 
    public void actionSaveAs() {    //opci�n seleccionada: "Guardar como"
    }
 
    public void actionPrint() {    //opci�n seleccionada: "Imprimir"
    }
 
    public void actionExit() {    //opci�n seleccionada: "Salir"
    }
 
    public void actionUndo() {    //opci�n seleccionada: "Deshacer"
    }
 
    public void actionRedo() {    //opci�n seleccionada: "Rehacer"
    }
 
    public void actionSearch() {    //opci�n seleccionada: "Buscar"
    }
 
    public void actionSearchNext() {    //opci�n seleccionada: "Buscar siguiente"
    }
 
    public void actionGoToLine() {    //opci�n seleccionada: "Ir a la l�nea..."
    }
 
    public void actionSelectFont() {   //opci�n seleccionada: "Fuente de letra"
    }
 
    public void actionSelectFontColor() {    //opci�n seleccionada: "Color de letra"
    }
 
    public void actionSelectBackgroundColor() {    //opci�n seleccionada: "Color de fondo"
    }
 
    private static JFileChooser getJFileChooser() {    //retorna un JFileChooser
    }
 
    //clase an�nima interna que define un filtro de extensiones
    private static FileFilter textFileFilter = new FileFilter() {
        public boolean accept(File f) {
        }
 
        public String getDescription() {
        }
    };
 
    private static String shortPathName(String longpath) {    //comprime una ruta de archivo muy larga
    }
 
    private static String roundFileSize(long length) {    //retorna el tama�o de un archivo redondeado
    }
}