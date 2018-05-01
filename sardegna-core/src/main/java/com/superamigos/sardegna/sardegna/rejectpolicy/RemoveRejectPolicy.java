/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.sardegna.rejectpolicy;

import com.superamigos.sardegna.sardegna.algorithm.ModifiedAbstractGeneticAlgorithm;
import com.superamigos.sardegna.sardegna.utils.PrinterUtils;
import java.util.List;
import org.uma.jmetal.solution.Solution;

/**
 *
 * @author fably
 */

public class RemoveRejectPolicy<S extends Solution> implements RejectPolicy<S> {

    @Override
    public void applyStrategy(ModifiedAbstractGeneticAlgorithm<S> algorithm, List<S> rejectedIndividuals, List<S> superPareto) {
        List<S> population = algorithm.getPopulation();
        int populationMaxSize = algorithm.getMaxPopulationSize();
        int newPopulationSize = population.size() - rejectedIndividuals.size();
        if ((newPopulationSize*2) >= populationMaxSize){
            PrinterUtils.Printer.debug("Sto scartando qualcosa");
            population.removeAll(rejectedIndividuals);
            algorithm.setPopulation(population);
        }
        else{
            new RandomReplacementRejectPolicy<S>().applyStrategy(algorithm, rejectedIndividuals, superPareto);
        }
    }

}
