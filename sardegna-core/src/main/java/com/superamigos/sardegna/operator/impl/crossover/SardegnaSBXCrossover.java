/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.operator.impl.crossover;

import java.io.Serializable;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.util.RepairDoubleSolution;
import org.uma.jmetal.solution.util.RepairDoubleSolutionAtBounds;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

/**
 *
 * @author fably
 */
public class SardegnaSBXCrossover<S extends Solution<?>> extends SBXCrossover{
    
    /** Constructor */
  public SardegnaSBXCrossover(double crossoverProbability, double distributionIndex) {
    this (crossoverProbability, distributionIndex, new RepairDoubleSolutionAtBounds()) ;
  }

  /** Constructor */
  public SardegnaSBXCrossover(double crossoverProbability, double distributionIndex, RepairDoubleSolution solutionRepair) {
	  super(crossoverProbability, distributionIndex, solutionRepair, (RandomGenerator<Double> & Serializable)() -> JMetalRandom.getInstance().nextDouble());
  }
    
}
