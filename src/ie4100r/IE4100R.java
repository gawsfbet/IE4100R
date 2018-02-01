/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie4100r;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

/**
 *
 * @author Kevin-Notebook
 */
public class IE4100R {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        model1();
    }
    //Example website: https://github.com/zonbeka/Cplex-Examples
    /*
    min 0.12x + 0.15y
    st. 60x + 60y >= 300
        12x + 6y >= 36
        10x + 30y >= 90
    x, y >= 0
    */
    public static void model1() {
        try {
            IloCplex cplex = new IloCplex();
            
            //variables
            IloNumVar x = cplex.numVar(0, Double.MAX_VALUE, "x"); //x >= 0
            IloNumVar y = cplex.numVar(0, Double.MAX_VALUE, "y"); //y >= 0
            
            //expressions
            IloLinearNumExpr objective = cplex.linearNumExpr();
            objective.addTerm(0.12, x);
            objective.addTerm(0.15, y);
            
            //define objective
            cplex.addMinimize(objective);
            
            //define constraints
            IloConstraint con = cplex.addGe(cplex.sum(cplex.prod(60, x), cplex.prod(60, y)), 300);
            cplex.addGe(cplex.sum(cplex.prod(12, x), cplex.prod(6, y)), 36);
            cplex.addGe(cplex.sum(cplex.prod(10, x), cplex.prod(30, y)), 90);
            
            //solve
            if (cplex.solve()) {
                System.out.println("Obj = " + cplex.getObjValue());
                System.out.println("Obj = " + cplex.getValue(objective));
                System.out.println("x   = " + cplex.getValue(x));
                System.out.println("y   = " + cplex.getValue(y));
            } else {
                System.out.println("Solution not found.");
            }
            
            cplex.delete(con);
            
            //solve
            if (cplex.solve()) {
                System.out.println("Obj = " + cplex.getObjValue());
                System.out.println("Obj = " + cplex.getValue(objective));
                System.out.println("x   = " + cplex.getValue(x));
                System.out.println("y   = " + cplex.getValue(y));
            } else {
                System.out.println("Solution not found.");
            }
        } catch (IloException ex) {
            ex.printStackTrace();
        }
    }
}
