/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.sardegna.utils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 *
 * @author fably
 */
public class PrinterUtils {

    public static class Printer {

        private static PrintWriter myPw;

        public static void print(String s) {
            myPw.append(s);
            System.out.print(s);
        }

        public static void setPw(String name) throws FileNotFoundException {
            myPw = new PrintWriter(name);
        }
        
        public static void closePw(){
            myPw.close();
        }
    }
}
