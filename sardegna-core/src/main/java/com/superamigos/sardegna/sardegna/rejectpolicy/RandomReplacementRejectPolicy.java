/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.sardegna.rejectpolicy;

import com.superamigos.sardegna.sardegna.algorithm.ModifiedAbstractGeneticAlgorithm;
import com.superamigos.sardegna.sardegna.utils.PrinterUtils;
import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.solution.Solution;

/**
 *
 * @author fably
 */
public class RandomReplacementRejectPolicy<S extends Solution> implements RejectPolicy<S> {

    @Override
    public void applyStrategy(ModifiedAbstractGeneticAlgorithm<S> algorithm, List<S> rejectedIndividuals, List<S> superPareto) {
        PrinterUtils.Printer.print(new java.util.Date() + " - Sto rimpiazzando qualcosa\n\n");
        int howMuch = rejectedIndividuals.size();
        List<S> population = algorithm.getPopulation();
        List<S> newIndividuals = new ArrayList<>();
        population.removeAll(rejectedIndividuals);
        for (int i = 0; i < howMuch; i++) {
            S newIndividual = algorithm.getProblem().createSolution();
            newIndividuals.add(newIndividual);
        }
        newIndividuals = algorithm.extraEvaluation(newIndividuals);
        for (S s : newIndividuals){
            PrinterUtils.Printer.print(new java.util.Date() + " - NEW INDIVIDUAL: "+s.getObjective(0)+"\n\n");
        }
        population.addAll(newIndividuals);
        algorithm.setPopulation(population);
    }

}
