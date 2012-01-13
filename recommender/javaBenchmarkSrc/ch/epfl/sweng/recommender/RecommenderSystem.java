package ch.epfl.sweng.recommender;

import java.util.Collection;
import java.util.Set;

/**
 * Interface for a recommender system.
 */
public interface RecommenderSystem {

    /**
     * Gets a set of recommendations for a user using a given algorithm.
     * 
     * @param user
     *            the user for which recommendations are provided.
     * @param algorithm
     *            the algorithm used to generate recommendations.
     * @return the set of recommendations for the specified user.
     */

    Set<Recommendation> getRecommendations(User user, RecommenderAlgorithm algorithm);

    /**
     * Reads the data from the provided input files. It can be called multiple times, to add data from multiple input files.
     * 
     * @param usersFile
     *            path to the users data file.
     * @param itemsFile
     *            path to the items data file.
     * @param ratingsFile
     *            path to the ratings data file.
     * @return <tt>true</tt> if success, <tt>false</tt> otherwise.
     */
    boolean readData(String usersFile, String itemsFile, String ratingsFile);

    /**
     * Update the recommender system after new data has been added. It should be called after reading all the required data. It should update the internals of the algorithms that are added to this recommender system.
     */
    void update();

    /**
     * Get the user with the specified ID. Mostly for the ability to easily test.
     * 
     * @param id
     *            the ID of the user.
     * @return The <tt>User</tt> object matching the ID or null if the user does not exist.
     */
    User getUserById(int id);

    /**
     * Add a new algorithm that can be used to compute recommendations. Multiple algorithms can exist simultaneously.
     * 
     * @param algorithm
     *            a recommender algorithm.
     */
    void addAlgorithm(RecommenderAlgorithm algorithm);

    /**
     * @return the next available user ID.
     */
    int getNextUserId();

    /**
     * @return all the items in the system.
     */
    Collection<Item> getItems();
}
