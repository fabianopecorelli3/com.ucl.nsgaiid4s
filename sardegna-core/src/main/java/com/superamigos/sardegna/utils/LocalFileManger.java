/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author fably
 */
public class LocalFileManger implements FileManager {

    @Override
    public InputStream openR(String hdfsPath, String fileName) throws FileNotFoundException {
        return new FileInputStream(fileName);
    }

    @Override
    public OutputStream openW(String hdfsPath, String fileName, boolean append) throws FileNotFoundException {
        return new FileOutputStream(fileName, append);
    }

    @Override
    public void close(String hdfsPath, OutputStream os) throws IOException {
        os.close();
    }

    @Override
    public boolean exists(String hdfsPath, String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    @Override
    public boolean isDirectory(String hdfsPath, String fileName) {
        File file = new File(fileName);
        return file.isDirectory();
    }

    @Override
    public boolean delete(String hdfsPath, String fileName) {
        File file = new File(fileName);
        return file.delete();
    }

    @Override
    public boolean mkdirs(String hdfsPath, String fileName) {
        File file = new File(fileName);
        return file.mkdirs();
    }

}
