/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import Data.Bandera;
import ArduinoConnection.ArduinoConnection;
import Data.DataCalculation;
import com.panamahitek.ArduinoException;
import com.panamahitek.PanamaHitek_MultiMessage;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;


/**
 *
 * @author Andrés
 */
public class Ventana extends javax.swing.JFrame {
    
    /**
     * Initializing libraries
     */
    
    private ArduinoConnection ino = new ArduinoConnection();
    private PanamaHitek_MultiMessage multi = new PanamaHitek_MultiMessage(1,ino.arduinoReturn());
    private DataCalculation datacalc = new DataCalculation(ino);
    public static Bandera working = new Bandera (false);
    private Bandera local = new Bandera (true);
    public static Bandera calibrating = new Bandera (false);
    private Bandera demo = new Bandera (true);    
    private Bandera fileloaded = new Bandera (false);
    public Timer timerProgress=new Timer(100, new progress());
    private Timer timerComplex=new Timer(100, new listenerComplex());
    private Timer timerDelay =new Timer(100, new delayAction());
    private CalibrationJFrame calibframe = new CalibrationJFrame(datacalc, ino, multi); 
    private ArrayList<Integer> dataComplex = new ArrayList<Integer>();
    private ArrayList<Integer> valveComplex = new ArrayList<Integer>();
    private int indexComplex = 0;
    
    DecimalFormat decimales = new DecimalFormat("0.0#");
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    
   
    
    /*
    LISTENER FOR SERIAL PORT MESSAGES
    */
    
    SerialPortEventListener inoListener = new SerialPortEventListener(){
        @Override
        public void serialEvent(SerialPortEvent spe) {
            try {                              
                
//              Si hay un mensaje en el bus de datos, lo vuelca en un string                
                if (multi.dataReceptionCompleted()){
                    String msgOK = multi.getMessage(0);
                    String msg = msgOK.substring(msgOK.indexOf(" ")+1,msgOK.indexOf(" ")+2);
                    
                    
                    System.out.println(msgOK);
                    System.out.println(msg);

                    
                    switch(msg) {
                         
//  Sends stop to arduino and resets jTextField values               
                        case "s" :                            
                            if(calibrating.getFlag()){                         
                                
                                
                            }
                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Pump stopped at " + msgOK.substring(5) + "\n");
                            datacalc.setTimeD(Long.parseLong(msgOK.substring(5)));
                            
                            
                            break;
                            
//  Shows Local mode activation in Log
                        case "m" :      
                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Local mode activated " + "\n");          
                            break;
                            
//  Shows Remote mode activation in Log                            
                        case "r" :         
                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Remote mode activated " + "\n");        
                            break;
                            
//  Shows reload activation in Log                            
                        case "l" :         
                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Reload activated " + "\n");        
                            break;
                            
//  Shows the beginning of the dispense action                         
                        case "g" :         
                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Pump started at " + msgOK.substring(5) + "\n");
                            datacalc.setTimeG(Long.parseLong(msgOK.substring(5)));        
                            break;
//  Shows every speed change.
                        case "u" :         
                            int veloc = Math.round((Integer.parseInt(msgOK.substring(5))/(float)1024.0)*(float)2000.0);
                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Speed increased to " + veloc + "\n");                            
                            jSpeedSlider.setValue(veloc);
//                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Flow rate is " + jTextFieldFlow.getText() + " "+ jComboBoxUnits.getItemAt(jComboBoxUnits.getSelectedIndex()) + " \n");
                            break;

                        case "d" :                    
                            int veloc1 = Math.round((Integer.parseInt(msgOK.substring(5))/(float)1024.0)*(float)2000.0);
                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Speed decreased to " + veloc1 + "\n");                            
                            jSpeedSlider.setValue(veloc1);
//                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Flow rate is " + jTextFieldFlow.getText() + " "+ jComboBoxUnits.getItemAt(jComboBoxUnits.getSelectedIndex()) + " \n");
                            break;
                            
//  Shows flow speed changes                         
                        case "f" :         
                            int veloc2 = Math.round((Integer.parseInt(msgOK.substring(5))/(float)1024.0)*(float)2000.0);
                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Speed changed to: " + veloc2 + "\n");        
                            break;
                            
//  Shows valve state configuration                          
                        case "w" :         
                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Valve configuration set to " + msgOK.substring(5) + "\n");        
                            break;
//  Shows ON/OFF button manual activation (RED LIGHT)                            
                        case "a" :         
                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" ON/OFF Switch button activated " + "\n");
                            if ("0".equals(msgOK.substring(5))){
//                             Motor stops manually
                                jToggleButtonSTOP.doClick();
                            }
                            else if ("1".equals(msgOK.substring(5))){
//                             Motor turns on manually
                                jToggleButtonSTART.doClick();                                
                            }  
                            break;
//  Shows LOAD button manual activation (YELLOW LIGHT)                         
                        case "b" :         
                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" LOAD Switch button activated " + "\n");
                            if ("0".equals(msgOK.substring(5))){
//                             Paro manual de recarga
                                jToggleButtonSTART.doClick();
                            }
                            else if ("1".equals(msgOK.substring(5))){
//                             Encendido manual de recarga
                                jToggleButtonRELOAD.doClick();                                
                            }  
                            break;    
                            

//  Shows the dispense limit and saves the time value. (milliseconds)                          
                        case "x" :
                            
                            datacalc.setTimeD(Long.parseLong(msgOK.substring(5)));
                            if (calibrating.getFlag()){
                                calibframe.jButtonNext4.setEnabled(true);
                                calibframe.jLabelGif4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/B1_small.png")));                                
                                calibframe.jLabel4.setText("Infusion completed. Click next to continue. ");
                                calibframe.jLabelTimeElapsed.setText("Time elapsed: "+ (datacalc.getTimeD()-datacalc.getTimeG()) +" (ms)");
                                
                            }
                                
                            else {   
                                
                                stop();                                
                                datacalc.setVolumeRemaining(0);
                                jTextFieldLoad.setText("0,0");
                                jTextFieldUnload.setText(""+decimales.format(datacalc.getSyringeVol(datacalc.getStroke())));
                                jTextFieldX100.setText("0");
                                jProgressBar1.setValue(0);
                            }
                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Pump stopped at " + msgOK.substring(5) + "\n");
                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Dispense limit switch reached at " + msgOK.substring(5) + "\n");       
                            break;
                            
 
 // Shows the load limit upon reaching and saves the time value (milliseconds)
 //                          
                        case "y" :         
                            if (calibrating.getFlag()){
                                calibframe.jButtonNext2.setEnabled(true);
                                calibframe.jLabelGif2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/B1_small.png")));
                                calibframe.jLabel2.setText("Withdrawal completed. Click next to continue. ");                                
                            }
// Refreshes every load/dispense value, then updates every textfield.                            
                            else{                                
                                stop();                               
                                datacalc.setVolumeRemaining(datacalc.getSyringeVol(datacalc.getStroke()));
                                jTextFieldLoad.setText(""+decimales.format(datacalc.getVolumeRemaining()));
                                jTextFieldUnload.setText("0,0");
                                jTextFieldX100.setText("100");
                                jProgressBar1.setValue(100);
                            }
                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Pump stopped at " + msgOK.substring(5) + "\n");
                            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Load limit switch reached at " + msgOK.substring(5) + "\n");

//  Code for spring correction:                         

//      -Sends dispense signal                      
                            
                            ino.sendSpeed(1990, demo);                           
                            
//      
//      -The dispense action takes place for an specific amount of time (Depending on selected microstep and speed)
                            double tperstep = 250;//(ms)
                            double tperlap = tperstep*datacalc.getMstep();
                            double ttotal = tperlap*4;
                            System.out.println(ttotal/1000);
//      -It stops once the time is reached.                                                       
                            ino.sendUnload(demo);
                            timerDelay.setRepeats(false);
                            timerDelay.setInitialDelay((int)ttotal);                            
                            ino.sendUnload(demo);
                            timerDelay.start();
                            
                            break;
                        
//  
//  Gets the speed value sent from Arduino and sets the slider in that said value                          
                            
                        case "v" :         
                            if (local.getFlag()){                          

                                int veloc3 = Math.round((Integer.parseInt(msgOK.substring(5))/(float)1024.0)*(float)2000.0);
                                jSpeedSlider.setValue(veloc3);
//                                jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Speed changed to: " + veloc3 + "\n"); 
//                                jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Flow rate is " + jTextFieldFlow.getText() + " "+ jComboBoxUnits.getItemAt(jComboBoxUnits.getSelectedIndex()) + " \n");
                            }        
                            break;     
                            
                         default :
                            
                            break;
                    }

// Once finished, data bus gets flushed.
                    multi.flushBuffer();
                }
            } catch (SerialPortException | ArduinoException ex) {
                Logger.getLogger(Ventana.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    };
     


    /**
     * Creates new form Interface
     */
    
    public Ventana() {
 
     
// Changing window action    
    
    this.setDefaultCloseOperation(this.DO_NOTHING_ON_CLOSE);
 
    this.addWindowListener( new WindowAdapter()
{
    public void windowClosing(WindowEvent e)
    {
        JFrame frame = (JFrame)e.getSource();
 
// Shows Confirm Window        
        int result = JOptionPane.showConfirmDialog(
            frame,
            "Are you sure you want to exit the application?",
            "Exit Application",
            JOptionPane.YES_NO_OPTION);
                 
        switch(result) {
            
// Closes program if answer is "Yes"            
         case 0 :
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             System.out.println("Yes");
             ino.sendLocal(demo);
            break;
// Doesn't close program if answer is "No"           
         case 1 :         
            System.out.println("No");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);            
            break;
// Doesn't close program if answer is Confirm Window is closed          
         default :
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            System.out.println("Close");
            break;
      } 
            
    }
});
 
        this.setVisible(true);
    
    
    
        //Control variables
        
        initComponents();
        
        jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Software initialization " + "\n");
        
        //Checks avaliable COM ports
        
        jComboBoxCOM.setModel(new javax.swing.DefaultComboBoxModel(new String[] {}));
        List<String> portsNames;
        portsNames = ino.arduinoReturn().getSerialPorts();
                 
        for (String portsName : portsNames) {
            jComboBoxCOM.addItem(portsName);
        }
        jComboBoxCOM.addItem("DEMO");
        jComboBoxCOM.setSelectedItem("DEMO");
                
/*
* Arduino initialization once any USB port is selected in JComboBoxCOM        
*/
        
        jComboBoxCOM.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                demo.setFlag(jComboBoxCOM.getSelectedItem().equals("DEMO"));

            if (!demo.getFlag()){
                try {
                   // arduino.ShowMessageDialogs(false);
                    ino.arduinoReturn().killArduinoConnection();
                } catch (Exception ex) {
                    Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                   // arduino.ShowMessageDialogs(false);
                    ino.arduinoReturn().arduinoRXTX((String)jComboBoxCOM.getSelectedItem(), 9600, inoListener);
                } catch (Exception ex) {
                    Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
                }
                

            }
            }
        });
        
        
        jButtonGroupStartStop.add(jToggleButtonSTART);
        jButtonGroupStartStop.add(jToggleButtonSTOP);
        jButtonGroupStartStop.add(jToggleButtonRELOAD);
       
        
        jToggleButtonSTOP.setSelected(true);
        
        
        jTableProgram.getTableHeader().setBackground(Color.BLACK);
        jSpinnerSpeed.setEditor(new JSpinner.NumberEditor(jSpinnerSpeed,"0000"));
        center();
        
        //Variables init
        enableLocal();
        reset();
        ImageIcon img = new ImageIcon("/Imagenes/stop.ico");      
        
                
                    
    

           
    }
    
    
    
    
    /*
    * TIMERS FOR LOAD AND UNLOAD PROGRESSION
    */
    
    public class progress implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt){            
           
            double VolT = datacalc.getSyringeVol(datacalc.getStroke());
            double VolV = VolT - datacalc.getVolumeRemaining();
            double pbvalue = (datacalc.getVolumeRemaining()/VolT)*100; 


//  If it is in dispense mode, progressbar progress decreases and values get updated

            if(jToggleButtonSTART.isSelected()){
                if (datacalc.getVolumeRemaining()<0 & demo.getFlag()){
                    datacalc.setVolumeRemaining(0);
                    jProgressBar1.setValue(0);
                    pbvalue=0;                                      
                    VolV=VolT;                    
                    stop();
                    

                } else {            

                    datacalc.setTimeProgress(datacalc.getTimeProgress()+0.1);
                    jTextFieldTime.setText(""+decimales.format(datacalc.getTimeProgress()));                                        
                    datacalc.setVolumeRemaining((datacalc.getVolumeRemaining()-datacalc.flow(jSpeedSlider.getValue())*0.1*0.001)); 

                }
            }
            
//  If it is in loading mode, progressbar progress increases and values get updated

            if(jToggleButtonRELOAD.isSelected()){
                datacalc.setTimeProgress(0);
                if(datacalc.getVolumeRemaining()>VolT & demo.getFlag()){             
                    datacalc.setVolumeRemaining(VolT);                
                    pbvalue=100;                    
                    VolV=0;
                    stop();
                
                } else {            
                    datacalc.setVolumeRemaining((datacalc.getVolumeRemaining()+datacalc.flow(1999)*0.001*0.1));                    
                    
                }
            }

//  Shows updated values in their corresponding fields

            jTextFieldLoad.setText(""+decimales.format(datacalc.getVolumeRemaining()));
            jTextFieldUnload.setText(""+decimales.format(VolV));
            jTextFieldX100.setText((String.valueOf((int)pbvalue)));
            jProgressBar1.setValue((int) pbvalue);

        }
    }
    
    

