/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fably
 */
public class Population<S> implements Serializable{
    private List<S> population;
    private List<S> elite;
    
    public Population(){
    }
    
    public Population(List<S> population, List<S> elite) {
        this.population = population;
        this.elite = elite;
    }

    public List<S> getPopulation() {
        return population;
    }

    public void setPopulation(List<S> population) {
        this.population = population;
    }

    public List<S> getElite() {
        return elite;
    }

    public void setElite(List<S> elite) {
        this.elite = elite;
    }
        
}
