/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ucl.nsgaiid4s.core;

import com.amazonaws.services.s3.AmazonS3;
import com.ucl.nsgaiid4s.algorithm.impl.NSGAIID4S;
import com.ucl.nsgaiid4s.operator.impl.crossover.SardegnaSBXCrossover;
import com.ucl.nsgaiid4s.operator.impl.mutation.SardegnaPolynomialMutation;
import com.ucl.nsgaiid4s.rejectpolicy.RandomReplacementRejectPolicy;
import com.ucl.nsgaiid4s.utils.PrinterUtils;
import com.ucl.nsgaiid4s.utils.FileManager;
import com.ucl.nsgaiid4s.utils.LocalFileManger;
import com.ucl.nsgaiid4s.experiment.component.SardegnaExperimentAlgorithm;
import com.ucl.nsgaiid4s.utils.S3FileManager;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT1;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT2;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT3;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT4;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT6;
import org.uma.jmetal.qualityindicator.impl.Epsilon;
import org.uma.jmetal.qualityindicator.impl.GenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistancePlus;
import org.uma.jmetal.qualityindicator.impl.Spread;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentBuilder;
import org.uma.jmetal.util.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.util.experiment.util.ExperimentProblem;

/**
 *
 * @author fably
 */
public class NSGAIID4SRunner {

    private int independentRuns;
    private String path;
    private JavaSparkContext sparkContext;
    int numberOfPartitions;
    int populationSize;
    int numberOfIterations;
    FileManager fileManager;
    int k;
    int numberOfThreads;
    int delay;
    boolean locale;
    
    public NSGAIID4SRunner() {
        AmazonS3 s3client;
    }

    public NSGAIID4SRunner(String path, int numberOfPartitions, int k, int independentRuns, int populationSize, int numberOfIterations, boolean locale, int numberOfThreads, int delay) {
        this.path = path;
        this.numberOfPartitions = numberOfPartitions;
        this.independentRuns = independentRuns;
        this.populationSize = populationSize;
        this.numberOfIterations = numberOfIterations;
        this.k = k;
        SparkConf sparkConf = new SparkConf().setAppName("Sardegna");
        if (locale) {
            sparkConf.setMaster("local[2]")/*.set("spark.executor.memory", "1g")*/;
            fileManager = new LocalFileManger();
        }
        else{
            fileManager = new S3FileManager();
            fileManager.setNumberOfPartitions(numberOfPartitions);
        }
        this.sparkContext = new JavaSparkContext(sparkConf);
        this.numberOfThreads = numberOfThreads;
        this.locale = locale;
    }

    public void run() throws FileNotFoundException, IOException {
        String experimentBaseDirectory = path;

        PrinterUtils.Printer.setMasterPw(experimentBaseDirectory + "/master-log.txt");
        PrinterUtils.Printer.setWorkersPw(experimentBaseDirectory + "/workers-log.txt");

        List<ExperimentProblem<DoubleSolution>> problemList = new ArrayList<>();
        problemList.add(new ExperimentProblem<>(new ZDT1()));
        problemList.add(new ExperimentProblem<>(new ZDT2()));
        problemList.add(new ExperimentProblem<>(new ZDT3()));
        problemList.add(new ExperimentProblem<>(new ZDT4()));
        problemList.add(new ExperimentProblem<>(new ZDT6()));
      /*  problemList.add(new ExperimentProblem<>(new WFG1()));
        problemList.add(new ExperimentProblem<>(new WFG2()));
        problemList.add(new ExperimentProblem<>(new WFG3()));
        problemList.add(new ExperimentProblem<>(new WFG4()));
        problemList.add(new ExperimentProblem<>(new WFG5()));
        problemList.add(new ExperimentProblem<>(new WFG6()));
        problemList.add(new ExperimentProblem<>(new WFG7()));
        problemList.add(new ExperimentProblem<>(new WFG8()));
        problemList.add(new ExperimentProblem<>(new WFG9()));
        problemList.add(new ExperimentProblem<>(new Binh2()));
        problemList.add(new ExperimentProblem<>(new Golinski()));
        problemList.add(new ExperimentProblem<>(new Kursawe()));
        problemList.add(new ExperimentProblem<>(new DTLZ1()));
        problemList.add(new ExperimentProblem<>(new DTLZ2()));
        problemList.add(new ExperimentProblem<>(new DTLZ3()));
        problemList.add(new ExperimentProblem<>(new DTLZ4()));
        problemList.add(new ExperimentProblem<>(new DTLZ5()));
        problemList.add(new ExperimentProblem<>(new DTLZ6()));
        //problemList.add(new ExperimentProblem<>(new DTLZ7()));
*/
        List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithmList
                = configureAlgorithmList(problemList);
        List<String> referenceFrontFileNames = Arrays.asList("ZDT1.pf","ZDT2.pf", "ZDT3.pf", "ZDT4.pf", "ZDT6.pf"/*, "WFG1.2D.pf", "WFG2.2D.pf", "WFG3.2D.pf", "WFG4.2D.pf", "WFG5.2D.pf", "WFG6.2D.pf", "WFG7.2D.pf", "WFG8.2D.pf", "WFG9.2D.pf", "Binh2.pf", "Golinski.pf", "Kursawe.pf", "DTLZ1.3D.pf", "DTLZ2.3D.pf", "DTLZ3.3D.pf", "DTLZ4.3D.pf", "DTLZ5.3D.pf", "DTLZ6.3D.pf", "DTLZ7.3D.pf"*/);

        Experiment<DoubleSolution, List<DoubleSolution>> experiment
                = new ExperimentBuilder<DoubleSolution, List<DoubleSolution>>("ZDTStudy")
                .setAlgorithmList(algorithmList)
                .setProblemList(problemList)
                .setExperimentBaseDirectory(experimentBaseDirectory)
                .setOutputParetoFrontFileName("FUN")
                .setOutputParetoSetFileName("VAR")
                .setReferenceFrontDirectory("/pareto_fronts")
                .setReferenceFrontFileNames(referenceFrontFileNames)
                .setIndicatorList(Arrays.asList(new Epsilon<DoubleSolution>(),
                        new Spread<DoubleSolution>(),
                        new GenerationalDistance<DoubleSolution>(),
                        new PISAHypervolume<DoubleSolution>(),
                        new InvertedGenerationalDistance<DoubleSolution>(),
                        new InvertedGenerationalDistancePlus<DoubleSolution>()))
                .setIndependentRuns(independentRuns)
                .setNumberOfCores(8)
                .build();

        new ExecuteAlgorithms<>(experiment, fileManager, locale).run();
       /* new ComputeQualityIndicators<>(experiment).run();
        new GenerateExcelResultsFile(experiment).run();
        new GenerateLatexTablesWithStatistics(experiment).run();
        
        new GenerateWilcoxonTestTablesWithR<>(experiment).run();
//        new GenerateFriedmanTestTables<>(experiment).run();
        new GeneratePDFBoxplotsWithR<>(experiment).setRows(1).setColumns(1).run();
        */PrinterUtils.Printer.closePw();
    }