//  Listener for complex flow programming
    public class listenerComplex implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt){
            

//  If indexComplex is greater than dataComplex size it stops
            if (indexComplex > dataComplex.size()) {
                
                stop();
                
            }

//  If indexComplex is less than dataComplex, it sends speed value and adds 1 to indexComplex         
            else{                
                int veloc = dataComplex.get(indexComplex);
                int valve = valveComplex.get(indexComplex);
                ino.sendSpeed(veloc,demo);
                ino.sendValve(valve,demo);
                jSpeedSlider.setValue(veloc);                
                indexComplex += 1;
            }
        
            
        }
    }
    

//  Listener for spring correction
    public class delayAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt){
            ino.sendStop(demo);
            ino.sendSpeed(jSpeedSlider.getValue(), demo);
        }
    }

      
//Function to enable local mode   
    private void enableLocal(){

// Sends Local mode to arduino      
        ino.sendLocal(demo);

// Then disables every clickable object from interface
        jToggleButtonSTART.setEnabled(false);
        jToggleButtonSTOP.setEnabled(false);
        jToggleButtonRELOAD.setEnabled(false);
        jSpeedSlider.setEnabled(false);
        jSpinnerSpeed.setEnabled(false);
            
            }
    
//Function to enable remote mode     
    private void enableRemote(){
                
// // Sends Remote mode to arduino
        ino.sendRemote(demo);
// Then enables every clickable object from interface         
        jToggleButtonSTART.setEnabled(true);
        jToggleButtonSTOP.setEnabled(true);
        jToggleButtonRELOAD.setEnabled(true);
        jSpeedSlider.setEnabled(true);
        jSpinnerSpeed.setEnabled(true);
    }
    
//  Function to start liquid infusion.  
    private void start(){
        
        working.setFlag(true);        
        //Timer that controls progressBar gets activated
        timerProgress.start(); 

        //Then sends the motor the order to go forward, infusing liquid
        ino.sendUnload(demo);
 

        }
    
        

