/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ucl.nsgaiid4s.utils;

import java.util.List;
/**
 *
 * @author fably
 */
public abstract class FileManager {
    
    private int numberOfPartitions;
    
    public abstract void write(String path, List<String> toWrite);
    
    public int getNumberOfPartitions(){
        return numberOfPartitions;
    }
    
    public void setNumberOfPartitions(int numberOfPartitions) {
        this.numberOfPartitions = numberOfPartitions;
    }
}
