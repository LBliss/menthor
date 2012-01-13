package ch.epfl.sweng.recommender.movie;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import ch.epfl.sweng.recommender.Item;
import ch.epfl.sweng.recommender.Recommendation;
import ch.epfl.sweng.recommender.RecommenderAlgorithm;
import ch.epfl.sweng.recommender.RecommenderSystem;
import ch.epfl.sweng.recommender.User;

/**
 * The MovieRecommenderSystem.
 * 
 * @author olivier
 */
public class MovieRecommenderSystem implements RecommenderSystem {
    private static final String SEPARATOR = "::";
    private final Set<RecommenderAlgorithm> algorithms;
    private final HashMap<Integer, User> users;
    private final HashMap<Integer, Item> items;
    private static final int VALUES_PER_FILE_LINE = 3;

    /**
     * The constructor.
     */
    public MovieRecommenderSystem() {
        this.algorithms = new HashSet<RecommenderAlgorithm>();
        this.users = new HashMap<Integer, User>();
        this.items = new HashMap<Integer, Item>();
    }

    @Override
    public void addAlgorithm(RecommenderAlgorithm algorithm) {
        algorithms.add(algorithm);
    }

    @Override
    public Set<Recommendation> getRecommendations(User user, RecommenderAlgorithm algorithm) {
        return algorithms.contains(algorithm) ? algorithm.computeRecommendations(user) : new HashSet<Recommendation>();
    }

    @Override
    public User getUserById(int id) {
        return users.get(id);
    }

    public void readData(String ratingsFile) throws IOException {
        BufferedReader ratingsReader = new BufferedReader(new FileReader(ratingsFile));
        String line = ratingsReader.readLine();
        while (line != null) {
            if (!line.trim().isEmpty()) {
                String[] splited = line.split(",");
                if (splited.length != VALUES_PER_FILE_LINE) {
                    throw new IllegalArgumentException();
                }
                int userId = Integer.parseInt(splited[0]);
                int itemId = Integer.parseInt(splited[1]);
                int rating = Integer.parseInt(splited[2]);
                if (items.get(itemId) == null)
                    items.put(itemId, new Movie(itemId, "" + itemId, "" + itemId));
                Item item = items.get(itemId);

                if (users.get(userId) == null)
                    users.put(userId, new MovieUser(userId));
                User user = users.get(userId);
                user.addRating(item, rating);
                item.addUserRating(user, rating);
            }
            line = ratingsReader.readLine();
        }
    }

    @Override
    public boolean readData(String usersFile, String itemsFile, String ratingsFile) {
        BufferedReader usersReader = null;
        BufferedReader itemsReader = null;
        BufferedReader ratingsReader = null;
        int nLine = 1;
        String currentFile = usersFile;
        try {
            try {
                usersReader = new BufferedReader(new FileReader(usersFile));
                itemsReader = new BufferedReader(new FileReader(itemsFile));
                ratingsReader = new BufferedReader(new FileReader(ratingsFile));
                String line = usersReader.readLine();
                while (line != null) {
                    if (!line.trim().isEmpty()) {
                        String[] splited = line.split(SEPARATOR);
                        if (splited.length != 2) {
                            throw new IllegalArgumentException();
                        }
                        int id = Integer.parseInt(splited[0]);
                        // int age = Integer.parseInt(splited[1]);
                        if (users.get(id) != null) {
                            throw new IllegalArgumentException();
                        }
                        users.put(id, new MovieUser(id));
                    }
                    line = usersReader.readLine();
                    ++nLine;
                }

                line = itemsReader.readLine();
                nLine = 1;
                currentFile = itemsFile;
                while (line != null) {
                    if (!line.trim().isEmpty()) {
                        String[] splited = line.split(SEPARATOR);
                        String info = "";
                        if (splited.length != VALUES_PER_FILE_LINE && splited.length != 2) {
                            throw new IllegalArgumentException();
                        } else if (splited.length == VALUES_PER_FILE_LINE) {
                            info = splited[2];
                        }
                        int id = Integer.parseInt(splited[0]);
                        String title = splited[1];
                        if (items.get(id) != null) {
                            throw new IllegalArgumentException();
                        }
                        items.put(id, new Movie(id, title, info));
                    }
                    line = itemsReader.readLine();
                    ++nLine;
                }

                line = ratingsReader.readLine();
                nLine = 1;
                currentFile = ratingsFile;
                while (line != null) {
                    if (!line.trim().isEmpty()) {
                        String[] splited = line.split(SEPARATOR);
                        if (splited.length != VALUES_PER_FILE_LINE) {
                            throw new IllegalArgumentException();
                        }
                        int userId = Integer.parseInt(splited[0]);
                        int itemId = Integer.parseInt(splited[1]);
                        int rating = Integer.parseInt(splited[2]);
                        User user = users.get(userId);
                        Item item = items.get(itemId);
                        if (user == null || item == null || rating < 1|| user.hasRated(item)) {
                            throw new IllegalArgumentException();
                        }
                        user.addRating(item, rating);
                        item.addUserRating(user, rating);
                    }
                    line = ratingsReader.readLine();
                    ++nLine;
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Parse error in \"" + currentFile + "\" at line " + nLine + ".");
                return false;
            } catch (IOException e) {
                System.err.println("IOException while reading a file.");
                return false;
            }
            return true;
        } finally {
            for (BufferedReader bufferReader : new BufferedReader[] { usersReader, itemsReader, ratingsReader }) {
                try {
                    if (bufferReader != null) {
                        bufferReader.close();
                    }
                } catch (IOException e) {
                    System.err.println("IOException while closing a BufferedReader.");
                }
            }
        }
    }

    @Override
    public void update() {
        for (RecommenderAlgorithm algorithm : algorithms) {
            algorithm.setItems(items.values());
            algorithm.setUsers(users.values());
            algorithm.update();
        }
    }

    @Override
    public Collection<Item> getItems() {
        return items.values();
    }

    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public int getNextUserId() {
        int id = Math.abs(new Random().nextInt());
        while (users.get(id) != null) {
            if (id == Integer.MAX_VALUE) {
                id = 0;
            } else {
                ++id;
            }
        }
        return id;
    }
}
