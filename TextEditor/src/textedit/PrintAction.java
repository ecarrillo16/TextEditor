package textedit;		//se define el paquete donde debe estar este archivo

import java.awt.BorderLayout;    //importamos todo lo que se utilizar�
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * Clase que implementa la interface java.awt.print.Printable para imprimir el documento
 * presente en el �rea de edici�n.
 * 
 * @author ecarrillo
 */

public class PrintAction implements Printable {    //clase publica que implementa la interface Printable
	 
    private final JTextArea jTextArea;    //�rea de edici�n donde se encuentra el documento actual
    private JDialog dialog;               //dialogo de estado, muestra el estado de la impresi�n
    private int[] pageBreaks;             //arreglo de quiebres de p�gina
    private String[] textLines;           //arreglo de l�neas de texto
    private int currentPage = -1;         //p�gina actual impresa, por defecto inicilizada en -1
    private boolean result = false;       //resultado de la impresi�n, por defecto es negativo
 
    /**
     * Constructor de la clase.
     * 
     * @param jComponent componente cuyo contenido se imprimir�
     */
    public PrintAction(JComponent jComponent) {    //constructor de la clase PrintAction
    	this.jTextArea = (JTextArea) jComponent;    //guarda la instancia del �rea de edici�n
    }
 
    /**
     * M�todo est�tico que construye e inicializa la clase PrintAction para imprimir
     * el documento presente en el �rea de edici�n.
     * 
     * @param jComponent componente cuyo contenido se imprimir�
     * @param owner la ventana padre
     * @return true si la impresi�n fue exitosa, false en caso contrario
     */
    public static boolean print(JComponent jComponent, Frame owner) {
    	PrintAction pa = new PrintAction(jComponent);   //construye una instancia de PrintAction
        return pa.printDialog(owner);                   //inicia la impresi�n y retorna un valor booleano
    }
 
    /**
     * Le permite al usuario configurar algunos aspectos de la impresi�n antes de comenzar. Durante
     * la impresi�n muestra una ventana de dialogo con informaci�n de la misma.
     * 
     * @param owner la ventana padre
     * @return true si la impresi�n fue exitosa, false en caso contrario
     */
    public boolean printDialog(Frame owner) {
    	//construye un trabajo de impresi�n
        final PrinterJob pj = PrinterJob.getPrinterJob();
        //construye un conjunto de atributos para la impresi�n
        final PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        //establece a la clase PrintAction como responsable de renderizar las p�ginas del documento
        pj.setPrintable(this);
     
        boolean option = pj.printDialog(pras);    //presenta un dialogo de impresi�n
     
        if (option == true) {    //si el usuario acepta
            //construye el dialogo modal de estado sobre la ventana padre
            dialog = new PrintingMessageBox(owner, pj);
     
            //crea un nuevo hilo para que se ocupe de la impresi�n
            new Thread(new Runnable() {
     
                @Override
                public void run() {
                    try {
                        pj.print();                        //inicia la impresi�n
                        PrintAction.this.result = true;    //resultado positivo
                    } catch (PrinterException ex) {        //en caso de que ocurra una excepci�n
                        System.err.println(ex);
                    }
     
                    dialog.setVisible(false);    //oculta el dialogo de estado
                }
            }).start();    //inicia el hilo de impresi�n
     
            dialog.setVisible(true);     //hace visible el dialogo de estado
        }
     
        return PrintAction.this.result;    //retorna el resultado de la impresi�n
    }
 
