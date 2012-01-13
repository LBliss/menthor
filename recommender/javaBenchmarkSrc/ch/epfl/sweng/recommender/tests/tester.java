package ch.epfl.sweng.recommender.tests;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.epfl.sweng.recommender.RecommenderAlgorithm;
import ch.epfl.sweng.recommender.User;
import ch.epfl.sweng.recommender.algorithm.ParallelItemSimilarityAlgorithm;
import ch.epfl.sweng.recommender.movie.MovieRecommenderSystem;

public class tester {

    /**
     * @param args
     */
    public static void main(String[] args) {
        int poolSize = Integer.parseInt(args[0]);
        
        final MovieRecommenderSystem movieRecSys = new MovieRecommenderSystem();
        try {
            movieRecSys.readData(args[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // using the default constructor
        final RecommenderAlgorithm algorithm = new ParallelItemSimilarityAlgorithm(poolSize);
        movieRecSys.addAlgorithm(algorithm);

        long startTime = System.currentTimeMillis();
        movieRecSys.update();
        long updateTime = System.currentTimeMillis();
        
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                Long.MAX_VALUE,
                TimeUnit.DAYS,
                new LinkedBlockingQueue<Runnable>()
        );
        
        for (final User user : movieRecSys.getUsers()) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    movieRecSys.getRecommendations(user, algorithm);
                }
            });
        }
        threadPoolExecutor.shutdown();
        try {
            threadPoolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("sim:" + (updateTime - startTime) + "ms        rec:" + (System.currentTimeMillis() - updateTime) + "ms");
    }
}
