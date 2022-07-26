/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ArduinoUno;

/**
 *
 * @author GIEA
 */

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class WritetoFile {
    
    private String filepath;
    private boolean appendtext;
    private String syringes;
    
    public WritetoFile(String file_path, boolean append_text){
        filepath = file_path;
        appendtext = append_text;
    }
    public void writeToFile(String textLine) throws IOException{
        FileWriter write = new FileWriter(filepath, appendtext);
        PrintWriter printed = new PrintWriter(write);
        printed.printf("%s"+"%n",textLine);
        printed.close();
    }
}
