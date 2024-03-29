package com.ucl.nsgaiid4s.algorithm;

import com.ucl.nsgaiid4s.utils.PrinterUtils;
import com.ucl.nsgaiid4s.rejectpolicy.RejectPolicy;
import org.uma.jmetal.problem.Problem;

import java.util.List;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;

/**
 * Abstract class representing an evolutionary algorithm
 *
 * @author Fabiano Pecorelli
 * @author Carlo Di Domenico
 */
@SuppressWarnings("serial")
public abstract class DistributedAbstractGeneticAlgorithm<S extends Solution> extends AbstractGeneticAlgorithm<S, List<S>> {

    protected List<S> pareto;

    public DistributedAbstractGeneticAlgorithm(Problem<S> problem) {
        super(problem);
    }

    /**
     * Modified by 'Superamigos'. Splitted the method in two methods in order to
     * improve modularity
     */
    public void startExecution() {

        population = createInitialPopulation();
        population = evaluatePopulation(population);
    }

    public void executeIteration() {

        if (population == null) {
            startExecution();
        }

        List<S> offspringPopulation;
        List<S> matingPopulation;

        long timeStartSelection = System.currentTimeMillis();
        PrinterUtils.Printer.info("START Selection phase", false);
        matingPopulation = selection(population);
        PrinterUtils.Printer.info("ENDED Selection phase in: " + (System.currentTimeMillis() - timeStartSelection)+ " ms", false);

        long timeStartReproduction = System.currentTimeMillis();
        PrinterUtils.Printer.info("START Reproduction phase", false);
        offspringPopulation = reproduction(matingPopulation);
        PrinterUtils.Printer.info("ENDED Reproduction phase in: " + (System.currentTimeMillis() - timeStartReproduction)+ " ms", false);

        long timeStartEvaluation = System.currentTimeMillis();
        PrinterUtils.Printer.info("START Evaluation phase", false);
        offspringPopulation = evaluatePopulation(offspringPopulation);
        PrinterUtils.Printer.info("ENDED Evaluation phase in: " + (System.currentTimeMillis() - timeStartEvaluation)+ " ms", false);

        PrinterUtils.Printer.info("START Replacement phase", false);
        population = replacement(population, offspringPopulation);
        PrinterUtils.Printer.info("ENDED Evaluation phase in: " + (System.currentTimeMillis() - timeStartEvaluation)+ " ms", false);

    }

    abstract public List<S> extraEvaluation(List<S> solutionList);

    abstract public void computePareto();

    abstract public void receiveRejectedIndividuals(List<S> rejectedIndividuals, RejectPolicy strategy, List<S> superPareto);

    @Override
    public List<S> getResult() {
        computePareto();
        return pareto;
    }

    /**
     * A crossover operator is applied to a number of parents, and it assumed
     * that the population contains a valid number of solutions. This method
     * checks that.
     *
     * @param population
     * @param numberOfParentsForCrossover
     */
    public void checkNumberOfParents(List<S> population, int numberOfParentsForCrossover) {
        if ((population.size() % numberOfParentsForCrossover) != 0) {
            throw new JMetalException("Wrong number of parents: the reminder if the "
                    + "population size (" + population.size() + ") is not divisible by "
                    + numberOfParentsForCrossover);
        }
    }
}
