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
    
    public static void writeIterToFiles(ArrayList<Integer>[] iterations, String folderName) {
        try {
            File dir = new File(folderName);
            if (!dir.exists()) {
                dir.mkdir();
            }
            
            FileWriter writer = new FileWriter(String.format("%s\\iteration.csv", folderName));
            
            for (int i = 0; i < iterations.length; i++) {
                for (int iteration : iterations[i]) {
                    writer.append(Integer.toString(iteration));
                    writer.append(',');
                }
                writer.append('\n');
            }

            //generate whatever data you want
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing to file " + e.getCause().toString());
        }
    }
    
    public static void writeMeanToFiles(ArrayList<Double>[] means, ArrayList<Double>[] sds, String folderName) {
        try {
            File dir = new File(folderName);
            if (!dir.exists()) {
                dir.mkdir();
            }
            
            FileWriter writer1 = new FileWriter(String.format("%s\\mean.csv", folderName));
            FileWriter writer2 = new FileWriter(String.format("%s\\sd.csv", folderName));
            
            for (int i = 0; i < means.length; i++) {
                for (double mean : means[i]) {
                    writer1.append(Double.toString(mean));
                    writer1.append(',');
                }
                writer1.append('\n');
                
                for (double sd : sds[i]) {
                    writer2.append(Double.toString(sd));
                    writer2.append(',');
                }
                writer2.append('\n');
            }
            
            writer1.flush();
            writer2.flush();
            writer1.close();
            writer2.close();
        } catch (IOException e) {
            System.err.println("Error writing to file " + e.getCause().toString());
        }
    }
    
    public static ArrayList<HashMap<String, Double>>[] readPrelimData(String folderName, int n0, int k) {
        String line1 = "", line2 = "", line3 = "";
        String cvsSplitBy = ",";
        ArrayList<HashMap<String, Double>>[] data = new ArrayList[k];
        
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(String.format("%s\\demand.csv", folderName)));
            BufferedReader br2 = new BufferedReader(new FileReader(String.format("%s\\distance.csv", folderName)));
            BufferedReader br3 = new BufferedReader(new FileReader(String.format("%s\\locker.csv", folderName)));
            for (int i = 0; i < k; i++) {
                data[i] = new ArrayList<>();
                line1 = br1.readLine();
                line2 = br2.readLine();
                line3 = br3.readLine();
                double[] entries1 = Arrays.stream(line1.split(cvsSplitBy)).mapToDouble(Double::parseDouble).toArray(),
                        entries2 = Arrays.stream(line2.split(cvsSplitBy)).mapToDouble(Double::parseDouble).toArray(),
                        entries3 = Arrays.stream(line3.split(cvsSplitBy)).mapToDouble(Double::parseDouble).toArray();
                for (int j = 0; j < n0; j++) {
                    HashMap<String, Double> entry = new HashMap<>();
                    entry.put("total", entries1[j] - 0.3 * entries2[j] - 150 * entries3[j]);
                    entry.put("demand", entries1[j]);
                    entry.put("distance", entries2[j]);
                    entry.put("locker", entries3[j]);
                    data[i].add(entry);
                }
            }
            
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
