package algorithm

import menthor._
import scala.collection.mutable.HashMap

/**
 * @param userID unique identifier of the user.
 * @param ratings sorted list of ratings.
 */
case class UserVertex(userID: UserID, ratings: DataWrapper) extends Vertex[DataWrapper]("User #" + userID, ratings) {
  // Number of items in each user that will be considered to find the recommended items.
  val THRESHOLD_nFavoriteItems = 10
  // Number of item in the favoriteItems list that a recommended item must be similar to.
  val THRESHOLD_nSimilarRequired = 1
  // Number of recommended item that we want
  val TOP_K = 10
  
  def update(): Substep[DataWrapper] = {
    {
      /*
       * Compute the mean of the ratings to normalize the grades.
       * Send the ratings and the mean to all ItemVertex concerned.
       */
      // Ratings contains the list of 2-tuples (itemID, grade).
      ratings match {
        case Ratings(ratings) =>
          
          val favoriteItems = ratings.sort(_._2 > _._2).take(THRESHOLD_nFavoriteItems).map(_._1)
          val mean = ratings.map(_._2).reduce(_ + _) / ratings.length.toDouble
          
          // The neighbors are all the itemVertex that this user rated.
          neighbors match {
            case neighbors: List[ItemVertex] =>
              var partialRatings = ratings
              for (neighbor <- neighbors) yield {
                // Optimization: Only send ratings to one item of each pair to prevent duplicate computation (criteria: itemID => neighborID).
                partialRatings = partialRatings.dropWhile(ratings => ratings._1 < neighbor.itemID)
                Message(this, neighbor, MeanAndRatings(mean, partialRatings, favoriteItems))
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
      
      val similaritiesMap = new HashMap[ItemID, List[Similarity]]
      for (message <- incoming) {
        message.value match {
          case Similarities(similarities) =>
            for ((itemID, similarity) <- similarities) {
              similaritiesMap.put(itemID, similarity :: similaritiesMap.getOrElseUpdate(itemID, List()))                        
            }
          case _ => sys.error("Internal error")
        }
      }
      
      val similarities: List[(ItemID, Similarity)] =
        for ((itemID, similarity) <- similaritiesMap.toList
          if (similarity.size >= THRESHOLD_nSimilarRequired))
          yield (itemID, similarity.reduce(_ + _) / similarity.size)

      val finalTopK = similarities.sort(_._2 > _._2).take(TOP_K).map(_._1)
      
      println("TOP K of " + userID + " is :" + finalTopK)
      
      List() // No outgoing messages
    }
    
  }
}