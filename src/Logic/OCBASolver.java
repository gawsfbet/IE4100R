/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex;
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
public class OCBASolver {

    private IloCplex cplex;

    private static final int T = 2000000000; //large integer (2 billion)

    private int R; //total number of residential demand nodes
    private int M; //total number of MRT demand nodes
    private int F; //total number of shopping mall modes (for placement)
    private int I; //total number of POPStation nodes (competition)
    private int S; //Maximum allowable distance (1250)
    private int C; //Maximum holding capacity of a locker for demand

    private int p; //Number of new lockers to place

    private int[] a; //Demand volume at residential nodes
    private int[] b; //Demand volume at MRT nodes
    //Now alpha and beta is randomised for each location
    private double[] alpha; //Proportion of residents that use E-commerce
    private double[] beta; //Proportion of MRT riders that use E-commerce
    private int[][] d; //Distance from resident demand node r to shopping mall f
    private int[][] e; //Distance from MRT demand mode m to shopping mall f
    private int[][] h; //Distance from resident demand node r to POPStation i
    private int[][] l; //Distance from MRT demand mode m to POPStation i

    private int[] y; //new variable: facilities that were placed

    IloIntVar[][] c; //demand from residential r to shopping mall f
    IloIntVar[][] g; //demand from MRT m to shopping mall f
    IloIntVar[][] j; //demand from residential r to POPStation i
    IloIntVar[][] k; //demand from MRT m to POPStation i

    IloIntVar[][] w; //if demand can flow from residential r to shopping mall f
    IloIntVar[][] x; //if demand can flow from MRT m to shopping mall f
    IloIntVar[][] n; //if demand can flow from residential r to POPStation i
    IloIntVar[][] o; //if demand can flow from MRT m to POPStation i

    IloIntVar[] z; //if POPstation at i is full

    IloLinearIntExpr demandObjective;
    IloLinearIntExpr distanceObjective;
    IloLinearIntExpr[] demandPerPop;

    public OCBASolver(int[] a, int[] b, double[] alpha, double[] beta, int[][] d, int[][] e, int[][] h, int[][] l, int p, int C, int S, int[] y) throws IloException {
        this.cplex = new IloCplex();

        this.a = a;
        this.b = b;
        this.alpha = Arrays.copyOf(alpha, alpha.length);
        this.beta = Arrays.copyOf(beta, beta.length);
        this.d = d;
        this.e = e;
        this.h = h;
        this.l = l;

        this.R = a.length;
        this.M = b.length;
        this.F = d[0].length;
        this.I = h[0].length;

        this.p = p;
        this.C = C;
        this.S = S;

        this.y = y;

        cplex.setOut(null);
    }

    public HashMap facilityLocation(double demandCoeff, double distanceCoeff, double lockerCoeff, int folderName) throws IloException {
        assert demandCoeff >= 0;
        assert distanceCoeff <= 0;
        assert lockerCoeff <= 0;

        System.out.println("LP problem formulated with:");
        System.out.println(String.format("%d residential nodes", R));
        System.out.println(String.format("%d MRT nodes", M));
        System.out.println(String.format("%d potential locations", F));
        System.out.println(String.format("%d existing POPStations", I));
        System.out.println();
        System.out.println(String.format("Number of new lockers: %d", p));
        System.out.println(String.format("Maximum allowable distance: %d metres", S));
        System.out.println(String.format("Locker capacity: %d", C));
        System.out.println();

        //variables
        createVariables();

        //expressions
        defineObjectives(demandCoeff, distanceCoeff, lockerCoeff);

        //constraints
        //Demand constraints
        IloConstraint[] demandConstraints = addDemandConstraints();

        //Binary constraints
        IloConstraint[] binaryConstraints = addBinaryConstraints();

        //Flow constraints
        IloConstraint[] flowConstraints = addFlowConstraints();

        //Distance constraints
        IloConstraint[] distanceConstraints = addDistanceConstraints();

        //Capacity constraints
        IloConstraint[] capacityConstraints = addCapacityConstraints();

        //Competition constraints
        addCompetitionConstraints();
        //cplex.addLe(cplex.sum(y), p);

        //solve
        System.out.println("Solving...");
        return solve();
    }

