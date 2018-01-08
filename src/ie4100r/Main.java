/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie4100r;

import FileManager.CsvReader;
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
        int[] a = CsvReader.readCsvFile1Dim("data/a.csv");
        int[] b = CsvReader.readCsvFile1Dim("data/b.csv");
        int[][] d = CsvReader.readCsvFile2Dim("data/d.csv");
        int[][] e = CsvReader.readCsvFile2Dim("data/e.csv");
        int[][] h = CsvReader.readCsvFile2Dim("data/h.csv");
        int[][] l = CsvReader.readCsvFile2Dim("data/l.csv");
        
        int p = 100; //number of new lockers
        int C = 540; //locker capacity
        int S = 1250; //distance permitted
        
        try {
            Solver solver = new Solver(a, b, 0.0317725, 0.01588, d, e, h, l, p, C, S);
            solver.facilityLocation(1.0, -0.2, -200, 8);
        } catch (IloException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
