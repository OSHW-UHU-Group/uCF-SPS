/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import ArduinoConnection.ArduinoConnection;
import Data.Bandera;
import Data.Calibration;
import Data.DataCalculation;
import com.panamahitek.ArduinoException;
import com.panamahitek.PanamaHitek_MultiMessage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 *
 * @author Andrés
 */
public class CalibrationJFrame extends javax.swing.JFrame {
    
    private DataCalculation datacalc = new DataCalculation();
    private ArduinoConnection ino = new ArduinoConnection();
    private PanamaHitek_MultiMessage multi2 = new PanamaHitek_MultiMessage(1,ino.arduinoReturn());
    private int stepcount = 1;
    private int instepcount = 0;
    private ArrayList<Double> xdata = new ArrayList<>();
    private ArrayList<Double> ydata = new ArrayList<>();
    private Bandera demo = new Bandera (false);
    public String calresult = new String("");
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    DecimalFormat decimales = new DecimalFormat("0.0#");

    
    

   
    public CalibrationJFrame() {       
        
        
    
        
        initComponents();
        
        int ancho = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
        int altura = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
        //this.setBounds(1, 1, 430, 230);
        int anchoV = this.getWidth();
        int alturaV= this.getHeight();                
        this.setBounds((ancho/2)-(anchoV/2),(altura/2)-(alturaV/2), anchoV, alturaV);
        
        jPanelWelcome.setVisible(true);
        jPanelStep.setVisible(false);        
        jPanelEnd.setVisible(false);
        

        
    }
    