    /**
     * Se renderiza cada p�gina solicitada por el sistema de impresi�n.
     * 
     * @param g objeto de gr�ficos
     * @param pf el formato de la p�gina
     * @param pageIndex el �ndice de la p�gina a imprimir
     * @return PAGE_EXISTS si la p�gina se tiene que imprimir, NO_SUCH_PAGE si la p�gina no es valida
     */
    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) {    //implemento de la interface Printable
    	Graphics2D g2d = (Graphics2D) g;                      //conversi�n de gr�ficos simples a gr�ficos 2D
        g2d.setFont(new Font("Serif", Font.PLAIN, 10));       //establece un fuente para todo el texto
        int lineHeight = g2d.getFontMetrics().getHeight();    //obtiene la altura del fuente
     
        if (pageBreaks == null) {    //si los quiebres de p�gina no fueron calculados
            //construye un arreglo con las l�neas de texto presentes en el �rea de edici�n
            textLines = jTextArea.getText().split("\n");
            //calcula el n�mero de l�neas que caben en cada p�gina
            int linesPerPage = (int) (pf.getImageableHeight() / lineHeight);
            //calcula el n�mero de quiebres de p�gina necesarios para imprimir todo el documento
            int numBreaks = (textLines.length - 1) / linesPerPage;
            //construye un arreglo con los quiebres de p�gina 
            pageBreaks = new int[numBreaks];
            for (int i = 0 ; i < numBreaks ; i++) {
                //se calcula la posici�n para cada quiebre de p�gina
                pageBreaks[i] = (i + 1) * linesPerPage;
            }
        }
     
        //si el �ndice de p�gina solicitado es menor o igual que la cantidad de quiebres total
        if (pageIndex <= pageBreaks.length) {
            /* establece una igualdad entre el origen del espacio gr�fico (x:0,y:0) y el origen 
            del �rea imprimible definido por el formato de p�gina */
            g2d.translate(pf.getImageableX(), pf.getImageableY());
     
            int y = 0;    //coordenada "y", inicializada en 0 (principio de p�gina)
            //obtiene la primera l�nea para la p�gina actual
            int startLine = (pageIndex == 0) ? 0 : pageBreaks[pageIndex - 1];
            //obtiene la �ltima l�nea para la p�gina actual
            int endLine = (pageIndex == pageBreaks.length) ? textLines.length : pageBreaks[pageIndex];
     
            //itera sobre las l�neas que forman parte de la p�gina actual
            for (int line = startLine ; line < endLine ; line++) {
                y += lineHeight;                          //aumenta la coordenada "y" para cada l�nea
                g2d.drawString(textLines[line], 0, y);    //imprime la linea en las coordenadas actuales
            }
     
            updateStatus(pageIndex);    //actualiza el estado de impresi�n
     
            return PAGE_EXISTS;     //la p�gina solicitada ser� impresa
        } else {
            return NO_SUCH_PAGE;    //la pagina solicitada no es valida
        }
    }
 
    /**
     * Actualiza la informaci�n en el dialogo de estado de la impresi�n.
     * 
     * @param pageIndex �ndice de la p�gina impresa
     */
    private void updateStatus(int pageIndex) {    //actualiza el estado de la impresi�n en el dialogo de estado
    	if (pageIndex != currentPage) {
            currentPage++;    //incrementa la p�gina actual    
     
            //acceso seguro al EDT (Event Dispatch Thread) para actualizar la GUI
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
     
                @Override
                public void run() {
                    //actualiza la informaci�n de la etiqueta lbStatusMsg
                    ((PrintingMessageBox) dialog).setStatusMsg("Imprimiendo p�gina " + (currentPage + 1) + " ...");
                }
            });
        }
    }
 
    /**
     * Clase interna que extiende javax.swing.JDialog para presentar una ventana modal de dialogo
     * que muestra el estado de la impresi�n y un bot�n para cancelar la operaci�n.
     */
    private class PrintingMessageBox extends JDialog {
 
        private JLabel lbStatusMsg;    //etiqueta que muestra el estado de impresi�n
 
        /**
         * Constructor de esta clase.
         * 
         * Construye una ventana modal de dialogo sobre una ventana padre.
         * 
         * @param owner la ventana padre
         * @param pj trabajo de impresi�n
         */
        public PrintingMessageBox(Frame owner, final PrinterJob pj) {    //constructor de la clase PrintingMessageBox
        	/** invoca el constructor de la superclase para establecer la ventana padre, el t�tulo
            de la ventana, y que ser� una ventana modal */
            super(owner, "Impresi�n", true);
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
         
            //construye y configura la etiqueta que muestra el estado
            lbStatusMsg = new JLabel("Iniciando ...");
            lbStatusMsg.setPreferredSize(new Dimension(200, 30));
            lbStatusMsg.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         
            //construye el bot�n de cancelar
            JButton buttonCancel = new JButton("Cancelar");
            JPanel jp = new JPanel();
            jp.add(buttonCancel);
         
            //asigna un manejador de eventos para el bot�n de cancelar
            buttonCancel.addActionListener(new ActionListener() {
         
                @Override
                public void actionPerformed(ActionEvent e) {
                    pj.cancel();          //cancela el trabajo de impresi�n
                    setVisible(false);    //oculta esta ventana
                }
            });
         
            getContentPane().add(lbStatusMsg, BorderLayout.CENTER);    //a�ade la etiqueta en el CENTRO
            getContentPane().add(jp, BorderLayout.SOUTH);              //a�ade el bot�n, orientaci�n SUR
         
            //asigna un manejador de eventos para cuando la ventana pierde la visibilidad
            this.addComponentListener(new ComponentAdapter() {
         
                @Override
                public void componentHidden(ComponentEvent e) {
                    Window w = (Window) e.getComponent();    //convierte el componente afectado en una ventana
                    w.dispose();                             //destruye la ventana
                }
            });
         
            setResizable(false);             //no se permite redimensionar la ventana
            pack();                          //se le da el tama�o preferido
            setLocationRelativeTo(owner);    //centra la ventana sobre el editor de texto
        }
 
        /**
         * Establece el texto de estado que se muestra en el dialogo.
         * 
         * @param statusMsg texto de estado
         */
        public void setStatusMsg(String statusMsg){    //establece el texto de la etiqueta lbStatusMsg
        	lbStatusMsg.setText(statusMsg);    //establece el texto de la etiqueta lbStatusMsg
        }
    }
}