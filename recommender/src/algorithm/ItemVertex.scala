package algorithm

import menthor._
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

case class ItemVertex(itemID: ItemID) extends Vertex[DataWrapper]("Item #" + itemID, null) {
  // Minimum number of ratings to take the similarity into account. MUST BE BIGGER THAN 0.
  val THRESHOLD_nMinimumRatings = 5

  def update(): Substep[DataWrapper] = {
    {
      /*
       * Nothing to do and no message to send
       */
      List()
    } then {
      
      // First element of the 2-tuple in the list is the weighted rating, second is userID to send back the results.
      val gradesMap = new HashMap[ItemID, List[(User, WeightedGrade)]]
      val selfRatingMap = new HashMap[User, WeightedGrade]
      var interestedUsersMap = new HashMap[User, List[ItemID]]

      // Prepare the hashmaps with the data received via messages
      for (message <- incoming) {
        message.value match {
          case MeanAndRatings(mean, ratings, favoriteItems) => {
            // The first rating is the rate of this itself.
            val weightedRating = ratings.head._2 - mean
            val source = message.source
            selfRatingMap.put(source, weightedRating)

            // Take k the favorite items.
            interestedUsersMap.put(source, favoriteItems)

            // The other ratings are added 
            for ((itemID, grade) <- ratings.tail) {
              var entry = gradesMap.getOrElseUpdate(itemID, List())
              entry ::= (source, grade - mean)
              gradesMap.put(itemID, entry)
            }
          }
          case _ => sys.error("Internal error")
        }
      }

      /*
       * Magical formula :P
       * sum(r(i)*r(j) / sqrt(sum(r(i)^2) + sum(r(j)^2))
       */
      val similarities: List[(ItemID, Similarity, List[User])] = 
        (for ((otherItemID, ratings) <- gradesMap
          if (ratings.length >= THRESHOLD_nMinimumRatings))
          yield {
            var num = 0d
            var de1 = 0d
            var de2 = 0d
            val sources =
              for ((source, otherGrade) <- ratings) yield {
                val selfGrade = selfRatingMap.get(source).get
                num += (otherGrade * selfGrade)
                de1 += (otherGrade * otherGrade)
                de2 += (selfGrade * selfGrade)
                source // yield this !
              }
            val result = num / scala.math.sqrt(de1 + de2)
            (otherItemID, result, sources) // yield this !
          }
        ).toList

      val messagesMap = new HashMap[Vertex[DataWrapper], List[(Int, Double)]]
      for ((otherItemID, similarity, destinations) <- similarities) {
        for (destination <- destinations) {
          val favoriteItems = interestedUsersMap.get(destination).get

          // If the user (destination) is interested by the similarity, we send it with the value weighted (TODO: explain better)
          if (favoriteItems.contains(itemID)) {
            // The user is interested by items similar to this item
            messagesMap.put(destination, (otherItemID, similarity * selfRatingMap.get(destination).get) :: messagesMap.getOrElseUpdate(destination, List()))
          } else if (favoriteItems.contains(otherItemID)) {
            // The user is interested by this item
            messagesMap.put(destination, (itemID, similarity * selfRatingMap.get(destination).get) :: messagesMap.getOrElseUpdate(destination, List()))
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