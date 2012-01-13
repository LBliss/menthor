package ch.epfl.sweng.recommender.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.epfl.sweng.recommender.Item;
import ch.epfl.sweng.recommender.Recommendation;
import ch.epfl.sweng.recommender.RecommenderAlgorithm;
import ch.epfl.sweng.recommender.User;

/**
 * The ItemSimilarityAlgorithm.
 * 
 * @author olivier
 */
public class ItemSimilarityAlgorithm implements RecommenderAlgorithm {
    private Collection<Item> items;
    private Collection<User> users;
    private HashMap<Item, List<Pair<Item, Double>>> similarities;

    /**
     * The Default constructor.
     */
    public ItemSimilarityAlgorithm() {
        items = new ArrayList<Item>();
        users = new ArrayList<User>();
        similarities = new HashMap<Item, List<Pair<Item, Double>>>();
    }

    @Override
    public SortedSet<Recommendation> computeRecommendations(User u) {
        SortedSet<Recommendation> returnSet = new TreeSet<Recommendation>();
        if (users.contains(u)) {
            for (Item item : items) {
                if (!u.getRatedItems().contains(item)) {
                    List<Pair<Item, Double>> similarItems = similarities.get(item);
                    double numerator = 0;
                    double denominator = 0;
                    for (Pair<Item, Double> pair : similarItems) {
                        Item otherItem = pair.first();
                        double similarity = pair.second();
                        if (u.hasRated(otherItem)) { // new line
                            numerator += similarity * (u.hasRated(otherItem) ? u.getRating(otherItem) : 0);
                            denominator += Math.abs(similarity);
                        }
                    }
                    double prediction = numerator / denominator;
                        returnSet.add(new Recommendation(item, prediction));
                }
            }
        }
        return returnSet;
    }

    @Override
    public void setItems(Collection<Item> newItems) {
        this.items = newItems;
    }

    @Override
    public void setUsers(Collection<User> newUsers) {
        this.users = newUsers;
    }

    @Override
    public void update() {
        similarities = new HashMap<Item, List<Pair<Item, Double>>>();
        Item[] itemsArray = items.toArray(new Item[0]);
        for (Item item : itemsArray) {
            similarities.put(item, new ArrayList<Pair<Item, Double>>());
        }
        for (int i = 0; i < itemsArray.length; i++) {
            Item aItem = itemsArray[i];
            for (int j = i + 1; j < itemsArray.length; j++) {
                Item anotherItem = itemsArray[j];
                double numerator = 0;
                double firstHalfDenominator = 0;
                double secondHalfDenominator = 0;
                boolean uIsEmpty = true;
                for (User user : aItem.getRatingUsers()) {
                    if (user.hasRated(anotherItem)) {
                        uIsEmpty = false;
                        double aItemWeightedRating = user.getRating(aItem) - user.getAverageRating(); // changed from aItem.getAverage to user.getAvergae
                        double anotherItemWeightedRating = user.getRating(anotherItem) - user.getAverageRating();
                        numerator += aItemWeightedRating * anotherItemWeightedRating;
                        firstHalfDenominator += aItemWeightedRating * aItemWeightedRating;
                        secondHalfDenominator += anotherItemWeightedRating * anotherItemWeightedRating;
                    }
                }
                if (!uIsEmpty) {
                    double denominator = Math.sqrt(firstHalfDenominator) * Math.sqrt(secondHalfDenominator);
                    double similarity = denominator == 0 ? 1 : numerator / denominator;
                    similarities.get(aItem).add(new Pair<Item, Double>(anotherItem, similarity));
                    similarities.get(anotherItem).add(new Pair<Item, Double>(aItem, similarity));
                }
            }
        }
    }
}
