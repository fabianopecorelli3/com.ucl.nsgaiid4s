/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.sardegna.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.util.fileoutput.FileOutputContext;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

/**
 *
 * @author fably
 */
public class SardegnaExperimentAlgorithm<S extends Solution<?>, Result> extends ExperimentAlgorithm<S, Result> {

    public SardegnaExperimentAlgorithm(Algorithm<Result> algorithm, String algorithmTag, String problemTag) {
        super(algorithm, algorithmTag, problemTag);
    }

    public SardegnaExperimentAlgorithm(Algorithm<Result> algorithm, String problemTag) {
        super(algorithm, problemTag);
    }

    public void runAlgorithm(int id, Experiment<?, ?> experimentData) {
        String outputDirectoryName = experimentData.getExperimentBaseDirectory()
                + "/data/"
                + getAlgorithmTag()
                + "/"
                + getProblemTag();

        File outputDirectory = new File(outputDirectoryName);
        if (!outputDirectory.exists()) {
            boolean result = new File(outputDirectoryName).mkdirs();
            if (result) {
                JMetalLogger.logger.info("Creating " + outputDirectoryName);
            } else {
                JMetalLogger.logger.severe("Creating " + outputDirectoryName + " failed");
            }
        }

        String funFile = outputDirectoryName + "/FUN" + id + ".tsv";
        String varFile = outputDirectoryName + "/VAR" + id + ".tsv";
        String timesFile = outputDirectoryName + "/TIMES";

        JMetalLogger.logger.info(
                " Running algorithm: " + getAlgorithmTag()
                + ", problem: " + getProblemTag()
                + ", run: " + id
                + ", funFile: " + funFile);

        long startTime = System.currentTimeMillis();

        getAlgorithm().run();
        Result population = getAlgorithm().getResult();

        long endTime = System.currentTimeMillis();

        new SolutionListOutput((List<S>) population)
                .setSeparator("\t")
                .setVarFileOutputContext(new DefaultFileOutputContext(varFile))
                .setFunFileOutputContext(new DefaultFileOutputContext(funFile))
                .print();

        long timeInSecond = (endTime - startTime) / 1000;

        printTimesToFile(timeInSecond, timesFile);

    }

    /*
    public void printTimesToFile(FileOutputContext context, long timeInSecond) {
        BufferedWriter bufferedWriter = context.getFileWriter();

        try {
            bufferedWriter.write(timeInSecond + context.getSeparator());
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            throw new JMetalException("Error printing objecives to file: ", e);
        }
    }
     */
    
    public void printTimesToFile(Long timeInSecond, String fileName) {
        FileWriter os;
        try {
            os = new FileWriter(fileName, true);
            os.write("" + timeInSecond + "\n");
            os.close();
        } catch (IOException ex) {
            throw new JMetalException("Error writing time to file" + ex);
        }
    }

}
