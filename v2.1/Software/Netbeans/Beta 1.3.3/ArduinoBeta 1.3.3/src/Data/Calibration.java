/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Data;

import java.util.ArrayList;

/**
 *
 * @author Andres
 */
public class Calibration {
 
    private ArrayList<Double> x = new ArrayList<Double>();
    private ArrayList<Double> y = new ArrayList<Double>();
    private int n;          //número de datos
    public double a, b;    //pendiente y ordenada en el origen
    
    private DataCalculation datacalc = new DataCalculation();
    ArrayList<points> pointlist = new ArrayList<points>();
   
    
    
    public Calibration (DataCalculation datacalc){
        
    
    }
    
    
    
    public double lineal(ArrayList<Double> x, ArrayList<Double> y){
        this.x=x;
        this.y=y;
        n=x.size(); //número de datos
        double pxy, sx, sy, sx2, sy2;
        pxy=sx=sy=sx2=sy2=0.0;
        for(int i=0; i<n; i++){
            sx+=x.get(i);
            sy+=y.get(i);
            sx2+=x.get(i)*x.get(i);
            sy2+=y.get(i)*y.get(i);
            pxy+=x.get(i)*y.get(i);
        }
        a=(n*pxy-sx*sy)/(n*sx2-sx*sx);
        b=(sy-b*sx)/n;
        System.out.println(a);
        System.out.println(b);
        double diameter = Math.sqrt(-160000*datacalc.getMstep()/(Math.PI*datacalc.getSyrnum()*a));
        
        
        return diameter;

        
    }
    public double correlacion(){
      //valores medios
        double suma=0.0;
        for(int i=0; i<n; i++){
            suma+=x.get(i);
        }
        double mediaX=suma/n;

        suma=0.0;
        for(int i=0; i<n; i++){
            suma+=y.get(i);
        }
        double mediaY=suma/n;
    //coeficiente de correlación
        double pxy, sx2, sy2;
        pxy=sx2=sy2=0.0;
        for(int i=0; i<n; i++){
            pxy+=(x.get(i)-mediaX)*(y.get(i)-mediaY);
            sx2+=(x.get(i)-mediaX)*(x.get(i)-mediaX);
            sy2+=(y.get(i)-mediaY)*(y.get(i)-mediaY);
        }
        return pxy/Math.sqrt(sx2*sy2);
    }

        
        
}
    
    

