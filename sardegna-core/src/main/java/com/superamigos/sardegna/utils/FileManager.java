/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author fably
 */
public interface FileManager {
   
    InputStream openR(String hdfsPath, String fileName) throws FileNotFoundException;
    OutputStream openW(String hdfsPath, String fileName, boolean append) throws FileNotFoundException;
    void close(String hdfsPath, OutputStream os) throws IOException;
    boolean exists(String hdfsPath, String fileName);
    boolean isDirectory(String hdfsPath, String fileName);
    boolean delete(String hdfsPath, String fileName);
    boolean mkdirs(String hdfsPath, String fileName);
}
