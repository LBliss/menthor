package ch.epfl.sweng.recommender.movie;

import java.util.HashMap;
import java.util.Set;

import ch.epfl.sweng.recommender.Item;
import ch.epfl.sweng.recommender.User;

/**
 * MovieUser.
 * @author olivier
 */
public class MovieUser implements User {
    private final HashMap<Item, Integer> ratings;
    private final int id;
    private double ratingsSum;
    private double average;

    /**
     * @param userId the id
     */
    public MovieUser(int userId) {
        this.ratings = new HashMap<Item, Integer>();
        this.id = userId;
        this.ratingsSum = 0;
        this.average = 0;
    }

    @Override
    public void addRating(Item item, int rating) {
        ratingsSum += rating;
        ratings.put(item, new Integer(rating));
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
    public Set<Item> getRatedItems() {
        return ratings.keySet();
    }

    @Override
    public Integer getRating(Item item) {
        return ratings.get(item);
    }

    @Override
    public boolean hasRated(Item item) {
        return ratings.get(item) != null;
    }
}
