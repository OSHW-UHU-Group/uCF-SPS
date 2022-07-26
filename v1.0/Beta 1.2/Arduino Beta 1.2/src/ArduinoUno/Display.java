package ArduinoUno;


//import java.awt.Color;
import com.panamahitek.ArduinoException;
import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import javax.swing.*;
//import Arduino.Arduino;
import com.panamahitek.PanamaHitek_Arduino;
import java.awt.Color;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import jssc.SerialPortException;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Código fuente para programación de uCF-SPS
 *
 * @author Samuel D. Fernández Silva, 4º Grado en Química UHU
 * @tutortfg  Juan D. Mozo Llamazares. Departamento de Quimica-Física UHU
 */

public class Display extends javax.swing.JFrame implements ChangeListener {

    PanamaHitek_Arduino arduino = new PanamaHitek_Arduino();
    private boolean Working;
    private boolean Local;
    private boolean calibrating;
    private Double VolT;
    private Double VolR;
    private Double VolV;
    private Double pbvalue;
    private Double pvalue;
    private Double time;
    private Double pend;
    private Double ord;
    private int veloc;
    private int volt;
    private Double timetemp;
    private int syrnum; //syringe number (used)
    private int syrcap; //syringe capacity in mL
    private boolean demo = true;
    private long caltinit = 0;
    private long caltfin = 0;
    private long elapsed = 0;
    private long elapsed1 = 0;
    private long elapsed2 = 0;
    private long elapsed3 = 0;
    private Double watermass1 = 0.0;
    private Double watermass2 = 0.0;
    private Double watermass3 = 0.0;
    private long t1;
    private long t2;
    private int currentstep;
    private int maxsteps;
    Locale locale  = new Locale("en", "UK");
    String pattern = "0.0##";
    DecimalFormat decimales = (DecimalFormat)NumberFormat.getNumberInstance(locale);

    
    /*
    * LISTENER FOR SERIAL PORT MESSAGES
    */
    
    SerialPortEventListener event = new SerialPortEventListener(){
        @Override
        public void serialEvent(SerialPortEvent spe) {
            try {
                if (arduino.isMessageAvailable() == true){
                    String msg = arduino.printMessage();
                    jTextArea1.append(msg + "\n");
                    //System.out.println(msg);
                    if (msg.startsWith("v") == true){
                        if (!Local){
                            try {
                                arduino.sendData("r");
                            } catch (ArduinoException | SerialPortException ex) {
                                Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            veloc = Math.round((Integer.parseInt(msg.substring(1))/(float)1023.0)*(float)1999.0);
                            speedslider.setValue(veloc);
                            flow(veloc,true);
                        }
                    }
                    if (msg.startsWith("t") == true & msg.startsWith("l", 1) == true){
                        endreload();
                    }
                    if (msg.startsWith("t") == true & msg.startsWith("g", 1) == true){
                        end();
                        jButtonCalibrate.setEnabled(true);
                        jButtonFR1.setEnabled(true);
                    }
                    if (msg.startsWith("s") == true){
                        stop();
                        jTextFieldVR.setText(""+0.0);
                        jTextFieldVV.setText(""+VolT);
                        jToggleButtonSTOP.setSelected(true);
                    }
                    if (msg.startsWith("Ok, g") == true){
                        t1 = 0;
                        t2 = 0;
                    }
                    if (msg.startsWith("Ok, r") == true){
                        sendveloc();
                    }
                    if (msg.startsWith("t") == true & msg.substring(1)!="l" & msg.substring(1)!="g"){
                        if (t1 == 0){
                            t1 = Long.parseLong(msg.substring(1));
                            caltinit = t1;
                        }
                        else {
                            t2 = Long.parseLong(msg.substring(1));
                            caltfin = t2;
                            elapsed = caltfin-caltinit;
                            jTextField5.setText(elapsed+"");
                        }
                        
                    }
                }
            } catch (SerialPortException | ArduinoException ex) {
                Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    /*
    ** Timers
    */

    private Timer timer;
    private Timer timerREL;

    /**
     * Creates new form Arduino (constructor initialized)
     */

    public Display() {

        decimales.applyPattern(pattern);

        //Control variables
        Working = false;
        Local = false;
        calibrating = false;

        //Components + window
        initComponents();
        jTableProgram.getTableHeader().setBackground(Color.BLACK);
        speed.setEditor(new JSpinner.NumberEditor(speed,"0000"));
        center();
        sendveloc();
        
        jButtonCalibrateAbort.setVisible(false);

        //StdOut redirection
        PrintStream printstream = new PrintStream(new StandardOutput(jTextArea1));
        jTextArea1.append("Standard I/O will be redirected to this Window");
        System.setOut(printstream);
        System.setErr(printstream);
        
        //ABOUT TAB
        jTextArea2.append("GUI INTERFACE FOR ARDUINO DEVICE uCF-SPS \n");
        jTextArea2.append("----------------------------------------- \n");
        jTextArea2.append("DEVELOPED BY SAMUEL DAVID FERNÁNDEZ SILVA \n");
        jTextArea2.append("AS STUDENT FOR HIS FINAL DEGREE WORK \n");
        jTextArea2.append("AND JUAN DANIEL MOZO LLAMAZARES \n");
        jTextArea2.append("AS TUTOR OF THIS FINAL DEGREE WORK\n\n");
        jTextArea2.append("uCF-SPS Beta v1.1");
        


//        //COM Ports Available
        jComboBoxCOM.setModel(new javax.swing.DefaultComboBoxModel(new String[] {}));
        //arduino.ShowMessageDialogs(false);
        List<String> portsNames;
         //portsNames = new ArrayList<>
        portsNames = arduino.getSerialPorts();
                 
        for (String portsName : portsNames) {
            jComboBoxCOM.addItem(portsName);
//            String PortName = arduino.NameSerialPortAt(i);
//            if (i>1){
//                PortName = (String)PortName.subSequence(arduino.NameSerialPortAt(i-1).length()+1,arduino.NameSerialPortAt(i).length());
//            }
//            jComboBoxCOM.addItem(PortName);
        }
        jComboBoxCOM.addItem("DEMO");
        jComboBoxCOM.setSelectedItem("DEMO");


        //Button group
        onoff.add(jToggleButtonLOCAL);
        onoff.add(jToggleButtonREMOTE);

        startstop.add(jToggleButtonSTART);
        startstop.add(jToggleButtonSTOP);

        //Variables init
        reset();
        sendveloc();

        /*
        * ARDUINO INITIALIZATION
        */
        
        jComboBoxCOM.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
            demo = jComboBoxCOM.getSelectedItem().equals("DEMO");
            try {
                    jComboBoxCOM.setSelectedItem("COM3");
                } catch (Exception ex) {
                    Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
                }
            if (!demo){
                try {
                   // arduino.ShowMessageDialogs(false);
                    arduino.killArduinoConnection();
                } catch (Exception ex) {
                    Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                   // arduino.ShowMessageDialogs(false);
                    arduino.arduinoRXTX((String)jComboBoxCOM.getSelectedItem(), 9600, event);
                } catch (Exception ex) {
                    Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
                }
                //Enables Remote Control as soon as COM PORT Selected
                disablelocal();
            }
            }
        });


        /*
        * LISTENER FOR SLIDER
        */
        
        ChangeListener ch = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e){
            if (speedslider.getValueIsAdjusting()){
                sendveloc();
            }
        }
        };
        speedslider.addChangeListener(ch);

    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    /*
    * TIMERS FOR LOAD AND UNLOAD PROGRESSION
    */
    