//  Function to stop liquid infusion or load.
    public void stop (){
        
        if (working.getFlag()){
        
            //timerProgress gets stopped to stop progressBar
            timerProgress.stop();
            //timerComplex also gets stopped in case we are working on complex mode
            timerComplex.stop();
            working.setFlag(false);
            jToggleButtonSTOP.setSelected(true);            
            //Then sends the motor the order to stop
            ino.sendStop(demo);
            
        }
    }    
    

    //  Function to start liquid load.
    private void reload(){
        
        double time = 0;        
        jTextFieldTime.setText(""+decimales.format(time));
        //Activa el timer que controla la barra de progreso
        timerProgress.start();
        working.setFlag(true);      
        //Envía la orden de activar el motor en modo descarga
        ino.sendLoad(demo);
        
    }
    
    // Función que resetea todos los valores mostrados en jTextField 
    private void reset(){
        working.setFlag(false);
        
        double VolT = datacalc.getSyringeVol(datacalc.getStroke()) ;

        jTextFieldUnload.setText(decimales.format(VolT));

        double VolR = 0.0;
        jTextFieldLoad.setText(decimales.format(VolR));

        double pbvalue = 0.0;
        jTextFieldX100.setText(decimales.format(pbvalue));
        jProgressBar1.setValue((int) pbvalue);

        double  time = 0.0;
        jTextFieldTime.setText(decimales.format(time));
        
    }
    
    // Función que centra la ventana del programa respecto a la resolución de la pantalla
    public void center(){
        int ancho = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
        int altura = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
        int anchoV = this.getWidth();
        int alturaV= this.getHeight();                
        this.setBounds((ancho/2)-(anchoV/2),(altura/2)-(alturaV/2), anchoV, alturaV);

    }
    
    
    
    // Función que permite separar un 
    public float separar(String original){
        
        
        try {
            if (original.contains(".")){
            
            String[] parts = original.split("\\.");
            String numero = parts[0];
            String decimales = parts[1];
            int n = decimales.length();

            float resultado = (float) (Float.parseFloat(numero)+(Float.parseFloat(decimales)/Math.pow(10, n)));
            System.out.println(resultado);
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

        }}
        
        catch (NumberFormatException e) {
           
        }
        return -1;
    }
        
    
    private void binarycalc(){
    int valvevalue = 0;
        
            if(jToggleButtonV1.isSelected()){
                
                valvevalue = valvevalue+1;
           
            }
            
            if(jToggleButtonV2.isSelected()){
                
                valvevalue = valvevalue+2;
           
            }
            
            if(jToggleButtonV3.isSelected()){
                
                valvevalue = valvevalue+4;
           
            }
            
            if(jToggleButtonV4.isSelected()){
                
                valvevalue = valvevalue+8;
           
            }
            if(jToggleButtonInj.isSelected()){
                
                valvevalue = valvevalue+16;
           
            }
            
         datacalc.setValves(valvevalue);
         jLabelValveValue.setText(String.valueOf(datacalc.getValves()));
         ino.sendValve(valvevalue,demo);
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButtonGroupStartStop = new javax.swing.ButtonGroup();
        jButtonGroupMode = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelMain = new javax.swing.JPanel();
        jToggleButtonSTOP = new javax.swing.JToggleButton();
        jToggleButtonSTART = new javax.swing.JToggleButton();
        jToggleButtonRELOAD = new javax.swing.JToggleButton();
        jSpeedSlider = new javax.swing.JSlider();
        jSpinnerSpeed = new javax.swing.JSpinner();
        jTextFieldFlow = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldLoad = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldUnload = new javax.swing.JTextField();
        jTextFieldTime = new javax.swing.JTextField();
        jTextFieldX100 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jComboBoxUnits = new javax.swing.JComboBox<>();
        jPanelConfig = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jComboBoxCOM = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        jComboBoxVOLT = new javax.swing.JComboBox<>();
        jLabel13 = new javax.swing.JLabel();
        jLayeredPane2 = new javax.swing.JLayeredPane();
        jLabelValveValue = new javax.swing.JLabel();
        jCheckBoxAuto = new javax.swing.JCheckBox();
        jToggleButtonV1 = new javax.swing.JToggleButton();
        jToggleButtonV2 = new javax.swing.JToggleButton();
        jToggleButtonV3 = new javax.swing.JToggleButton();
        jToggleButtonV4 = new javax.swing.JToggleButton();
        jToggleButtonInj = new javax.swing.JToggleButton();
        jLabel25 = new javax.swing.JLabel();
        jComboBoxStep = new javax.swing.JComboBox<>();
        jLabel18 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jTextFieldStroke = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jLabelVolume = new javax.swing.JLabel();
        jLabelVOLT = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jButtonLocalRemote = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanelCalibration = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jButtonCalibrateLoad = new javax.swing.JButton();
        jButtonCalibrate = new javax.swing.JButton();
        jLabelCalibrated1 = new javax.swing.JLabel();
        jComboSteps = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jTextFieldTemperature = new javax.swing.JTextField();
        jLabelCalibrated2 = new javax.swing.JLabel();
        jPanelProgramming = new javax.swing.JPanel();
        jCheckBoxComplex = new javax.swing.JCheckBox();
        jSpinnerStep = new javax.swing.JSpinner();
        jLabel20 = new javax.swing.JLabel();
        jButtonRefresh = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableProgram = new javax.swing.JTable();
        jButtonProgramSave = new javax.swing.JButton();
        jButtonProgramLoad = new javax.swing.JButton();
        jTextFieldProgramTime = new javax.swing.JTextField();
        jTextFieldProgramVolume = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jPanelLog = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaLog = new javax.swing.JTextArea();
        jPanelAbout = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Arduino uCF-SPS");

        jTabbedPane1.setPreferredSize(new java.awt.Dimension(520, 260));

        jPanelMain.setPreferredSize(new java.awt.Dimension(520, 260));

        jToggleButtonSTOP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/stop.png"))); // NOI18N
        jToggleButtonSTOP.setPreferredSize(new java.awt.Dimension(65, 23));
        jToggleButtonSTOP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonSTOPActionPerformed(evt);
            }
        });

        jToggleButtonSTART.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/play.png"))); // NOI18N
        jToggleButtonSTART.setPreferredSize(new java.awt.Dimension(65, 23));
        jToggleButtonSTART.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonSTARTActionPerformed(evt);
            }
        });

        jToggleButtonRELOAD.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/rev2.png"))); // NOI18N
        jToggleButtonRELOAD.setPreferredSize(new java.awt.Dimension(65, 23));
        jToggleButtonRELOAD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonRELOADActionPerformed(evt);
            }
        });

        jSpeedSlider.setMaximum(2000);
        jSpeedSlider.setValue(1000);
        jSpeedSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpeedSliderStateChanged(evt);
            }
        });

        jSpinnerSpeed.setModel(new javax.swing.SpinnerNumberModel(1000, 0, 2000, 1));
        jSpinnerSpeed.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerSpeedStateChanged(evt);
            }
        });

        jTextFieldFlow.setEditable(false);
        jTextFieldFlow.setText("0,01");
        jTextFieldFlow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFlowActionPerformed(evt);
            }
        });

        jLabel1.setText("Speed:");

        jTextFieldLoad.setEditable(false);
        jTextFieldLoad.setText("0");
        jTextFieldLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldLoadActionPerformed(evt);
            }
        });

        jLabel3.setText("Load:");

        jTextFieldUnload.setEditable(false);
        jTextFieldUnload.setText("0");
        jTextFieldUnload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldUnloadActionPerformed(evt);
            }
        });

        jTextFieldTime.setEditable(false);
        jTextFieldTime.setText("0");
        jTextFieldTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTimeActionPerformed(evt);
            }
        });

        jTextFieldX100.setEditable(false);
        jTextFieldX100.setText("0");
        jTextFieldX100.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldX100ActionPerformed(evt);
            }
        });

        jLabel4.setText("mL");

        jLabel5.setText("Unload:");

        jLabel6.setText("mL");

        jLabel7.setText("Time:");

        jLabel8.setText("s");

        jLabel9.setText("% filled");

        jComboBoxUnits.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "μL/s", "μL/min", "μL/h", "mL/s", "mL/min", "mL/h", "L/s", "L/min", "L/h" }));
        jComboBoxUnits.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxUnitsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelMainLayout = new javax.swing.GroupLayout(jPanelMain);
        jPanelMain.setLayout(jPanelMainLayout);
        jPanelMainLayout.setHorizontalGroup(
            jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMainLayout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelMainLayout.createSequentialGroup()
                        .addComponent(jToggleButtonSTART, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButtonSTOP, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButtonRELOAD, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelMainLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addGap(35, 35, 35)
                        .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelMainLayout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addComponent(jTextFieldUnload, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6))
                            .addComponent(jLabel5))))
                .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelMainLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelMainLayout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldTime, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelMainLayout.createSequentialGroup()
                                        .addGap(46, 46, 46)
                                        .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(jPanelMainLayout.createSequentialGroup()
                                        .addComponent(jTextFieldX100, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(39, 39, 39))))
                            .addGroup(jPanelMainLayout.createSequentialGroup()
                                .addComponent(jSpeedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMainLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinnerSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldFlow, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxUnits, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(47, 47, 47))))
            .addGroup(jPanelMainLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 605, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanelMainLayout.setVerticalGroup(
            jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMainLayout.createSequentialGroup()
                .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelMainLayout.createSequentialGroup()
                        .addGap(61, 61, 61)
                        .addComponent(jSpeedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextFieldFlow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSpinnerSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(jComboBoxUnits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelMainLayout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jToggleButtonSTART, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jToggleButtonSTOP, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jToggleButtonRELOAD, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldLoad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jTextFieldUnload, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldX100, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        jTabbedPane1.addTab("Main", jPanelMain);

        jLabel10.setText("Serial port :");

        jComboBoxCOM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCOMActionPerformed(evt);
            }
        });

        jLabel12.setText("Syringe Capacity:");

        jComboBoxVOLT.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1x0.3", "2x0.3", "1x1", "2x1", "1x2", "2x2", "1x5", "2x5", "1x10", "2x10", "1x20", "2x20", "1x30", "2x30", "1x50", "2x50" }));
        jComboBoxVOLT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxVOLTActionPerformed(evt);
            }
        });

        jLabel13.setText("mL");

        jLayeredPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Hydro\n"));

        jLabelValveValue.setText("0");

        jCheckBoxAuto.setText("Auto");
        jCheckBoxAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAutoActionPerformed(evt);
            }
        });

        jToggleButtonV1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/izq.png"))); // NOI18N
        jToggleButtonV1.setText("V1");
        jToggleButtonV1.setBorderPainted(false);
        jToggleButtonV1.setContentAreaFilled(false);
        jToggleButtonV1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonV1ActionPerformed(evt);
            }
        });

        jToggleButtonV2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/izq.png"))); // NOI18N
        jToggleButtonV2.setText("V2");
        jToggleButtonV2.setBorderPainted(false);
        jToggleButtonV2.setContentAreaFilled(false);
        jToggleButtonV2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonV2ActionPerformed(evt);
            }
        });

        jToggleButtonV3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/izq.png"))); // NOI18N
        jToggleButtonV3.setText("V3");
        jToggleButtonV3.setBorderPainted(false);
        jToggleButtonV3.setContentAreaFilled(false);
        jToggleButtonV3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonV3ActionPerformed(evt);
            }
        });

        jToggleButtonV4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/izq.png"))); // NOI18N
        jToggleButtonV4.setText("V4");
        jToggleButtonV4.setBorderPainted(false);
        jToggleButtonV4.setContentAreaFilled(false);
        jToggleButtonV4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonV4ActionPerformed(evt);
            }
        });

        jToggleButtonInj.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/izq.png"))); // NOI18N
        jToggleButtonInj.setText("Inj");
        jToggleButtonInj.setBorderPainted(false);
        jToggleButtonInj.setContentAreaFilled(false);
        jToggleButtonInj.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonInjActionPerformed(evt);
            }
        });

        jLabel25.setText("Code:");

        jLayeredPane2.setLayer(jLabelValveValue, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(jCheckBoxAuto, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(jToggleButtonV1, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(jToggleButtonV2, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(jToggleButtonV3, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(jToggleButtonV4, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(jToggleButtonInj, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(jLabel25, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane2Layout = new javax.swing.GroupLayout(jLayeredPane2);
        jLayeredPane2.setLayout(jLayeredPane2Layout);
        jLayeredPane2Layout.setHorizontalGroup(
            jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane2Layout.createSequentialGroup()
                .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToggleButtonV1)
                    .addComponent(jToggleButtonV2)
                    .addComponent(jToggleButtonV3)
                    .addComponent(jToggleButtonV4)
                    .addComponent(jToggleButtonInj))
                .addGap(18, 18, 18)
                .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jLayeredPane2Layout.createSequentialGroup()
                        .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addComponent(jLabelValveValue, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCheckBoxAuto))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLayeredPane2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jToggleButtonInj, jToggleButtonV1, jToggleButtonV2, jToggleButtonV3, jToggleButtonV4});

        jLayeredPane2Layout.setVerticalGroup(
            jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane2Layout.createSequentialGroup()
                .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jLayeredPane2Layout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelValveValue)
                            .addComponent(jLabel25)))
                    .addGroup(jLayeredPane2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jToggleButtonV1)
                            .addComponent(jCheckBoxAuto))
                        .addGap(0, 0, 0)
                        .addComponent(jToggleButtonV2)
                        .addGap(0, 0, 0)
                        .addComponent(jToggleButtonV3)
                        .addGap(0, 0, 0)
                        .addComponent(jToggleButtonV4)))
                .addGap(0, 0, 0)
                .addComponent(jToggleButtonInj)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLayeredPane2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jToggleButtonInj, jToggleButtonV1, jToggleButtonV2, jToggleButtonV3, jToggleButtonV4});

        jComboBoxStep.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "x2", "x8", "x32" }));
        jComboBoxStep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxStepActionPerformed(evt);
            }
        });

        jLabel18.setText("Micro-steps:");

        jLabel21.setText("Plunger Stroke:");

        jTextFieldStroke.setText("64");
        jTextFieldStroke.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldStrokeActionPerformed(evt);
            }
        });

        jLabel24.setText("mm");

        jLabelVolume.setText("Volume:");

        jLabel15.setText("Local");

        jButtonLocalRemote.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/drcha.png"))); // NOI18N
        jButtonLocalRemote.setBorderPainted(false);
        jButtonLocalRemote.setContentAreaFilled(false);
        jButtonLocalRemote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLocalRemoteActionPerformed(evt);
            }
        });

        jLabel16.setText("Remote");

        jLabel2.setText("mL");

        javax.swing.GroupLayout jPanelConfigLayout = new javax.swing.GroupLayout(jPanelConfig);
        jPanelConfig.setLayout(jPanelConfigLayout);
        jPanelConfigLayout.setHorizontalGroup(
            jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelConfigLayout.createSequentialGroup()
                .addGroup(jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelConfigLayout.createSequentialGroup()
                        .addGap(95, 95, 95)
                        .addGroup(jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18)
                            .addGroup(jPanelConfigLayout.createSequentialGroup()
                                .addGroup(jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel21)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabelVolume))
                                .addGap(20, 20, 20)
                                .addGroup(jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelVOLT, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jComboBoxStep, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jComboBoxVOLT, javax.swing.GroupLayout.Alignment.LEADING, 0, 69, Short.MAX_VALUE)
                                        .addComponent(jComboBoxCOM, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextFieldStroke, javax.swing.GroupLayout.Alignment.LEADING)))
                                .addGap(18, 18, 18)
                                .addGroup(jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel13)
                                    .addComponent(jLabel2))))
                        .addGap(66, 66, 66))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelConfigLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel16)
                        .addGap(5, 5, 5)
                        .addComponent(jButtonLocalRemote, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(105, 105, 105)))
                .addComponent(jLayeredPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        jPanelConfigLayout.setVerticalGroup(
            jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelConfigLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelConfigLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBoxCOM, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
                        .addGap(10, 10, 10)
                        .addGroup(jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel18)
                            .addComponent(jComboBoxStep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelConfigLayout.createSequentialGroup()
                                .addGroup(jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(jComboBoxVOLT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel13))
                                .addGap(10, 10, 10)
                                .addGroup(jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel21)
                                    .addComponent(jTextFieldStroke, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel24))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabelVolume)
                                    .addGroup(jPanelConfigLayout.createSequentialGroup()
                                        .addComponent(jLabelVOLT, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(1, 1, 1))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelConfigLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(1, 1, 1)))
                        .addGroup(jPanelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonLocalRemote)
                            .addGroup(jPanelConfigLayout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(jLabel16))
                            .addGroup(jPanelConfigLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel15))))
                    .addComponent(jLayeredPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Config", jPanelConfig);

        jLabel14.setText("Calibration Setup:");

        jButtonCalibrateLoad.setText("Import...");
        jButtonCalibrateLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCalibrateLoadActionPerformed(evt);
            }
        });

        jButtonCalibrate.setText("Calibrate...");
        jButtonCalibrate.setEnabled(false);
        jButtonCalibrate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCalibrateActionPerformed(evt);
            }
        });

        jLabelCalibrated1.setText("Calibration not detected. Data used to calculate flow values will be aproximated.");

        jComboSteps.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3" }));
        jComboSteps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboStepsActionPerformed(evt);
            }
        });

        jLabel11.setText("Steps:");

        jLabel19.setText("Room Temp. (Cº):");

        jButton1.setText("Save as...");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextFieldTemperature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTemperatureActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelCalibrationLayout = new javax.swing.GroupLayout(jPanelCalibration);
        jPanelCalibration.setLayout(jPanelCalibrationLayout);
        jPanelCalibrationLayout.setHorizontalGroup(
            jPanelCalibrationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCalibrationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelCalibrationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelCalibrationLayout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanelCalibrationLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanelCalibrationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabelCalibrated1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanelCalibrationLayout.createSequentialGroup()
                                .addGroup(jPanelCalibrationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelCalibrationLayout.createSequentialGroup()
                                        .addComponent(jLabel11)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jComboSteps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanelCalibrationLayout.createSequentialGroup()
                                        .addComponent(jLabel19)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextFieldTemperature, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 239, Short.MAX_VALUE)
                                .addGroup(jPanelCalibrationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButtonCalibrate, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanelCalibrationLayout.createSequentialGroup()
                                        .addComponent(jButtonCalibrateLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                            .addComponent(jLabelCalibrated2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(25, 25, 25))))
        );
        jPanelCalibrationLayout.setVerticalGroup(
            jPanelCalibrationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelCalibrationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanelCalibrationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelCalibrationLayout.createSequentialGroup()
                        .addGroup(jPanelCalibrationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboSteps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelCalibrationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel19)
                            .addComponent(jTextFieldTemperature, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelCalibrationLayout.createSequentialGroup()
                        .addComponent(jButtonCalibrate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelCalibrationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonCalibrateLoad)
                            .addComponent(jButton1))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                .addComponent(jLabelCalibrated1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabelCalibrated2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35))
        );

        jTabbedPane1.addTab("Calibration", jPanelCalibration);

        jCheckBoxComplex.setText("Complex");
        jCheckBoxComplex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxComplexActionPerformed(evt);
            }
        });

        jSpinnerStep.setModel(new javax.swing.SpinnerNumberModel(4, 2, 10, 1));
        jSpinnerStep.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerStepStateChanged(evt);
            }
        });

        jLabel20.setText("Steps");

        jButtonRefresh.setText("Refresh");
        jButtonRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRefreshActionPerformed(evt);
            }
        });

        jTableProgram.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "S in", "S end", "T (s)", "Valve", "V (uL)"
            }
        ));
        jScrollPane1.setViewportView(jTableProgram);

        jButtonProgramSave.setText("Save...");
        jButtonProgramSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonProgramSaveActionPerformed(evt);
            }
        });

        jButtonProgramLoad.setText("Load...");
        jButtonProgramLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonProgramLoadActionPerformed(evt);
            }
        });

        jTextFieldProgramVolume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldProgramVolumeActionPerformed(evt);
            }
        });

        jLabel22.setText("Elapsed time");

        jLabel23.setText("Total Vol.");

        javax.swing.GroupLayout jPanelProgrammingLayout = new javax.swing.GroupLayout(jPanelProgramming);
        jPanelProgramming.setLayout(jPanelProgrammingLayout);
        jPanelProgrammingLayout.setHorizontalGroup(
            jPanelProgrammingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelProgrammingLayout.createSequentialGroup()
                .addGroup(jPanelProgrammingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelProgrammingLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldProgramTime, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldProgramVolume, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelProgrammingLayout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(jPanelProgrammingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelProgrammingLayout.createSequentialGroup()
                                .addComponent(jButtonProgramSave, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButtonProgramLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jButtonRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxComplex)
                            .addGroup(jPanelProgrammingLayout.createSequentialGroup()
                                .addComponent(jSpinnerStep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel20)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 402, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18))
        );
        jPanelProgrammingLayout.setVerticalGroup(
            jPanelProgrammingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelProgrammingLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanelProgrammingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldProgramTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22)
                    .addComponent(jTextFieldProgramVolume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addGap(23, 23, 23))
            .addGroup(jPanelProgrammingLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jCheckBoxComplex)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelProgrammingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerStep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 54, Short.MAX_VALUE)
                .addComponent(jButtonRefresh)
                .addGap(10, 10, 10)
                .addGroup(jPanelProgrammingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonProgramSave)
                    .addComponent(jButtonProgramLoad))
                .addGap(40, 40, 40))
        );

        jTabbedPane1.addTab("Programming", jPanelProgramming);

        jTextAreaLog.setEditable(false);
        jTextAreaLog.setColumns(20);
        jTextAreaLog.setRows(5);
        jScrollPane2.setViewportView(jTextAreaLog);

        javax.swing.GroupLayout jPanelLogLayout = new javax.swing.GroupLayout(jPanelLog);
        jPanelLog.setLayout(jPanelLogLayout);
        jPanelLogLayout.setHorizontalGroup(
            jPanelLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 515, Short.MAX_VALUE)
            .addGroup(jPanelLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE))
        );
        jPanelLogLayout.setVerticalGroup(
            jPanelLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 232, Short.MAX_VALUE)
            .addGroup(jPanelLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelLogLayout.createSequentialGroup()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("Log", jPanelLog);

        javax.swing.GroupLayout jPanelAboutLayout = new javax.swing.GroupLayout(jPanelAbout);
        jPanelAbout.setLayout(jPanelAboutLayout);
        jPanelAboutLayout.setHorizontalGroup(
            jPanelAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 515, Short.MAX_VALUE)
        );
        jPanelAboutLayout.setVerticalGroup(
            jPanelAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 232, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("About", jPanelAbout);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCalibrateLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCalibrateLoadActionPerformed
        // It pops up the load window and sets user file as main directory
    JFileChooser loadCalibFileChooser = new JFileChooser();
    loadCalibFileChooser.setDialogTitle("Select file to load");
    loadCalibFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    loadCalibFileChooser.setDialogType(JFileChooser.FILES_ONLY);
    // It filters which file extensions are allowed to be shown in browser and accepted to read
   
    loadCalibFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Calibration sequences","clb"));
    loadCalibFileChooser.setAcceptAllFileFilterUsed(true);
    
    int respuesta = loadCalibFileChooser.showSaveDialog(this); // Load window is opened
    switch(respuesta){ // Depending on user's choice different algorythms are executed
        case JFileChooser.APPROVE_OPTION:
          //In case there is no file to load, it shows a warning window that returns to the main window  
          File loadFile = loadCalibFileChooser.getSelectedFile();
          if (!loadFile.exists()){
              JOptionPane.showMessageDialog(this,"File doesn't exist", "Warning",JOptionPane.WARNING_MESSAGE);
              return;
          }
          
        try{
            fileloaded.setFlag(true);
            FileReader FR= new FileReader(loadFile);
            BufferedReader BR = new BufferedReader(FR);
            Scanner SC = new Scanner(BR);
//          Date            
            String dataload = SC.nextLine();
            String calibtext =  ("Calibration file loaded. "+ "["+ dataload +"]");
                        
            
//          Diameter
            dataload = SC.nextLine();
            String[] diameter = dataload.split("_");
            double valuedouble = Double.valueOf(diameter[1]);
            calibtext = calibtext + (" Syringe diameter: "+ decimales.format(valuedouble)+"mm ");
            jLabelCalibrated1.setText(calibtext);
            datacalc.setDiameter(valuedouble*Math.sqrt((double)1024/2000));
            
                        
//          Syringe number and volume

            dataload = SC.nextLine();
            String[] syrnum = dataload.split("_");
            int valueint = Integer.valueOf(syrnum[1]);
            jComboBoxVOLT.setSelectedIndex(valueint);
            
//          Stroke
            dataload = SC.nextLine();
            String[] stroke = dataload.split("_");
            valueint = Integer.valueOf(stroke[1]);
            jTextFieldStroke.setText(stroke[1]);
            datacalc.setStroke(valueint);
            
//          Microstep
            dataload = SC.nextLine();
            String[] mstep = dataload.split("_");
            valueint = Integer.valueOf(mstep[1]);
            jComboBoxStep.setSelectedIndex(valueint);
            
            
//          Temperature
            dataload = SC.nextLine();
            String[] temperature = dataload.split("_");           
            jTextFieldTemperature.setText(temperature[1]);
            
//          Calibration curve
            dataload = SC.nextLine();dataload = SC.nextLine();
            dataload = SC.nextLine();
            String[] slope = dataload.split("_");
            dataload = SC.nextLine();
            String[] b = dataload.split("_");                                        
            calibtext =("Calibration curve: "+ slope[1] +"x " + b[1]);
            dataload = SC.nextLine();
            String[] r2 = dataload.split("_");
            calibtext = calibtext + (" Correlation coefficient: " + decimales.format(r2[1]));       
            jLabelCalibrated2.setText(calibtext);
            
              
            
            
            SC.close();
            BR.close();
            FR.close();
            
            
            
        } catch (FileNotFoundException ex) {
        java.util.logging.Logger.getLogger(Ventana.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
        java.util.logging.Logger.getLogger(Ventana.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        case JFileChooser.CANCEL_OPTION:
          System.out.println("CancelCalibrateLoad");
          break;
        
        default :
          System.out.println("Error");
          break;    
    }    
 
    }//GEN-LAST:event_jButtonCalibrateLoadActionPerformed

    private void jCheckBoxComplexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxComplexActionPerformed
    
        if (jCheckBoxComplex.isSelected()){
            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Complex mode enabled" + "\n");
             
        }
        else{
            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Complex mode disabled" + "\n");
        }
        
    }//GEN-LAST:event_jCheckBoxComplexActionPerformed

    private void jButtonRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRefreshActionPerformed
    
        int totalRow = jTableProgram.getRowCount(); int totalCol = jTableProgram.getColumnCount();
        boolean vacio = false; boolean error = false;
        int dataCell = 0;
        
        //Checking if there is no empty cells
        for (int col = 0; col < totalCol-1 ; col++){
            for (int row = 0; row < totalRow; row++){
                if (jTableProgram.getValueAt(row, col) == null) {                   
                    vacio = true;
                }       
            }        
        }
        
        if (!vacio){
            //Checking if flow values are positive and below 1999
            for (int col = 0; col < 1 ; col++){
                for (int row = 0 ; row < totalRow ; row++){
                    try{
                    dataCell = Integer.parseInt(String.valueOf(jTableProgram.getValueAt(row,col)));
                    }
                    catch (Exception e) {
                    JOptionPane.showMessageDialog(this,"Cell value is incorrect","Warning",JOptionPane.WARNING_MESSAGE);
                    }
                    if ( dataCell < 0 || dataCell > 1999){
                        error = true;
                    }
                }
            }
            //Comprueba que los valores de tiempo sean positivos
            for (int row = 0 ; row < totalRow ; row++){
                try{
                dataCell= Integer.parseInt(String.valueOf(jTableProgram.getValueAt(row,3)));
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(this,"Cell value is incorrect","Warning",JOptionPane.WARNING_MESSAGE);}
                if ( dataCell < 0){
                    error = true;
                }
            }
        }
               
        
        // Si no hay valores erróneos comienza a calcular
        if (!vacio & !error){      

            int data0; int data1; int data2;
            // Pasa por cada columna y fila y lo almacena en variables             
            for (int row = 0; row < totalRow ; row++){
              data0=Integer.parseInt(String.valueOf(jTableProgram.getValueAt(row, 0)));
              data1=Integer.parseInt(String.valueOf(jTableProgram.getValueAt(row, 1)));
              data2=Integer.parseInt(String.valueOf(jTableProgram.getValueAt(row, 2)));
            // Transforma la velocidad (0-1999) en valores de flujo y lo redondea          
              double flow0 = datacalc.flow(data0);
              double flow1 = datacalc.flow(data1);
              double calc = Math.round((flow0+flow1)*data2/2);              
              jTableProgram.setValueAt(calc, row, 4);
            }
            int totalTime = 0; double totalVolume = 0; int timeRow; double volumeRow;
            //Calcula el tiempo total transcurrido
            for (int row = 0; row <= totalRow-1; row++){
              timeRow = Integer.parseInt(String.valueOf(jTableProgram.getValueAt(row, 2)));
              totalTime = totalTime + timeRow;
                
            }
            
            //Calculo de volumen total dispensado
            for (int row = 0; row <= totalRow-1; row++){
              volumeRow = Double.parseDouble(String.valueOf(jTableProgram.getValueAt(row, 4)));
              totalVolume = totalVolume + volumeRow;
                
            }
            //Muestra una ventana emergente si el volumen total está por encima del
            //volumen total de jeringas
        if (totalVolume > datacalc.getSyringeVol(datacalc.getStroke())*1000){
            JOptionPane.showMessageDialog(this,"Total volume exceeds syringe capacity","Warning",JOptionPane.WARNING_MESSAGE);
            }
            jTextFieldProgramTime.setText(String.valueOf(totalTime));
            jTextFieldProgramVolume.setText(String.valueOf(totalVolume));
        //Muestra una ventana emergente si hay algun error en las celdas   
        } else{
            JOptionPane.showMessageDialog(this,"Cell value is incorrect","Warning",JOptionPane.WARNING_MESSAGE);
            
            
        }
        dataComplex.clear();
        valveComplex.clear();
        for (int row=0; row<totalRow; row++){                //recorre la tabla
            //el tiempo en la tabla esta en segundos pero el indice del bucle 
            // avanza en saltos de 100 ms
            int tiempo = 1000 * Integer.parseInt(String.valueOf(jTableProgram.getValueAt(row, 2)));
            //calculo de parametros de la recta v vs t
            double pte, ord;
            ord = Double.parseDouble(String.valueOf(jTableProgram.getValueAt(row, 0)));
            pte = (Double.parseDouble(String.valueOf(jTableProgram.getValueAt(row, 1))) - ord) / tiempo;
            int valve = Integer.parseInt(String.valueOf(jTableProgram.getValueAt(row,3)));
            int t = 0;                            //indice del bucle de tiempo
            while (t <= tiempo){
                dataComplex.add((int)(pte * t) + (int)ord);
                valveComplex.add(valve);
//                System.out.println((int)(pte * t) + (int)ord);
//                System.out.println(valve);
               
                //aumenta el contador
                t += 100;
            }
        }
        
    }//GEN-LAST:event_jButtonRefreshActionPerformed

    private void jComboBoxVOLTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxVOLTActionPerformed
     
        if (demo.getFlag() & jComboBoxVOLT.getSelectedItem().equals("2x10") == false){
            JOptionPane.showMessageDialog(null, "Only 2x10 can be selected in DEMO mode");
            jComboBoxVOLT.setSelectedItem("2x10");
        } else {
            reset();
            System.out.println(jComboBoxVOLT.getSelectedIndex());
                
        }   
              
    
        
    //String containing every plunger stroke for each syringe volume in mm
        String[] strokes = new String[] {"49","49","68","68","45","45","52","52","64","64","76","76","85","85","118","118"};
    //Array containing every inner diameter for each syringe volume in mm    
        Double[] diameters = new Double[] {3.5,3.5,4.78,4.78,8.66,8.66,12.07,12.07,14.5,14.5,19.13,19.13,21.69,21.69,26.72,26.72};
        
        if (!fileloaded.getFlag()){
            datacalc.setDiameter(diameters[jComboBoxVOLT.getSelectedIndex()]);
            jTextFieldStroke.setText(strokes[jComboBoxVOLT.getSelectedIndex()]);
        }
        
        datacalc.setSyrnum(Integer.parseInt(String.valueOf(jComboBoxVOLT.getSelectedItem()).substring(0, 1)));       
        setTitle("Arduino uCF-SPS (" + String.valueOf(jComboBoxVOLT.getSelectedItem()) +" mL)");
        
        
        jLabelVOLT.setText(String.valueOf(decimales.format(datacalc.getSyringeVol(datacalc.getStroke()))));
        jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Syringe capacity changed to " + String.valueOf(jComboBoxVOLT.getSelectedItem()) + "\n"); 
   
     
    }//GEN-LAST:event_jComboBoxVOLTActionPerformed

    private void jButtonCalibrateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCalibrateActionPerformed

        if (jTextFieldTemperature.getText().isEmpty()){
        
            JOptionPane.showMessageDialog(null, "Temperature value is empty.");
        }
        
        else if(separar(jTextFieldTemperature.getText())==-1){
            
            JOptionPane.showMessageDialog(null, "Incorrect temperature value.");
        }
        
        else if (separar(jTextFieldTemperature.getText())<10 || separar(jTextFieldTemperature.getText())>40 ){
            
            JOptionPane.showMessageDialog(null, "Temperature value out of range (10ºC-40ºC).");
        }
        
        else{
            
            datacalc.setTemperature(Integer.parseInt(jTextFieldTemperature.getText()));
            datacalc.setStroke((Integer.parseInt(jTextFieldStroke.getText())));
            calibrating.setFlag(true);
            calibframe.setVisible(true);
            
        }
           
   
    }//GEN-LAST:event_jButtonCalibrateActionPerformed

    private void jButtonProgramSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProgramSaveActionPerformed
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
                java.util.logging.Logger.getLogger(Ventana.class.getName()).log(Level.SEVERE, null, ex);
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
                for (int col=1; col < 4; col++){
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
          System.out.println("CancelSaveTabla");
          break;
        default :
          System.out.println("Error");
          break;
    }    // TODO add your handling code here:
    }//GEN-LAST:event_jButtonProgramSaveActionPerformed

    private void jSpinnerStepStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerStepStateChanged
    DefaultTableModel model = (DefaultTableModel) jTableProgram.getModel();
    int spinnerData = Integer.parseInt(String.valueOf(jSpinnerStep.getValue()));
    model.setRowCount(spinnerData);     // TODO add your handling code here:
    }//GEN-LAST:event_jSpinnerStepStateChanged

    private void jTextFieldProgramVolumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldProgramVolumeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldProgramVolumeActionPerformed

    private void jButtonProgramLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProgramLoadActionPerformed
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
                for (int col=0 ; col< 4; col++){
                   jTableProgram.setValueAt(SC.nextInt(), row, col);
                }
            }
            SC.close();
            BR.close();
            FR.close();
            
            //Once every cell is filled with the corresponding value, it exectues the refresh button
            jButtonRefresh.doClick();
            
        } catch (FileNotFoundException ex) {
        java.util.logging.Logger.getLogger(Ventana.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
        java.util.logging.Logger.getLogger(Ventana.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    
        
        case JFileChooser.CANCEL_OPTION:
          System.out.println("CancelLoadTabla");
          break;
        
        default :
          System.out.println("Error");
          break;    
    }    
    }//GEN-LAST:event_jButtonProgramLoadActionPerformed

    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        
        
    JFileChooser saveCalibFileChooser = new JFileChooser();
    saveCalibFileChooser.setDialogTitle("Save as...");    
    saveCalibFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
    
    
    //saveFC.setFont(new java.awt.Font("Lucida Handwriting", 0, 18)); // (optional) Choosing text font
    //seleccion.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //(opcional) It used to show only directories

    int respuesta = saveCalibFileChooser.showSaveDialog(this); // Window is open
    switch(respuesta){ // Depending on user's choice different algorythms are executed
        
        case JFileChooser.APPROVE_OPTION:
          ArrayList dataSave = new ArrayList();
          File saveFile = saveCalibFileChooser.getSelectedFile();
          String filter = ".clb";
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
                java.util.logging.Logger.getLogger(Ventana.class.getName()).log(Level.SEVERE, null, ex);
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
        // It writes calibrame data in file  

        PW.println(calibframe.calresult);       
         
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
          System.out.println("CancelCalibrationSave");
          break;
        default :
          System.out.println("Error");
          break;
    }    // TODO add your handling code here:
    
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jComboStepsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboStepsActionPerformed
    
    datacalc.setSteps(Integer.parseInt(jComboSteps.getSelectedItem().toString()));
    System.out.println(datacalc.getSteps());
        
    }//GEN-LAST:event_jComboStepsActionPerformed

    private void jTextFieldTemperatureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTemperatureActionPerformed
        
    try {        
        datacalc.setTemperature(separar(jTextFieldTemperature.getText()));
        System.out.println(datacalc.getTemperature());
    }
    
    catch (Exception e) {
        
        JOptionPane.showMessageDialog(this,"Incorrect value","Warning",JOptionPane.WARNING_MESSAGE);
        
    }
    
        
    }//GEN-LAST:event_jTextFieldTemperatureActionPerformed

    private void jComboBoxCOMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCOMActionPerformed
    
        if (jComboBoxCOM.getSelectedItem().equals("DEMO")){
            demo.setFlag(true);
            datacalc.setPend(-278.3);
            datacalc.setOrd(142.51);
            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" COM port changed to DEMO "  + "\n");
            jButtonCalibrate.setEnabled(false);

        }
        else {
            demo.setFlag(false);
            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" COM port changed to " + String.valueOf(jComboBoxCOM.getSelectedItem()) + "\n");
            jButtonCalibrate.setEnabled(true);
        }

        
        
    }//GEN-LAST:event_jComboBoxCOMActionPerformed

    private void jButtonLocalRemoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLocalRemoteActionPerformed
        // TODO add your handling code here:
        if (local.getFlag()){            //remote mode
            jButtonLocalRemote.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/izq.png")));
            jTextAreaLog.append(dtf.format(LocalDateTime.now()) + " Remote mode selected" + "\n");
            enableRemote();
        } else {                //local mode
            jButtonLocalRemote.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/drcha.png")));
            jTextAreaLog.append(dtf.format(LocalDateTime.now()) + " Local mode selected" + "\n");
            enableLocal();
        }

        local.setFlag(!local.getFlag());
    }//GEN-LAST:event_jButtonLocalRemoteActionPerformed

    private void jTextFieldX100ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldX100ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldX100ActionPerformed

    private void jTextFieldTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTimeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldTimeActionPerformed

    private void jTextFieldLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldLoadActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldLoadActionPerformed

    private void jTextFieldFlowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFlowActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFlowActionPerformed

