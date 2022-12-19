/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Data;

/**
 *
 * @author Andres
 */
public class points {
    
    private int x;
    private int y;
    private int id;
    
//    Constructor de puntos
    
    public points (int x, int y, int id){
    
        this.x = x;
        this.y = y;
        this.id = id;    
        
    }
    
    
//    Constructor de copia de puntos
    public points (points p){
        
        this.x = p.getX();
        this.y = p.getY();
        this.id = p.getId();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    
    
//    Constructor vacío
    public points (){
        
    }
    
    

}