    public CalibrationJFrame(DataCalculation datacalc, ArduinoConnection ino, PanamaHitek_MultiMessage multi) {
        
        this.datacalc = datacalc;
        this.ino = ino;
        this.multi2= multi;
        initComponents();
        
        int ancho = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
        int altura = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
        //this.setBounds(1, 1, 430, 230);
        int anchoV = this.getWidth();
        int alturaV= this.getHeight();                
        this.setBounds((ancho/2)-(anchoV/2),(altura/2)-(alturaV/2), anchoV, alturaV);
        
        jPanelWelcome.setVisible(true);
        jPanelStep.setVisible(false);        
        jPanelEnd.setVisible(false);
        
        
        

        
        
    //  Cambio de acción al cerrar la ventana en los pasos intermedios    

 
    this.addWindowListener(new WindowAdapter()
    {
        public void windowClosing(WindowEvent e)
        {
            JFrame frame = (JFrame)e.getSource();
            
            if (instepcount >= 1 && instepcount <=5){
            
                int result = JOptionPane.showConfirmDialog(
                    frame,
                    "Are you sure you want to abort calibration? Calibration data will be lost",
                    "Exit Application",
                    JOptionPane.YES_NO_OPTION);

                switch(result) {
                 case 0 :
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    stepcount = 1;
                    instepcount = 0;
                    xdata.clear();
                    ydata.clear();
                    jPanelWelcome.setVisible(true);
                    jPanelStep.setVisible(false);
                    Ventana.calibrating.setFlag(false);
                    System.out.println("Yes"); 
                    break;

                 case 1 :         
                    System.out.println("No");
                    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);            
                    break;

                 default :
                    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);                   
                    System.out.println("Close");
                    break;
                }
            }

            
        }
    });        
        
        
    }
    
    private float separar(String original){
        
        
        
        if (original.contains(".")){
            
            String[] parts = original.split("\\.");
            String numero = parts[0];
            String decimales = parts[1];
            int n = decimales.length();

            float resultado = (float) (Float.parseFloat(numero)+(Float.parseFloat(decimales)/Math.pow(10, n)));
            return resultado;   
        
        }
    
        if (original.contains(",")){
            

            String[] parts = original.split(",");
            String numero = parts[0];
            String decimales = parts[1];
            int n = decimales.length();

            float resultado = (float) (Float.parseFloat(numero)+(Float.parseFloat(decimales)/Math.pow(10, n)));
            return resultado;   

        }
    
        if (!original.contains(",")&& !original.contains(".")) {
            

            float resultado = Float.parseFloat(original);
            return resultado;

        }
        
    return -1;    
        
    }
    
    // Método para abortar calibrado, cerrando la ventana pidiendo confirmación
    
    public void abortcal (){
      
    int result = JOptionPane.showConfirmDialog(
            this,            
            "Are you sure you want to abort calibration? Calibration data will be lost",
            "Exit Application",
            JOptionPane.YES_NO_OPTION);
        
        switch(result) {
         case 0 :
            this.dispose();
            stepcount = 1;
            instepcount = 0;
            xdata.clear();
            ydata.clear(); 
            ino.sendStop(demo);
            jPanelStep.setVisible(false);
            jPanelWelcome.setVisible(true);            
            Ventana.calibrating.setFlag(false);
            System.out.println("Yes"); 
            break;
            
         case 1 :         
            System.out.println("No");
            this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);            
            break;
         
         default :
            this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            System.out.println("Close");
            break;
        }
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPaneStart = new javax.swing.JLayeredPane();
        jPanelWelcome = new javax.swing.JPanel();
        jLabel0 = new javax.swing.JLabel();
        jButtonStart = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabelWelcome = new javax.swing.JLabel();
        jPanelStep = new javax.swing.JPanel();
        jLabelStep = new javax.swing.JLabel();
        jPanelStep1 = new javax.swing.JPanel();
        jButtonNext1 = new javax.swing.JButton();
        jButtonAbort1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanelStep2 = new javax.swing.JPanel();
        jButtonAbort2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jButtonNext2 = new javax.swing.JButton();
        jLabelGif2 = new javax.swing.JLabel();
        jPanelStep3 = new javax.swing.JPanel();
        jButtonNext3 = new javax.swing.JButton();
        jButtonAbort3 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jPanelStep4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jButtonAbort4 = new javax.swing.JButton();
        jButtonNext4 = new javax.swing.JButton();
        jLabelGif4 = new javax.swing.JLabel();
        jPanelStep5 = new javax.swing.JPanel();
        jButtonNextStep = new javax.swing.JButton();
        jButtonAbort5 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabelWatermass = new javax.swing.JLabel();
        jTextFieldMass = new javax.swing.JTextField();
        jLabelTimeElapsed = new javax.swing.JLabel();
        jPanelEnd = new javax.swing.JPanel();
        jLabelEnd = new javax.swing.JLabel();
        jButtonEndCancel = new javax.swing.JButton();
        jButtonEndOK = new javax.swing.JButton();
        jLabelEndResults = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Calibration");
        setAlwaysOnTop(true);

        jPaneStart.setPreferredSize(new java.awt.Dimension(630, 300));

        jPanelWelcome.setPreferredSize(new java.awt.Dimension(630, 300));

        jLabel0.setText("Follow the instructions below before starting calibration.");

        jButtonStart.setText("Start");
        jButtonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartActionPerformed(evt);
            }
        });

        jTextArea1.setEditable(false);
        jTextArea1.setBackground(new java.awt.Color(204, 204, 204));
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jTextArea1.setRows(5);
        jTextArea1.setText("1- Set syringes and tube them, using fitting if needed. Adjust stroke, microstep, number of syringes and syringe volume.\n\n2- Prepare a lab scale and a beaker with distilled water (x2 syringe volume at least).\n\n3- The tube must always be submerged in water. Use parafilm to cover the beaker in order to avoid evaporation.\n\n4- Once ready, fully load syringes preventing air from forming bubbles. Then empty syringes.\n\n5- Turn on the scale and push Start button. Further explanation will be given in each step.");
        jScrollPane1.setViewportView(jTextArea1);

        jLabelWelcome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Calib.gif"))); // NOI18N

        javax.swing.GroupLayout jPanelWelcomeLayout = new javax.swing.GroupLayout(jPanelWelcome);
        jPanelWelcome.setLayout(jPanelWelcomeLayout);
        jPanelWelcomeLayout.setHorizontalGroup(
            jPanelWelcomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelWelcomeLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(jPanelWelcomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel0)
                    .addGroup(jPanelWelcomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelWelcomeLayout.createSequentialGroup()
                            .addComponent(jLabelWelcome)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonStart, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 550, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        jPanelWelcomeLayout.setVerticalGroup(
            jPanelWelcomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelWelcomeLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanelWelcomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonStart)
                    .addGroup(jPanelWelcomeLayout.createSequentialGroup()
                        .addComponent(jLabel0, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabelWelcome)))
                .addGap(40, 40, 40))
        );

        jPanelStep.setPreferredSize(new java.awt.Dimension(630, 300));

        jLabelStep.setText("Step");

        jPanelStep1.setPreferredSize(new java.awt.Dimension(630, 255));

        jButtonNext1.setText("Next");
        jButtonNext1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNext1ActionPerformed(evt);
            }
        });

        jButtonAbort1.setText("Abort");
        jButtonAbort1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAbort1ActionPerformed(evt);
            }
        });

        jLabel1.setText("Load syringe");

        javax.swing.GroupLayout jPanelStep1Layout = new javax.swing.GroupLayout(jPanelStep1);
        jPanelStep1.setLayout(jPanelStep1Layout);
        jPanelStep1Layout.setHorizontalGroup(
            jPanelStep1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStep1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonNext1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonAbort1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
            .addGroup(jPanelStep1Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelStep1Layout.setVerticalGroup(
            jPanelStep1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStep1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 163, Short.MAX_VALUE)
                .addGroup(jPanelStep1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAbort1)
                    .addComponent(jButtonNext1))
                .addGap(40, 40, 40))
        );

        jPanelStep2.setPreferredSize(new java.awt.Dimension(630, 255));

        jButtonAbort2.setText("Abort");
        jButtonAbort2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAbort2ActionPerformed(evt);
            }
        });

        jLabel2.setText("Loading syringe. Please Wait");

        jButtonNext2.setText("Next");
        jButtonNext2.setEnabled(false);
        jButtonNext2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNext2ActionPerformed(evt);
            }
        });

        jLabelGif2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/AniB_small.gif"))); // NOI18N
        jLabelGif2.setText("jLabel10");

        javax.swing.GroupLayout jPanelStep2Layout = new javax.swing.GroupLayout(jPanelStep2);
        jPanelStep2.setLayout(jPanelStep2Layout);
        jPanelStep2Layout.setHorizontalGroup(
            jPanelStep2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStep2Layout.createSequentialGroup()
                .addContainerGap(384, Short.MAX_VALUE)
                .addComponent(jButtonNext2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonAbort2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
            .addGroup(jPanelStep2Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jLabelGif2, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50)
                .addComponent(jLabel2)
                .addGap(73, 73, 73))
        );
        jPanelStep2Layout.setVerticalGroup(
            jPanelStep2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStep2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanelStep2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelGif2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(27, 27, 27)
                .addGroup(jPanelStep2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAbort2)
                    .addComponent(jButtonNext2))
                .addGap(40, 40, 40))
        );

        jPanelStep3.setPreferredSize(new java.awt.Dimension(630, 255));

        jButtonNext3.setText("Next");
        jButtonNext3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNext3ActionPerformed(evt);
            }
        });

        jButtonAbort3.setText("Abort");
        jButtonAbort3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAbort3ActionPerformed(evt);
            }
        });

        jLabel3.setText("Tare scale before next step.");

        javax.swing.GroupLayout jPanelStep3Layout = new javax.swing.GroupLayout(jPanelStep3);
        jPanelStep3.setLayout(jPanelStep3Layout);
        jPanelStep3Layout.setHorizontalGroup(
            jPanelStep3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStep3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonNext3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonAbort3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
            .addGroup(jPanelStep3Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel3)
                .addContainerGap(453, Short.MAX_VALUE))
        );
        jPanelStep3Layout.setVerticalGroup(
            jPanelStep3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStep3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 163, Short.MAX_VALUE)
                .addGroup(jPanelStep3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonNext3)
                    .addComponent(jButtonAbort3))
                .addGap(40, 40, 40))
        );

        jPanelStep4.setPreferredSize(new java.awt.Dimension(630, 255));

        jLabel4.setText("Unloading syringe. Please wait. ");

        jButtonAbort4.setText("Abort");
        jButtonAbort4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAbort4ActionPerformed(evt);
            }
        });

        jButtonNext4.setText("Next");
        jButtonNext4.setEnabled(false);
        jButtonNext4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNext4ActionPerformed(evt);
            }
        });

        jLabelGif4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/AniB_small.gif"))); // NOI18N
        jLabelGif4.setText("jLabel11");

        javax.swing.GroupLayout jPanelStep4Layout = new javax.swing.GroupLayout(jPanelStep4);
        jPanelStep4.setLayout(jPanelStep4Layout);
        jPanelStep4Layout.setHorizontalGroup(
            jPanelStep4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStep4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonNext4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonAbort4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
            .addGroup(jPanelStep4Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jLabelGif4, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50)
                .addComponent(jLabel4)
                .addGap(73, 73, 73))
        );
        jPanelStep4Layout.setVerticalGroup(
            jPanelStep4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStep4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanelStep4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabelGif4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addGroup(jPanelStep4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAbort4)
                    .addComponent(jButtonNext4))
                .addGap(40, 40, 40))
        );

        jPanelStep5.setPreferredSize(new java.awt.Dimension(630, 255));

        jButtonNextStep.setText("Next Step");
        jButtonNextStep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNextStepActionPerformed(evt);
            }
        });

        jButtonAbort5.setText("Abort");
        jButtonAbort5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAbort5ActionPerformed(evt);
            }
        });

        jLabel5.setText("Complete with water mass");

        jLabelWatermass.setText("Water mass (g):");

        jTextFieldMass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldMassActionPerformed(evt);
            }
        });

        jLabelTimeElapsed.setText("Time Elapsed:");

        javax.swing.GroupLayout jPanelStep5Layout = new javax.swing.GroupLayout(jPanelStep5);
        jPanelStep5.setLayout(jPanelStep5Layout);
        jPanelStep5Layout.setHorizontalGroup(
            jPanelStep5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStep5Layout.createSequentialGroup()
                .addContainerGap(384, Short.MAX_VALUE)
                .addComponent(jButtonNextStep, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonAbort5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
            .addGroup(jPanelStep5Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(jPanelStep5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelTimeElapsed)
                    .addGroup(jPanelStep5Layout.createSequentialGroup()
                        .addComponent(jLabelWatermass)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldMass, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelStep5Layout.setVerticalGroup(
            jPanelStep5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStep5Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel5)
                .addGap(40, 40, 40)
                .addGroup(jPanelStep5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelWatermass)
                    .addComponent(jTextFieldMass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addComponent(jLabelTimeElapsed)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 54, Short.MAX_VALUE)
                .addGroup(jPanelStep5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonNextStep)
                    .addComponent(jButtonAbort5))
                .addGap(40, 40, 40))
        );

        javax.swing.GroupLayout jPanelStepLayout = new javax.swing.GroupLayout(jPanelStep);
        jPanelStep.setLayout(jPanelStepLayout);
        jPanelStepLayout.setHorizontalGroup(
            jPanelStepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStepLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabelStep)
                .addGap(591, 591, 591))
            .addComponent(jPanelStep1, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE)
            .addGroup(jPanelStepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanelStep2, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE))
            .addGroup(jPanelStepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanelStep4, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE))
            .addGroup(jPanelStepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanelStep5, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE))
            .addGroup(jPanelStepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanelStep3, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE))
        );
        jPanelStepLayout.setVerticalGroup(
            jPanelStepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStepLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabelStep)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanelStep1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanelStepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStepLayout.createSequentialGroup()
                    .addGap(0, 42, Short.MAX_VALUE)
                    .addComponent(jPanelStep2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(jPanelStepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStepLayout.createSequentialGroup()
                    .addGap(0, 45, Short.MAX_VALUE)
                    .addComponent(jPanelStep4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(jPanelStepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStepLayout.createSequentialGroup()
                    .addGap(0, 45, Short.MAX_VALUE)
                    .addComponent(jPanelStep5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(jPanelStepLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStepLayout.createSequentialGroup()
                    .addGap(0, 45, Short.MAX_VALUE)
                    .addComponent(jPanelStep3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanelEnd.setPreferredSize(new java.awt.Dimension(630, 300));

        jLabelEnd.setText("End");

        jButtonEndCancel.setText("Cancel");
        jButtonEndCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEndCancelActionPerformed(evt);
            }
        });

        jButtonEndOK.setText("OK");
        jButtonEndOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEndOKActionPerformed(evt);
            }
        });

        jLabelEndResults.setText("Results are");

        javax.swing.GroupLayout jPanelEndLayout = new javax.swing.GroupLayout(jPanelEnd);
        jPanelEnd.setLayout(jPanelEndLayout);
        jPanelEndLayout.setHorizontalGroup(
            jPanelEndLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelEndLayout.createSequentialGroup()
                .addContainerGap(384, Short.MAX_VALUE)
                .addComponent(jButtonEndOK, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonEndCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
            .addGroup(jPanelEndLayout.createSequentialGroup()
                .addGroup(jPanelEndLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelEndLayout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jLabelEnd))
                    .addGroup(jPanelEndLayout.createSequentialGroup()
                        .addGap(56, 56, 56)
                        .addComponent(jLabelEndResults)))
                .addContainerGap(520, Short.MAX_VALUE))
        );
        jPanelEndLayout.setVerticalGroup(
            jPanelEndLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelEndLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabelEnd)
                .addGap(49, 49, 49)
                .addComponent(jLabelEndResults)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 145, Short.MAX_VALUE)
                .addGroup(jPanelEndLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonEndCancel)
                    .addComponent(jButtonEndOK))
                .addGap(40, 40, 40))
        );

        jPaneStart.setLayer(jPanelWelcome, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jPaneStart.setLayer(jPanelStep, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jPaneStart.setLayer(jPanelEnd, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jPaneStartLayout = new javax.swing.GroupLayout(jPaneStart);
        jPaneStart.setLayout(jPaneStartLayout);
        jPaneStartLayout.setHorizontalGroup(
            jPaneStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jPaneStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanelWelcome, javax.swing.GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE))
            .addGroup(jPaneStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanelStep, javax.swing.GroupLayout.PREFERRED_SIZE, 652, Short.MAX_VALUE))
            .addGroup(jPaneStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanelEnd, javax.swing.GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE))
        );
        jPaneStartLayout.setVerticalGroup(
            jPaneStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jPaneStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanelWelcome, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 319, Short.MAX_VALUE))
            .addGroup(jPaneStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanelStep, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
            .addGroup(jPaneStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanelEnd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 622, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPaneStart, javax.swing.GroupLayout.PREFERRED_SIZE, 622, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 299, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPaneStart, javax.swing.GroupLayout.PREFERRED_SIZE, 299, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartActionPerformed
     
        jPanelWelcome.setVisible(false);
        jPanelStep.setVisible(true);
        jPanelStep1.setVisible(true);
        jPanelStep2.setVisible(false);
        jPanelStep3.setVisible(false);
        jPanelStep4.setVisible(false);
        jPanelStep5.setVisible(false);
        
        
        jLabelStep.setText("Step" + " " + 1);
        instepcount = instepcount +1;
        
        
        
        
    }//GEN-LAST:event_jButtonStartActionPerformed

    private void jButtonNextStepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNextStepActionPerformed
  
        
        datacalc.setWMass(separar(jTextFieldMass.getText()));
        
        long timeelapsed = datacalc.getTimeD()-datacalc.getTimeG();
        
        double invFlow = timeelapsed/datacalc.getWMass()*datacalc.getWDensity();
        
        xdata.add((double)Ventana.jSpeedSlider.getValue());
        ydata.add(invFlow);
                
        instepcount = instepcount +1;

        stepcount = stepcount +1;

        jLabelStep.setText("Step" + " " + stepcount);        
         

        if (stepcount > datacalc.getSteps()){

            jPanelStep.setVisible(false);
            jPanelEnd.setVisible(true);
            xdata.add((double)2000);
            ydata.add((double)0);
          
//          llamar rutina datacalc.lineal (se obtiene diámetro temporal)
            datacalc.lineal(xdata, ydata);
//          importar valores de a y b de datacalc.lineal (hacer get set a y b)
            datacalc.getPend();
            datacalc.getOrd();
//          importar valor de r^2 de datacalc.correlation                     

//          rellenar etiquetas de jPanelEnd con valores
            jLabelEndResults.setText("Calibration curve: " + decimales.format(datacalc.getPend()) +" + "+ decimales.format(datacalc.getOrd()) + " " + "\n" + "Correlation coefficient: " + datacalc.correlacion(xdata, ydata));
 
        }

        else {

            instepcount = 1;
            jPanelStep1.setVisible(true);
            jPanelStep5.setVisible(false);

        }
//    }       
      
    }//GEN-LAST:event_jButtonNextStepActionPerformed

    private void jButtonEndCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEndCancelActionPerformed
        
        
        
        jPanelEnd.setVisible(false);
        jPanelWelcome.setVisible(true);
        stepcount = 1;
        instepcount = 0;
        xdata.clear();
        ydata.clear();
        this.dispose();
        Ventana.calibrating.setFlag(false);
        
        
    }//GEN-LAST:event_jButtonEndCancelActionPerformed

    private void jButtonAbort5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAbort5ActionPerformed
        this.dispose();
        
    }//GEN-LAST:event_jButtonAbort5ActionPerformed

    private void jButtonNext3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNext3ActionPerformed
        
        instepcount = instepcount + 1;
        ino.sendUnload(demo);       
        jPanelStep3.setVisible(false);
        jPanelStep4.setVisible(true);
        if (datacalc.getSteps() == 1) {
            Ventana.jSpeedSlider.setValue(1500);
        }else{
        
            switch(stepcount){
               
                case 3:
                    Ventana.jSpeedSlider.setValue(1250);
                    break;
                case 2:
                    Ventana.jSpeedSlider.setValue(1500);
                    break;
                case 1:
                    Ventana.jSpeedSlider.setValue(1750);
                    break;    
                    
                
            } 
                       
        }       

        
    }//GEN-LAST:event_jButtonNext3ActionPerformed

    private void jButtonNext1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNext1ActionPerformed
    
        instepcount = instepcount + 1;
        
        ino.sendLoad(demo);
        jPanelStep1.setVisible(false);
        jPanelStep2.setVisible(true);
                     
   
        
        
    }//GEN-LAST:event_jButtonNext1ActionPerformed

    private void jButtonAbort1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAbort1ActionPerformed
        abortcal();
    }//GEN-LAST:event_jButtonAbort1ActionPerformed

    private void jButtonAbort2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAbort2ActionPerformed
        abortcal();
    }//GEN-LAST:event_jButtonAbort2ActionPerformed

    private void jButtonAbort3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAbort3ActionPerformed
        abortcal();
    }//GEN-LAST:event_jButtonAbort3ActionPerformed

    private void jButtonAbort4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAbort4ActionPerformed
        abortcal();
    }//GEN-LAST:event_jButtonAbort4ActionPerformed

    private void jButtonNext2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNext2ActionPerformed
    
    instepcount = instepcount + 1;   
    jButtonNext4.setEnabled(false);
    jPanelStep2.setVisible(false);
    jPanelStep3.setVisible(true);
    jButtonNext2.setEnabled(false);
    jLabelGif2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/AniB_small.gif")));
    jLabel2.setText("Loading syringe. Please wait. ");
        
    }//GEN-LAST:event_jButtonNext2ActionPerformed

    private void jButtonNext4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNext4ActionPerformed
    
    instepcount = instepcount + 1;   
                
    jPanelStep4.setVisible(false);
    jPanelStep5.setVisible(true);
    jButtonNext4.setEnabled(false);
    jLabelGif4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/AniB_small.gif")));
    jLabel4.setText("Unloading syringe. Please wait. ");
        
    }//GEN-LAST:event_jButtonNext4ActionPerformed

    private void jTextFieldMassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldMassActionPerformed
      
    }//GEN-LAST:event_jTextFieldMassActionPerformed

    private void jButtonEndOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEndOKActionPerformed
        
        datacalc.setDiameter(datacalc.lineal(xdata,ydata));
        
