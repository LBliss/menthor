package ch.epfl.sweng.recommender;

import java.util.Set;

/**
 * Interface defining a user of the recommender system.
 */

public interface User {
    /**
     * Get the ID of the user.
     * @return the user ID.
     */
    int getId();

    /**
     * Add a rating made by the user.
     * @param item the rated item.
     * @param rating the rating.
     */
    void addRating(Item item, int rating);

    /**
     * Check if a user rated an item.
     * @param item the item.
     * @return <tt>true</tt> if the item has been rated,
     *         <tt>false</tt> otherwise.
     */
    boolean hasRated(Item item);

    /**
     * Get the rating of a given item.
     * @param item the item.
     * @return the rating for that item,
     *         or <tt>null</tt> if the item has not been rated.
     */
    Integer getRating(Item item);

    /**
     * Get the set of rated items.
     * @return the set of rated items.
     */
    Set<Item> getRatedItems();

    /**
     * Get the average rating of this user over all rated items.
     * @return the average rating.
     */
    double getAverageRating();
}
