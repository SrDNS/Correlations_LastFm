/* * LensKit, an open source recommender systems toolkit. * Copyright 2010-2013 Regents of the University of Minnesota and contributors * Work on LensKit has been funded by the National Science Foundation under * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697. * * This program is free software; you can redistribute it and/or modify * it under the terms of the GNU Lesser General Public License as * published by the Free Software Foundation; either version 2.1 of the * License, or (at your option) any later version. * * This program is distributed in the hope that it will be useful, but WITHOUT * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more * details. * * You should have received a copy of the GNU General Public License along with * this program; if not, write to the Free Software Foundation, Inc., 51 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA. */ package org.grouplens.lenskit.eval.metrics.predict;

import com.google.common.collect.ImmutableList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 * * Evaluate a recommender's prediction accuracy with RMSE. * * @author <a
 * href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class LastFmEvaluation extends AbstractTestUserMetric {

    private static final PearsonsCorrelation p = new PearsonsCorrelation();

    private static final Logger logger = LoggerFactory.getLogger(RMSEPredictMetric.class);
    private static final ImmutableList<String> COLUMNS = ImmutableList.of("PearsonCor.ByRating", "PearsonCor.ByUser");
    private static final ImmutableList<String> USER_COLUMNS = ImmutableList.of("PearsonCor");

    static int uniqueUsers = 0;
    static double sumGolden = 0;
    static double sumA = 0;
    static double sumB = 0;
    static double sumC = 0;
    static double avgGolden;
    static double avgA;
    static double avgB;
    static double avgC;
    
    static double corA;
    static double corB;
    static double corC;
    static double sumPearsonA;
    static double sumPearsonB;
    static double sumPearsonC;
    static int nusers;

//    @Override
//    public TestUserMetricAccumulator makeAccumulator(AlgorithmInstance algo, TTDataSet ds) {
//        return new PAccum();
//    }
    @Override
    public List<String> getColumnLabels() {
        return COLUMNS;
    }

    @Override
    public List<String> getUserColumnLabels() {
        return USER_COLUMNS;
    }

//    public static void main(String[] args) {
//
//        LastFmEvaluation obj = new LastFmEvaluation();
//        obj.run();
//
//    }
    public static void main(String[] args) {

        String csvFile = "/home/user/Desktop/LastFM_exps/LastFM_1_of_5_20140606-0338.csv";
        BufferedReader br = null;
        String sCurrentLine = "";
        String cvsSplitBy = ",";

        try {

            TreeMap<Integer, ArrayList<Double>> golden = new TreeMap<Integer, ArrayList<Double>>();
            TreeMap<Integer, ArrayList<Double>> methodA = new TreeMap<Integer, ArrayList<Double>>();
            TreeMap<Integer, ArrayList<Double>> methodB = new TreeMap<Integer, ArrayList<Double>>();
            TreeMap<Integer, ArrayList<Double>> methodC = new TreeMap<Integer, ArrayList<Double>>();

            br = new BufferedReader(new FileReader(csvFile));
//            String header = br.readLine();
//            System.out.println(header);

            while ((sCurrentLine = br.readLine()) != null) {

                String[] cols = sCurrentLine.split(cvsSplitBy);
                int userId = Integer.parseInt(cols[0]);
                double goldenValue = Double.parseDouble(cols[5]);
                double methodAValue = Double.parseDouble(cols[2]);
                double methodBValue = Double.parseDouble(cols[3]);
                double methodCValue = Double.parseDouble(cols[4]);

                //Actual Values TreeMap creation
                if (!golden.containsKey(userId)) {
                    golden.put(userId, new ArrayList<Double>());
                    golden.get(userId).add(goldenValue);
                } else {
                    golden.get(userId).add(goldenValue);
                }
                //Method A TreeMap creation
                if (!methodA.containsKey(userId)) {
                    methodA.put(userId, new ArrayList<Double>());
                    methodA.get(userId).add(methodAValue);
                } else {
                    methodA.get(userId).add(methodAValue);
                }
                //Method B TreeMap creation
                if (!methodB.containsKey(userId)) {
                    methodB.put(userId, new ArrayList<Double>());
                    methodB.get(userId).add(methodBValue);
                } else {
                    methodB.get(userId).add(methodBValue);
                }
                //Method C TreeMap creation
                if (!methodC.containsKey(userId)) {
                    methodC.put(userId, new ArrayList<Double>());
                    methodC.get(userId).add(methodCValue);
                } else {
                    methodC.get(userId).add(methodCValue);
                }

            }

            uniqueUsers = golden.size();
            System.out.println("To synolo twn monadikwn xristwn sto dataset einai: " + uniqueUsers);
            
            nusers = 0;
            sumPearsonA = 0;
            sumPearsonB = 0;
            sumPearsonC = 0;
            for (Integer i : golden.keySet()) {

                Double[] ADoubles = new Double[methodA.get(i).size()];
                methodA.get(i).toArray(ADoubles);
                Double[] BDoubles = new Double[methodB.get(i).size()];
                methodB.get(i).toArray(BDoubles);
                Double[] CDoubles = new Double[methodC.get(i).size()];
                methodC.get(i).toArray(CDoubles);
                Double[] goldenDoubles = new Double[golden.get(i).size()];
                golden.get(i).toArray(goldenDoubles);

                if (goldenDoubles.length > 1) {
                    corA = p.correlation(ArrayUtils.toPrimitive(ADoubles), ArrayUtils.toPrimitive(goldenDoubles));
                    corB = p.correlation(ArrayUtils.toPrimitive(BDoubles), ArrayUtils.toPrimitive(goldenDoubles));
                    corC = p.correlation(ArrayUtils.toPrimitive(CDoubles), ArrayUtils.toPrimitive(goldenDoubles));
                    
                    sumPearsonA += corA;
                    sumPearsonB += corB;
                    sumPearsonC += corC;
                    nusers++;
//                for (int arrayCounter = 0; arrayCounter < goldenDoubles.length; arrayCounter++) {
//
//                    sumA += ADoubles[arrayCounter];
//                    sumB += BDoubles[arrayCounter];
//                    sumC += CDoubles[arrayCounter];
//                    sumGolden += goldenDoubles[arrayCounter];
//                    
//                }
//
//                avgA = sumA / ADoubles.length;
//                avgB = sumB / BDoubles.length;
//                avgC = sumC / CDoubles.length;
//                avgGolden = sumGolden / goldenDoubles.length;

                    System.out.println(sumPearsonA);
                    System.out.println(sumPearsonB);
                    System.out.println(sumPearsonC);
                    System.out.println(nusers);
                    System.out.println("----------------------------------");
//                sumGolden = 0; sumA = 0; sumB = 0; sumC = 0;
                } else {
                    System.out.println("User has only ONE rating!!!!! ");
                    System.out.println("----------------------------------");
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Done");
    }

    abstract class PAccum implements TestUserMetricAccumulator {
//
//        int nusers;
//        double sumPearson;
////        private double sse = 0;
//        private double r = 0;
////        private int nratings = 0;
////        private int nusers = 0;
//
//        @Nonnull
//        @Override
//        public Object[] evaluate(TestUser user) {
//            SparseVector ratings = user.getTestRatings();
//            SparseVector predictions = user.getPredictions();
//            double[] x = new double[ratings.size()];//{1,2,3,4};//;
//            double[] y = new double[ratings.size()];//{1,2.5,2.5,8};//[ratings.size()];
////            double usse = 0;
//            int n = 0;
//            for (VectorEntry e : ratings.fast()) {
//
//                x[n] = e.getValue();
//                if (!Double.isNaN(predictions.get(e.getKey()))) {
//                    y[n] = predictions.get(e.getKey());
//                }
//
////                System.out.println(x[n] + ", " + y[n]);
////                double err = e.getValue() - ratings.get(e.getKey());
////                usse += err * err;
//                n++;
//
//            }
//
////            System.out.println(x.length + ", " + y.length);
////            sse += usse;
////            nratings += n;
//            if (n > 1) {
//
//                r = p.correlation(x, y);//new double []{9,9,9,5}, new double []{7.471499034956037,7.471499034956037,7.471499034956037,7.471499034956037});
//                if (Double.isNaN(r)) {
//                    for (int i = 1; i < x.length; i++) {
//                        System.out.println(x[i] + ", " + y[i]);
//                    }
//                    r = 1;
//                }
//
//                sumPearson += r;
//                nusers++;
//                System.out.println(r + ":" + user.getUserId() + ":" + sumPearson / nusers);
//                return new Object[]{r};//sumPearson/nusers
//            } else {
//                return new Object[]{0};
//            }
//        }
//
//        @Nonnull
//        @Override
//        public Object[] finalResults() {
////            double v = sqrt(sse / nratings);
////            logger.info("PC: {}", v);
//            return new Object[]{sumPearson / nusers, r};
//        }
    }
}
