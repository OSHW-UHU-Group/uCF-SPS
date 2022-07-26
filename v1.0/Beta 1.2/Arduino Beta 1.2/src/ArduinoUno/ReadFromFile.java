/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ArduinoUno;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author GIEA
 */
public class ReadFromFile {
    public ArrayList readLine (String file_path/*, String text*/) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(file_path));
        ArrayList array1 = new ArrayList();
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            //CharSequence cs1 = text;
            int countline = 0;
            
            while (/*line.contains (cs1) == false*/ line != null){
                if (line.startsWith("#") == true){
                    System.out.println(line.substring(1));
                    array1.add(countline, line.substring(1));
                    countline++;
                    //String[] array1 = 
                }
                sb.append(line);
                sb.append("\n");
                System.out.println(line);
                line = br.readLine();
                //countline = countline + 1;
            }
            //System.out.println(line+" in line "+countline);
            // String result = "Text "+text+" is contained in line "+String.valueOf(countline);
            return array1;
        } finally {
            br.close();
        }
    }
}
