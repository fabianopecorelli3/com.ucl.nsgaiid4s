/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.sardegna.utils;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.qualityindicator.QualityIndicator;
import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentComponent;
import org.uma.jmetal.util.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.util.experiment.util.ExperimentProblem;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.util.PointSolution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.uma.jmetal.problem.Problem;

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
public class GenerateExcelResultsFile<S extends Solution<?>, Result> implements ExperimentComponent {

    private final Experiment<S, Result> experiment;

    public GenerateExcelResultsFile(Experiment<S, Result> experiment) {
        this.experiment = experiment;
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

        for (GenericIndicator<S> indicator : experiment.getIndicatorList()) {
            String indicatorName = indicator.getName();
            //creo foglio
            int rowNumber = 0;
            HSSFSheet sheet = workbook.createSheet(indicatorName);
            Row row = sheet.createRow(rowNumber++);
            Cell cellProblem = row.createCell(0);
            cellProblem.setCellValue("Problem");
            Cell cellRun = row.createCell(1);
            cellRun.setCellValue("Run");
            int i = 2;
            int numberOfAlgorithm = experiment.getAlgorithmList().size() / experiment.getProblemList().size();
            for (int j = 0; j < numberOfAlgorithm; j++) {
                Cell tempAlgorithm = row.createCell(i++);
                tempAlgorithm.setCellValue(experiment.getAlgorithmList().get(j).getAlgorithmTag());
            }
            int independentRuns = experiment.getIndependentRuns();
            for (ExperimentProblem<S> problem : experiment.getProblemList()) {
                for (int run = 1; run <= experiment.getIndependentRuns(); run++) {
                    row = sheet.createRow(rowNumber++);
                    cellProblem = row.createCell(0);
                    cellProblem.setCellValue(problem.getTag());
                    cellRun = row.createCell(1);
                    cellRun.setCellValue(run);
                }
            }
            rowNumber = 1;
            int columnOffset = 2;
            int rowOffset = 0;
            String problemName = experiment.getProblemList().get(0).getTag();
            int problemId = 0;
            for (ExperimentAlgorithm<?, Result> algorithm : experiment.getAlgorithmList()) {
                if (!problemName.equals(algorithm.getProblemTag())) {
                    problemId++;
                    problemName = algorithm.getProblemTag();
                    columnOffset = 2;
                    rowOffset += independentRuns;
                }
                String algorithmDirectory;
                algorithmDirectory = experiment.getExperimentBaseDirectory() + "/data/"
                        + algorithm.getAlgorithmTag();

                String problemDirectory = algorithmDirectory + "/" + problemName;
                String referenceFrontDirectory = experiment.getReferenceFrontDirectory();
                String referenceFrontName = referenceFrontDirectory
                        + "/" + experiment.getReferenceFrontFileNames().get(problemId);

                JMetalLogger.logger.info("RF: " + referenceFrontName);;

                String qualityIndicatorFile = problemDirectory + "/" + indicator.getName();

                Front referenceFront = new ArrayFront(referenceFrontName);

                FrontNormalizer frontNormalizer = new FrontNormalizer(referenceFront);
                Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);

                indicator.setReferenceParetoFront(normalizedReferenceFront);
                for (int run = 0; run < experiment.getIndependentRuns(); run++) {
                    String frontFileName = problemDirectory + "/"
                            + experiment.getOutputParetoFrontFileName() + run + ".tsv";

                    Front front = new ArrayFront(frontFileName);
                    Front normalizedFront = frontNormalizer.normalize(front);
                    List<PointSolution> normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront);
                    Double indicatorValue = (Double) indicator.evaluate((List<S>) normalizedPopulation);
                    JMetalLogger.logger.info(indicator.getName() + ": " + indicatorValue);

                    row = sheet.getRow(rowNumber + rowOffset);
                    Cell cell = row.createCell(columnOffset);
                    cell.setCellValue(indicatorValue);
                    rowNumber++;

                }
                columnOffset++;
                rowNumber = 1;
            }
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(fileName);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