    public void createVariables() throws IloException {
        System.out.println("Creating decision variables...");
        c = new IloIntVar[R][F]; //demand from residential r to shopping mall f
        g = new IloIntVar[M][F]; //demand from MRT m to shopping mall f
        j = new IloIntVar[R][I]; //demand from residential r to POPStation i
        k = new IloIntVar[M][I]; //demand from MRT m to POPStation i

        for (int i = 0; i < R; i++) {
            c[i] = cplex.intVarArray(F, 0, a[i]);
            j[i] = cplex.intVarArray(I, 0, a[i]);
        }

        for (int i = 0; i < M; i++) {
            g[i] = cplex.intVarArray(F, 0, b[i]);
            k[i] = cplex.intVarArray(I, 0, b[i]);
        }

        w = new IloIntVar[R][F]; //if demand can flow from residential r to shopping mall f
        x = new IloIntVar[M][F]; //if demand can flow from MRT m to shopping mall f
        n = new IloIntVar[R][I]; //if demand can flow from residential r to POPStation i
        o = new IloIntVar[M][I]; //if demand can flow from MRT m to POPStation i

        for (int i = 0; i < R; i++) {
            w[i] = cplex.boolVarArray(F);
            n[i] = cplex.boolVarArray(I);
        }

        for (int i = 0; i < M; i++) {
            x[i] = cplex.boolVarArray(F);
            o[i] = cplex.boolVarArray(I);
        }

        //IloIntVar[] y = cplex.boolVarArray(F);
        z = cplex.boolVarArray(I);
    }

    public IloObjective defineObjectives(double demandCoeff, double distanceCoeff, double lockerCoeff) throws IloException {
        assert demandCoeff >= 0;
        assert distanceCoeff <= 0;
        assert lockerCoeff <= 0;

        demandObjective = cplex.linearIntExpr();
        for (int i = 0; i < F; i++) {
            for (int i1 = 0; i1 < R; i1++) {
                demandObjective.addTerm(1, c[i1][i]);
            }

            for (int i2 = 0; i2 < M; i2++) {
                demandObjective.addTerm(1, g[i2][i]);
            }
        }

        distanceObjective = cplex.linearIntExpr();
        for (int i = 0; i < R; i++) {
            distanceObjective.addTerms(d[i], c[i]);
        }
        for (int i = 0; i < M; i++) {
            distanceObjective.addTerms(e[i], g[i]);
        }

        /*IloLinearIntExpr lockerObjective = cplex.linearIntExpr();
        for (int i = 0; i < F; i++) {
            lockerObjective.addTerm(1, y[i]);
        }*/
        //no lockers here
        IloNumExpr combinedObjective = cplex.sum(
                cplex.prod(demandCoeff, demandObjective), //maximize
                cplex.prod(distanceCoeff, distanceObjective)); //minimize
        combinedObjective = cplex.sum(lockerCoeff * Arrays.stream(y).sum(), combinedObjective);
        //define objective
        return cplex.addMaximize(combinedObjective);
    }

    //remove these constraints to change alpha and beta
    public IloConstraint[] addDemandConstraints() throws IloException {
        System.out.println("Adding demand constraints...");
        IloConstraint[] demandConstraints = new IloConstraint[R + M];

        for (int i = 0; i < R; i++) {
            demandConstraints[i] = cplex.addGe(alpha[i] * a[i], cplex.sum(cplex.sum(c[i]), cplex.sum(j[i])));
        }
        for (int i = 0; i < M; i++) {
            demandConstraints[R + i] = cplex.addGe(beta[i] * b[i], cplex.sum(cplex.sum(g[i]), cplex.sum(k[i])));
        }

        return demandConstraints;
    }

    public IloConstraint[] addBinaryConstraints() throws IloException {
        System.out.println("Adding binary constraints...");
        ArrayList<IloConstraint> binaryConstraints = new ArrayList<>();

        for (int i = 0; i < R; i++) {
            for (int i1 = 0; i1 < F; i1++) {
                binaryConstraints.add(cplex.addLe(w[i][i1], y[i1]));
            }
        }
        for (int i = 0; i < M; i++) {
            for (int i1 = 0; i1 < F; i1++) {
                binaryConstraints.add(cplex.addLe(x[i][i1], y[i1]));
            }
        }

        return binaryConstraints.toArray(new IloConstraint[0]);
    }

    public IloConstraint[] addFlowConstraints() throws IloException {
        System.out.println("Adding flow constraints...");
        ArrayList<IloConstraint> flowConstraints = new ArrayList<>();

        for (int i = 0; i < R; i++) {
            for (int i1 = 0; i1 < F; i1++) {
                flowConstraints.add(cplex.addLe(c[i][i1], cplex.prod(T, w[i][i1])));
            }
            for (int i2 = 0; i2 < I; i2++) {
                flowConstraints.add(cplex.addLe(j[i][i2], cplex.prod(T, n[i][i2])));
            }
        }
        for (int i = 0; i < M; i++) {
            for (int i1 = 0; i1 < F; i1++) {
                flowConstraints.add(cplex.addLe(g[i][i1], cplex.prod(T, x[i][i1])));
            }
            for (int i2 = 0; i2 < I; i2++) {
                flowConstraints.add(cplex.addLe(k[i][i2], cplex.prod(T, o[i][i2])));
            }
        }

        return flowConstraints.toArray(new IloConstraint[0]);
    }

