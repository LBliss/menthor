package algorithm

import menthor._

import scala.collection.mutable.HashMap

/**
 * @param userID unique identifier of the user.
 * @param ratings list of ratings.
 */
case class UserVertex(val userID: UserID, var ratings: List[(ItemID, Grade)]) extends Vertex[DataWrapper]("u" + userID, null) {


  def update(): Substep[DataWrapper] = {

    var favoriteItems: List[(ItemID, Grade)] = null

    {
      favoriteItems = ratings.sortWith(_._2 > _._2).take(THRESHOLD_nFavoriteItems)
      if(userID == 1) 
        value = FavoriteMap(HashMap(favoriteItems.map(x => (x._1, List(this))) : _*))
      else
        value = FavoriteMap(new HashMap[ItemID, List[User]])
      List()
    } crunch ((v1, v2) => {
      (v1, v2) match {
        case (FavoriteMap(map1), FavoriteMap(map2)) => FavoriteMap(map1 ++ map2.map{ case (k,v) => k -> (v ::: map1.getOrElse(k,List()))})
        case _ => sys.error("Internal error")
        }
    }) then {
      /*
       * Compute the mean of the ratings to normalize the grades.
       * Also compute the favoriteItems list to reduce the amount of messages which are later send back by the ItemVertex.
       * Send the ratings, the mean and the favoriteItems list to all ItemVertex concerned.
       */
      if (!DATA_SORTED) {
        ratings = ratings.sortWith(_._1 < _._1)
      }

      val mean = ratings.map(_._2).reduce(_ + _) / ratings.length.toDouble

      // The neighbors are all the ItemVertex that this user graded.
      for (neighbor <- neighbors.asInstanceOf[List[ItemVertex]].sortWith((x, y) => x.itemID < y.itemID)) yield {
        // Optimization: Only send ratings to one item of each pair to prevent duplicate computation (criteria: itemID >= neighborID).
        // Note: We used this special splitting later on to easily get the grade of the item that received the list (the first rating)
        ratings = ratings.dropWhile(ratings => ratings._1 < neighbor.itemID)
        Message(this, neighbor, MeanAndRatings(mean, ratings))
      }
    } then {
      /*
       * Nothing to do and no message to send.
       */
      //value = Similarities(List())
      value = Similarities(new HashMap[ItemID, List[(ItemID, Similarity)]])
      
      List()
    } crunch ((v1, v2) => {
      (v1, v2) match {
        //case (Similarities(list1), Similarities(list2)) => Similarities(list1 ::: list2)
        case (Similarities(map1), Similarities(map2)) => Similarities(map1 ++ map2.map { case (k, v) => k -> (v ::: map1.getOrElse(k, List())) })
      }
    }) then {
      /*
       * Compute the top K recommendations using the weighted similarities that the ItemVertex send us back.
       */
      val similaritiesMap = new HashMap[ItemID, List[Similarity]]
//      val favoriteItemsMap = HashMap(favoriteItems: _*)

//      incoming.head.value match {
//        case Similarities(similarities) =>
//          for ((itemID1, itemID2, similarity) <- similarities) {
//            var grade: Option[Grade] = favoriteItemsMap.get(itemID1)
//            if (grade != None) {
//              similaritiesMap.put(itemID2, similarity * grade.get :: similaritiesMap.getOrElseUpdate(itemID2, List()))
//            } else {
//              grade = favoriteItemsMap.get(itemID2)
//              if (grade != None) {
//                similaritiesMap.put(itemID1, similarity * grade.get :: similaritiesMap.getOrElseUpdate(itemID1, List()))
//              }
//            }
//
//          }
//        case _ => sys.error("Internal error")
//      }
      incoming.head.value match {
      	case Similarities(similarities) =>
      	  for((itemID, grade) <- favoriteItems) {
      	    for((itemID, similarity) <- similarities.get(itemID).getOrElse(List())) {
      	      similaritiesMap.put(itemID, similarity * grade :: similaritiesMap.getOrElseUpdate(itemID, List()))
      	    }
      	  }
      }

      val similarities: List[(ItemID, Similarity)] =
        for (
          (itemID, similarity) <- similaritiesMap.toList if (similarity.size >= THRESHOLD_nSimilarRequired)
        ) yield (itemID, similarity.reduce(_ + _) / similarity.size)

      val finalTopK = similarities.sortWith(_._2 > _._2).take(TOP_K).map(_._1)

      if (finalTopK.isEmpty) {
        numberOfEmptyRecommandations += 1
      } else {
        if (userID % 99 == 0) // DEBUG
          println("TOP K of " + userID + " is : " + finalTopK)
      }

      List() // No outgoing messages.
    }
  }
}