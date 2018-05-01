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

        public static void info(String s) {
            myPw.append(new java.util.Date() + " [INFO] " + s + "\n");
            System.out.print(new java.util.Date() + " [INFO] " + s + "\n");
        }

        public static void error(String s) {
            myPw.append(new java.util.Date() + " [ERROR] " + s + "\n");
            System.out.print(new java.util.Date() + " [ERROR] " + s + "\n");
        }
        
        public static void warn(String s) {
            myPw.append(new java.util.Date() + " [WARN] " + s + "\n");
            System.out.print(new java.util.Date() + " [WARN] " + s + "\n");
        }
        
        public static void debug(String s) {
            myPw.append(new java.util.Date() + " [DEBUG] " + s + "\n");
            System.out.print(new java.util.Date() + " [DEBUG] " + s + "\n");
        }
               

        public static void setPw(String name) throws FileNotFoundException {
            myPw = new PrintWriter(name);
        }

        public static void closePw() {
            myPw.close();
        }
    }
}