/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Data;

import ArduinoConnection.ArduinoConnection;
import java.util.ArrayList;


/**
 *
 * @author Andrés
 */
public class DataCalculation {
    
    private ArduinoConnection arduino;
    private double pendiente = -278.3;
    private double ordenada = 142.51;
    private double VolR = 0;
    private double time = 0;    
    private int stepscalib = 1;
    private float temperature = 20; //temperatura (ºC)
    private float wmass ;   //masa de agua (g)
    private long tg;    //tiempo de inicio de descarga  (ms)
    private long td;    //tiempo final de descarga  (ms)
    private int valvevalue = 0; //estado de las válvulas
    private double eDiameter =14.5; //diámetro nominal (mm)
    private int mstep = 2;  //microstep elegido
    private int syrnum = 2; //número de jeringas
    private int stroke = 64; //longitud de embolo estirado (mm) 
    
    private double a, b;    //pendiente y ordenada en el origen
    
   
    public DataCalculation () {
       
    
}   
    public DataCalculation (ArduinoConnection ino) {
       
    this.arduino = ino;
    
}
    
    
   //  Métodos para obtener y modificar las válvulas activas
    
    public void setValves(int value){
        this.valvevalue = value;
    
}  
    public int getValves (){
        return valvevalue;
        
    }
    //  Métodos para obtener y modificar valores de volumen cargado
    public double getVolumeRemaining () {
        return VolR;
    } 
    public void setVolumeRemaining (double x){
        this.VolR=x;
    }
    //  Métodos para obtener y modificar valores de tiempo en progress
    public double getTimeProgress () {
        return time;
    } 
    public void setTimeProgress (double x){
        this.time=x;
    }
    

//    Método para obtener la cantidad de volumen dispensado segun número de 
    //jeringas
    public double getSyringeVol (int stroke) {
        
        double VolT = getSyrnum()*stroke*Math.pow(getDiameter(), 2)*Math.PI/4000;
        return VolT;
    
}
    //  Métodos para obtener y modificar valores de microstep
    public int getMstep (){
        return mstep;
    }
    
    public void setMstep (int x){
        this.mstep=x;
    }
    //  Métodos para obtener y modificar número de jeringas
    public int getSyrnum (){
        return syrnum;
    }
    
    public void setSyrnum (int x){
        this.syrnum=x;
    }
    
    //  Métodos para obtener y modificar valor de stroke
    public int getStroke(){
        return stroke;
    }
    
    public void setStroke (int x){
        this.stroke=x;
    }
    
    
//  Métodos para obtener y modificar valores de temperatura ambiente
    public float getTemperature () {
        return temperature;
    } 
    public void setTemperature (float x){
        this.temperature=x;
    }
    
 //  Métodos para obtener el valor de densidad del agua
    public double getWDensity(){

        double wdensity = -4.8238*Math.pow(10, -6)*Math.pow(getTemperature(), 2)-1.0727*Math.pow(10, -5)*getTemperature()+1.0003;
        
        return wdensity;
    }
 //  Métodos para obtener y modificar valores de masa de agua
    public float getWMass () {
        return wmass;
    } 
    public void setWMass (float x){
        this.wmass=x;
    }
    
//  Métodos para obtener y modificar valores de pendientes y ordenadas
//    de la recta de calibrado
    public double getPend (){
        return pendiente;
    }
    
    public void setPend (double x){
        this.pendiente=x;
    }
         
    public double getOrd (){
        return ordenada;
    }
    
    public void setOrd (double x){
        this.ordenada=x;
    }
    
    //  Métodos para obtener y modificar valores de diametro interno de jeringas
    public double getDiameter (){
        return eDiameter;
    }
    
    public void setDiameter (double x){
        this.eDiameter=x;
    }
    
    
//    Métodos para obtener y modificar valores de pasos de calibración
    
    public void setSteps(int step){
        this.stepscalib = step;
    
}  
    public int getSteps (){
        return stepscalib;
        
    }
    
//    Métodos para obtener y modificar valores de tiempos iniciales (tg) y 
//    finales (td) de carga y descarga
    
    public void setTimeG(long time){
        this.tg = time;
    
}  
    public long getTimeG (){
        return tg;
        
    }
    
    public void setTimeD(long time){
        this.td = time;
    
}  
    public long getTimeD (){
        return td;
        
    }
    
    
   
//    Devuelve el valor de flujo dependiendo de la velocidad empleando los diámetros nominales
//    de cada jeringa.
    public double flow(int speed0){

//  Toma la velocidad y la pasa a base 1023
        double volt = Math.round((speed0/(float)2000.0)*(float)1024.0);
//  Si la velocidad en base 1023 es mayor, aseguramos que el máximo sea 1023      
        if (volt>1023) volt=1023;            

//  Se calcula el flujo a través de la expresión que relaciona el diámetro nominal, la
//  el número de jeringas, el microstep y la velocidad en base 1023.
        Double flow = Math.PI*Math.pow(getDiameter(), 2)*getSyrnum()/(160000*getMstep()*(1024-volt));                
             
//  Devuelve el valor del flujo en uL/s
        return 1000000*flow;
    }
    
    
//  Función que calcula regresión lineal en función del diámetro   
    public double lineal(ArrayList<Double> x, ArrayList<Double> y){
        
        int size=x.size(); //número de datos
        double pxy, sx, sy, sx2, sy2;
        pxy=sx=sy=sx2=sy2=0.0;
        for(int i=0; i<size; i++){
            sx+=x.get(i);
            sy+=y.get(i);
            sx2+=x.get(i)*x.get(i);
            sy2+=y.get(i)*y.get(i);
            pxy+=x.get(i)*y.get(i);
        }
        a =(size*pxy-sx*sy)/(size*sx2-sx*sx);
        b =(sy-a*sx)/size;
        setPend(a);
        setOrd(b);
        double diameter = Math.sqrt(-160000*getMstep()/(Math.PI*getSyrnum()*a));
        
        
        return diameter;

        
    }
    public double correlacion(ArrayList<Double> x, ArrayList<Double> y){
      //valores medios
        int size=x.size(); //número de datos
        double suma=0.0;
        for(int i=0; i<size; i++){
            suma+=x.get(i);
        }
        double mediaX=suma/size;

        suma=0.0;
        for(int i=0; i<size; i++){
            suma+=y.get(i);
        }
        double mediaY=suma/size;
    //coeficiente de correlación
        double pxy, sx2, sy2;
        pxy=sx2=sy2=0.0;
        for(int i=0; i<size; i++){
            pxy+=(x.get(i)-mediaX)*(y.get(i)-mediaY);
            sx2+=(x.get(i)-mediaX)*(x.get(i)-mediaX);
            sy2+=(y.get(i)-mediaY)*(y.get(i)-mediaY);
        }
        return pxy/Math.sqrt(sx2*sy2);
    }
    
}
