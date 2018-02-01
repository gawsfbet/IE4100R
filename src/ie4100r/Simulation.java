/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie4100r;

import FileManager.CsvReader;
import Logic.MIPSolver;
import Utils.RNG;
import ilog.concert.IloException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author Kevin-Notebook
 */
public class Simulation {
    private int[] a = CsvReader.readCsvFile1Dim("data/a.csv"); //demand at residential nodes
    private int[] b = CsvReader.readCsvFile1Dim("data/b.csv"); //demand at MRT nodes
    private int[][] d = CsvReader.readCsvFile2Dim("data/d.csv");
    private int[][] e = CsvReader.readCsvFile2Dim("data/e.csv");
    private int[][] h = CsvReader.readCsvFile2Dim("data/h.csv");
    private int[][] l = CsvReader.readCsvFile2Dim("data/l.csv");
    
    public HashMap simulate() {
        int p = 100; //number of new lockers
        int C = 540; //locker capacity
        int S = 1250; //distance permitted
        
        RNG rng = new RNG();
        double[] aModifier = rng.generateNormalVars(a.length);
        
        IntStream.range(0, a.length).map(i -> (int) aModifier[i] * a[i]).toArray();
        
        try {
            MIPSolver solver = new MIPSolver(a, b, 0.0317725, 0.01588, d, e, h, l, p, C, S);
            solver.facilityLocation(1.0, -0.2, -150, 0);
        } catch (IloException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
