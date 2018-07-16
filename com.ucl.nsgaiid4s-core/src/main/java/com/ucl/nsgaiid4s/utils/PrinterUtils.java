/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ucl.nsgaiid4s.utils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 *
 * @author fably
 */
public class PrinterUtils {

    public static class Printer {

        private static PrintWriter masterPw;
        private static PrintWriter workersPw;
        private static PrintWriter currentPw;

        public static void info(String s, boolean flag) {
           // currentPw = (flag)? masterPw:workersPw;
            //currentPw.append(new java.util.Date() + " [INFO] " + s + "\n");
            System.out.print(new java.util.Date() + " [INFO] " + s + "\n");
        }

        public static void error(String s, boolean flag) {
            //currentPw = (flag)? masterPw:workersPw;
            //currentPw.append(new java.util.Date() + " [ERROR] " + s + "\n");
            System.out.print(new java.util.Date() + " [ERROR] " + s + "\n");
        }
        
        public static void warn(String s, boolean flag) {
            //currentPw = (flag)? masterPw:workersPw;
            //currentPw.append(new java.util.Date() + " [WARN] " + s + "\n");
            System.out.print(new java.util.Date() + " [WARN] " + s + "\n");
        }
        
        public static void debug(String s, boolean flag) {
            //currentPw = (flag)? masterPw:workersPw;
            //currentPw.append(new java.util.Date() + " [DEBUG] " + s + "\n");
            System.out.print(new java.util.Date() + " [DEBUG] " + s + "\n");
        }
               

        public static void setMasterPw(String name) throws FileNotFoundException {
            //masterPw = new PrintWriter(name);
        }
        
        public static void setWorkersPw(String name) throws FileNotFoundException {
            //workersPw = new PrintWriter(name);
        }

        public static void closePw() {
            //masterPw.close();
            //workersPw.close();
        }
    }
}