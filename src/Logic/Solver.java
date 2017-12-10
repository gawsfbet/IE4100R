/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;

/**
 *
 * @author Kevin-Notebook
 */
public class Solver {
    private IloCplex cplex;
    
    private int T = 2000000000; //large integer (2 billion)
    
    private int R; //total number of residential demand nodes
    private int M; //total number of MRT demand nodes
    private int F; //total number of shopping mall modes (for placement)
    private int I; //total number of POPStation nodes (competition)
    private int S; //Maximum allowable distance (1250)
    private int C; //Maximum holding capacity of a locker for demand
    
    private int p; //Number of new lockers to place
    
    private int[] a;
    private int[] b;
    private double alpha;
    private double beta;
    private int[][] d;
    private int[][] e;
    private int[][] h;
    private int[][] l;
    
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
        //variables
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
        //define objective
        //constraints
    }
}
