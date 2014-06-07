/* * LensKit, an open source recommender systems toolkit. * Copyright 2010-2013 Regents of the University of Minnesota and contributors * Work on LensKit has been funded by the National Science Foundation under * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697. * * This program is free software; you can redistribute it and/or modify * it under the terms of the GNU Lesser General Public License as * published by the Free Software Foundation; either version 2.1 of the * License, or (at your option) any later version. * * This program is distributed in the hope that it will be useful, but WITHOUT * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more * details. * * You should have received a copy of the GNU General Public License along with * this program; if not, write to the Free Software Foundation, Inc., 51 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA. */ package org.grouplens.lenskit.eval.metrics.predict;

import com.google.common.collect.ImmutableList;
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
import static java.lang.Math.sqrt;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

/**
 * * Evaluate a recommender's prediction accuracy with RMSE. * * @author <a
 * href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SpearmansCorPredictMetric extends AbstractTestUserMetric {

    private static final SpearmansCorrelation p = new SpearmansCorrelation();

    private static final Logger logger = LoggerFactory.getLogger(RMSEPredictMetric.class);
    private static final ImmutableList<String> COLUMNS = ImmutableList.of("SpearmansCor.ByRating", "SpearmansCor.ByUser");
    private static final ImmutableList<String> USER_COLUMNS = ImmutableList.of("SpearmansCor");

    @Override
    public TestUserMetricAccumulator makeAccumulator(AlgorithmInstance algo, TTDataSet ds) {
        return new PAccum();
    }

    @Override
    public List<String> getColumnLabels() {
        return COLUMNS;
    }

    @Override
    public List<String> getUserColumnLabels() {
        return USER_COLUMNS;
    }

    class PAccum implements TestUserMetricAccumulator {

        int nusers;
        double sumPearson;
//        private double sse = 0;
        private double r = 0;
//        private int nratings = 0;
//        private int nusers = 0;

        @Nonnull
        @Override
        public Object[] evaluate(TestUser user) {
            SparseVector ratings = user.getTestRatings();
            SparseVector predictions = user.getPredictions();
            double[] x = new double[ratings.size()];//{1,2,3,4};//;
            double[] y = new double[ratings.size()];//{1,2.5,2.5,8};//[ratings.size()];
//            double usse = 0;
            int n = 0;
            for (VectorEntry e : ratings.fast()) {

                x[n] = e.getValue();
                if (!Double.isNaN(predictions.get(e.getKey()))) {
                    y[n] = predictions.get(e.getKey());
                }

//                System.out.println(x[n] + ", " + y[n]);
//                double err = e.getValue() - ratings.get(e.getKey());
//                usse += err * err;
                n++;

            }

//            System.out.println(x.length + ", " + y.length);
//            sse += usse;
//            nratings += n;
            if (n > 1) {

                r = p.correlation(x, y);//new double []{9,9,9,5}, new double []{7.471499034956037,7.471499034956037,7.471499034956037,7.471499034956037});
                if (Double.isNaN(r)) {
                    for (int i = 1; i < x.length; i++) {
                        System.out.println(x[i] + ", " + y[i]);
                    }
                    r = 1;
                }

                sumPearson += r;
                nusers++;
                System.out.println(r + ":" + user.getUserId() + ":" + sumPearson / nusers);
                return new Object[]{r};//sumPearson/nusers
            } else {
                return new Object[]{0};
            }
        }

        @Nonnull
        @Override
        public Object[] finalResults() {
//            double v = sqrt(sse / nratings);
//            logger.info("PC: {}", v);
            return new Object[]{sumPearson / nusers,r};
        }
    }
}
