/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ucl.nsgaiid4s.experiment.component;

import com.ucl.nsgaiid4s.utils.FileManager;
import com.ucl.nsgaiid4s.utils.SolutionListOutput;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.util.ExperimentAlgorithm;

/**
 *
 * @author fably
 */
public class NSGAIID4SExperimentAlgorithm<S extends Solution<?>, Result> extends ExperimentAlgorithm<S, Result> {

    private FileManager fileManager;
    private ArrayList<String> totalExecutionTime;
    private boolean local;
    private int numberOfPartitions;

    public NSGAIID4SExperimentAlgorithm(Algorithm<Result> algorithm, String algorithmTag, String problemTag, FileManager fileManager, boolean local, int numberOfPartitions) {
        super(algorithm, algorithmTag, problemTag);
        this.fileManager = fileManager;
        totalExecutionTime = new ArrayList<>();
        this.local = local;
        this.numberOfPartitions = numberOfPartitions;
    }

    public NSGAIID4SExperimentAlgorithm(Algorithm<Result> algorithm, String problemTag) {
        super(algorithm, problemTag);
    }

    public ArrayList<String> getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public void runAlgorithm(int id, Experiment<?, ?> experimentData) {
        long startTime, endTime, timeInSecond;

        String outputDirectoryName = experimentData.getExperimentBaseDirectory()
                + "/data/"
                + getAlgorithmTag()
                + "/"
                + getProblemTag();

        if (local) {
            File outputDirectory = new File(outputDirectoryName);
            if (!outputDirectory.exists()) {
                boolean result = new File(outputDirectoryName).mkdirs();
                if (result) {
                    JMetalLogger.logger.info("Creating " + outputDirectoryName);
                } else {
                    JMetalLogger.logger.severe("Creating " + outputDirectoryName + " failed");
                }
            }
        }

        String funFile = outputDirectoryName + "/FUN" + id + ".tsv";
        String varFile = outputDirectoryName + "/VAR" + id + ".tsv";

        JMetalLogger.logger.info(
                " Running algorithm: " + getAlgorithmTag()
                + ", problem: " + getProblemTag()
                + ", run: " + id
                + ", funFile: " + funFile);

        startTime = System.currentTimeMillis();

        getAlgorithm().run();
        Result population = getAlgorithm().getResult();

        endTime = System.currentTimeMillis();
        timeInSecond = (endTime - startTime);

        totalExecutionTime.add(timeInSecond + "\n");

        SolutionListOutput slo = new SolutionListOutput((List<S>) population, fileManager);
        slo.setFunFile(funFile);
        slo.setVarFile(varFile);
        slo.print();

    }

    public int getNumberOfPartitions() {
        return numberOfPartitions;
    }

    
}
