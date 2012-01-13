package ch.epfl.sweng.recommender;

/**
 * Class that represents a recommendation given to a user.
 */

public class Recommendation implements Comparable<Recommendation> {

    /** The recommended item. */
    private Item item;

    /** The associated rating. */
    private double rating;

    /**
     * Construct a new recommendation for a given item, with a given rating.
     * 
     * @param itemToRecommend
     *            the recommended item
     * @param theRating
     *            the rating of this recommendation.
     */
    public Recommendation(Item itemToRecommend, double theRating) {
        this.item = itemToRecommend;
        this.rating = theRating;
    }

    /**
     * Get the recommended rating.
     * 
     * @return the suggested rating value
     */
    public double getRating() {
        return rating;
    }

    /**
     * Get the item for which this recommendation is made.
     * 
     * @return the item of this recommendation.
     */
    public Item getItem() {
        return item;
    }

    /**
     * String representation of this rating.
     * 
     * @return the string containing the item id and the rating.
     */
    @Override
    public String toString() {
        return item.getId() + " " + rating;
    }

    /**
     * Reverse comparison for recommendations (highest rating first). This is the order in which recommendations will be displayed to users.
     * 
     * @param rec
     *            the recommendation to compare with.
     * @return 0 - if the ratings are identical<br/>
     *         1 - if rec.rating is larger than this.rating<br/>
     *         -1 - if rec.rating is less than this.rating<br/>
     */
    @Override
    public int compareTo(Recommendation rec) {
        // inverse comparison (note the reverse order of the parameters).
        int compareResult = Double.compare(rec.rating, rating);
        // equal ratings, order based on item id
        if (compareResult != 0) {
            return compareResult;
        } else {
            Item otherItem = rec.item;
            if (item.getId() < otherItem.getId()) {
                return -1;
            } else {
                if (item.getId() > otherItem.getId()) {
                    return 1;
                } else {
                    // equal rating and item IDs => they are equal
                    return 0;
                }
            }
        }
    }

    /**
     * Implementation of equals() that is consistent with that of compareTo(). It is always good practice to implement both compareTo() and equals() when using the class in a Set or SortedSet.
     * 
     * @param rec
     *            the recommendation to compare against.
     * @return true if the recommendations are equal, false otherwise.
     */
    public boolean equals(Recommendation rec) {
        boolean ratingsAreEqual = rating == rec.rating;
        if (!ratingsAreEqual) {
            return false;
        } else {
            boolean itemIdsAreEqual = rec.getItem().getId() == item.getId();
            return itemIdsAreEqual;
        }
    }

    /**
     * Hash code implementation that is consistent with equals.
     * 
     * @return the hash code of the string representation.
     */
    @Override
    public int hashCode() {
        String representation = rating + " " + item.getId();
        return representation.hashCode();
    }
}