//      Save string creation

//        -Date
        calresult = (dtf.format(LocalDateTime.now())+ "\n");

//        -Diameter
        calresult = calresult + ("Diameter (mm)" + "_" + datacalc.getDiameter()+ "\n" );

//        -Syringe number and volume

        calresult = calresult + ("Syringe Number " + Ventana.jComboBoxVOLT.getSelectedItem()+ "_" + Ventana.jComboBoxVOLT.getSelectedIndex() + "\n");

//        -Stroke
        calresult = calresult + ("Stroke (mm)" + "_" + Ventana.jTextFieldStroke.getText() + "\n" );

//        -Microstep
        calresult = calresult + ("Microstep " + Ventana.jComboBoxStep.getSelectedItem() + "_" + Ventana.jComboBoxStep.getSelectedIndex() + "\n" );

//        -Temperature
        calresult = calresult + ("Temperature" + "_" + datacalc.getTemperature()+ "\n" );
   

//        -Array x,y
        calresult = calresult + ("X" + "_" + xdata + "\n" );
        calresult = calresult + ("Y" + "_" + ydata + "\n" );
                
//        -a b y r^2
        calresult = calresult + ("a" + "_" + datacalc.getPend()+ "\n" );
        calresult = calresult + ("b" + "_" + datacalc.getOrd()+ "\n" );
        calresult = calresult + ("r^2" + "_" + datacalc.correlacion(xdata, ydata)+ "\n" );
       
        
        jButtonEndCancel.doClick();     
         
       
        
        
    }//GEN-LAST:event_jButtonEndOKActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CalibrationJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CalibrationJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CalibrationJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CalibrationJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CalibrationJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAbort1;
    private javax.swing.JButton jButtonAbort2;
    private javax.swing.JButton jButtonAbort3;
    private javax.swing.JButton jButtonAbort4;
    private javax.swing.JButton jButtonAbort5;
    private javax.swing.JButton jButtonEndCancel;
    private javax.swing.JButton jButtonEndOK;
    private javax.swing.JButton jButtonNext1;
    public javax.swing.JButton jButtonNext2;
    private javax.swing.JButton jButtonNext3;
    public javax.swing.JButton jButtonNext4;
    private javax.swing.JButton jButtonNextStep;
    private javax.swing.JButton jButtonStart;
    private javax.swing.JLabel jLabel0;
    private javax.swing.JLabel jLabel1;
    public javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    public javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelEnd;
    private javax.swing.JLabel jLabelEndResults;
    public javax.swing.JLabel jLabelGif2;
    public javax.swing.JLabel jLabelGif4;
    private javax.swing.JLabel jLabelStep;
    public javax.swing.JLabel jLabelTimeElapsed;
    private javax.swing.JLabel jLabelWatermass;
    private javax.swing.JLabel jLabelWelcome;
    private javax.swing.JLayeredPane jPaneStart;
    private javax.swing.JPanel jPanelEnd;
    private javax.swing.JPanel jPanelStep;
    private javax.swing.JPanel jPanelStep1;
    private javax.swing.JPanel jPanelStep2;
    private javax.swing.JPanel jPanelStep3;
    private javax.swing.JPanel jPanelStep4;
    private javax.swing.JPanel jPanelStep5;
    private javax.swing.JPanel jPanelWelcome;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextFieldMass;
    // End of variables declaration//GEN-END:variables
}
