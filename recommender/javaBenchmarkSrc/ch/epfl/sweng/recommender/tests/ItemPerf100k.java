package ch.epfl.sweng.recommender.tests;

import java.io.IOException;
import org.junit.Test;

import ch.epfl.sweng.recommender.RecommenderAlgorithm;
import ch.epfl.sweng.recommender.User;
import ch.epfl.sweng.recommender.algorithm.ItemSimilarityAlgorithm;
import ch.epfl.sweng.recommender.movie.MovieRecommenderSystem;

public class ItemPerf100k {
    @Test
    public void test() {
        MovieRecommenderSystem movieRecSys = new MovieRecommenderSystem();
        try {
            movieRecSys.readData("sp/dataset_mod5_342k.dat");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // using the default constructor
        RecommenderAlgorithm algorithm = new ItemSimilarityAlgorithm();
        movieRecSys.addAlgorithm(algorithm);

        movieRecSys.update();
        
        for (User user : movieRecSys.getUsers()) {
            movieRecSys.getRecommendations(user, algorithm);
        }
    }
}
