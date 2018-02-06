/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie4100r;

import FileManager.CsvReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Kevin-Notebook
 */
public class OCBA {
    public static void main(String[] args){
        int[][] y = new int[20][];
        for (int i = 0; i < 20; i++) {
            y[i] = CsvReader.readCsvFileForY(String.format("output/%d/y.csv", i + 1));
        }
        String folderName = "ocba";
        
        try {
            File dir = new File(folderName);
            if (!dir.exists()) {
                dir.mkdir();
            }
            
            FileWriter writer3 = new FileWriter(String.format("%s\\locker.csv", folderName));
            
            for (int i = 0; i < y.length; i++) {
                for (int j = 0; j < 10; j++) {
                    writer3.append(Integer.toString(Arrays.stream(y[i]).sum()));
                    writer3.append(',');
                }
                writer3.append('\n');
            }

            //generate whatever data you want
            writer3.flush();
            writer3.close();
        } catch (IOException e) {
            System.err.println("Error writing to file " + e.getCause().toString());
        }
    }
}
