/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie4100r;

import Logic.Solver;
import ilog.concert.IloException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kevin-Notebook
 */
//d.csv -> 139, BF blank
public class Main {

    public static void main(String[] args) {
        int[] a = readCsvFile1Dim("data/a.csv");
        int[] b = readCsvFile1Dim("data/b.csv");
        int[][] d = readCsvFile2Dim("data/d.csv");
        int[][] e = readCsvFile2Dim("data/e.csv");
        int[][] h = readCsvFile2Dim("data/h.csv");
        int[][] l = readCsvFile2Dim("data/l.csv");
        
        try {
            Solver solver = new Solver(a, b, 0.5, 0.5, d, e, h, l, 20, 540, 1250);
            solver.facilityLocation();
        } catch (IloException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static int[] readCsvFile1Dim(String fileName) {
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

    private static int[][] readCsvFile2Dim(String fileName) {
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