    public IloConstraint[] addDistanceConstraints() throws IloException {
        System.out.println("Adding distance constraints...");
        ArrayList<IloConstraint> distanceConstraints = new ArrayList<>();

        for (int i = 0; i < R; i++) {
            for (int i1 = 0; i1 < F; i1++) {
                distanceConstraints.add(cplex.addLe(cplex.prod(d[i][i1], w[i][i1]), S));
            }
            for (int i2 = 0; i2 < I; i2++) {
                distanceConstraints.add(cplex.addLe(cplex.prod(h[i][i2], n[i][i2]), S));
            }
        }
        for (int i = 0; i < M; i++) {
            for (int i1 = 0; i1 < F; i1++) {
                distanceConstraints.add(cplex.addLe(cplex.prod(e[i][i1], x[i][i1]), S));
            }
            for (int i2 = 0; i2 < I; i2++) {
                distanceConstraints.add(cplex.addLe(cplex.prod(l[i][i2], o[i][i2]), S));
            }
        }

        return distanceConstraints.toArray(new IloConstraint[0]);
    }

    public IloConstraint[] addCapacityConstraints() throws IloException {
        System.out.println("Adding capacity constraints...");
        IloConstraint[] capacityConstraints = new IloConstraint[F + I];

        IloLinearIntExpr[] demandPerLocker = new IloLinearIntExpr[F];
        for (int i = 0; i < F; i++) {
            demandPerLocker[i] = cplex.linearIntExpr();

            for (int i1 = 0; i1 < R; i1++) {
                demandPerLocker[i].addTerm(1, c[i1][i]);
            }
            for (int i2 = 0; i2 < M; i2++) {
                demandPerLocker[i].addTerm(1, g[i2][i]);
            }

            capacityConstraints[i] = cplex.addLe(demandPerLocker[i], C);
        }
        demandPerPop = new IloLinearIntExpr[I];
        for (int i = 0; i < I; i++) {
            demandPerPop[i] = cplex.linearIntExpr();
            for (int i1 = 0; i1 < R; i1++) {
                demandPerPop[i].addTerm(1, j[i1][i]);
            }
            for (int i2 = 0; i2 < M; i2++) {
                demandPerPop[i].addTerm(1, k[i2][i]);
            }

            capacityConstraints[F + i] = cplex.addLe(demandPerPop[i], C);
        }

        return capacityConstraints;
    }

    public void addCompetitionConstraints() throws IloException {
        System.out.println("Adding competition constraints...");
        int numElements = F * I * (R + M) + I * I * (R + M);
        System.out.println(numElements);

        for (int i1 = 0; i1 < F; i1++) {
            for (int i2 = 0; i2 < I; i2++) {
                for (int i = 0; i < R; i++) {
                    cplex.addLe(cplex.prod(d[i][i1], w[i][i1]), cplex.sum(h[i][i2] - 1, cplex.prod(T, cplex.sum(1, cplex.prod(-1, z[i2])))));
                }

                for (int i = 0; i < M; i++) {
                    cplex.addLe(cplex.prod(e[i][i1], x[i][i1]), cplex.sum(l[i][i2] - 1, cplex.prod(T, cplex.sum(1, cplex.prod(-1, z[i2])))));
                }
            }
        }

        for (int i1 = 0; i1 < I; i1++) {
            cplex.addGe(z[i1], cplex.sum(1, cplex.prod(-1, cplex.prod(1.0 / C, demandPerPop[i1]))));
            //cplex.addGe(z[i], cplex.prod(1.0 / C, cplex.sum(C, cplex.prod(-1, demandPerPop[i]))));
            for (int i2 = 0; i2 < I; i2++) {
                if (i1 == i2) {
                    continue;
                }
                for (int i = 0; i < R; i++) {
                    cplex.addLe(cplex.prod(h[i][i2], n[i][i2]), cplex.sum(h[i][i1], cplex.prod(T, cplex.sum(1, cplex.prod(-1, z[i1])))));
                }
                for (int i = 0; i < M; i++) {
                    cplex.addLe(cplex.prod(l[i][i2], o[i][i2]), cplex.sum(l[i][i1], cplex.prod(T, cplex.sum(1, cplex.prod(-1, z[i1])))));
                }
            }
        }
    }

    public void changeAlphaAndBeta(double[] alpha, double[] beta) {
        this.alpha = Arrays.copyOf(alpha, alpha.length);
        this.beta = Arrays.copyOf(beta, beta.length);
    }

