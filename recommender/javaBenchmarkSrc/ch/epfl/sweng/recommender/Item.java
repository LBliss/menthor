package ch.epfl.sweng.recommender;

import java.util.Set;

/**
 * Interface for the items of the recommender system.
 */
public interface Item {

    /**
     * Add rating for a user.
     * @param user the user.
     * @param rating the rating.
     */
    void addUserRating(User user, int rating);

    /**
     * Get the item ID.
     * @return the item ID.
     */
    int getId();

    /**
     * Get the users that rated this item.
     * @return the set of users.
     */
    Set<User> getRatingUsers();

    /**
     * Get average rating over all users.
     * @return the average rating as a double.
     */
    double getAverageRating();

    /**
     * Get the rating of a specific user.
     * @param user a user that rated the item.
     * @return the rating if the user rated the item or <tt>null</tt> otherwise.
     */
    Integer getRating(User user);
}
