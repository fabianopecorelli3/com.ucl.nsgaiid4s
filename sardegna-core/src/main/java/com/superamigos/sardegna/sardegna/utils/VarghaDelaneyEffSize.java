/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.sardegna.utils;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math.stat.ranking.NaturalRanking;

/**
 *
 * @author Carlo scarso?
 */
public class VarghaDelaneyEffSize {

    public static double varghaDelaneyEffSize(double[] A, double[] B) {
        int l1 = A.length;
        int l2 = B.length;
        double[] C = new double[l1 + l2];

        NaturalRanking naturalRanking = new NaturalRanking();

        C = (double[]) ArrayUtils.addAll(A, B);

        double[] rankC = naturalRanking.rank(C);

        double sumRank = 0;

        for (int i = 0; i < l1; i++) {
            sumRank += rankC[i];
        }

        double effectSize = ((sumRank / l1) - ((double) (l1 + 1) / 2)) / l2;

        return effectSize;

    }

}
