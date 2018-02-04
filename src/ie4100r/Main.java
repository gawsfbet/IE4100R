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
import ilog.concert.IloObjective;
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

        double[] alpha = new double[a.length], beta = new double[b.length];
        int[][] y = new int[20][];
        
        for (int i = 0; i < 20; i++) {
            y[i] = CsvReader.readCsvFileForY(String.format("output/%d/y.csv", i + 1));
        }

        try {
            double demandCoeff = 1.0, distanceCoeff = -0.002, lockerCoeff = -150;

            OCBASolver solver = new OCBASolver(a, b, d, e, h, l, p, C, S);
            solver.initVariablesAndOtherConstraints();
            
            for (int i = 0; i < 20; i++) {
                alpha = random.generateNormalVars(alpha.length, 0.0317725, 0.005);
                beta = random.generateNormalVars(beta.length, 0.01588, 0.0025);

                solver.setY(y[i]);
                IloConstraint[] binaryConstraints = solver.addBinaryConstraints();

                solver.setAlphaAndBeta(alpha, beta);
                IloConstraint[] demandConstraints = solver.addDemandConstraints();

                IloObjective objective = solver.defineObjectives(demandCoeff, distanceCoeff, lockerCoeff);
                solver.solve();
                
                solver.deleteObjective(objective);
                solver.deleteConstraint(binaryConstraints);
                solver.deleteConstraint(demandConstraints);
            }
        } catch (IloException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public double calculateMean(ArrayList<Double> values) {
        return 0;
    }
}
