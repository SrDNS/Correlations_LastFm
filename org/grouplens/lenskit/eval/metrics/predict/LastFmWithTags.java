/* * LensKit, an open source recommender systems toolkit. * Copyright 2010-2013 Regents of the University of Minnesota and contributors * Work on LensKit has been funded by the National Science Foundation under * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697. * * This program is free software; you can redistribute it and/or modify * it under the terms of the GNU Lesser General Public License as * published by the Free Software Foundation; either version 2.1 of the * License, or (at your option) any later version. * * This program is distributed in the hope that it will be useful, but WITHOUT * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more * details. * * You should have received a copy of the GNU General Public License along with * this program; if not, write to the Free Software Foundation, Inc., 51 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA. */ package org.grouplens.lenskit.eval.metrics.predict;

import com.google.common.collect.ImmutableList;
import gr.hua.dit.basics.Edge;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang3.ArrayUtils;
import static org.apache.commons.math3.complex.Quaternion.K;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

/**
 * * Evaluate a recommender's prediction accuracy with RMSE. * * @author <a
 * href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class LastFmWithTags extends AbstractTestUserMetric {

//    private static final PearsonsCorrelation p = new PearsonsCorrelation();
    private static final SpearmansCorrelation p = new SpearmansCorrelation();

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

    static int fold = 1;

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

//        String trainFile = "/home/user/Desktop/LastFM_exps/reresultset1of5fold/Fold_" + fold + "_train.csv";
//        String testFile = "/home/user/Desktop/LastFM_exps/reresultset1of5fold/Fold_" + fold + "_test.csv";
//        String userTagsFile = "/home/user/Desktop/LastFM_exps/reresultset1of5fold/user_taggedartists.csv";
        String trainFile = "/home/user/Desktop/LastFM_exps/reresultset1of5fold/train.csv";
        String testFile = "/home/user/Desktop/LastFM_exps/reresultset1of5fold/Fold_" + fold + "_test.csv";
        String userTagsFile = "/home/user/Desktop/LastFM_exps/reresultset1of5fold/tags.csv";
        BufferedReader brTrain = null;
        BufferedReader brTest = null;
        BufferedReader brTags = null;
        String sCurrentLine = "";
        String splitBy = ",";

        try {
            PrintWriter writeEnhancedTrain = new PrintWriter("/home/user/Desktop/LastFM_exps/reresultset1of5fold/EnhancedTrain_fold " + fold + ".txt", "UTF-8");

            TreeMap<Integer, ArrayList<Integer[]>> UserArtist = new TreeMap<>();
            TreeMap<Integer, ArrayList<Integer[]>> UserTag = new TreeMap<>();
            Set<Integer> ArtistArtist = new HashSet<>();
            Set<Integer> UserUser = new HashSet<>();
//            TreeMap<Integer, Integer> ArtistArtist = new TreeMap<>();
//            TreeMap<Integer, ArrayList<Integer[]>> ArtistTag = new TreeMap<>();

            brTrain = new BufferedReader(new FileReader(trainFile));
            brTest = new BufferedReader(new FileReader(testFile));
            brTags = new BufferedReader(new FileReader(userTagsFile));

            while ((sCurrentLine = brTrain.readLine()) != null) {

                String[] cols = sCurrentLine.split(splitBy);

                int userId = Integer.parseInt(cols[0]);
                int artistId = Integer.parseInt(cols[1]);
                double actualValue = Double.parseDouble(cols[5]);

                Integer[] ArtistValue = new Integer[cols.length - 4];

                for (int i = 1; i < cols.length - 3; i++) {
                    if (i != 1) {
                        ArtistValue[i - 1] = Integer.parseInt(cols[i + 3]);
                    } else {
                        ArtistValue[i - 1] = Integer.parseInt(cols[i]);
                    }
                }

                // Create User --> Artist --> Hits TreeMap
                if (!UserArtist.containsKey(userId)) {
                    UserArtist.put(userId, new ArrayList<Integer[]>());
                    UserArtist.get(userId).add(ArtistValue);
                } else {
                    UserArtist.get(userId).add(ArtistValue);
                }

                // Create User --> User
//                UserUser.add(userId);
                // Create Artist --> Artist
                ArtistArtist.add(artistId);

            }

            // Print User --> Artist
            for (Integer uid : UserArtist.keySet()) {
                for (Integer[] rows : UserArtist.get(uid)) {
                    writeEnhancedTrain.print(uid + " ");
                    for (Integer col : rows) {
                        writeEnhancedTrain.print(col.toString() + " ");//rows.toString()
                    }
                    writeEnhancedTrain.println();
                }
            }

            // Print -Artist --> Artist
            for (Integer art : ArtistArtist) {
                writeEnhancedTrain.println("-" + art + " " + art + " 1");
            }

            // Print -User --> User
//            for (Integer usr : UserUser) {
//                writeEnhancedTrain.println("-" + usr + " " + usr + " 1");
//            }
            // Create Artist --> Tag
            String line = "";
            HashMap<Integer, HashMap<Integer, Integer>> atMap = new HashMap();
            HashMap<Integer, Integer> tcMap = null;// = new HashMap<>();
            HashMap<Integer, HashMap<Integer, Integer>> utMap = new HashMap();
            HashMap<Integer, Integer> hsMap = null;
            String headerLine = brTags.readLine();
            while ((line = brTags.readLine()) != null) {
                String[] cols = line.split(splitBy);
                int userId = Integer.parseInt(cols[0]);
                int artistId = Integer.parseInt(cols[1]);
                int tagId = Integer.parseInt(cols[2]);

                tcMap = atMap.get(artistId);
                if (tcMap == null) {
                    tcMap = new HashMap<>();
                    tcMap.put(tagId, 1);
                    atMap.put(artistId, tcMap);
                } else {
//                    atMap.put(artistId, tcMap);
//                    Integer count = atMap.get(artistId).get(tagId);
//                    count++;
                    if (atMap.get(artistId).get(tagId) != null){
                        tcMap.put(tagId, atMap.get(artistId).get(tagId)+1);
                        atMap.put(artistId, tcMap);
                    } else {
                        System.out.println("To tag den exei ksanaemfanistei!!");
                    }
                }

                // Create User --> TagHits
                hsMap = utMap.get(userId);
                if (hsMap == null) {
                    hsMap = new HashMap<>();
                    hsMap.put(tagId, 0);
                    utMap.put(userId, hsMap);
                } else {
                    for (Integer uid : UserArtist.keySet()) {
                        for (Integer[] rows : UserArtist.get(uid)) {
                            int aid = rows[0];
                            int hits = rows[1];
                            HashMap tags = atMap.get(artistId);
                            Set alltags = tags.keySet();
                            for (Object tid : alltags) {
                                try {
                                    Integer a = utMap.get(uid).get(tagId);
                                    if (a == null) {
                                        utMap.get(uid).put((Integer) tid, hits);
                                    } else {
                                        utMap.get(uid).put((Integer) tid, hits + a);
                                    }
                                } catch (java.lang.NullPointerException er) {
                                    System.out.println(er);//er.printStackTrace();
                                }
                            }
                        }
                    }
                }

            }

            // Print Artist --> Tag
            for (Integer atMapEntry : atMap.keySet()) {
                Integer artid = atMapEntry;
                for (Integer tcMapEntry : tcMap.keySet()) {
                    Integer tagid = tcMapEntry;
                    Integer tagCounter = tcMap.get(atMapEntry);
                    writeEnhancedTrain.println(artid + " " + tagid + " " + tagCounter);
                }
            }

            // Print User --> TagHits
            for (Integer userTagHits : utMap.keySet()) {
                Integer user = userTagHits;
                for (Integer hsCounter : hsMap.keySet()) {
                    Integer tag = hsCounter;
                    Integer hits = hsMap.get(userTagHits);
                    writeEnhancedTrain.println(user + " " + tag + " " + hits);
                }
            }

//            System.out.println(ArtistArtist.size());
//            System.out.println(UserUser.size());
            writeEnhancedTrain.flush();
            writeEnhancedTrain.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (brTrain != null) {
                try {
                    brTrain.close();
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
