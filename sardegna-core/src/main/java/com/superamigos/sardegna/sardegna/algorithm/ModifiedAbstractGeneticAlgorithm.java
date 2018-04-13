package com.superamigos.sardegna.sardegna.algorithm;

import com.superamigos.sardegna.sardegna.utils.PrinterUtils;
import com.superamigos.sardegna.sardegna.rejectpolicy.RejectPolicy;
import org.uma.jmetal.problem.Problem;

import java.util.List;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;

/**
 * Abstract class representing an evolutionary algorithm
 *
 * @param <S> Solution
 *
 * @author Fabiano Pecorelli
 * @author Carlo Di Domenico
 */
@SuppressWarnings("serial")
public abstract class ModifiedAbstractGeneticAlgorithm<S extends Solution> extends AbstractGeneticAlgorithm<S, List<S>> {

    protected List<S> pareto;
    
    public ModifiedAbstractGeneticAlgorithm(Problem<S> problem) {
        super(problem);
    }

    /**
     * Modified by 'Superamigos'. Splitted the method in two methods in order to
     * improve modularity
     */
    public void startExecution() {
        population = createInitialPopulation();
        population = evaluatePopulation(population);
        PrinterUtils.Printer.print(new java.util.Date() + "STAMPO "+population.size()+" INDIVIDUI\n\n");
        for (S s : population) {
            PrinterUtils.Printer.print(new java.util.Date() + " - POPULATION SOTTO (0): " + s + "\n\n");
        }
    }

    public void executeIteration() {
        
        if (population == null)
            startExecution();
        
        List<S> offspringPopulation;
        List<S> matingPopulation;
        matingPopulation = selection(population);
        offspringPopulation = reproduction(matingPopulation);
        offspringPopulation = evaluatePopulation(offspringPopulation);
        population = replacement(population, offspringPopulation);
    }

    abstract public void computePareto();

    abstract public void receiveRejectedIndividuals(List<S> rejectedIndividuals, RejectPolicy strategy, List<S> superPareto);

    @Override
    public List<S> getResult() {
        computePareto();
        return pareto;
    }
    
   /**
   * A crossover operator is applied to a number of parents, and it assumed that the population contains
   * a valid number of solutions. This method checks that.
   * @param population
   * @param numberOfParentsForCrossover
   */
  public void checkNumberOfParents(List<S> population, int numberOfParentsForCrossover) {
    if ((population.size() % numberOfParentsForCrossover) != 0) {
      throw new JMetalException("Wrong number of parents: the remainder if the " +
              "population size (" + population.size() + ") is not divisible by " +
              numberOfParentsForCrossover) ;
    }
  }
}
