package algorithm

import menthor._
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

case class ItemVertex(itemID: ItemID) extends Vertex[DataWrapper]("Item #" + itemID, null) {

  def update(): Substep[DataWrapper] = {
    {
      /*
       * Nothing to do and no message to send.
       */
      List()
    } then {
      startTimer // DEBUG
      /*
       * The big substep, three phases:
       *  - First, we do some data handling (mostly filling hashmaps with data from the messages we received from the UserVertex).
       *  - Then, we compute the pairwise similarity between this item and all the other that have an ID greater (because of the way we split the ratings).
       *  - Finally, we prepare the messages for each user using their favoriteItems list to decide if they're interested by a specific similarity.
       */
      val otherGradesMap = new HashMap[ItemID, List[(User, WeightedGrade)]]
      val selfGradesMap = new HashMap[User, WeightedGrade]
      val favoriteItemsMap = new HashMap[User, List[ItemID]]
      val selfFavoriteUserSet = new HashSet[User]

      /*
       *  Prepare the hashmaps with the data received via messages.
       */
      for (message <- incoming) {
        message.value match {
          case MeanAndRatings(mean, ratings, favoriteItems) => {
            val source = message.source
            // The first rating is the grade of this item because of the way we split the ratings.
            val weightedGrade = ratings.head._2 - mean
            selfGradesMap.put(source, weightedGrade)

            favoriteItemsMap.put(source, favoriteItems)
            if(favoriteItems.contains(itemID)) {
              selfFavoriteUserSet.add(source)
            }

            // The other ratings are added to the otherGradesMap.
            for ((itemID, grade) <- ratings.tail) {
              var entry = otherGradesMap.getOrElseUpdate(itemID, List())
              entry ::= (source, grade - mean)
              otherGradesMap.put(itemID, entry)
            }
          }
          case _ => sys.error("Internal error")
        }
      }
      time_itemSubstepData += stopTimer // DEBUG
      startTimer // DEBUG
      /*
       * Magical formula to compute the pairwise similarity :P
       * sum(r(i)*r(j) / sqrt(sum(r(i)^2) + sum(r(j)^2))
       */
      var result: Double = 0d
      var sources: List[(User, WeightedGrade)] = List()
      val similarities: List[(ItemID, Similarity, List[(User, WeightedGrade)])] =
        (for (
          (otherItemID, ratings) <- otherGradesMap if (ratings.length >= THRESHOLD_nMinimumRatings && {
            var num = 0d
            var de1 = 0d
            var de2 = 0d
            sources =
              for (
                (source, otherGrade) <- ratings if {
                  val selfGrade = selfGradesMap.get(source).get
                  num += (otherGrade * selfGrade)
                  de1 += (otherGrade * otherGrade)
                  de2 += (selfGrade * selfGrade)
                  favoriteItemsMap.get(source).get.contains(otherItemID)
                }
              ) yield {
                (source, otherGrade) // yield this !
              }
            result = num / scala.math.sqrt(de1 + de2)
            result > THRESHOLD_minimalSimilarity
          })
        ) yield {
          // println("Similarity between: " + itemID + " and " + otherItemID + " is: " + result) // DEBUG
          (otherItemID, result, sources) // yield this !
        }).toList
      time_itemSubstepSimilarity += stopTimer // DEBUG
      startTimer // DEBUG
      /*
       * We prepare the messages for each user.
       * Important: As we compute later a weighted average, we weight the similarities right here to simplify things later.
       */
      val messagesMap = new HashMap[User, List[(ItemID, Similarity)]]
      for ((otherItemID, similarity, destinations) <- similarities) {
        if(itemID == 28 || otherItemID == 28) {
          println("Similarity between: " + itemID + " and " + otherItemID + " is: " + similarity) // DEBUG
        }
    	  for(destination <- selfFavoriteUserSet) {
    	    count2 += 1 // DEBUG
    	    messagesMap.put(destination, (otherItemID, similarity * selfGradesMap.get(destination).get) :: messagesMap.getOrElseUpdate(destination, List()))
    	  }
    	  for((destination, otherItemGrade) <- destinations) {
    	    count3 += 1 // DEBUG
    	    messagesMap.put(destination, (itemID, similarity * otherItemGrade) :: messagesMap.getOrElseUpdate(destination, List()))
    	  }
//        val favoriteItems = favoriteItemsMap.get(destination).get
//        // If the user (destination) is interested by this specific similarity, we add it to its message.
//        if (favoriteItems.contains(itemID)) {
//          // The user is interested by items similar to this item.
//          // println(similarity + " :: " + selfGradesMap.get(destination).get + " :: " + itemID + " :: " + destination.asInstanceOf[UserVertex].userID + " :: " + favoriteItems) // DEBUG
//          messagesMap.put(destination, (otherItemID, similarity * selfGradesMap.get(destination).get) :: messagesMap.getOrElseUpdate(destination, List()))
//        } else if (favoriteItems.contains(otherItemID)) {
//          // The user is interested by this item.
//          messagesMap.put(destination, (itemID, similarity * otherItemGrade) :: messagesMap.getOrElseUpdate(destination, List()))
//        }
        
      }
      time_itemSubstepMessages += stopTimer // DEBUG
      count += 1 // DEBUG
      progression += (numberOfItems - itemID) // DEBUG
      println((progression / (numberOfItems * (numberOfItems + 1)) * 200).round + " % - " + count + " of " + numberOfItems + " done. ItemID " + itemID + ".") // DEBUG
      
      /*
       * Send the messages.
       */
      (for ((destination, similarities) <- messagesMap) yield Message(this, destination, Similarities(similarities))).toList
    } then {
      /*
       * Nothing to do and no message to send.
       */
      List()
    }
  }
}