/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ucl.nsgaiid4s.rejectpolicy;

import com.ucl.nsgaiid4s.algorithm.DistributedAbstractGeneticAlgorithm;
import com.ucl.nsgaiid4s.utils.PrinterUtils;
import java.util.List;
import org.uma.jmetal.solution.Solution;

/**
 *
 * @author fably
 */

public class RemoveRejectPolicy<S extends Solution> implements RejectPolicy<S> {

    @Override
    public void applyStrategy(DistributedAbstractGeneticAlgorithm<S> algorithm, List<S> rejectedIndividuals, List<S> superPareto) {
        List<S> population = algorithm.getPopulation();
        int populationMaxSize = algorithm.getMaxPopulationSize();
        int newPopulationSize = population.size() - rejectedIndividuals.size();
        if ((newPopulationSize*2) >= populationMaxSize){
            population.removeAll(rejectedIndividuals);
            algorithm.setPopulation(population);
        }
        else{
            new RandomReplacementRejectPolicy<S>().applyStrategy(algorithm, rejectedIndividuals, superPareto);
        }
    }

}
