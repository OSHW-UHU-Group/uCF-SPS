/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Data;

/**
 *
 * @author Andrés
 */
public class Bandera {
 
    private boolean bandera;
        
        public Bandera(boolean x){
        this.bandera=x;       
        
        }
        
        public boolean getFlag(){
        return bandera;
        
        }
        
        public void setFlag (boolean x){
            this.bandera=x;
        }
    
}
