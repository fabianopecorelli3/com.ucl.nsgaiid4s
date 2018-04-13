package com.superamigos.sardegna;

import java.util.Collection;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.Problem;

import java.util.List;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;

/**
 * Abstract class representing an evolutionary algorithm
 * @param <S> Solution
 * @param <R> Result
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public abstract class ModifiedAbstractGeneticAlgorithm<S, R>  extends AbstractGeneticAlgorithm<S, R>{

    public ModifiedAbstractGeneticAlgorithm(Problem<S> problem) {
        super(problem);
    }
  
  /**
   * Modified by 'Superamigos'.
   * Splitted the method in two methods in order to improve modularity
   */
  
  public void startExecution(){
    System.out.println("STARTING EXECUTION");
    population = createInitialPopulation();
    population = evaluatePopulation(population);
  }
  
  public Population<S> executeIteration(Population<S> result){
      System.out.println("EXECUTING ITERATION");
      population = result.getPopulation();
      if (population == null)
          startExecution();
      else{
          System.out.println("POPULATIONNOTNULL");
      }
      List<S> offspringPopulation;
      List<S> matingPopulation;
      matingPopulation = selection(population);
      offspringPopulation = reproduction(matingPopulation);
      offspringPopulation = evaluatePopulation(offspringPopulation);
      population = replacement(population, offspringPopulation);
      result.setPopulation(population);
      result.setElite((List<S>) getResult());
      return result;
      //result.addAll(population);
  }
}