    /**
     * The algorithm list is composed of pairs
     * {@link Algorithm} + {@link Problem} which form part of a
     * {@link ExperimentAlgorithm}, which is a decorator for class
     * {@link Algorithm}. The {@link
     * ExperimentAlgorithm} has an optional tag component, that can be set as it
     * is shown in this example, where four variants of a same algorithm are
     * defined.
     */
    public List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> configureAlgorithmList(
            List<ExperimentProblem<DoubleSolution>> problemList) {
        List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms = new ArrayList<>();

        for (int i = 0; i < problemList.size(); i++) {
          /*  Algorithm<List<DoubleSolution>> algorithm = new NSGAIIBuilder<>(
                    problemList.get(i).getProblem(),
                    new SBXCrossover(1.0, 5),
                    new PolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0))
                    .setMaxEvaluations(populationSize * numberOfIterations)
                    .setPopulationSize(populationSize)
                    //.setSolutionListEvaluator(new MultithreadedSolutionListEvaluator<>(numberOfThreads, problemList.get(i).getProblem()))
                    .build();
            algorithms.add(new SardegnaExperimentAlgorithm<>(algorithm, "jMetal", problemList.get(i).getTag(),fileManager, locale, numberOfPartitions));
           */Algorithm<List<DoubleSolution>> sardegna_RR = new NSGAIID4S<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    populationSize * numberOfIterations,
                    k,
                    numberOfPartitions,
                    populationSize,
                    sparkContext,
                    new RandomReplacementRejectPolicy(),
                    new SardegnaSBXCrossover(1.0, 5),
                    new SardegnaPolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0),
                    numberOfThreads
            );
            algorithms.add(new SardegnaExperimentAlgorithm<>(sardegna_RR, "RR", problemList.get(i).getTag(), fileManager, locale, 1));
           /* Algorithm<List<DoubleSolution>> sardegna_REPL = new Sardegna<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    populationSize * numberOfIterations,
                    k,
                    numberOfPartitions,
                    populationSize,
                    sparkContext,
                    new ReplacementRejectPolicy(),
                    new SardegnaSBXCrossover(1.0, 5),
                    new SardegnaPolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0),
                    numberOfThreads
            );
            algorithms.add(new SardegnaExperimentAlgorithm<>(sardegna_REPL, "REPL", problemList.get(i).getTag(),fileManager));
            Algorithm<List<DoubleSolution>> sardegna_DR = new Sardegna<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    populationSize * numberOfIterations,
                    k,
                    numberOfPartitions,
                    populationSize,
                    sparkContext,
                    new DominanceRankingRejectPolicy(),
                    new SardegnaSBXCrossover(1.0, 5),
                    new SardegnaPolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0),
                    numberOfThreads
            );
            algorithms.add(new SardegnaExperimentAlgorithm<>(sardegna_DR, "DR", problemList.get(i).getTag(),fileManager));
            Algorithm<List<DoubleSolution>> sardegna_REPR = new Sardegna<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    populationSize * numberOfIterations,
                    k,
                    numberOfPartitions,
                    populationSize,
                    sparkContext,
                    new ReproductionRejectPolicy(),
                    new SardegnaSBXCrossover(1.0, 5),
                    new SardegnaPolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0),
                    numberOfThreads
            );
            algorithms.add(new SardegnaExperimentAlgorithm<>(sardegna_REPR, "REPR", problemList.get(i).getTag(),fileManager));
            Algorithm<List<DoubleSolution>> sardegna_RM = new Sardegna<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    populationSize * numberOfIterations,
                    k,
                    numberOfPartitions,
                    populationSize,
                    sparkContext,
                    new RemoveRejectPolicy(),
                    new SardegnaSBXCrossover(1.0, 5),
                    new SardegnaPolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0),
                    numberOfThreads
            );
            algorithms.add(new SardegnaExperimentAlgorithm<>(sardegna_RM, "REM", problemList.get(i).getTag(),fileManager));
      */  }
        return algorithms;
    }

}
