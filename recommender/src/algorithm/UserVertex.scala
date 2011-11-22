package algorithm

import menthor._
import scala.collection.mutable.HashMap

/**
 * SORTED LIST OF RATINGS
 */
case class UserVertex(userID: Int, ratings: DataWrapper) extends Vertex[DataWrapper]("User #" + userID, ratings) {
  def update(): Substep[DataWrapper] = {
    {
      /*
       * Compute the mean of the ratings to normalize the grades.
       * Send the ratings and the mean to all ItemVertex concerned.
       */
      // Value contains the list of 2-tuples (itemID, rating).
      value match {
        case Ratings(ratings) =>
          // Take k the favorite items.
          val THRESHOLD = 10
          val favoriteItems = ratings.sort(_._2 > _._2).take(THRESHOLD).map(_._1)

          val mean = ratings.map(_._2).reduce(_ + _) / ratings.length.toDouble
          
          // The neighbors are all the itemVertex that this user rated.
          neighbors match {
            case neighbors: List[ItemVertex] => {
              var partialRatings = ratings
              for (neighbor <- neighbors) yield {
                val neighborID = neighbor.itemID
                // Optimization: Only send ratings to one item of each pair to prevent duplicate computation (criteria: itemID => neighborID).
                partialRatings = partialRatings.dropWhile(ratings => ratings._1 < neighborID)
                Message(this, neighbor, MeanAndRatings(mean, partialRatings, favoriteItems))
              }
            }
            case _ => sys.error("Internal error")
          }
        case _ => sys.error("Internal error")
      }
    } then {
      /*
       * Nothing to do and no message to send
       */
      List()
    } then {
      val similaritiesMap = new HashMap[Int, List[Double]]
      for (message <- incoming) {
        message.value match {
          case Similarities(similarities) => {
            for (similarity <- similarities) {
              similaritiesMap.put(similarity._1, similarity._2 :: similaritiesMap.getOrElseUpdate(similarity._1, List()))                        
            }
          }
          case _ => sys.error("Internal error")
        }
      }
      var similarities: List[(Int, Double)] = List()
      for (similarity <- similaritiesMap) {
        // Number of different top-k item that the item must be similar to.
        val THRESHOLD = 1
        if (similarity._2.size >= THRESHOLD) {
          similarities ::= (similarity._1, similarity._2.reduce(_ + _)/similarity._2.size)
        }
      }
      // TOP K
      val K = 10
      val finalTopK = similarities.sort(_._2 > _._2).take(K).map(_._1)
      println("TOP K of " + userID + " is :" + finalTopK)
      List() // No outgoing messages
    }
  }
}