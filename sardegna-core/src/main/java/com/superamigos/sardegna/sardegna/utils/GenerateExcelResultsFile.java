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
import java.lang.reflect.Array;
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
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
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

                    String toWrite = String.format(Locale.ENGLISH, "%.4f", indicatorValue);
                    cell.setCellValue(Double.parseDouble(toWrite));
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
        try {
            generateFinalExcel();
        } catch (InvalidFormatException ex) {
            Logger.getLogger(GenerateExcelResultsFile.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void generateFinalExcel() throws IOException, InvalidFormatException {
        int problemListSize = experiment.getProblemList().size();
        int algorithmListSize = experiment.getAlgorithmList().size() / experiment.getProblemList().size();
        double[][][] values = new double[problemListSize][algorithmListSize][experiment.getIndependentRuns()];
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
            throw new FileNotFoundException();
        }
        int j = 0;

        String fileName2 = experiment.getExperimentBaseDirectory() + "/stats.xls";
        HSSFWorkbook wb = prepareOutputFile(fileName2);

        for (GenericIndicator<S> indicator : experiment.getIndicatorList()) {
            int offset = 0;
            HSSFSheet sheet = workbook.getSheet(indicator.getName());
            values = new double[problemListSize][algorithmListSize][experiment.getIndependentRuns()];
            for (int p = 0; p < problemListSize; p++) {
                for (int t = 0; t < algorithmListSize; t++) {
                    for (int i = 0; i < experiment.getIndependentRuns(); i++) {
                        Row row = sheet.getRow(i + offset + 1);
                        values[p][t][i] = row.getCell(t + 2).getNumericCellValue();
                    }
                }
                offset += experiment.getIndependentRuns();
            }
            calculatepValue(fileName2, wb, j, indicator.getName(), values);
            j++;
        }

        closeOutputFile(fileName2, wb);
        //for problemList
        //compara a 2 a 2 tutte le tecniche
    }

    private void calculatepValue(String fileName, HSSFWorkbook workbook, int pos, String indicatorName, double[][][] values) throws IOException, InvalidFormatException {
        int algorithmListSize = (experiment.getAlgorithmList().size() / experiment.getProblemList().size()) - 1;
        int problemListSize = experiment.getProblemList().size();
        //apro file     
        WilcoxonSignedRankTest wilcoxonSignedRankTest = new WilcoxonSignedRankTest();
        HSSFSheet sheet = workbook.getSheet("Stats");
        Row row = sheet.getRow(0);
        int startcell = 2 + (pos * 2 * algorithmListSize);
        int endcell = startcell + (2 * algorithmListSize) - 1;
        PrinterUtils.Printer.print("WRITING " + indicatorName + " FROM " + startcell + " TO " + endcell + "\n\n\n");
        Cell cell = row.createCell(2 + (pos * 2 * algorithmListSize));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, startcell, endcell));
        cell.setCellValue(indicatorName);
        CellStyle style = cell.getCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        cell.setCellStyle(style);
        int offset = 3;
        for (int p = 0; p < problemListSize; p++) {
            for (int t = 0; t < algorithmListSize; t++) {
                double pVal = wilcoxonSignedRankTest.wilcoxonSignedRankTest(values[p][0], values[p][t+1], true);
                sheet.getRow(offset).createCell((t * 2) + startcell).setCellValue(pVal);
                //sheet.getRow(offset).createCell((t*2)+1).setCellValue(effectSize);
            }
            offset++;
        }

    }

    private HSSFWorkbook prepareOutputFile(String fileName) throws IOException {
        int algorithmListSize = (experiment.getAlgorithmList().size() / experiment.getProblemList().size());
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
        HSSFSheet sheet = workbook.createSheet("Stats");
        sheet.createRow(0);
        int offset = 2;
        Row row = sheet.createRow(1);
        Row row2 = sheet.createRow(2);

        String firstAlgorithmTag = experiment.getAlgorithmList().get(0).getAlgorithmTag();
        for (GenericIndicator<S> indicator : experiment.getIndicatorList()) {
            for (int i = 1; i < algorithmListSize; i++) {
                Cell cell = row.createCell(offset);
                row2.createCell(offset).setCellValue("pValue");
                row2.createCell(offset + 1).setCellValue("effectSize");
                sheet.addMergedRegion(new CellRangeAddress(1, 1, offset, offset + 1));
                cell.setCellValue(experiment.getAlgorithmList().get(i).getAlgorithmTag());
                CellStyle style = cell.getCellStyle();
                style.setAlignment(HorizontalAlignment.CENTER);
                cell.setCellStyle(style);
                offset += 2;
            }
        }
        offset = 3;
        for (ExperimentProblem<S> problem : experiment.getProblemList()) {
            row = sheet.createRow(offset++);
            row.createCell(0).setCellValue(problem.getTag());
            row.createCell(1).setCellValue(firstAlgorithmTag);
        }
        return workbook;
    }

    private void closeOutputFile(String fileName, HSSFWorkbook workbook) {
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
