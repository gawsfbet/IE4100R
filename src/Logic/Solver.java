/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import java.io.FileWriter;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Kevin-Notebook
 */
public class Solver {

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
    private double alpha; //Proportion of residents that use E-commerce
    private double beta; //Proportion of MRT riders that use E-commerce
    private int[][] d; //Distance from resident demand node r to shopping mall f
    private int[][] e; //Distance from MRT demand mode m to shopping mall f
    private int[][] h; //Distance from resident demand node r to POPStation i
    private int[][] l; //Distance from MRT demand mode m to POPStation i

    public Solver(int[] a, int[] b, double alpha, double beta, int[][] d, int[][] e, int[][] h, int[][] l, int p, int C, int S) throws IloException {
        this.cplex = new IloCplex();

        this.a = a;
        this.b = b;
        this.alpha = alpha;
        this.beta = beta;
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
    }

    public Solver(int R, int M, int F, int I, int S, int C, int p, double alpha, double beta) throws IloException {
        this.cplex = new IloCplex();

        this.R = R;
        this.M = M;
        this.F = F;
        this.I = I;
        this.S = S;
        this.C = C;

        this.p = p;

        this.alpha = alpha;
        this.beta = beta;

        this.a = new int[R];
        this.b = new int[M];
        this.d = new int[R][F];
        this.e = new int[M][F];
        this.h = new int[R][I];
        this.l = new int[M][I];
    }

