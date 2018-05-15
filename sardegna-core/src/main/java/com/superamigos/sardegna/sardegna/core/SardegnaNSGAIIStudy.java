package com.superamigos.sardegna.sardegna.core;

import com.superamigos.sardegna.sardegna.rejectpolicy.RejectPolicy;
import org.uma.jmetal.util.JMetalException;

import java.io.IOException;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Example of experimental study based on solving the ZDT problems with four versions of NSGA-II,
 * each of them applying a different crossover probability (from 0.7 to 1.0).
 *
 * This experiment assumes that the reference Pareto front are known, so the names of files
 * containing them and the directory where they are located must be specified.
 *
 * Six quality indicators are used for performance assessment.
 *
 * The steps to carry out the experiment are: 1. Configure the experiment 2. Execute the algorithms
 * 3. Compute the quality indicators 4. Generate Latex tables reporting means and medians 5.
 * Generate Latex tables with the result of applying the Wilcoxon Rank Sum Test 6. Generate Latex
 * tables with the ranking obtained by applying the Friedman test 7. Generate R scripts to obtain
 * boxplots
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class SardegnaNSGAIIStudy {

  public static void main(String[] args) throws IOException {
    if (args.length != 6) {
      throw new JMetalException("Arguments needed: 6; Arguments passed: "+args.length);
    }
    String experimentBaseDirectory = args[0];
    int numberOfPartitions = Integer.parseInt(args[1]);
    int independentRuns = Integer.parseInt(args[2]);
    int populationSize = Integer.parseInt(args[3]);
    int numberOfIterations = Integer.parseInt(args[4]);
    boolean locale = Boolean.parseBoolean(args[5]);
    String masterURL = args[6];
//    RejectPolicy policy = new RandomReplacementRejectPolicy();
    SardegnaRunner runner =  new SardegnaRunner(experimentBaseDirectory, numberOfPartitions, independentRuns, populationSize, numberOfIterations, locale);
    //SmallRunner runner =  new SmallRunner(experimentBaseDirectory, sparkContext);
    runner.run();
    
  }
}