//    Listener para cambiar el estado de jSpeedSlider y jTextFieldFlow (con uds)
//    cada vez que jSpinnerSpeed cambia de valor. 
    private void jSpinnerSpeedStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerSpeedStateChanged

        switch(jComboBoxUnits.getSelectedItem().toString())
        {
            case "μL/s":
                jTextFieldFlow.setText(String.valueOf(decimales.format(datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "μL/min":
                jTextFieldFlow.setText(String.valueOf(decimales.format(60*datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "μL/h":
                jTextFieldFlow.setText(String.valueOf(decimales.format(3600*datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "mL/s":
                jTextFieldFlow.setText(String.valueOf(decimales.format(0.001*datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "mL/min":
                jTextFieldFlow.setText(String.valueOf(decimales.format(0.001*60*datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "mL/h":
                jTextFieldFlow.setText(String.valueOf(decimales.format(0.001*3600*datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "L/s":
                jTextFieldFlow.setText(String.valueOf(decimales.format(1e-6*datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "L/min":
                jTextFieldFlow.setText(String.valueOf(decimales.format(1e-6*60*datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "L/h":
                jTextFieldFlow.setText(String.valueOf(decimales.format(1e-6*3600*datacalc.flow(jSpeedSlider.getValue()))));
                break;    
            default:
                System.out.println("Error");
        }

        jSpeedSlider.setValue((int)jSpinnerSpeed.getValue());
        if (!local.getFlag()){
                ino.sendSpeed(jSpeedSlider.getValue(), demo);
        }            
        
    }//GEN-LAST:event_jSpinnerSpeedStateChanged

//  Listener para cambiar el valor de jSpinnerSpeed cada vez que se desplaza 
//  jSpeedSlider
    private void jSpeedSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpeedSliderStateChanged

        if(!jSpeedSlider.getValueIsAdjusting()){
            jSpinnerSpeed.setValue(jSpeedSlider.getValue());
//            ino.sendSpeed(jSpeedSlider.getValue(), demo);
            
        }
        
    }//GEN-LAST:event_jSpeedSliderStateChanged

    private void jToggleButtonRELOADActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonRELOADActionPerformed


        if (jProgressBar1.getValue()<100){
            
            if(!working.getFlag()){
                jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Reload selected" + "\n");    
                reload();
            }    
            
        }else{
            JOptionPane.showMessageDialog(null, "Syringe fully loaded. Press start");
            jToggleButtonSTOP.setSelected(true);
        }
    }//GEN-LAST:event_jToggleButtonRELOADActionPerformed

    private void jToggleButtonSTARTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonSTARTActionPerformed
      
        if (jProgressBar1.getValue()>0){
            //  Si es flujo complejo se activa el timer y el listener envía las velocidades
            //  del objeto dataComplex
            if (jCheckBoxComplex.isSelected()){

                //            Activa el timer y el listener se encarga de enviar las velocidades
                //         *  del elemento dataComplex y del valveComplex

                indexComplex = 0;
                ino.sendSpeed(dataComplex.get(0),demo);
                ino.sendValve(valveComplex.get(0),demo);
                jToggleButtonSTART.setEnabled(true);
                jToggleButtonSTOP.setEnabled(true);
                jToggleButtonRELOAD.setEnabled(true);
                jSpeedSlider.setEnabled(false);
                jSpinnerSpeed.setEnabled(false);
                timerComplex.start();
                jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Complex Start selected" + "\n");
                start();

            }
            //Si el flujo no es complejo envia la velocidad que muestra el slider
            else {

//                ino.sendSpeed((int)jSpinnerSpeed.getValue(), demo);
                jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Pump start selected" + "\n");
                start();

            }
        }

        else {
            
            if(!calibrating.getFlag()){
                
                JOptionPane.showMessageDialog(null, "Please, load first.");
                jToggleButtonSTOP.setSelected(true);
            }
            
            
        }


    }//GEN-LAST:event_jToggleButtonSTARTActionPerformed

    private void jToggleButtonSTOPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonSTOPActionPerformed

        if (jCheckBoxComplex.isSelected()){
            timerComplex.stop();        }
        
        jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Pump stop selected" + "\n");
        stop();
    }//GEN-LAST:event_jToggleButtonSTOPActionPerformed

    private void jToggleButtonV1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonV1ActionPerformed
    
        if (jToggleButtonV1.isSelected()){            
                jToggleButtonV1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/drcha.png")));
                jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Valve 1 ON " + "\n");
                binarycalc();
        } 
        else {            
                jToggleButtonV1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/izq.png")));
                jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Valve 1 OFF " + "\n");
                binarycalc();
        }    
        
        
    }//GEN-LAST:event_jToggleButtonV1ActionPerformed

    private void jCheckBoxAutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAutoActionPerformed
        
        if (jCheckBoxAuto.isSelected()){
            if (jToggleButtonV1.isSelected()){
                jToggleButtonV1.doClick();
                jToggleButtonV1.setEnabled(false);
            }
            else{
                jToggleButtonV1.setEnabled(false);
            }
        }           
                    
        else {
            
        jToggleButtonV1.setEnabled(true);    
            
        }
        
        
    }//GEN-LAST:event_jCheckBoxAutoActionPerformed

    private void jToggleButtonInjActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonInjActionPerformed
        if (jToggleButtonInj.isSelected()){

            jToggleButtonInj.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/drcha.png")));
            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Inj. Valve ON " + "\n");
            binarycalc();
        } 

        else {            
            jToggleButtonInj.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/izq.png")));
            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Inj. Valve OFF " + "\n");
            binarycalc();
        } 
    }//GEN-LAST:event_jToggleButtonInjActionPerformed

    private void jToggleButtonV3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonV3ActionPerformed
        
        if (jToggleButtonV3.isSelected()){
            
            jToggleButtonV3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/drcha.png")));
            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Valve 3 ON " + "\n");
            binarycalc();
        } 

        else {            
            jToggleButtonV3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/izq.png")));
            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Valve 3 OFF " + "\n");
            binarycalc();
        } 
    }//GEN-LAST:event_jToggleButtonV3ActionPerformed

    private void jToggleButtonV2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonV2ActionPerformed
        
        if (jToggleButtonV2.isSelected()){
            
            jToggleButtonV2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/drcha.png")));
            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Valve 2 ON " + "\n");
            binarycalc();
        } 

        else {            
            jToggleButtonV2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/izq.png")));
            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Valve 2 OFF " + "\n");
            binarycalc();
        } 
    }//GEN-LAST:event_jToggleButtonV2ActionPerformed

    private void jToggleButtonV4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonV4ActionPerformed
        if (jToggleButtonV4.isSelected()){

            jToggleButtonV4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/drcha.png")));
            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Valve 4 ON " + "\n");
            binarycalc();
        } 

        else {            
            jToggleButtonV4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/izq.png")));
            jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Valve 4 OFF " + "\n");
            binarycalc();
        } 
    }//GEN-LAST:event_jToggleButtonV4ActionPerformed

    private void jComboBoxStepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxStepActionPerformed
        
        datacalc.setMstep(Integer.parseInt(String.valueOf(jComboBoxStep.getSelectedItem()).substring(1)));
        jLabelVOLT.setText(String.valueOf(decimales.format(datacalc.getSyringeVol(datacalc.getStroke()))));
        jTextAreaLog.append(dtf.format(LocalDateTime.now()) +" Microstep changed to " + datacalc.getMstep() + "\n");
        
        
    }//GEN-LAST:event_jComboBoxStepActionPerformed

    private void jTextFieldStrokeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldStrokeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldStrokeActionPerformed

    private void jTextFieldUnloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldUnloadActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldUnloadActionPerformed

    private void jComboBoxUnitsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxUnitsActionPerformed
    
        switch(jComboBoxUnits.getSelectedItem().toString())
        {
            case "μL/s":
                jTextFieldFlow.setText(String.valueOf(decimales.format(datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "μL/min":
                jTextFieldFlow.setText(String.valueOf(decimales.format(60*datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "μL/h":
                jTextFieldFlow.setText(String.valueOf(decimales.format(3600*datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "mL/s":
                jTextFieldFlow.setText(String.valueOf(decimales.format(0.001*datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "mL/min":
                jTextFieldFlow.setText(String.valueOf(decimales.format(0.001*60*datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "mL/h":
                jTextFieldFlow.setText(String.valueOf(decimales.format(0.001*3600*datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "L/s":
                jTextFieldFlow.setText(String.valueOf(decimales.format(1e-6*datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "L/min":
                jTextFieldFlow.setText(String.valueOf(decimales.format(1e-6*60*datacalc.flow(jSpeedSlider.getValue()))));
                break;
            case "L/h":
                jTextFieldFlow.setText(String.valueOf(decimales.format(1e-6*3600*datacalc.flow(jSpeedSlider.getValue()))));
                break;    
            default:
                System.out.println("Error");
        }
     
    }//GEN-LAST:event_jComboBoxUnitsActionPerformed

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
            java.util.logging.Logger.getLogger(Ventana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Ventana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Ventana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Ventana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Ventana().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonCalibrate;
    private javax.swing.JButton jButtonCalibrateLoad;
    private javax.swing.ButtonGroup jButtonGroupMode;
    private javax.swing.ButtonGroup jButtonGroupStartStop;
    private javax.swing.JButton jButtonLocalRemote;
    private javax.swing.JButton jButtonProgramLoad;
    private javax.swing.JButton jButtonProgramSave;
    private javax.swing.JButton jButtonRefresh;
    private javax.swing.JCheckBox jCheckBoxAuto;
    private javax.swing.JCheckBox jCheckBoxComplex;
    private javax.swing.JComboBox<String> jComboBoxCOM;
    public static javax.swing.JComboBox<String> jComboBoxStep;
    private javax.swing.JComboBox<String> jComboBoxUnits;
    public static javax.swing.JComboBox<String> jComboBoxVOLT;
    private javax.swing.JComboBox<String> jComboSteps;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelCalibrated1;
    private javax.swing.JLabel jLabelCalibrated2;
    private javax.swing.JLabel jLabelVOLT;
    private javax.swing.JLabel jLabelValveValue;
    private javax.swing.JLabel jLabelVolume;
    private javax.swing.JLayeredPane jLayeredPane2;
    private javax.swing.JPanel jPanelAbout;
    private javax.swing.JPanel jPanelCalibration;
    private javax.swing.JPanel jPanelConfig;
    private javax.swing.JPanel jPanelLog;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelProgramming;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    public static javax.swing.JSlider jSpeedSlider;
    private javax.swing.JSpinner jSpinnerSpeed;
    private javax.swing.JSpinner jSpinnerStep;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableProgram;
    private javax.swing.JTextArea jTextAreaLog;
    private javax.swing.JTextField jTextFieldFlow;
    private javax.swing.JTextField jTextFieldLoad;
    private javax.swing.JTextField jTextFieldProgramTime;
    private javax.swing.JTextField jTextFieldProgramVolume;
    public static javax.swing.JTextField jTextFieldStroke;
    private javax.swing.JTextField jTextFieldTemperature;
    private javax.swing.JTextField jTextFieldTime;
    private javax.swing.JTextField jTextFieldUnload;
    private javax.swing.JTextField jTextFieldX100;
    private javax.swing.JToggleButton jToggleButtonInj;
    private javax.swing.JToggleButton jToggleButtonRELOAD;
    private javax.swing.JToggleButton jToggleButtonSTART;
    public static javax.swing.JToggleButton jToggleButtonSTOP;
    private javax.swing.JToggleButton jToggleButtonV1;
    private javax.swing.JToggleButton jToggleButtonV2;
    private javax.swing.JToggleButton jToggleButtonV3;
    private javax.swing.JToggleButton jToggleButtonV4;
    // End of variables declaration//GEN-END:variables
}