    public void facilityLocation() throws IloException {
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
        System.out.println("Creating decision variables...");
        IloIntVar[][] c = new IloIntVar[R][F]; //demand from residential r to shopping mall f
        IloIntVar[][] g = new IloIntVar[M][F]; //demand from MRT m to shopping mall f
        IloIntVar[][] j = new IloIntVar[R][I]; //demand from residential r to POPStation i
        IloIntVar[][] k = new IloIntVar[M][I]; //demand from MRT m to POPStation i

        for (int i = 0; i < R; i++) {
            c[i] = cplex.intVarArray(F, 0, a[i]);
            j[i] = cplex.intVarArray(I, 0, a[i]);
        }

        for (int i = 0; i < M; i++) {
            g[i] = cplex.intVarArray(F, 0, b[i]);
            k[i] = cplex.intVarArray(I, 0, b[i]);
        }

        IloIntVar[][] w = new IloIntVar[R][F]; //if demand can flow from residential r to shopping mall f
        IloIntVar[][] x = new IloIntVar[M][F]; //if demand can flow from MRT m to shopping mall f
        IloIntVar[][] n = new IloIntVar[R][I]; //if demand can flow from residential r to POPStation i
        IloIntVar[][] o = new IloIntVar[M][I]; //if demand can flow from MRT m to POPStation i

        for (int i = 0; i < R; i++) {
            w[i] = cplex.boolVarArray(F);
            n[i] = cplex.boolVarArray(I);
        }

        for (int i = 0; i < M; i++) {
            x[i] = cplex.boolVarArray(F);
            o[i] = cplex.boolVarArray(I);
        }

        IloIntVar[] y = cplex.boolVarArray(F);
        IloIntVar[] z = cplex.boolVarArray(I);

        //expressions
        System.out.println("Defining objectives...");
        IloLinearIntExpr demandObjective = cplex.linearIntExpr();
        for (int i = 0; i < F; i++) {
            for (int i1 = 0; i1 < R; i1++) {
                demandObjective.addTerm(1, c[i1][i]);
            }

            for (int i2 = 0; i2 < M; i2++) {
                demandObjective.addTerm(1, g[i2][i]);
            }
        }

        IloLinearIntExpr distanceObjective = cplex.linearIntExpr();
        for (int i = 0; i < R; i++) {
            distanceObjective.addTerms(d[i], w[i]);
        }
        for (int i = 0; i < M; i++) {
            distanceObjective.addTerms(e[i], x[i]);
        }

        IloLinearIntExpr lockerObjective = cplex.linearIntExpr();
        for (int i = 0; i < F; i++) {
            lockerObjective.addTerm(1, y[i]);
        }
        //define objective
        cplex.addMaximize(demandObjective);
        //cplex.addMinimize(distanceObjective);
        //cplex.addMinimize(lockerObjective);

        //constraints
        //Demand constraints
        System.out.println("Adding demand constraints...");
        for (int i = 0; i < R; i++) {
            cplex.addGe(alpha * a[i], cplex.sum(cplex.sum(c[i]), cplex.sum(j[i])));
        }
        for (int i = 0; i < M; i++) {
            cplex.addGe(beta * b[i], cplex.sum(cplex.sum(g[i]), cplex.sum(k[i])));
        }

        //Binary constraints
        System.out.println("Adding binary constraints...");
        for (int i = 0; i < R; i++) {
            for (int i1 = 0; i1 < F; i1++) {
                cplex.addLe(w[i][i1], y[i1]);
            }
        }
        for (int i = 0; i < M; i++) {
            for (int i1 = 0; i1 < F; i1++) {
                cplex.addLe(x[i][i1], y[i1]);
            }
        }

        //Flow constraints
        System.out.println("Adding flow constraints...");
        for (int i = 0; i < R; i++) {
            //System.out.println(i);
            for (int i1 = 0; i1 < F; i1++) {
                cplex.addLe(c[i][i1], cplex.prod(T, w[i][i1]));
            }
            for (int i2 = 0; i2 < I; i2++) {
                cplex.addLe(j[i][i2], cplex.prod(T, n[i][i2]));
            }
        }
        for (int i = 0; i < M; i++) {
            for (int i1 = 0; i1 < F; i1++) {
                cplex.addLe(g[i][i1], cplex.prod(T, x[i][i1]));
            }
            for (int i2 = 0; i2 < I; i2++) {
                cplex.addLe(k[i][i2], cplex.prod(T, o[i][i2]));
            }
        }

        //Distance constraints
        System.out.println("Adding distance constraints...");
        for (int i = 0; i < R; i++) {
            for (int i1 = 0; i1 < F; i1++) {
                cplex.addLe(cplex.prod(d[i][i1], w[i][i1]), S);
            }
            for (int i2 = 0; i2 < I; i2++) {
                cplex.addLe(cplex.prod(h[i][i2], n[i][i2]), S);
            }
        }
        for (int i = 0; i < M; i++) {
            for (int i1 = 0; i1 < F; i1++) {
                cplex.addLe(cplex.prod(e[i][i1], x[i][i1]), S);
            }
            for (int i2 = 0; i2 < I; i2++) {
                cplex.addLe(cplex.prod(l[i][i2], o[i][i2]), S);
            }
        }

        //Capacity constraints
        System.out.println("Adding capacity constraints...");
        IloLinearIntExpr[] demandPerLocker = new IloLinearIntExpr[F];
        for (int i = 0; i < F; i++) {
            demandPerLocker[i] = cplex.linearIntExpr();

            for (int i1 = 0; i1 < R; i1++) {
                demandPerLocker[i].addTerm(1, c[i1][i]);
            }
            for (int i2 = 0; i2 < M; i2++) {
                demandPerLocker[i].addTerm(1, g[i2][i]);
            }

            cplex.addLe(demandPerLocker[i], C);
        }
        IloLinearIntExpr[] demandPerPop = new IloLinearIntExpr[I];
        for (int i = 0; i < I; i++) {
            demandPerPop[i] = cplex.linearIntExpr();
            for (int i1 = 0; i1 < R; i1++) {
                demandPerPop[i].addTerm(1, j[i1][i]);
            }
            for (int i2 = 0; i2 < M; i2++) {
                demandPerPop[i].addTerm(1, k[i2][i]);
            }

            cplex.addLe(demandPerPop[i], C);
        }

        //Competition constraints
        System.out.println("Adding competition constraints...");
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
        cplex.addLe(cplex.sum(y), p);

        //solve
        System.out.println("Solving...");
        if (cplex.solve()) {
            //System.out.println("Obj = " + cplex.getObjValue());
            System.out.println("Demand Obj = " + cplex.getValue(demandObjective));
            System.out.println("Distance Obj = " + cplex.getValue(distanceObjective));
            System.out.println("Locker Obj = " + cplex.getValue(lockerObjective));
            //System.out.println("x   = " + cplex.getValue(x));
            //System.out.println("y   = " + cplex.getValue(y));

            writeToCsv2Dim(c, "c");
            writeToCsv2Dim(g, "g");
            
            writeToCsv2Dim(w, "w");
            writeToCsv2Dim(x, "x");
            
            writeToCsv1Dim(y, "y");
        } else {
            System.out.println("Solution not found.");
        }
    }
    
    public void writeToCsv1Dim(IloIntVar[] output, String fileName) throws IloException {
        try {
            File dir = new File("output");
            if (!dir.exists()) {
                dir.mkdir();
            }
            
            FileWriter writer = new FileWriter(String.format("output\\%s.csv", fileName));
            
            for (int i = 0; i < output.length; i++) {
                writer.append(Double.toString(cplex.getValue(output[i])));
                writer.append(',');
            }

            //generate whatever data you want
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToCsv2Dim(IloIntVar[][] output, String fileName) throws IloException {
        try {
            File dir = new File("output");
            if (!dir.exists()) {
                dir.mkdir();
            }
            
            FileWriter writer = new FileWriter(String.format("output\\%s.csv", fileName));

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
            e.printStackTrace();
        }
    }
}
