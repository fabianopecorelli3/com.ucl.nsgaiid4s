/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.rejectpolicy;

import com.superamigos.sardegna.algorithm.ModifiedAbstractGeneticAlgorithm;
import java.util.List;
import org.uma.jmetal.solution.Solution;
import java.io.Serializable;


/**
 *
 * @author fably
 */
public interface RejectPolicy<S extends Solution> extends Serializable {
    
    void applyStrategy(ModifiedAbstractGeneticAlgorithm<S> algorithm, List<S> rejectedIndividuals, List<S> superPareto);
    
}
