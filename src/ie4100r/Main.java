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
import ilog.concert.IloObjective;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 *
 * @author Kevin-Notebook
 */
//d.csv -> 139, BF blank
public class Main {
    
    private static RNG random = new RNG();

    public static void main(String[] args) {
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
        
        //OCBA parameters
        int T = 300, n0 = 10, totalComputingBudget = k * n0; //max allowed computing budget and initial reps
        int[] Nlast = new int[k], Nnext = new int[k]; //number of replications
        Arrays.fill(Nlast, 0);
        Arrays.fill(Nnext, n0);
        int delta = 2; //incremental number of simulations
        //list of results from model
        ArrayList<HashMap<String, Double>>[] results = new ArrayList[k];
        ArrayList<Integer>[] iterations = new ArrayList[k];
        ArrayList<Double>[] means = new ArrayList[k], sds = new ArrayList[k];
        Arrays.parallelSetAll(results, i -> new ArrayList<>());
        Arrays.parallelSetAll(iterations, i -> new ArrayList<>());
        Arrays.parallelSetAll(means, i -> new ArrayList<>());
        Arrays.parallelSetAll(sds, i -> new ArrayList<>());

        try {
            final double demandCoeff = 1.0, distanceCoeff = -0.3, lockerCoeff = -100;

            OCBASolver solver = new OCBASolver(a, b, d, e, h, l, p, C, S);
            solver.initVariablesAndOtherConstraints();
            
            double[] J = new double[k]; //sample means
            double[] s = new double[k]; //sample sd
            
            int count = 0;
            
            while (Arrays.stream(Nnext).sum() < T) {
                count++;
                //simulation part
                for (int i = 0; i < k; i++) {
                    System.out.println(String.format("Design %d, performing %d replications", i + 1, Math.max(0, Nnext[i] - Nlast[i])));
                    solver.setY(Arrays.stream(y[i]).sum());
                    IloConstraint[] binaryConstraints = solver.addBinaryConstraints(y[i]);
                    
                    for (int j = 0; j < 15; j++) { //simulation for each design
                        iterations[i].add(count);
                        System.out.println(String.format("Iteration %d, simulating design %d, replication %d", count, i + 1, j + 1));
                        
                        random.generateNormalVars(alpha, 0.0317725, 0.01);
                        random.generateNormalVars(beta, 0.01588, 0.005);

                        IloConstraint[] demandConstraints = solver.addDemandConstraints(alpha, beta);

                        IloObjective objective = solver.defineObjectives(demandCoeff, distanceCoeff, lockerCoeff);
                        HashMap<String, Double> result = solver.solve();
                        if (result != null) results[i].add(result);

                        solver.deleteObjective(objective);
                        solver.deleteConstraint(demandConstraints);
                    }
                    solver.deleteConstraint(binaryConstraints);
                    
                    J[i] = calculateMean(results[i]);
                    s[i] = calculateSD(results[i], J[i]);
                    means[i].add(J[i]);
                    sds[i].add(s[i]);
                }
                
                //allocation part
                int best = maxIndex(J), ref;
                IntStream.range(0, k).forEach(i -> Nlast[i] = Nnext[i]);
                
                totalComputingBudget += delta;
                double[] ratios = new double[k];
                if (best == 0) {
                    ratios[1] = 1;
                    ref = 1;
                } else {
                    ratios[0] = 1;
                    ref = 0;
                }
                for (int i = 0; i < k; i++) {
                    if (i == best || i == ref) continue;

                    ratios[i] = ((s[i] * (J[best] - J[ref])) / (s[ref] * (J[best] - J[i]))) * ((s[i] * (J[best] - J[ref])) / (s[ref] * (J[best] - J[i])));
                }
                ratios[best] = s[best] * Math.sqrt(IntStream.range(0, k).filter(i -> i != best).mapToDouble(i -> (ratios[i] / s[i]) * (ratios[i] / s[i])).sum());
  
                double totalRatio = Arrays.stream(ratios).sum();
                for (int i = 0; i < k; i++) {
                    Nnext[i] = (int) Math.round(totalComputingBudget * ratios[i] / totalRatio);
                }
                System.out.println(String.format("Iteration %d complete, best design: %d", count, best + 1));
            }
            
            CsvReader.writeDataToFiles(results, "ocba\\ua\\largesd");
            CsvReader.writeIterToFiles(iterations, "ocba\\ua\\largesd");
            CsvReader.writeMeanToFiles(means, sds, "ocba\\ua\\largesd");
        } catch (IloException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static double calculateMean(ArrayList<HashMap<String, Double>> results) {
        return results.stream().mapToDouble(result -> result.get("total")).sum() / results.size();
    }
    
    public static double calculateSD(ArrayList<HashMap<String, Double>> results, double mean) {
        return Math.sqrt(results.stream().mapToDouble(result -> (result.get("total") - mean) * (result.get("total") - mean)).sum() / (results.size() - 1));
    }
    
    public static int maxIndex(double[] values) {
        double max = Double.NEGATIVE_INFINITY;
        int b = 0;
        
        for (int i = 0; i < values.length; i++) {
            if (max < values[i]) {
                max = values[i];
                b = i;
            }
        }
        
        return b;
    }
}
