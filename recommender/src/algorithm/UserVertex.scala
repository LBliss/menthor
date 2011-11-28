package algorithm

import menthor._
import scala.collection.mutable.HashMap

/**
 * @param userID unique identifier of the user.
 * @param ratings list of ratings.
 */
case class UserVertex(userID: UserID, ratings: DataWrapper) extends Vertex[DataWrapper]("User #" + userID, ratings) {

  def update(): Substep[DataWrapper] = {
    {
      /*
       * Compute the mean of the ratings to normalize the grades.
       * Also compute the favoriteItems list to reduce the amount of messages which are later send back by the ItemVertex.
       * Send the ratings, the mean and the favoriteItems list to all ItemVertex concerned.
       */
      ratings match {
        case Ratings(data) =>
          val ratings =
            if (DATA_SORTED) {
              data
            } else {
              data.sortWith(_._1 < _._1)
            }
          
          val favoriteItems = ratings.sortWith(_._2 > _._2).take(THRESHOLD_nFavoriteItems).map(_._1) // TODO: Should we take the max n times instead?
          
          val mean = ratings.map(_._2).reduce(_ + _) / ratings.length.toDouble

          var partialRatings = ratings
          // The neighbors are all the ItemVertex that this user graded.
          for (neighbor <- neighbors.asInstanceOf[List[ItemVertex]].sortWith((x, y) => x.itemID < y.itemID)) yield {
            // Optimization: Only send ratings to one item of each pair to prevent duplicate computation (criteria: itemID >= neighborID).
            // Note: We used this special splitting later on to easily get the grade of the item that received the list (the first rating)
            partialRatings = partialRatings.dropWhile(ratings => ratings._1 < neighbor.itemID)
            Message(this, neighbor, MeanAndRatings(mean, partialRatings, favoriteItems))
          }
        case _ => sys.error("Internal error")
      }
    } then {
      /*
       * Nothing to do and no message to send.
       */
      List()
    } then {
      /*
       * Compute the top K recommendations using the weighted similarities that the ItemVertex send us back.
       */
      val similaritiesMap = new HashMap[ItemID, List[Similarity]]
      for (message <- incoming) {
        message.value match {
          case Similarities(similarities) =>
            for ((itemID, similarity) <- similarities) {
              if(userID == 1204) println(itemID + ":::" + similarity)
              similaritiesMap.put(itemID, similarity :: similaritiesMap.getOrElseUpdate(itemID, List()))
            }
          case _ => sys.error("Internal error")
        }
      }

      val similarities: List[(ItemID, Similarity)] =
        for (
          (itemID, similarity) <- similaritiesMap.toList if (similarity.size >= THRESHOLD_nSimilarRequired)
        ) yield (itemID, similarity.reduce(_ + _) / similarity.size)

      val finalTopK = similarities.sortWith(_._2 > _._2).take(TOP_K).map(_._1)

      if(finalTopK.isEmpty) {
        numberOfEmptyRecommandations += 1
      } else {
    	 if(userID == 1204) println("TOP K of " + userID + " is : " + finalTopK)        
      }

      List() // No outgoing messages.
    }
  }
}