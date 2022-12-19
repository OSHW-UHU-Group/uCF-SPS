/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ArduinoConnection;

import Data.Bandera;
import com.panamahitek.ArduinoException;
import com.panamahitek.PanamaHitek_Arduino;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import jssc.SerialPortEvent;
//import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 *
 * @author Andrés
 */
public class ArduinoConnection {
        
    PanamaHitek_Arduino arduino = new PanamaHitek_Arduino();
    
    
     
    
//    Método que devuelve una instancia de arduino para usar en cualquier otro archivo
    
    public PanamaHitek_Arduino arduinoReturn(){
        return arduino;
    }
    
   
//     Envía la orden de trabajar en modo local, desde el panel de la bomba
    public void sendLocal (Bandera demo){
                
        if (!demo.getFlag()){
            try {
                arduino.sendData("m");
            } catch (ArduinoException | SerialPortException ex) {

            }
        }
    }
    
    //     Envía la orden de trabajar en modo remoto, desde la interfaz del PC
    public void sendRemote (Bandera demo){
        if (!demo.getFlag()){        
            try {
                arduino.sendData("r");
            } catch (ArduinoException | SerialPortException ex) {

            }
        }
    }

//    Envía configuración de válvulas a Arduino
    
    public void sendValve(int valvevalue, Bandera demo){
       
        if (!demo.getFlag()){
            try {   arduino.sendData("w"+valvevalue);

                } catch (ArduinoException | SerialPortException ex) {

                }
    
        }
    }
    

//    Envía la velocidad a Arduino en forma de byte alto y bajo
    public void sendSpeed (int veloc, Bandera demo){
        
        int newVeloc = veloc*1024/2000;               

        
        if (!demo.getFlag()){
            try {   arduino.sendData("f"+newVeloc);

            } catch (ArduinoException | SerialPortException ex) {
                                
            }
        }
    }
    
//     Envía la orden de cargar las jeringas a Arduino
    public void sendLoad (Bandera demo){
        
        if (!demo.getFlag()){        
            try {
                arduino.sendData("l");
            } catch (ArduinoException | SerialPortException ex) {

            }
        }
    }
    
//    Envia la orden de descargar las jeringas a Arduino
    public void sendUnload (Bandera demo){
        if (!demo.getFlag()){
            try {
                arduino.sendData("g");
            } catch (ArduinoException | SerialPortException ex) {

            }
        }
    }
    
    
//    Envía la orden de detener la acción carga/
    public void sendStop (Bandera demo){
        
        
        if (!demo.getFlag()){
            try {
                    arduino.sendData("s");
                } catch (ArduinoException | SerialPortException ex) {

                }
        }
    }
   
    }
    

