/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ucl.nsgaiid4s.experiment.component;

import java.io.File;
import java.io.IOException;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentComponent;
import org.uma.jmetal.util.experiment.util.ExperimentAlgorithm;

/**
 *
 * @author fably
 */
public class S3Downloader<S extends Solution<?>, Result> implements ExperimentComponent{

    private final Experiment<?, Result> experiment;
    private String s3Path;
    
    public S3Downloader(Experiment<S, Result> experiment, String s3Path){
        this.experiment = experiment;
        this.s3Path = s3Path;
    }
    
    @Override
    public void run() throws IOException {
        prepareOutputDirectory();
        for (ExperimentAlgorithm ea : experiment.getAlgorithmList()){
            prepareAlgorithmDirectory(ea);
            //download fun0,var0 e times da S3
        }
    }
    
    private void prepareOutputDirectory() {
        if (experimentDirectoryDoesNotExist()) {
            createExperimentDirectory();
        }
    }

    private boolean experimentDirectoryDoesNotExist() {
        boolean result;
        File experimentDirectory;

        experimentDirectory = new File(experiment.getExperimentBaseDirectory());
        if (experimentDirectory.exists() && experimentDirectory.isDirectory()) {
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    private void createExperimentDirectory() {
        File experimentDirectory;
        experimentDirectory = new File(experiment.getExperimentBaseDirectory());

        if (experimentDirectory.exists()) {
            experimentDirectory.delete();
        }

        boolean result;
        result = new File(experiment.getExperimentBaseDirectory()).mkdirs();
        if (!result) {
            throw new JMetalException("Error creating experiment directory: "
                    + experiment.getExperimentBaseDirectory());
        }
    }
    
    public void prepareAlgorithmDirectory(ExperimentAlgorithm ea){
        String outputDirectoryName = experiment.getExperimentBaseDirectory()
                + "/data/"
                + ea.getAlgorithmTag()
                + "/"
                + ea.getProblemTag();

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
    
}
