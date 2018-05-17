package com.superamigos.sardegna.experiment.component;

import com.superamigos.sardegna.utils.FileManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentComponent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class computes a number of statistical values (mean, median, standard
 * deviation, interquartile range) from the indicator files generated after
 * executing {@link ExecuteAlgorithms} and {@link ComputeQualityIndicators}.
 * After reading the data files and calculating the values, a Latex file is
 * created containing an script that generates tables with the best and second
 * best values per indicator. The name of the file is
 * {@link Experiment #getExperimentName()}.tex, which is located by default in
 * the directory {@link Experiment #getExperimentBaseDirectory()}/latex
 *
 * Although the maximum, minimum, and total number of items are also computed,
 * no tables are generated with them (this is a pending work).
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class GenerateLatexTablesWithStatistics implements ExperimentComponent {

    private static final String DEFAULT_LATEX_DIRECTORY = "latex";

    private final Experiment<?, ?> experiment;

    private double[][][] mean;
    private double[][][] median;
    private double[][][] stdDeviation;
    private double[][][] iqr;
    private double[][][] max;
    private double[][][] min;
    private double[][][] numberOfValues;
    private FileManager fileManager;
    private String hdfsPath;

    public GenerateLatexTablesWithStatistics(Experiment<?, ?> configuration, FileManager fileManager, String hdfsPath) {
        this.experiment = configuration;
        this.fileManager = fileManager;
        this.hdfsPath = hdfsPath;
        experiment.removeDuplicatedAlgorithms();
    }

    @Override
    public void run() throws IOException {
        List<List<List<List<Double>>>> data = readDataFromFiles();
        computeDataStatistics(data);
        generateLatexScript(data);
    }

    private List<List<List<List<Double>>>> readDataFromFiles() throws IOException {
        List<List<List<List<Double>>>> data = new ArrayList<List<List<List<Double>>>>(experiment.getIndicatorList().size());

        for (int indicator = 0; indicator < experiment.getIndicatorList().size(); indicator++) {
            // A data vector per problem
            data.add(indicator, new ArrayList<List<List<Double>>>());
            for (int problem = 0; problem < experiment.getProblemList().size(); problem++) {
                data.get(indicator).add(problem, new ArrayList<List<Double>>());

                for (int algorithm = 0; algorithm < experiment.getAlgorithmList().size(); algorithm++) {
                    data.get(indicator).get(problem).add(algorithm, new ArrayList<Double>());

                    String directory = experiment.getExperimentBaseDirectory();
                    directory += "/data/";
                    directory += "/" + experiment.getAlgorithmList().get(algorithm).getAlgorithmTag();
                    directory += "/" + experiment.getProblemList().get(problem).getTag();
                    directory += "/" + experiment.getIndicatorList().get(indicator).getName();
                    // Read values from data files
                    InputStreamReader isr = new InputStreamReader(fileManager.openR(hdfsPath, directory));
                    BufferedReader br = new BufferedReader(isr);
                    String aux = br.readLine();
                    while (aux != null) {
                        data.get(indicator).get(problem).get(algorithm).add(Double.parseDouble(aux));
                        aux = br.readLine();
                    }
                    br.close();
                }
            }
        }

        return data;
    }

    private void computeDataStatistics(List<List<List<List<Double>>>> data) {
        int indicatorListSize = experiment.getIndicatorList().size();
        mean = new double[indicatorListSize][][];
        median = new double[indicatorListSize][][];
        stdDeviation = new double[indicatorListSize][][];
        iqr = new double[indicatorListSize][][];
        min = new double[indicatorListSize][][];
        max = new double[indicatorListSize][][];
        numberOfValues = new double[indicatorListSize][][];

        int problemListSize = experiment.getProblemList().size();
        for (int indicator = 0; indicator < indicatorListSize; indicator++) {
            // A data vector per problem
            mean[indicator] = new double[problemListSize][];
            median[indicator] = new double[problemListSize][];
            stdDeviation[indicator] = new double[problemListSize][];
            iqr[indicator] = new double[problemListSize][];
            min[indicator] = new double[problemListSize][];
            max[indicator] = new double[problemListSize][];
            numberOfValues[indicator] = new double[problemListSize][];

            int algorithmListSize = experiment.getAlgorithmList().size();
            for (int problem = 0; problem < problemListSize; problem++) {
                mean[indicator][problem] = new double[algorithmListSize];
                median[indicator][problem] = new double[algorithmListSize];
                stdDeviation[indicator][problem] = new double[algorithmListSize];
                iqr[indicator][problem] = new double[algorithmListSize];
                min[indicator][problem] = new double[algorithmListSize];
                max[indicator][problem] = new double[algorithmListSize];
                numberOfValues[indicator][problem] = new double[algorithmListSize];

                for (int algorithm = 0; algorithm < algorithmListSize; algorithm++) {
                    Collections.sort(data.get(indicator).get(problem).get(algorithm));

                    Map<String, Double> statValues = computeStatistics(data.get(indicator).get(problem).get(algorithm));

                    mean[indicator][problem][algorithm] = statValues.get("mean");
                    median[indicator][problem][algorithm] = statValues.get("median");
                    stdDeviation[indicator][problem][algorithm] = statValues.get("stdDeviation");
                    iqr[indicator][problem][algorithm] = statValues.get("iqr");
                    min[indicator][problem][algorithm] = statValues.get("min");
                    max[indicator][problem][algorithm] = statValues.get("max");
                    numberOfValues[indicator][problem][algorithm] = statValues.get("numberOfElements").intValue();
                }
            }
        }
    }

    private void generateLatexScript(List<List<List<List<Double>>>> data) throws IOException {
        String latexDirectoryName = experiment.getExperimentBaseDirectory() + "/" + DEFAULT_LATEX_DIRECTORY;
        if (!fileManager.exists(hdfsPath, latexDirectoryName)) {
            fileManager.mkdirs(hdfsPath, latexDirectoryName);
            JMetalLogger.logger.info("Creating " + latexDirectoryName + " directory");
        }
        //System.out.println("Experiment name: " + experimentName_);
        String latexFile = latexDirectoryName + "/" + experiment.getExperimentName() + ".tex";
        printHeaderLatexCommands(latexFile);
        for (int i = 0; i < experiment.getIndicatorList().size(); i++) {
            printData(latexFile, i, mean, stdDeviation, "Mean", "St. Dev.");
            printData(latexFile, i, median, iqr, "Median", "Int. Ran.");
        }
        printEndLatexCommands(latexFile);
    }

    /**
     * Computes the statistical values
     *
     * @param values
     * @return
     */
    private Map<String, Double> computeStatistics(List<Double> values) {
        Map<String, Double> results = new HashMap<>();

        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Double value : values) {
            stats.addValue(value);
        }

        results.put("mean", stats.getMean());
        results.put("median", stats.getPercentile(50.0));
        results.put("stdDeviation", stats.getStandardDeviation());
        results.put("iqr", stats.getPercentile(75) - stats.getPercentile(25));
        results.put("max", stats.getMax());
        results.put("min", stats.getMean());
        results.put("numberOfElements", (double) values.size());

        return results;
    }

    void printHeaderLatexCommands(String fileName) throws IOException {
        OutputStream os = fileManager.openW(hdfsPath, fileName, false);
        os.write(("\\documentclass{article}" + "\n").getBytes("UTF-8"));
        os.write(("\\title{Result tables}" + "\n").getBytes("UTF-8"));
        os.write(("\\usepackage{colortbl}" + "\n").getBytes("UTF-8"));
        os.write(("\\usepackage[a4paper,left=1cm,right=0cm]{geometry}" + "\n").getBytes("UTF-8"));
        os.write(("\\usepackage[table*]{xcolor}" + "\n").getBytes("UTF-8"));
        os.write(("\\xdefinecolor{gray95}{gray}{0.65}" + "\n").getBytes("UTF-8"));
        os.write(("\\xdefinecolor{gray25}{gray}{0.8}" + "\n").getBytes("UTF-8"));
        os.write(("\\author{Fabiano Pecorelli, Carlo Di Domenico}" + "\n").getBytes("UTF-8"));
        os.write(("\\begin{document}" + "\n").getBytes("UTF-8"));
        os.write(("\\maketitle" + "\n").getBytes("UTF-8"));
        os.write(("\\section{Tables}" + "\n").getBytes("UTF-8"));

        fileManager.close(hdfsPath, os);
    }

    void printEndLatexCommands(String fileName) throws IOException {
        OutputStream os = fileManager.openW(hdfsPath, fileName, true);
        os.write(("\\end{document}" + "\n").getBytes("UTF-8"));
        fileManager.close(hdfsPath, os);
    }

    private void printData(String latexFile, int indicatorIndex, double[][][] centralTendency, double[][][] dispersion, String caption1, String caption2) throws IOException {
        // Generate header of the table
        OutputStream os = fileManager.openW(hdfsPath, latexFile, true);
        os.write(("\n").getBytes("UTF-8"));
        os.write(("\\begin{table}" + "\n").getBytes("UTF-8"));
        os.write(("\\caption{" + experiment.getIndicatorList().get(indicatorIndex).getName() + ". " + caption1 + " and " + caption2 + "}" + "\n").getBytes("UTF-8"));
        os.write(("\\label{table: " + experiment.getIndicatorList().get(indicatorIndex).getName() + "}" + "\n").getBytes("UTF-8"));
        os.write(("\\centering" + "\n").getBytes("UTF-8"));
        os.write(("\\begin{scriptsize}" + "\n").getBytes("UTF-8"));
        os.write(("\\begin{tabular}{|l|").getBytes("UTF-8"));

        // calculate the number of columns
        os.write((StringUtils.repeat("l|l|", experiment.getAlgorithmList().size())).getBytes("UTF-8"));
        os.write(("}\n").getBytes("UTF-8"));
        os.write(("\\hline").getBytes("UTF-8"));

        // write table head
        for (int i = -1; i < experiment.getAlgorithmList().size(); i++) {
            if (i == -1) {
                os.write((" & ").getBytes("UTF-8"));
            } else if (i == (experiment.getAlgorithmList().size() - 1)) {
                os.write((" \\multicolumn{2}{|l|}{" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "}\\\\" + "\n").getBytes("UTF-8"));
            } else {
                os.write(("\\multicolumn{2}{|l|}{" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "} & ").getBytes("UTF-8"));
            }
        }
        os.write(("\\hline \n").getBytes("UTF-8"));

        for (int i = -1; i < experiment.getAlgorithmList().size(); i++) {
            if (i == -1) {
                os.write((" & ").getBytes("UTF-8"));
            } else if (i == (experiment.getAlgorithmList().size() - 1)) {
                os.write((" " + caption1 + " & " + caption2 + "\\\\" + "\n").getBytes("UTF-8"));
            } else {
                os.write(("" + caption1 + " & " + caption2 + " & ").getBytes("UTF-8"));
            }
        }
        os.write(("\\hline \n").getBytes("UTF-8"));

        // write lines
        for (int i = 0; i < experiment.getProblemList().size(); i++) {
            // find the best value and second best value
            double bestCentralTendencyValue;
            double bestDispersionValue;
            double secondBestCentralTendencyValue;
            double secondBestDispersionValue;
            int bestIndex = -1;
            int secondBestIndex = -1;

            if (experiment.getIndicatorList().get(indicatorIndex).isTheLowerTheIndicatorValueTheBetter()) {
                bestCentralTendencyValue = Double.MAX_VALUE;
                bestDispersionValue = Double.MAX_VALUE;
                secondBestCentralTendencyValue = Double.MAX_VALUE;
                secondBestDispersionValue = Double.MAX_VALUE;
                for (int j = 0; j < (experiment.getAlgorithmList().size()); j++) {
                    if ((centralTendency[indicatorIndex][i][j] < bestCentralTendencyValue)
                            || ((centralTendency[indicatorIndex][i][j]
                            == bestCentralTendencyValue) && (dispersion[indicatorIndex][i][j] < bestDispersionValue))) {
                        secondBestIndex = bestIndex;
                        secondBestCentralTendencyValue = bestCentralTendencyValue;
                        secondBestDispersionValue = bestDispersionValue;
                        bestCentralTendencyValue = centralTendency[indicatorIndex][i][j];
                        bestDispersionValue = dispersion[indicatorIndex][i][j];
                        bestIndex = j;
                    } else if ((centralTendency[indicatorIndex][i][j] < secondBestCentralTendencyValue)
                            || ((centralTendency[indicatorIndex][i][j]
                            == secondBestCentralTendencyValue) && (dispersion[indicatorIndex][i][j] < secondBestDispersionValue))) {
                        secondBestIndex = j;
                        secondBestCentralTendencyValue = centralTendency[indicatorIndex][i][j];
                        secondBestDispersionValue = dispersion[indicatorIndex][i][j];
                    }
                }
            } else {
                bestCentralTendencyValue = Double.MIN_VALUE;
                bestDispersionValue = Double.MIN_VALUE;
                secondBestCentralTendencyValue = Double.MIN_VALUE;
                secondBestDispersionValue = Double.MIN_VALUE;
                for (int j = 0; j < (experiment.getAlgorithmList().size()); j++) {
                    if ((centralTendency[indicatorIndex][i][j] > bestCentralTendencyValue)
                            || ((centralTendency[indicatorIndex][i][j]
                            == bestCentralTendencyValue) && (dispersion[indicatorIndex][i][j] < bestDispersionValue))) {
                        secondBestIndex = bestIndex;
                        secondBestCentralTendencyValue = bestCentralTendencyValue;
                        secondBestDispersionValue = bestDispersionValue;
                        bestCentralTendencyValue = centralTendency[indicatorIndex][i][j];
                        bestDispersionValue = dispersion[indicatorIndex][i][j];
                        bestIndex = j;
                    } else if ((centralTendency[indicatorIndex][i][j] > secondBestCentralTendencyValue)
                            || ((centralTendency[indicatorIndex][i][j]
                            == secondBestCentralTendencyValue) && (dispersion[indicatorIndex][i][j] < secondBestDispersionValue))) {
                        secondBestIndex = j;
                        secondBestCentralTendencyValue = centralTendency[indicatorIndex][i][j];
                        secondBestDispersionValue = dispersion[indicatorIndex][i][j];
                    }
                }
            }

            os.write((experiment.getProblemList().get(i).getTag().replace("_", "\\_") + " & ").getBytes("UTF-8"));
            for (int j = 0; j < (experiment.getAlgorithmList().size() - 1); j++) {

                String m = String.format(Locale.ENGLISH, "%.4f", centralTendency[indicatorIndex][i][j]);
                String s = String.format(Locale.ENGLISH, "%.4f", dispersion[indicatorIndex][i][j]);
                if (j == bestIndex) {
                    os.write(("\\cellcolor{gray95}$" + m + "$ & \\cellcolor{gray95}$" + s + "$ & ").getBytes("UTF-8"));
                } else if (j == bestIndex) {
                    os.write(("\\cellcolor{gray25}$" + m + "$ & \\cellcolor{gray25}$" + s + "$ & ").getBytes("UTF-8"));
                } else {
                    os.write(("$" + m + "$ & $" + s + "$ & ").getBytes("UTF-8"));
                }
            }

            String m = String.format(Locale.ENGLISH, "%.4f",
                    centralTendency[indicatorIndex][i][experiment.getAlgorithmList().size() - 1]);
            String s = String.format(Locale.ENGLISH, "%.4f",
                    dispersion[indicatorIndex][i][experiment.getAlgorithmList().size() - 1]);
            if (bestIndex == (experiment.getAlgorithmList().size() - 1)) {
                os.write(("\\cellcolor{gray95}$" + m + "$ & \\cellcolor{gray95}$" + s + "$ \\\\" + "\n").getBytes("UTF-8"));
            } else if (secondBestIndex == (experiment.getAlgorithmList().size() - 1)) {
                os.write(("\\cellcolor{gray25}$" + m + "$ & \\cellcolor{gray25}$" + s + "$ \\\\" + "\n").getBytes("UTF-8"));
            } else {
                os.write(("$" + m + "$ & $" + s + "$ \\\\" + "\n").getBytes("UTF-8"));
            }
        }

        // close table
        os.write(("\\hline" + "\n").getBytes("UTF-8"));
        os.write(("\\end{tabular}" + "\n").getBytes("UTF-8"));
        os.write(("\\end{scriptsize}" + "\n").getBytes("UTF-8"));
        os.write(("\\end{table}" + "\n").getBytes("UTF-8"));
        fileManager.close(hdfsPath, os);
    }

}
