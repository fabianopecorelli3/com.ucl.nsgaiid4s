package com.ucl.nsgaiid4s.experiment.component;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import org.uma.jmetal.qualityindicator.QualityIndicator;
import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentComponent;
import org.uma.jmetal.util.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.util.experiment.util.ExperimentProblem;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * This class computes the {@link QualityIndicator}s of an experiment. Once the
 * algorithms of an experiment have been executed through running an instance of
 * class {@link ExecuteAlgorithms}, the list of indicators in obtained from the
 * {@link ExperimentComponent #getIndicatorsList()} method. Then, for every
 * combination algorithm + problem, the indicators are applied to all the FUN
 * files and the resulting values are store in a file called as
 * {@link QualityIndicator #getName()}, which is located in the same directory
 * of the FUN files.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class GenerateNewTimes<S extends Solution<?>, Result> implements ExperimentComponent {

    private final Experiment<S, Result> experiment;
    private int numberOfEvaluations;
    private int numberOfPartitions;

    public GenerateNewTimes(Experiment<S, Result> experiment, int numberOfEvaluations, int numberOfPartitions) {
        this.experiment = experiment;
        this.numberOfEvaluations = numberOfEvaluations;
        this.numberOfPartitions = numberOfPartitions;
    }

    @Override
    public void run() throws IOException {
        //apro file        
        String fileName = experiment.getExperimentBaseDirectory() + "/results.xls";
        File file = new File(fileName);
        HSSFWorkbook workbook = null;

        if (file.exists()) {
            try {
                workbook = (HSSFWorkbook) WorkbookFactory.create(file);
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            }
        } else {
            workbook = new HSSFWorkbook();
        }
        ArrayList<GenericIndicator<S>> indicators = new ArrayList<>();
        //indicators.addAll(experiment.getIndicatorList());
        indicators.add(new GenericIndicator<S>() {
            @Override
            public boolean isTheLowerTheIndicatorValueTheBetter() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Double evaluate(List<S> evlt) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getName() {
                return "TIMES";
            }
        });
        for (ExperimentProblem problem : experiment.getProblemList()) {
            for (GenericIndicator<S> indicator : indicators) {
                String indicatorName = indicator.getName();

                int rowNumber = 0;
                int independentRuns = experiment.getIndependentRuns();
                String problemName = problem.getTag();
                for (ExperimentAlgorithm<?, Result> algorithm : experiment.getAlgorithmList()) {

                    String algorithmDirectory;
                    algorithmDirectory = experiment.getExperimentBaseDirectory() + "/data/"
                            + algorithm.getAlgorithmTag();

                    String problemDirectory = algorithmDirectory + "/" + problemName;

                    String qualityIndicatorFile = problemDirectory + "/" + indicator.getName();

                    BufferedReader indicatorFile = new BufferedReader(new FileReader(qualityIndicatorFile));
                    BufferedWriter indicatorFile10s = new BufferedWriter(new FileWriter(qualityIndicatorFile + "10s"));
                    BufferedWriter indicatorFile30s = new BufferedWriter(new FileWriter(qualityIndicatorFile + "30s"));

                    for (int run = 0; run < experiment.getIndependentRuns(); run++) {
                        double indicatorValue = Double.parseDouble(indicatorFile.readLine());
                        double indicatorValue10s;
                        double indicatorValue30s;

                        indicatorValue10s = ((indicatorValue / 1000) + 10 * numberOfEvaluations) / ((NSGAIID4SExperimentAlgorithm) algorithm).getNumberOfPartitions();
                        indicatorValue30s = ((indicatorValue / 1000) + 30 * numberOfEvaluations) / ((NSGAIID4SExperimentAlgorithm) algorithm).getNumberOfPartitions();

                        indicatorFile10s.write(indicatorValue10s + "\n");
                        indicatorFile30s.write(indicatorValue30s + "\n");
                        JMetalLogger.logger.info(indicator.getName() + ": " + indicatorValue);
                    }
                    indicatorFile10s.close();
                    indicatorFile30s.close();
                }
            }
        }

    }

}