    public class progress implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt){

            if (VolR>0){

                timetemp=timetemp+0.1;

                VolR = VolR-(flow(veloc,true)/(1000/*factor conversion*/*10/*100ms*/));

                VolV = syrnum*syrcap - VolR;

                pbvalue=(VolR/VolT)*100;

                time = timetemp;

                if(pbvalue>0){
                    jTextFieldVR.setText(""+decimales.format(VolR));
                    jTextFieldVV.setText(""+decimales.format(VolV));
                    jTextFieldX100.setText(""+pbvalue.intValue());
                    jProgressBar1.setValue(pbvalue.intValue());
                    jTextFieldT.setText(""+decimales.format(time));
                } else{ /*WILL NOT KEEP ADDING OR SUBSTRACTING VOLUME WHEN 0mL IS REACHED*/
                    pbvalue = 0.0;
                    jTextFieldX100.setText(""+pbvalue.intValue());
                    jProgressBar1.setValue(pbvalue.intValue());
                    jTextFieldT.setText(""+decimales.format(time));
                }
            } else { /*TIME WILL NOT STOP WHEN 0 IS REACHED UNLESS ECHO RECEIVED*/
                timetemp=timetemp+0.1;
                time = timetemp;
                jTextFieldT.setText(""+decimales.format(time));
                if (demo){
                    end();
                }
            }
        }
    }

    public class reloadTime implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt){
            if(VolR<VolT){
                jToggleButtonSTART.setEnabled(false);
                VolR = VolR + 0.2;
                VolV = VolV - 0.2;
                pvalue = (VolR/VolT)*100;
                jTextFieldX100.setText(""+pvalue.intValue());
                jProgressBar1.setValue(pvalue.intValue());
                jTextFieldVR.setText(""+decimales.format(VolR));
                jTextFieldVV.setText(""+decimales.format(VolV));
            }
            else{
                if (demo) {
                    endreload();
                }
            }
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

        onoff = new javax.swing.ButtonGroup();
        startstop = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTabbedPaneMAIN = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jButtonFR = new javax.swing.JButton();
        jToggleButtonSTART = new javax.swing.JToggleButton();
        jToggleButtonSTOP = new javax.swing.JToggleButton();
        jLabel2 = new javax.swing.JLabel();
        jToggleButtonLOCAL = new javax.swing.JToggleButton();
        jToggleButtonREMOTE = new javax.swing.JToggleButton();
        speed = new javax.swing.JSpinner();
        speedslider = new javax.swing.JSlider();
        jTextFieldFLU = new javax.swing.JTextField();
        jTextFieldVR = new javax.swing.JTextField();
        jTextFieldVV = new javax.swing.JTextField();
        jProgressBar1 = new javax.swing.JProgressBar();
        jTextFieldX100 = new javax.swing.JTextField();
        jTextFieldT = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jComboBoxCOM = new javax.swing.JComboBox();
        jComboBoxVOLT = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jButtonCalibrate = new javax.swing.JButton();
        jComboCalibrationSteps = new javax.swing.JComboBox();
        jButtonCalibrateImport = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jButtonCalibrateSave = new javax.swing.JButton();
        jTextField3 = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jButtonFR1 = new javax.swing.JButton();
        jButtonCalibrateAbort = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTableProgram = new javax.swing.JTable();
        jSpinnerStep = new javax.swing.JSpinner();
        jButtonRefresh = new javax.swing.JButton();
        jButtonSaveAs = new javax.swing.JButton();
        jButtonLoad = new javax.swing.JButton();
        jTextFieldTimeElapsed = new javax.swing.JTextField();
        jTextFieldFinalVolume = new javax.swing.JTextField();
        jLabelStep = new javax.swing.JLabel();
        jCheckBoxProgram = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Arduino uCF-SPS. Beta 1.1");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 190));

        jTabbedPaneMAIN.setMinimumSize(new java.awt.Dimension(400, 200));
        jTabbedPaneMAIN.setPreferredSize(new java.awt.Dimension(400, 200));

        jPanel1.setMaximumSize(new java.awt.Dimension(400, 190));
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 190));

        jButtonFR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ArduinoUno/fast-r.png"))); // NOI18N
        jButtonFR.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonFR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFRActionPerformed(evt);
            }
        });

        jToggleButtonSTART.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ArduinoUno/play.png"))); // NOI18N
        jToggleButtonSTART.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jToggleButtonSTART.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/ArduinoUno/play-pressed.png"))); // NOI18N
        jToggleButtonSTART.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/ArduinoUno/play-pressed.png"))); // NOI18N
        jToggleButtonSTART.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonSTARTActionPerformed(evt);
            }
        });

        jToggleButtonSTOP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ArduinoUno/stop.png"))); // NOI18N
        jToggleButtonSTOP.setSelected(true);
        jToggleButtonSTOP.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jToggleButtonSTOP.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/ArduinoUno/stop-pressed.png"))); // NOI18N
        jToggleButtonSTOP.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/ArduinoUno/stop-pressed.png"))); // NOI18N
        jToggleButtonSTOP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonSTOPActionPerformed(evt);
            }
        });

        jLabel2.setText("Speed:");

        jToggleButtonLOCAL.setText("Local");
        jToggleButtonLOCAL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonLOCALActionPerformed(evt);
            }
        });

        jToggleButtonREMOTE.setSelected(true);
        jToggleButtonREMOTE.setText("Remote");
        jToggleButtonREMOTE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonREMOTEActionPerformed(evt);
            }
        });

        speed.setModel(new javax.swing.SpinnerNumberModel(1000, 0, 1999, 1));

        speedslider.setMaximum(1999);
        speedslider.setValue(1000);
        speedslider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                speedsliderStateChanged(evt);
            }
        });

        jTextFieldFLU.setEditable(false);
        jTextFieldFLU.setText("3.795");
        jTextFieldFLU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFLUActionPerformed(evt);
            }
        });

        jTextFieldVR.setEditable(false);
        jTextFieldVR.setText("0");
        jTextFieldVR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldVRActionPerformed(evt);
            }
        });

        jTextFieldVV.setEditable(false);
        jTextFieldVV.setText("0");
        jTextFieldVV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldVVActionPerformed(evt);
            }
        });

        jTextFieldX100.setEditable(false);
        jTextFieldX100.setText("0");
        jTextFieldX100.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldX100ActionPerformed(evt);
            }
        });

        jTextFieldT.setEditable(false);
        jTextFieldT.setText("0");
        jTextFieldT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTActionPerformed(evt);
            }
        });

        jLabel4.setText("% filled");

        jLabel5.setText("Time:");

        jLabel6.setText("s");

        jLabel11.setText("Load:");

        jLabel12.setText("Unload:");

        jLabel14.setText("uL/s");

        jLabel22.setText("mL");

        jLabel23.setText("mL");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jToggleButtonLOCAL)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButtonREMOTE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jToggleButtonSTART, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jToggleButtonSTOP, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldVR, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel23)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldVV, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel22)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldT, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextFieldX100, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButtonFR, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(8, 8, 8)
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(speed, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextFieldFLU, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel14)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(speedslider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jToggleButtonLOCAL, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jToggleButtonREMOTE))
                        .addGap(8, 8, 8)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonFR, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jToggleButtonSTOP, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jToggleButtonSTART, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(speedslider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(jTextFieldFLU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(speed, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))))
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldVR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12)
                    .addComponent(jTextFieldVV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jTextFieldX100, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel22)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPaneMAIN.addTab("        Main      ", jPanel1);

        jPanel3.setPreferredSize(new java.awt.Dimension(400, 190));

        jComboBoxCOM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCOMActionPerformed(evt);
            }
        });

        jComboBoxVOLT.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1x5", "2x5", "1x10", "2x10", "1x20", "2x20" }));
        jComboBoxVOLT.setSelectedIndex(3);
        jComboBoxVOLT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxVOLTActionPerformed(evt);
            }
        });

        jLabel7.setText("Syringe capacity:");

        jLabel8.setText("mL");

        jLabel9.setText("Serial port:");

        jLabel10.setText("COM");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jComboBoxVOLT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxCOM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel9))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxCOM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addGap(25, 25, 25)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxVOLT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPaneMAIN.addTab("Options", jPanel3);

        jPanel4.setPreferredSize(new java.awt.Dimension(400, 190));

        jButtonCalibrate.setText("Calibrate");
        jButtonCalibrate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCalibrateActionPerformed(evt);
            }
        });

        jComboCalibrationSteps.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2", "3" }));
        jComboCalibrationSteps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboCalibrationStepsActionPerformed(evt);
            }
        });

        jButtonCalibrateImport.setText("Import");
        jButtonCalibrateImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCalibrateImportActionPerformed(evt);
            }
        });

        jLabel1.setText(" ");

        jLabel3.setText("Liquid mass (g):");

        jLabel13.setText("Press Calibrate to Start...");

        jButtonCalibrateSave.setText("Save");
        jButtonCalibrateSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCalibrateSaveActionPerformed(evt);
            }
        });

        jTextField3.setText("20");

        jLabel19.setText("Room Temperature (ºC):");

        jLabel20.setText("Number of steps:");

        jLabel18.setText("Calibration conditions:");

        jLabel21.setText("Step elapsed time (ms):");

        jButtonFR1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ArduinoUno/Rev.gif"))); // NOI18N
        jButtonFR1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonFR1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFR1ActionPerformed(evt);
            }
        });

        jButtonCalibrateAbort.setText("Abort");
        jButtonCalibrateAbort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCalibrateAbortActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel20)
                            .addComponent(jLabel19))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboCalibrationSteps, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButtonCalibrate))
                                .addGap(18, 18, 18)
                                .addComponent(jButtonFR1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jButtonCalibrateAbort, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonCalibrateImport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonCalibrateSave)
                        .addGap(22, 22, 22))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButtonFR1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonCalibrate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel20)
                            .addComponent(jComboCalibrationSteps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel19))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonCalibrateAbort))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonCalibrateSave)
                    .addComponent(jButtonCalibrateImport))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPaneMAIN.addTab("Calibration", jPanel4);

        jPanel5.setPreferredSize(new java.awt.Dimension(400, 190));

        jTableProgram.getTableHeader().setBackground(Color.GRAY);
        jTableProgram.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "S in", "S end", "T (s)", "V (uL)"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, true, true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }

        });
        jTableProgram.setGridColor(new java.awt.Color(153, 153, 153));
        jScrollPane5.setViewportView(jTableProgram);

        jSpinnerStep.setModel(new javax.swing.SpinnerNumberModel(4, 1, 10, 1));
        jSpinnerStep.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerStepStateChanged(evt);
            }
        });

        jButtonRefresh.setText("Refresh");
        jButtonRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRefreshActionPerformed(evt);
            }
        });

        jButtonSaveAs.setText("Save...");
        jButtonSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveAsActionPerformed(evt);
            }
        });

        jButtonLoad.setText("Load...");
        jButtonLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadActionPerformed(evt);
            }
        });

        jTextFieldTimeElapsed.setEditable(false);
        jTextFieldTimeElapsed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTimeElapsedActionPerformed(evt);
            }
        });

        jTextFieldFinalVolume.setEditable(false);
        jTextFieldFinalVolume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFinalVolumeActionPerformed(evt);
            }
        });

        jLabelStep.setText("Steps");

        jCheckBoxProgram.setText("Complex");
        jCheckBoxProgram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxProgramActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jButtonSaveAs, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonLoad)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 190, Short.MAX_VALUE)
                        .addComponent(jTextFieldTimeElapsed, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldFinalVolume, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonRefresh)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabelStep)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSpinnerStep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jCheckBoxProgram))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jCheckBoxProgram)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jSpinnerStep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelStep))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRefresh))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldTimeElapsed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldFinalVolume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonSaveAs)
                    .addComponent(jButtonLoad))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPaneMAIN.addTab("Programming", jPanel5);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane3.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3)
        );

        jTabbedPaneMAIN.addTab("Monitoring", jPanel2);

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane4.setViewportView(jTextArea2);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4)
        );

        jTabbedPaneMAIN.addTab("About", jPanel6);

        jScrollPane1.setViewportView(jTabbedPaneMAIN);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButtonREMOTEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonREMOTEActionPerformed
        disablelocal();
    }//GEN-LAST:event_jToggleButtonREMOTEActionPerformed

    private void jToggleButtonLOCALActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonLOCALActionPerformed
        enablelocal();
    }//GEN-LAST:event_jToggleButtonLOCALActionPerformed

    private void jToggleButtonSTOPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonSTOPActionPerformed
        stop();
    }//GEN-LAST:event_jToggleButtonSTOPActionPerformed

    private void jToggleButtonSTARTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonSTARTActionPerformed
        if (Working == false) {
            if (VolR>0){
                start();
            }
            else {
                JOptionPane.showMessageDialog(null, "Please, load first.");
                jToggleButtonSTOP.setSelected(true);
            }
        }
    }//GEN-LAST:event_jToggleButtonSTARTActionPerformed

    private void jButtonFRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFRActionPerformed
        if (Working == false){
        reload();
        }
    }//GEN-LAST:event_jButtonFRActionPerformed

    private void jComboBoxVOLTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxVOLTActionPerformed
        if (demo & jComboBoxVOLT.getSelectedItem().equals("2x10") == false){
            JOptionPane.showMessageDialog(null, "Only 2x10 can be selected in DEMO mode");
            jComboBoxVOLT.setSelectedItem("2x10");
        } else if (!demo) {
            reset();
            JOptionPane.showMessageDialog(null, "Calibration data is empty, import a calibration or perform a new one.");
        }
    }//GEN-LAST:event_jComboBoxVOLTActionPerformed

    private void jButtonCalibrateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCalibrateActionPerformed
        calibrate();
    }//GEN-LAST:event_jButtonCalibrateActionPerformed

    private void jButtonCalibrateSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCalibrateSaveActionPerformed
        // TODO add your handling code here:
        save();
    }//GEN-LAST:event_jButtonCalibrateSaveActionPerformed

    private void jButtonFR1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFR1ActionPerformed
        if (Working == false){
        reload();
        }
    }//GEN-LAST:event_jButtonFR1ActionPerformed

    private void jButtonCalibrateImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCalibrateImportActionPerformed
        importcal();
    }//GEN-LAST:event_jButtonCalibrateImportActionPerformed

    private void jButtonCalibrateAbortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCalibrateAbortActionPerformed
        abortcal();
    }//GEN-LAST:event_jButtonCalibrateAbortActionPerformed

    private void jButtonRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRefreshActionPerformed
        int totalRow = jTableProgram.getRowCount(); int totalCol = jTableProgram.getColumnCount();
        boolean vacio = false; boolean error = false;
        int dataCell;
        //Checking if flow values are positive and below 1999
        for (int col = 0; col < 2 ; col++){
            for (int row = 0 ; row < totalRow ; row++){
                dataCell = Integer.parseInt(String.valueOf(jTableProgram.getValueAt(row,col)));
                if ( dataCell < 0 || dataCell > 1999){
                    error = true;
                }
            }
        }
        //Checking if time values are positive
        for (int row = 0 ; row < totalRow ; row++){
            dataCell= Integer.parseInt(String.valueOf(jTableProgram.getValueAt(row,2)));
            if ( dataCell < 0){
                error = true;
            }
        }                     
               
        //Checking if there is no empty cells
        for (int col = 0; col < totalCol-1 ; col++){
            for (int row = 0; row < totalRow; row++){
                if (jTableProgram.getValueAt(row, col) == null) {                   
                    vacio = true;
                }       
            }        
        }
        // If there is no wrong cell values, it starts calculation
        if (!vacio & !error){      

            int data0; int data1; int data2;
            // It goes through every row and column and it stores it into data variable             
            for (int row = 0; row < totalRow ; row++){
              data0=Integer.parseInt(String.valueOf(jTableProgram.getValueAt(row, 0)));
              data1=Integer.parseInt(String.valueOf(jTableProgram.getValueAt(row, 1)));
              data2=Integer.parseInt(String.valueOf(jTableProgram.getValueAt(row, 2)));
            // It transforms speed (0-1999) into flow (uL/s) rounded value           
              double flow0 = flow(data0,false);
              double flow1 = flow(data1,false);
              double calc = Math.round((flow0+flow1)*data2/2);              
              jTableProgram.setValueAt(calc, row, 3);
            }
            int totalTime = 0; double totalVolume = 0; int timeRow; double volumeRow;
            //It calculates total time elapsed
            for (int row = 0; row <= totalRow-1; row++){
              timeRow = Integer.parseInt(String.valueOf(jTableProgram.getValueAt(row, 2)));
              totalTime = totalTime + timeRow;
                
            }
            
            //It calculates total dispatched volume
            for (int row = 0; row <= totalRow-1; row++){
              volumeRow = Double.parseDouble(String.valueOf(jTableProgram.getValueAt(row, 3)));
              totalVolume = totalVolume + volumeRow;
                
            }
            //It pops up a warning window if totalVolume is higher than total syringe volume
            if (totalVolume > VolT*1000){
                JOptionPane.showMessageDialog(this,"Total volume exceeds syringe capacity","Warning",JOptionPane.WARNING_MESSAGE);
            }
            jTextFieldTimeElapsed.setText(String.valueOf(totalTime));
            jTextFieldFinalVolume.setText(String.valueOf(totalVolume));
        //It pops up a warning window if there is any error related to cell values    
        } else{
            JOptionPane.showMessageDialog(this,"Cell value is incorrect","Warning",JOptionPane.WARNING_MESSAGE);
            
            
        }
    
                 // TODO add your handling code here:
    }//GEN-LAST:event_jButtonRefreshActionPerformed

    private void jTextFieldTimeElapsedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTimeElapsedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTimeElapsedActionPerformed

    private void jSpinnerStepStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerStepStateChanged
    DefaultTableModel model = (DefaultTableModel) jTableProgram.getModel();
    int spinnerData = Integer.parseInt(String.valueOf(jSpinnerStep.getValue()));
    model.setRowCount(spinnerData);    // TODO add your handling code here:
    }//GEN-LAST:event_jSpinnerStepStateChanged

    private void jButtonSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveAsActionPerformed
    JFileChooser saveFileChooser = new JFileChooser();
    saveFileChooser.setDialogTitle("Save as...");    
    saveFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
    
    //saveFC.setFont(new java.awt.Font("Lucida Handwriting", 0, 18)); // (optional) Choosing text font
    //seleccion.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //(opcional) It used to show only directories

    int respuesta = saveFileChooser.showSaveDialog(this); // Window is open
    switch(respuesta){ // Depending on user's choice different algorythms are executed
        case JFileChooser.APPROVE_OPTION:
          ArrayList dataSave = new ArrayList();
          File saveFile = saveFileChooser.getSelectedFile();
          String filter = ".flw";
        // Either if selected file does not have extension or does not exists, it creates one with .flw extension         
          if (!saveFile.getAbsolutePath().endsWith(filter)){
             File saveFileName = new File(saveFile.getAbsolutePath().concat(filter));
             saveFile=saveFileName;
          }   
          
        // If selected file already exists:        
          if (saveFile.exists()){
        // A window pops up asking if you want to overwrite its content           
              int n = JOptionPane.showConfirmDialog(this, "Selected file already exists. Do you want to overwrite it?",
                  "Warning", JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
              PrintWriter eraser = null;
        // In case is affirmative, it erases its content            
              if (n == JOptionPane.YES_OPTION) {
              try {
                eraser = new PrintWriter(saveFile);
                eraser.print("");
                eraser.close();  
              } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
              }
        // In case is negative or the warning window is closed it returns to main window   
            } else {
                return;              
            }
          
          }
             
          try {
            FileWriter FW = new FileWriter (saveFile,true);
            BufferedWriter BW = new BufferedWriter(FW);
            PrintWriter PW = new PrintWriter(BW);
        // It writes number of steps set in jSpinnerStep    
            PW.println(jSpinnerStep.getValue().toString());
        // It reads from every cell from jTableProgram and writes every value and separates them into rows    
            for (int row=0; row<Integer.parseInt(jSpinnerStep.getValue().toString()); row++){
                String Linea= jTableProgram.getValueAt(row, 0).toString();
                for (int col=1; col < 3; col++){
                    Linea=Linea.concat("\t");
                    Linea=Linea.concat(jTableProgram.getValueAt(row, col).toString());
                }
                PW.println(Linea);
                
            }
            PW.close();
            BW.close();
            FW.close();           
                                 
          }
          catch (IOException e){
              JOptionPane.showMessageDialog(this,"File doesn't exist");  
          }
          catch (NumberFormatException e){
              JOptionPane.showMessageDialog(this,"Wrong number format");
          }        
                              
        case JFileChooser.CANCEL_OPTION:
          System.out.println("Cancel");
          break;
        default :
          System.out.println("Error");
          break;
    }    // TODO add your handling code here:
    }//GEN-LAST:event_jButtonSaveAsActionPerformed

    private void jButtonLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadActionPerformed
    // It pops up the load window and sets user file as main directory
    JFileChooser loadFileChooser = new JFileChooser();
    loadFileChooser.setDialogTitle("Select file to load");
    loadFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    loadFileChooser.setDialogType(JFileChooser.FILES_ONLY);
    // It filters which file extensions are allowed to be shown in browser and accepted to read
   
    loadFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Flow sequences","flw"));
    loadFileChooser.setAcceptAllFileFilterUsed(true);
    
    int respuesta = loadFileChooser.showSaveDialog(this); // Load window is opened
    switch(respuesta){ // Depending on user's choice different algorythms are executed
        case JFileChooser.APPROVE_OPTION:
          //In case there is no file to load, it shows a warning window that returns to the main window  
          File loadFile = loadFileChooser.getSelectedFile();
          if (!loadFile.exists()){
              JOptionPane.showMessageDialog(this,"File doesn't exist", "Warning",JOptionPane.WARNING_MESSAGE);
              return;
          }
          
        try{
            FileReader FR= new FileReader(loadFile);
            BufferedReader BR = new BufferedReader(FR);
            Scanner SC = new Scanner(BR);
            //Scans the first value that correspond to step number and sets it in jSpinnerStep
            if (SC.hasNext()){
                jSpinnerStep.setValue(SC.nextInt());
                
            }
            //Scans the rest of the values the file has, going through every row and column, and sets it
            //in the corresponding cell of jTableProgram
            for (int row=0; row < Integer.parseInt(jSpinnerStep.getValue().toString()); row++){
                for (int col=0 ; col< 3; col++){
                   jTableProgram.setValueAt(SC.nextInt(), row, col);
                }
            }
            SC.close();
            BR.close();
            FR.close();
            
            //Once every cell is filled with the corresponding value, it exectues the refresh button
            jButtonRefresh.doClick();
            
        } catch (FileNotFoundException ex) {
        java.util.logging.Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
        java.util.logging.Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
    }
        
//        catch (FileNotFoundException ex) {
//            Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE,null,ex);
//        }
//        
//        catch (IOException ex) {
//            Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE,null,ex);
//        } 
           
    
        
        case JFileChooser.CANCEL_OPTION:
          System.out.println("Cancel");
          break;
        
        default :
          System.out.println("Error");
          break;    
    }    // TODO add your handling code here:
    }//GEN-LAST:event_jButtonLoadActionPerformed

    private void jTextFieldFinalVolumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFinalVolumeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFinalVolumeActionPerformed

    private void jCheckBoxProgramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxProgramActionPerformed

        speedslider.setEnabled(!jCheckBoxProgram.isSelected());
        speed.setEnabled(!jCheckBoxProgram.isSelected());

    }//GEN-LAST:event_jCheckBoxProgramActionPerformed

    private void jTextFieldVRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldVRActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVRActionPerformed

    private void jComboBoxCOMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCOMActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxCOMActionPerformed

    private void jTextFieldVVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldVVActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVVActionPerformed

    private void jTextFieldTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTActionPerformed

    private void jTextFieldX100ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldX100ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldX100ActionPerformed

    private void jTextFieldFLUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFLUActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFLUActionPerformed

    private void jComboCalibrationStepsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboCalibrationStepsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboCalibrationStepsActionPerformed

    private void speedsliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_speedsliderStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_speedsliderStateChanged

    /*
    * START/STOP
    */

    private void start(){
        Working = true;
        timer=new Timer(100, new progress());
        timer.start();
        jButtonFR.setEnabled(false);
        if (jCheckBoxProgram.isSelected()){
            int resolution = 10;
            speedslider.setValue(Integer.parseInt(jTableProgram.getValueAt(0,0).toString()));
            sendveloc();
            //Starts Unloading as "g" is sent
            if (!demo){
                try {
                    arduino.sendData("g");
                } catch (ArduinoException | SerialPortException ex) {
                    Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            for(int t = 0 ; t < Integer.parseInt(jSpinnerStep.getValue().toString()); t++ ){
                int vinic = Integer.parseInt(jTableProgram.getValueAt(t,0).toString());
                int vfin = Integer.parseInt(jTableProgram.getValueAt(t,1).toString());
                for (int v = vinic; v < vfin ; v = v +((vfin-vinic)/resolution)){
                    speedslider.setValue(v);
                   
                    sendveloc();
                    try {
                        Thread.sleep(1000*Integer.parseInt(jTableProgram.getValueAt(t, 2).toString())/resolution);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
                    }
                              
                }   
            stop();                       
            }
        }    
        else{
           //Starts Unloading as "g" is sent
            if (!demo){
                try {
                    arduino.sendData("g");
                } catch (ArduinoException | SerialPortException ex) {
                    Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //checkbuttons();
        }
    }

    private void stop (){
        timer.stop();
        Working = false;
        jButtonFR.setEnabled(true);
        if (calibrating){
            currentstep++;
            if (currentstep>maxsteps){
                jButtonCalibrate.setText("Finish");
            } else {
                jButtonCalibrate.setText("Run step "+currentstep);
            }
        }
        //Stops Action(Load/Unload) as "s" is sent
        if (!demo){
            try {
                arduino.sendData("s");
            } catch (ArduinoException | SerialPortException ex) {
                Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //checkbuttons();
    }

    private void end (){
        stop();
        jTextFieldVR.setText(""+0.0);
        jTextFieldVV.setText(""+VolT);
        jToggleButtonSTOP.setSelected(true);
    }

    private void reload(){
        softreset();
        timerREL = new Timer(200,new reloadTime());
        timerREL.start();
        Working = true;

        //Starts Loading As "l" is Sent
        if (!demo){
            try {
                arduino.sendData("l");
            } catch (ArduinoException | SerialPortException ex) {
                Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void endreload(){
        timerREL.stop();
        jToggleButtonSTART.setEnabled(true);
        Working = false;
        VolV = 0.0;
        VolR = VolT;
        pvalue = (VolR / VolT) * 100;
        jTextFieldX100.setText("" + pvalue.intValue());
        jProgressBar1.setValue(pvalue.intValue());
        jTextFieldVR.setText("" + decimales.format(VolR));
        jTextFieldVV.setText("" + decimales.format(VolV));
    }
    
    /*
    * CALIBRATE
    */
    
    private void calibrate(){
        if (!calibrating){
            calibrating = true;
            maxsteps = Integer.parseInt(jComboCalibrationSteps.getSelectedItem().toString());
            currentstep = 1;
            startcalib();
        } else {
            nextstep();
        }
    }

    private void startcalib(){
        Working = true;
        calibrating = true;
        jButtonCalibrateAbort.setVisible(true);
        jLabel13.setText("Calibrating...");
        if (maxsteps == 2 & currentstep == 1){
            speedslider.setValue(3*1999/6);
        }
        if (maxsteps == 2 & currentstep == 2){
            speedslider.setValue(1999/6*5);
        }
        if (maxsteps == 3 & currentstep == 1){
            speedslider.setValue(3*1999/6);
        }
        if (maxsteps == 3 & currentstep == 2){
            speedslider.setValue(4*1999/6);
        }
        if (maxsteps == 3 & currentstep == 3){
            speedslider.setValue(5*1999/6);
        }

        sendveloc();
        JOptionPane.showMessageDialog(null, "Calibrating...");
        start();
        jButtonCalibrate.setEnabled(false);
        jButtonFR1.setEnabled(false);
    }

    private void nextstep(){
        //save data
        if (elapsed1 == 0){
            elapsed1 = Long.parseLong(jTextField5.getText());
            watermass1 = Double.parseDouble(jTextField4.getText());
            System.out.println("Step data: time = "+elapsed1+" & watermass = "+watermass1+".");
        } else if (elapsed1 != 0){
            elapsed2 = Long.parseLong(jTextField5.getText());
            watermass2 = Double.parseDouble(jTextField4.getText());
            System.out.println("Step data: time = "+elapsed2+" & watermass = "+watermass2+".");
        } else if (elapsed2 != 0){
            elapsed3 = Long.parseLong(jTextField5.getText());
            watermass3 = Double.parseDouble(jTextField4.getText());
            System.out.println("Step data: time = "+elapsed3+" & watermass = "+watermass3+".");
        }
        if (currentstep<=maxsteps){
            startcalib();
        } else {
            calibrating = false;
            jLabel13.setText("Press Save to save data...");
            JOptionPane.showMessageDialog(null, "Calibration ended, press Save to save data");
        }
    }

    private void importcal(){
        Object[] options = {"1x5", "2x5", "1x10", "2x10", "1x20", "2x20"};
        Object Calibtype1 = JOptionPane.showInputDialog(null, "Syringe type: ", "Select type of calibration to import: ", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        String path = "C:/Calibrations/"+Calibtype1+".txt";
        try {
            read(path);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "El sistema no pudo encontrar la ruta especificada");
        }
    }

    private void read(String path){
        ReadFromFile lineread = new ReadFromFile();
        ArrayList calibration = new ArrayList();
        try{
            calibration = lineread.readLine(path);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        Object[] caliboptions = calibration.toArray();
        Object Calibtype2 = JOptionPane.showInputDialog(null, "Calibration: ", "Select calibration to import: ", JOptionPane.PLAIN_MESSAGE, null, caliboptions, caliboptions[0]);
        String[] Calibstring = Calibtype2.toString().split(", ", 4);
        double slope = Double.parseDouble(Calibstring[2]);
        double coord = Double.parseDouble(Calibstring[3]);
        JOptionPane.showMessageDialog(null, "Slope will be: \n"+slope+"\n"+"Origin coord. will be: \n"+coord);
        pend = slope;
        ord = coord;
    }

    private void write(String path){
        String calibf = getdata();
        System.out.println(calibf);
        String filename = JOptionPane.showInputDialog(null, "Set a name for this calibration:");
        while (filename.isEmpty() == true){
            JOptionPane.showMessageDialog(null, "Enter a valid name for the calibration");
            filename = JOptionPane.showInputDialog(null, "Set a name for this calibration:");
        }
        File calibrados = new File(path);
        if (calibrados.exists() == false){
            try{
            calibrados.getParentFile().mkdir();
            calibrados.createNewFile();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            try{
            WritetoFile calibrado = new WritetoFile(path, true);
            calibrado.writeToFile("{calibrations}");
            calibrado.writeToFile("{#name, date, slope, ord.}");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
        Calendar date = Calendar.getInstance();
        try {
            WritetoFile calibs = new WritetoFile(path, true);
            calibs.writeToFile('#'+filename+", "+dateFormat.format(date.getTime())+", "+pend+", "+ord);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Calibration: "+filename+" saved to "+path);
        calibreset();
    }

    private void save(){
        String combovolt = jComboBoxVOLT.getSelectedItem().toString();
        String path = "C:/Calibrations/"+combovolt+".txt";
        write (path);
    }

    private void calibreset(){
        elapsed1 = 0;
        elapsed2 = 0;
        elapsed3 = 0;
        watermass1 = 0.0;
        watermass2 = 0.0;
        watermass3 = 0.0;
        jButtonCalibrate.setText("Calibrate");
        jLabel13.setText("Press Calibrate to start...");
        jButtonCalibrateAbort.setVisible(false);
    }

    private String getdata(){
        if (maxsteps == 2){
            double realvol1 = watermass1/wdensity()*1000;
            double realvol2 = watermass2/wdensity()*1000;
            double invflow1 = 1/(realvol1/(elapsed1/1000));
            double invflow2 = 1/(realvol2/(elapsed2/1000));
            double ymed = (invflow1+invflow2)/2;
            double volt1 = 511.0;
            double volt2 = 852.0;
            double xmed = (volt1+volt2)/2;
            pend = (2*(volt1*invflow1+volt2*invflow2)-(volt1+volt2)*(invflow1+invflow2))/(2*(volt1*volt1+volt2*volt2)-(volt1+volt2)*(volt1+volt2));
            ord = ymed-pend*xmed;
        }
        else if (maxsteps == 3){
            double realvol1 = watermass1/wdensity()*1000;
            double realvol2 = watermass2/wdensity()*1000;
            double realvol3 = watermass3/wdensity()*1000;
            double invflow1 = 1/(realvol1/(elapsed1/1000));
            double invflow2 = 1/(realvol2/(elapsed2/1000));
            double invflow3 = 1/(realvol3/(elapsed3/1000));
            double ymed = (invflow1+invflow2+invflow3)/3;
            double volt1 = 511.0;
            double volt2 = 682.0;
            double volt3 = 852.0;
            double xmed = (volt1+volt2+volt3)/3;
            pend = (2*(volt1*invflow1+volt2*invflow2+volt3*invflow3)-(volt1+volt2+volt3)*(invflow1+invflow2+invflow3))/(2*(volt1*volt1+volt2*volt2+volt3*volt3)-(volt1+volt2+volt3)*(volt1+volt2+volt3));
            ord = ymed-pend*xmed;
        }
        String calibdata = "Slope is "+pend+" and origin is "+ord+".";
        return calibdata;
    }

    private double wdensity(){
        double temperature = Double.parseDouble(jTextField3.getText());
        double wdensity = -4.8238*Math.pow(10, -6)*Math.pow(temperature, 2)-1.0727*Math.pow(10, -5)*temperature+1.0003;
        return wdensity;
    }
    
    private void abortcal(){
        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure?");
        if (confirm == JOptionPane.YES_OPTION){
            calibrating = false;
            jButtonCalibrate.setEnabled(true);
            jButtonFR1.setEnabled(true);
            timer.stop();
            calibreset();
            reset();
        }
    }

    /*
    * SPEED SENDING
    */
    
    /*private int speed(){
        veloc = Math.round((speedslider.getValue()/(float)1999.0)*(float)1023.0);
        return veloc;
    }*/
    
    private void sendveloc(){
        veloc = speedslider.getValue();
        if (demo){
            pend = -2.571*Math.pow(10, -4)*2;
            ord = 0.2634*2;
            flow(veloc,true);
        }
        if(!demo){
            int HByte = (veloc)/256;
            int LByte = (veloc)-(HByte*256);
            flow(veloc,true);
            try {
                arduino.sendData("f"+HByte+","+LByte);
            } catch (ArduinoException | SerialPortException ex) {
                Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private double flow(int speed0, boolean printF){
        volt = Math.round((speed0/(float)1999.0)*(float)1023.0);
        //volt = veloc;
        Double inverseflow = pend*volt+ord;
        Double rflow = 1/(inverseflow)
                ;
        //Double rlflow = rflow*syrnum;
        if (printF){
            jTextFieldFLU.setText(decimales.format(rflow));
        }
        return rflow;
    }
    
    /*
    * LOCAL/REMOTE
    */

    private void enablelocal(){
        Local = true;
        try {
            Thread.sleep(200);
        }
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        try {
            arduino.sendData("m");
        } catch (ArduinoException | SerialPortException ex) {
            Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
        }
        checkbuttons();
    }

    private void disablelocal(){
        Local = false;
        try {
            Thread.sleep(200);
        }
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        try {
            arduino.sendData("r");
        } catch (ArduinoException | SerialPortException ex) {
            Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
        }
        checkbuttons();
    }

    /*
    * RESET AND GUI CUSTOMIZATION
    */

    private void reset(){
        Working = false;
        String syringes = jComboBoxVOLT.getSelectedItem().toString();
        syrnum = Integer.parseInt(syringes.substring(0, 1));
        syrcap = Integer.parseInt(syringes.substring(2));
        VolT = (double) syrnum*syrcap;

        VolV = VolT;
        jTextFieldVV.setText(""+VolV);

        VolR = 0.0;
        jTextFieldVR.setText(""+VolR);

        pbvalue = 0.0;
        jTextFieldX100.setText(""+pbvalue.intValue());
        jProgressBar1.setValue(pbvalue.intValue());

        time = 0.0;
        jTextFieldT.setText(""+time.intValue());
        timetemp = 0.0;
    }

    private void softreset(){
        VolT = (double) syrnum*syrcap;
        time = 0.0;
        jTextFieldT.setText(""+time.intValue());
        timetemp = 0.0;
    }

    private void center(){
        int ancho = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
        int altura = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
        //this.setBounds(1, 1, 430, 230);
        int anchoV = this.getWidth();
        int alturaV= this.getHeight();                
        this.setBounds((ancho/2)-(anchoV/2),(altura/2)-(alturaV/2), anchoV, alturaV);
    }

    private void checkbuttons(){
        /*if (Working == true & Local == false){
            jLabel3.setText("Current: Working");
            jLabel3.setForeground(Color.yellow);
        }
        else if (Working == false & Local == false){
            jLabel3.setText("Current: Ready");
            jLabel3.setForeground(Color.green);
        }
        if (Local == true){
            jLabel3.setText("Current: Local");
            jLabel3.setForeground(Color.red);
        }   */
        jToggleButtonSTART.setEnabled(!Local);
        jToggleButtonSTOP.setEnabled(!Local);
        jButtonFR.setEnabled(!Local);
        speedslider.setEnabled(!Local && !jCheckBoxProgram.isSelected());
        speed.setEnabled(!Local && !jCheckBoxProgram.isSelected());
    }

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
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Display.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Display().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCalibrate;
    private javax.swing.JButton jButtonCalibrateAbort;
    private javax.swing.JButton jButtonCalibrateImport;
    private javax.swing.JButton jButtonCalibrateSave;
    private javax.swing.JButton jButtonFR;
    private javax.swing.JButton jButtonFR1;
    private javax.swing.JButton jButtonLoad;
    private javax.swing.JButton jButtonRefresh;
    private javax.swing.JButton jButtonSaveAs;
    private javax.swing.JCheckBox jCheckBoxProgram;
    private javax.swing.JComboBox jComboBoxCOM;
    public static javax.swing.JComboBox jComboBoxVOLT;
    private javax.swing.JComboBox jComboCalibrationSteps;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelStep;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSpinner jSpinnerStep;
    private javax.swing.JTabbedPane jTabbedPaneMAIN;
    private javax.swing.JTable jTableProgram;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextFieldFLU;
    private javax.swing.JTextField jTextFieldFinalVolume;
    private javax.swing.JTextField jTextFieldT;
    private javax.swing.JTextField jTextFieldTimeElapsed;
    private javax.swing.JTextField jTextFieldVR;
    private javax.swing.JTextField jTextFieldVV;
    private javax.swing.JTextField jTextFieldX100;
    private javax.swing.JToggleButton jToggleButtonLOCAL;
    private javax.swing.JToggleButton jToggleButtonREMOTE;
    private javax.swing.JToggleButton jToggleButtonSTART;
    private javax.swing.JToggleButton jToggleButtonSTOP;
    private javax.swing.ButtonGroup onoff;
    private javax.swing.JSpinner speed;
    private javax.swing.JSlider speedslider;
    private javax.swing.ButtonGroup startstop;
    // End of variables declaration//GEN-END:variables
}
