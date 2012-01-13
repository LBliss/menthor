package ch.epfl.sweng.recommender.movie;

import java.util.HashMap;
import java.util.Set;

import ch.epfl.sweng.recommender.Item;
import ch.epfl.sweng.recommender.User;

/**
 * Movie.
 * @author olivier
 */
public class Movie implements Item {
    private final HashMap<User, Integer> ratings;
    private final int id;
    private final String title;
    private final String info;
    private double ratingsSum;
    private double average;

    /**
     * @param movieId the id
     * @param movieTitle the title
     * @param movieInfo the info
     */
    public Movie(int movieId, String movieTitle, String movieInfo) {
        this.ratings = new HashMap<User, Integer>();
        this.id = movieId;
        this.title = movieTitle;
        this.info = movieInfo;
        this.ratingsSum = 0;
        this.average = 0;
    }

    @Override
    public void addUserRating(User user, int rating) {
        ratingsSum += rating;
        ratings.put(user, new Integer(rating));
        average = ratingsSum / ratings.size();
    }

    @Override
    public double getAverageRating() {
        return average;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Integer getRating(User user) {
        return ratings.get(user);
    }

    @Override
    public Set<User> getRatingUsers() {
        return ratings.keySet();
    }

    @Override
    public String toString() {
        return title + " - " + info;
    }
}