    public HashMap solve() throws IloException {
        System.out.println("Solving...");
        if (cplex.solve()) {
            System.out.println("Total Weighted Obj = " + cplex.getObjValue());
            System.out.println("Demand Obj = " + cplex.getValue(demandObjective));
            System.out.println("Distance Obj = " + cplex.getValue(distanceObjective));
            System.out.println("Locker Obj (fixed) = " + Arrays.stream(y).sum());
            //System.out.println("x   = " + cplex.getValue(x));
            //System.out.println("y   = " + cplex.getValue(y));

            /*writeObjectives(demandCoeff, distanceCoeff, lockerCoeff, 
                    cplex.getObjValue(), cplex.getValue(demandObjective), cplex.getValue(distanceObjective), cplex.getValue(lockerObjective), folderName);

            writeToCsv2Dim(c, "c", folderName);
            writeToCsv2Dim(g, "g", folderName);
            
            writeToCsv2Dim(w, "w", folderName);
            writeToCsv2Dim(x, "x", folderName);
            
            writeToCsv1Dim(y, "y", folderName);*/
            HashMap<String, Integer> results = new HashMap<>();
            results.put("total", (int) Math.round(cplex.getObjValue()));
            results.put("demand", (int) Math.round(cplex.getValue(demandObjective)));
            results.put("distance", (int) Math.round(cplex.getValue(distanceObjective)));
            results.put("locker", Arrays.stream(y).sum());

            return results;
        } else {
            System.out.println("Solution not found.");
            return null;
        }
    }

    public void deleteConstraint(IloConstraint[] constraint) throws IloException {
        cplex.delete(constraint);
    }

    public void initVariablesAndOtherConstraints() throws IloException {
        createVariables();
        
        addBinaryConstraints();
        addFlowConstraints();
        addDistanceConstraints();
        addCapacityConstraints();
        addCompetitionConstraints();
    }

    public void makeOutputFolder() {
        File dir = new File("output");
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public void makeOutputFolder(int folderName) {
        File dir = new File(String.format("output\\%d", folderName));
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public void writeObjectives(double demandCoeff, double distanceCoeff, double lockerCoeff,
            double totalObj, double demandObj, double distanceObj, double lockerObj, int folderName) {
        try {
            makeOutputFolder();
            makeOutputFolder(folderName);

            FileWriter writer = new FileWriter(String.format("output\\%d\\objectives.txt", folderName));

            writer.append("Coefficients\r\n");
            writer.append(String.format("Demand Obj = %.2f\r\n", demandCoeff));
            writer.append(String.format("Distance Obj = %.2f\r\n", distanceCoeff));
            writer.append(String.format("Locker Obj = %.2f\r\n", lockerCoeff));
            writer.append("\r\n");

            writer.append("Adjusted coefficients\r\n");
            writer.append(String.format("Demand Obj = %f\r\n", demandCoeff / (demandCoeff - distanceCoeff - lockerCoeff)));
            writer.append(String.format("Distance Obj = %f\r\n", -distanceCoeff / (demandCoeff - distanceCoeff - lockerCoeff)));
            writer.append(String.format("Locker Obj = %f\r\n", -lockerCoeff / (demandCoeff - distanceCoeff - lockerCoeff)));
            writer.append("\r\n");

            writer.append("Results\r\n");
            writer.append(String.format("Total Weighted Obj = %d\r\n", Math.round(totalObj)));
            writer.append(String.format("Demand Obj = %d\r\n", Math.round(demandObj)));
            writer.append(String.format("Distance Obj = %d\r\n", Math.round(distanceObj)));
            writer.append(String.format("Locker Obj = %d\r\n", Math.round(lockerObj)));

            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing to file " + e.getCause().toString());
        }
    }

    public void writeToCsv1Dim(IloIntVar[] output, String fileName, int folderName) throws IloException {
        try {
            makeOutputFolder();
            makeOutputFolder(folderName);

            FileWriter writer = new FileWriter(String.format("output\\%d\\%s.csv", folderName, fileName));

            for (int i = 0; i < output.length; i++) {
                writer.append(Double.toString(cplex.getValue(output[i])));
                writer.append(',');
            }

            //generate whatever data you want
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing to file " + e.getCause().toString());
        }
    }

    public void writeToCsv2Dim(IloIntVar[][] output, String fileName, int folderName) throws IloException {
        try {
            makeOutputFolder();
            makeOutputFolder(folderName);

            FileWriter writer = new FileWriter(String.format("output\\%d\\%s.csv", folderName, fileName));

            for (int i1 = 0; i1 < output.length; i1++) {
                for (int i2 = 0; i2 < output[i1].length; i2++) {
                    writer.append(Double.toString(cplex.getValue(output[i1][i2])));
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
