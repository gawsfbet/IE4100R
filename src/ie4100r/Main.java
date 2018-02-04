/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie4100r;

import FileManager.CsvReader;
import Logic.MIPSolver;
import Logic.OCBASolver;
import Utils.RNG;
import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author Kevin-Notebook
 */
//d.csv -> 139, BF blank
public class Main {
    
    private static RNG random = new RNG();

    public static void main(String[] args) {
        int[] a = CsvReader.readCsvFile1Dim("data/a.csv"); //demand at residential nodes
        int[] b = CsvReader.readCsvFile1Dim("data/b.csv"); //demand at MRT nodes
        int[][] d = CsvReader.readCsvFile2Dim("data/d.csv");
        int[][] e = CsvReader.readCsvFile2Dim("data/e.csv");
        int[][] h = CsvReader.readCsvFile2Dim("data/h.csv");
        int[][] l = CsvReader.readCsvFile2Dim("data/l.csv");

        int p = 100; //number of new lockers
        int C = 540; //locker capacity
        int S = 1250; //distance permitted

        /*RNG rng = new RNG();
        double[] aModifierPercent = rng.generateNormalVars(a.length, 0, 5);
        
        int[] newa = IntStream.range(0, a.length).map(i -> (int) Math.round((aModifierPercent[i] + 100) / 100 * a[i])).toArray();
        
        for (int i = 0; i < newa.length; i++) {
            System.out.println(a[i] + " " + newa[i]);
        }*/
        double[] alpha = new double[a.length], beta = new double[b.length];
        int[] y = CsvReader.readCsvFileForY("data/y.csv");

        try {
            double demandCoeff = 1.0, distanceCoeff = -0.002, lockerCoeff = -150;
            
            Arrays.fill(alpha, 0.0317725);
            Arrays.fill(beta, 0.01588);

            OCBASolver solver = new OCBASolver(a, b, alpha, beta, d, e, h, l, p, C, S, y);
            solver.initVariablesAndOtherConstraints();
            solver.defineObjectives(demandCoeff, distanceCoeff, lockerCoeff);
            IloConstraint[] binaryConstraints = solver.addBinaryConstraints();
            IloConstraint[] demandConstraints = solver.addDemandConstraints();
            solver.solve();
            
            /*alpha = random.generateNormalVars(alpha.length, 0.0317725, 0.005);
            beta = random.generateNormalVars(beta.length, 0.01588, 0.0025);
            
            solver.changeAlphaAndBeta(alpha, beta);
            solver.deleteConstraint(demandConstraints);
            solver.addDemandConstraints();
            solver.solve();*/
        } catch (IloException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
