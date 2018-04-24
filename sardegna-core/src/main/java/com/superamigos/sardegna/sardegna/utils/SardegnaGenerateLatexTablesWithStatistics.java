package com.superamigos.sardegna.sardegna.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentComponent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.uma.jmetal.util.experiment.component.GenerateLatexTablesWithStatistics;

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
public class SardegnaGenerateLatexTablesWithStatistics extends GenerateLatexTablesWithStatistics{


    public SardegnaGenerateLatexTablesWithStatistics(Experiment<?, ?> configuration) {
        super(configuration);
    }
    
    void printHeaderLatexCommands(String fileName) throws IOException {
        FileWriter os = new FileWriter(fileName, false);
        os.write("\\documentclass{article}" + "\n");
        os.write("\\title{Result tables}" + "\n");
        os.write("\\usepackage{colortbl}" + "\n");
        os.write("\\usepackage[a4paper,left=1cm,right=0cm]{geometry}" + "\n");
        os.write("\\usepackage[table*]{xcolor}" + "\n");
        os.write("\\xdefinecolor{gray95}{gray}{0.65}" + "\n");
        os.write("\\xdefinecolor{gray25}{gray}{0.8}" + "\n");
        os.write("\\author{Fabiano Pecorelli, Carlo Di Domenico}" + "\n");
        os.write("\\begin{document}" + "\n");
        os.write("\\maketitle" + "\n");
        os.write("\\section{Tables}" + "\n");

        os.close();
    }

}
