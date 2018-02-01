/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Kevin-Notebook
 */
public class StringGen {
    public static void main(String[] args) {
        String s = "=('%d'!%s-'%d'!%s)/'%d'!%s";
        
        Scanner scanner = new Scanner(System.in);
        String cell1 = scanner.next();
        String cell2 = scanner.next();
        
        Pattern pattern = Pattern.compile("([A-Z]+)(\\d+)");
        Matcher m1 = pattern.matcher(cell1), m2 = pattern.matcher(cell2);
        
        m1.matches();
        m2.matches();
        
        cell1 = "$" + m1.group(1) + "$" + m1.group(2);
        cell2 = "$" + m2.group(1) + "$" + m2.group(2);
        
        for (int i = 2017; i > 2004; i--) {
            String later, original;
            if (i > 2010) {
                later = String.format("'%d'!%s", i, cell1);
            } else {
                later = String.format("'%d'!%s", i, cell2);
            }
            
            if (i - 5 > 2010) {
                original = String.format("'%d'!%s", i - 5, cell1);
            } else {
                original = String.format("'%d'!%s", i - 5, cell2);
            }
            
            System.out.println(String.format("=(%s-%s)/%s", later, original, original));
        }
    }
}
