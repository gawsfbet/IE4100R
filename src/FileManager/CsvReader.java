/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Kevin-Notebook
 */
public class CsvReader {
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
}
