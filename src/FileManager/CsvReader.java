/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Kevin-Notebook
 */
public class CsvReader {
    public static int[] readCsvFileForY(String fileName) {
        String line = "";
        String[] values;

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            line = br.readLine();
            values = line.split(",");
            
            return Arrays.stream(values).mapToDouble(Double::parseDouble).mapToInt(d -> (int) Math.round(d)).toArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static int[] readCsvFile1Dim(String fileName) {
        String line = "";
        ArrayList<Integer> values = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                // csv is a row
                values.add(Integer.parseInt(line));
            }
            
            return values.stream().mapToInt(v -> v).toArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int[][] readCsvFile2Dim(String fileName) {
        String line = "";
        String cvsSplitBy = ",";
        ArrayList<int[]> values = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                int[] entries = Arrays.stream(line.split(cvsSplitBy)).mapToInt(Integer::parseInt).toArray();
                values.add(entries);
            }
            
            return values.stream().map(v -> v).toArray(int[][]::new);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void writeDataToFiles(ArrayList<HashMap<String, Double>>[] data, String folderName) {
        try {
            File dir = new File(folderName);
            if (!dir.exists()) {
                dir.mkdir();
            }
            
            FileWriter writer1 = new FileWriter(String.format("%s\\demand.csv", folderName));
            FileWriter writer2 = new FileWriter(String.format("%s\\distance.csv", folderName));
            FileWriter writer3 = new FileWriter(String.format("%s\\locker.csv", folderName));
            
            for (int i = 0; i < data.length; i++) {
                for (HashMap<String, Double> entry : data[i]) {
                    writer1.append(Integer.toString((int) Math.round(entry.get("demand"))));
                    writer1.append(',');
                    writer2.append(Integer.toString((int) Math.round(entry.get("distance"))));
                    writer2.append(',');
                    writer3.append(Integer.toString((int) Math.round(entry.get("locker"))));
                    writer3.append(',');
                }
                writer1.append('\n');
                writer2.append('\n');
                writer3.append('\n');
            }

            //generate whatever data you want
            writer1.flush();
            writer2.flush();
            writer3.flush();
            writer1.close();
            writer2.close();
            writer3.close();
            System.out.println("Simulation data written to files in folder " + dir.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing to file " + e.getCause().toString());
        }
    }
}
