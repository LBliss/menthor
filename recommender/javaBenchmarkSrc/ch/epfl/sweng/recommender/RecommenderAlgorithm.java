package ch.epfl.sweng.recommender;

import java.util.Collection;
import java.util.SortedSet;

/**
 * Interface that defines a recommender algorithm.
 */
public interface RecommenderAlgorithm {

    /**
     * Provides a collection of users to the algorithm.
     * @param users list of unique users.
     */
    void setUsers(Collection<User> users);

    /**
     * Provides a collection of items to the algorithm.
     * @param items list of unique items.
     */
    void setItems(Collection<Item> items);

    /**
     * Computes a set of recommendations for a user. The <tt>update()</tt>
     * function must be called first in order to get the recommendations.
     * @param u the user for which recommendations are computed.
     * @return the set of recommendations, sorted by highest recommendations
     *         first.
     */
    SortedSet<Recommendation> computeRecommendations(User u);

    /**
     * Updates the internals of the algorithm.
     * Clients must call it after they add new users, new items, or new ratings.
     */
    void update();
}
