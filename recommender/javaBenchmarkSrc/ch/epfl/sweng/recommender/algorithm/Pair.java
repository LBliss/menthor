package ch.epfl.sweng.recommender.algorithm;

/**
 * Generic 2Tuple.
 * @author olivier
 *
 * @param <T1>
 * @param <T2>
 */
public class Pair<T1, T2> {
    private final T1 f;
    private final T2 s;

    /**
     * @param first f
     * @param second s
     */
    public Pair(T1 first, T2 second) {
        this.f = first;
        this.s = second;
    }

    /**
     * @return the first element of the fair
     */
    public T1 first() {
        return f;
    }
    /**
     * @return the second element of the fair
     */

    public T2 second() {
        return s;
    }
}
