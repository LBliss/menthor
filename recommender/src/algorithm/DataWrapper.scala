package algorithm

import scala.collection.mutable.HashMap

/*
 * Wrapper used for the different data types we have to handle.
 */
abstract class DataWrapper
case class Ratings(list: List[(ItemID, Grade)]) extends DataWrapper
case class MeanAndRatings(mean: Double, ratings: List[(ItemID, Grade)]) extends DataWrapper
case class FavoriteMap(map: HashMap[ItemID, List[User]]) extends DataWrapper
//case class Similarities(list: List[(ItemID, ItemID, Similarity)]) extends DataWrapper
case class Similarities(map: HashMap[ItemID, List[(ItemID, Similarity)]]) extends DataWrapper