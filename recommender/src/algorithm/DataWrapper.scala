package algorithm

/*
 * Wrapper used for the different data types we have to handle.
 */
abstract class DataWrapper
case class Ratings(list: List[(ItemID, Grade)]) extends DataWrapper
case class MeanAndRatings(mean: Double, ratings: List[(ItemID, Grade)], favoriteItems: List[ItemID]) extends DataWrapper
case class Similarities(list: List[(ItemID, Similarity)]) extends DataWrapper