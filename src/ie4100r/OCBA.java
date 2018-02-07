/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie4100r;

import FileManager.CsvReader;
import Logic.OCBASolver;
import Utils.RNG;
import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloObjective;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Kevin-Notebook
 */
public class OCBA {
    RNG random = new RNG();
    public static void main(String[] args){
        //fixed parameters
        int[] a = CsvReader.readCsvFile1Dim("data/a.csv"); //demand at residential nodes
        int[] b = CsvReader.readCsvFile1Dim("data/b.csv"); //demand at MRT nodes
        int[][] d = CsvReader.readCsvFile2Dim("data/d.csv");
        int[][] e = CsvReader.readCsvFile2Dim("data/e.csv");
        int[][] h = CsvReader.readCsvFile2Dim("data/h.csv");
        int[][] l = CsvReader.readCsvFile2Dim("data/l.csv");

        int p = 100; //number of new lockers
        int C = 540; //locker capacity
        int S = 1250; //distance permitted
        
        int k = 20; //number of designs
        
        //variable parameters
        double[] alpha = new double[a.length], beta = new double[b.length];
        int[][] y = new int[k][];
        for (int i = 0; i < k; i++) {
            y[i] = CsvReader.readCsvFileForY(String.format("output/%d/y.csv", i + 1));
        }
        
        ArrayList<HashMap<String, Double>>[] results = new ArrayList[k];
        ArrayList<Integer>[] iterations = new ArrayList[k];
        Arrays.parallelSetAll(results, i -> new ArrayList<>());
        Arrays.parallelSetAll(iterations, i -> new ArrayList<>());
        
        double[][] output = new double[20][4];
        
        final double demandCoeff = 1.0, distanceCoeff = -0.2, lockerCoeff = -100;
        
        try {
            OCBASolver solver = new OCBASolver(a, b, d, e, h, l, p, C, S);
            solver.initVariablesAndOtherConstraints();

            double[] J = new double[k]; //sample means
            double[] s = new double[k]; //sample sd
            
            for (int i = 0; i < k; i++) {
                System.out.println("Design " + (i + 1));
                solver.setY(Arrays.stream(y[i]).sum());
                IloConstraint[] binaryConstraints = solver.addBinaryConstraints(y[i]);
                
                Arrays.fill(alpha, 0.0317725);
                Arrays.fill(beta, 0.01588);
                
                IloConstraint[] demandConstraints = solver.addDemandConstraints(alpha, beta);

                IloObjective objective = solver.defineObjectives(demandCoeff, distanceCoeff, lockerCoeff);
                HashMap<String, Double> result = solver.solve();
                if (result != null) {
                    output[i][0] = result.get("total");
                    output[i][1] = result.get("demand");
                    output[i][2] = result.get("distance");
                    output[i][3] = result.get("locker");
                }

                solver.deleteObjective(objective);
                solver.deleteConstraint(demandConstraints);
                solver.deleteConstraint(binaryConstraints);
            }
            
            writeToCsv2Dim(output);
        } catch (IloException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void writeToCsv2Dim(double[][] output) {
        try {
            FileWriter writer = new FileWriter("stuff.csv");

            for (int i1 = 0; i1 < output.length; i1++) {
                for (int i2 = 0; i2 < output[i1].length; i2++) {
                    writer.append(Double.toString(output[i1][i2]));
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
}
