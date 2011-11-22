package algorithm

import menthor._
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

case class ItemVertex(itemID: Int) extends Vertex[DataWrapper]("Item #" + itemID, null) {
  def update(): Substep[DataWrapper] = {
    {
      /*
       * Nothing to do and no message to send
       */
      List()
    } then {
      // First element of the 2-tuple in the list is the weighted rating, second is userID to send back the results.
      val similarityMap = new HashMap[Int, (List[(Double, Vertex[DataWrapper])])]
      val selfRatingMap = new HashMap[Vertex[DataWrapper], Double]
      var interestedUsersMap = new HashMap[Vertex[DataWrapper], List[Int]]
      
      // Prepare the hashmaps with the data received via messages
      for (message <- incoming) {
        message.value match {
          case MeanAndRatings(mean, ratings, favoriteItems) => {
            // The first rating is the rate of this itself.
            val weightedRating = ratings.head._2 - mean
            val user = message.source
            selfRatingMap.put(user, weightedRating)
            
            // Take k the favorite items.
            interestedUsersMap.put(user, favoriteItems)

            // The other ratings are added 
            for (rating <- ratings.tail) {
              val weightedRating = rating._2 - mean
              val itemID = rating._1
              var entry = similarityMap.getOrElseUpdate(itemID, List())
              entry ::= (weightedRating, message.source)
              similarityMap.put(itemID, entry)
            }
          }
          case _ => sys.error("Internal error")
        }
      }

      /*
       * Magical formula :P
       * sum(r(i)*r(j) / sqrt(sum(r(i)^2) + sum(r(j)^2))
       */
      var similarities: List[(Int, Double, List[Vertex[DataWrapper]])] = List()
          
      for (similarity <- similarityMap) {
        val ratings = similarity._2
        var num = 0d
        var de1 = 0d
        var de2 = 0d
        for (rating <- ratings) {
          val otherRating = rating._1
          val selfRating = selfRatingMap.get(rating._2).get
          num += (otherRating * selfRating)
          de1 += (otherRating * otherRating)
          de2 += (selfRating * selfRating)
        }
        // Minimum number of ratings to take the similarity into account. MUST BE BIGGER THAN 0.
        val THRESHOLD = 5
        if (ratings.length >= THRESHOLD) {
          val destinations = ratings.map(_._2)
          val result = num / scala.math.sqrt(de1 + de2)
          val otherItemID = similarity._1
          similarities ::= (otherItemID, result, destinations)
//          for (destination <- destinations) {
//            /*
//             * Trolol j'avais raison
//             */
//            val weightedSimilarity = 
//            messagesMap.put(destination, (itemID, otherItemID, result) :: messagesMap.getOrElseUpdate(destination, List()))              
//          }
        }
      }

      val messagesMap = new HashMap[Vertex[DataWrapper], List[(Int, Double)]]
      for(similarity <- similarities) {
        for(destination <- similarity._3) {
          val favoriteItems = interestedUsersMap.get(destination).get
          
          // If the user (destination) is interested by the similarity, we send it with the value weighted (TODO: explain better)
          if(favoriteItems.contains(itemID)) {
            // The user is interested by items similar to this item
            messagesMap.put(destination, (similarity._1, similarity._2 * selfRatingMap.get(destination).get) :: messagesMap.getOrElseUpdate(destination, List()))            
          } else if(favoriteItems.contains(similarity._1)) {
            // The user is interested by this item
            messagesMap.put(destination, (itemID, similarity._2 * selfRatingMap.get(destination).get) :: messagesMap.getOrElseUpdate(destination, List()))            
          }
        }
      }
      /*
       * Send messages back with the similarity computed.
       */
      (for (entry <- messagesMap) yield Message(this, entry._1, Similarities(entry._2))).toList
    } then {
      /*
       * Nothing to do and no message to send
       */
      List()
    }
  }
}