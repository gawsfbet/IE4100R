/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;

/**
 *
 * @author Kevin-Notebook
 */
public class RNG {
    public Random random;
    
    public static void main(String[] args) {
        RNG rng = new RNG();
        for (double d : rng.generateNormalVars(20, 10, 10)) {
            System.out.println(d);
        }
    }
    
    public RNG() {
        this.random = new Random();
    }
    
    public RNG(long seed) {
        this.random = new Random(seed);
    }
    
    private DoubleStream generateDoubleStream(int numEntries) {
        return random.doubles().limit(numEntries);
    }
    
    public double[] generateUniformVars(int numEntries) {
        return generateDoubleStream(numEntries).toArray();
    }
    
    public double[] generateUniformVars(int numEntries, double lb, double ub) {
        return generateDoubleStream(numEntries).map(u -> u * (ub - lb) + lb).toArray();
    }
    
    public double[] generateNormalVars(int numEntries) {
        double[] values = numEntries % 2 == 1 ? generateDoubleStream(numEntries + 1).toArray() : generateDoubleStream(numEntries).toArray();
        
        for (int i = 0; i < numEntries; i += 2) {
            double u1 = values[i], u2 = values[i + 1];
            double z1 = Math.sqrt(-2 * Math.log(u1)) * Math.cos(2 * Math.PI * u2), z2 = Math.sqrt(-2 * Math.log(u1)) * Math.sin(2 * Math.PI * u2);
            values[i] = z1;
            values[i + 1] = z2;
        }
        
        return Arrays.copyOf(values, numEntries);
    }
    
    public double[] generateNormalVars(int numEntries, double mean, double sd) {
        double[] values = numEntries % 2 == 1 ? generateDoubleStream(numEntries + 1).toArray() : generateDoubleStream(numEntries).toArray();
        
        for (int i = 0; i < numEntries; i += 2) {
            double u1 = values[i], u2 = values[i + 1];
            double n1 = Math.sqrt(-2 * Math.log(u1)) * Math.cos(2 * Math.PI * u2), n2 = Math.sqrt(-2 * Math.log(u1)) * Math.sin(2 * Math.PI * u2);
            values[i] = mean + sd * n1;
            values[i + 1] = mean + sd * n2;
        }
        
        return Arrays.copyOf(values, numEntries);
    }
    
    public void generateNormalVars(double[] array, double mean, double sd) {
        for (int i = 0; i < array.length; i += 2) {
            double u1 = random.nextDouble(), u2 = random.nextDouble();
            double n1 = Math.sqrt(-2 * Math.log(u1)) * Math.cos(2 * Math.PI * u2), n2 = Math.sqrt(-2 * Math.log(u1)) * Math.sin(2 * Math.PI * u2);
            array[i] = mean + sd * n1;
            if (i < array.length - 1) {
                array[i + 1] = mean + sd * n2;
            }
        }
    }
}
