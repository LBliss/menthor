package algorithm

/*
 * Wrapper used for the different data types we have to handle.
 */
abstract class DataWrapper
case class Ratings(list: List[(Int, Int)]) extends DataWrapper
case class MeanAndRatings(mean: Double, ratings: List[(Int, Int)], favoriteItems: List[Int]) extends DataWrapper
case class Similarities(list: List[(Int, Double)]) extends DataWrapper