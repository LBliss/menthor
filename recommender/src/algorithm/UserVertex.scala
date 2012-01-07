package algorithm

import menthor._

import scala.collection.mutable.HashMap

/**
 * @param userID unique identifier of the user.
 * @param ratings list of ratings.
 */
case class UserVertex(val userID: UserID, var ratings: List[(ItemID, Grade)]) extends Vertex[DataWrapper]("u" + userID, null) {

  def update(): Substep[DataWrapper] = {

    var favoriteItems: List[(ItemID, Grade)] = ratings

    {
      /*
       * Compute the mean of the ratings to normalize the grades.
       * Send the ratings and the mean to all ItemVertex concerned.
       */
      if (!DATA_SORTED) {
        ratings = ratings.sortWith(_._1 < _._1)
      }
      
      val mean = ratings.map(_._2).reduce(_ + _) / ratings.length.toDouble

      // The neighbors are all the ItemVertex that this user graded.
      for (neighbor <- neighbors.asInstanceOf[List[ItemVertex]].sortWith((x, y) => x.itemID < y.itemID)) yield {
        // Strategy: only send ratings to one item of each pair to prevent duplicate computation (criteria: itemID >= neighborID).
        // Note: we used this special splitting later.
        ratings = ratings.dropWhile(ratings => ratings._1 < neighbor.itemID)
        Message(this, neighbor, MeanAndRatings(mean, ratings))
      }
    } then {
      value = Similarities(new HashMap[ItemID, List[(ItemID, Similarity)]])
      /*
       * No message to send.
       */
      List()
    } crunch ((v1, v2) => {
      (v1, v2) match {
        case (Similarities(map1), Similarities(map2)) => Similarities(map1 ++ map2.map { case (k, v) => k -> (v ::: map1.getOrElse(k, List())) })
      }
    }) then {
      value = null
      /*
       * Compute the top K recommendations using the similarities reduced.
       */
      
      favoriteItems = favoriteItems.sortWith(_._2 > _._2).take(THRESHOLD_nFavoriteItems)
      
      val similaritiesMap = new HashMap[ItemID, List[Similarity]]
      val similaritiesMap2 = new HashMap[ItemID, List[Similarity]]
      
      incoming.head.value match {
      	case Similarities(similarities) =>
      	  for((itemID, grade) <- favoriteItems) {
            for((itemID, similarity) <- similarities.get(itemID).getOrElse(List())) {
              similaritiesMap.put(itemID, similarity * grade :: similaritiesMap.getOrElseUpdate(itemID, List()))
              similaritiesMap2.put(itemID, (if(similarity > 0) similarity else -similarity) :: similaritiesMap2.getOrElseUpdate(itemID, List()))
            }
      	  }
      }

      val recommendations: List[(ItemID, Similarity)] = (
        for (
          (itemID, similarities) <- similaritiesMap.toList if (similarities.size >= THRESHOLD_nSimilarRequired)
        ) yield (itemID, similarities.reduce(_ + _) / similaritiesMap2.get(itemID).get.reduce(_ + _)))

      val finalTopK = recommendations.sortWith(_._2 > _._2).take(TOP_K)//.map(_._1)

      if (finalTopK.isEmpty) {
        numberOfEmptyRecommandations += 1
      } else {
       // println("TOP K of " + userID + " is : " + finalTopK)
      }

      List() // No outgoing messages.
    }
  }
}