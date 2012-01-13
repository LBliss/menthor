package ch.epfl.sweng.recommender.tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.epfl.sweng.recommender.RecommenderAlgorithm;
import ch.epfl.sweng.recommender.algorithm.ItemSimilarityAlgorithm;
import ch.epfl.sweng.recommender.movie.MovieRecommenderSystem;

public class ItemPerf1m {
    @Test
    public void test() {
    	long start = System.currentTimeMillis();
    	MovieRecommenderSystem movieRecSys = new MovieRecommenderSystem();
    	boolean success = movieRecSys.readData("movielens/1m/users.dat",
    			"movielens/1m/movies.dat", "movielens/1m/ratings.dat");
    	
    	assertTrue(success);
    	
    	// using the default constructor
    	RecommenderAlgorithm knnAlgo = new ItemSimilarityAlgorithm();
    	movieRecSys.addAlgorithm(knnAlgo);
    	
    	movieRecSys.update();
    	
    	movieRecSys.getRecommendations(movieRecSys.getUserById(1), knnAlgo);
    	assertTrue(System.currentTimeMillis() - start < 10 * 60 * 1000);
    }
	
}
