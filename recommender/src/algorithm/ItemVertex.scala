package algorithm

import menthor._
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

case class ItemVertex(itemID: ItemID) extends Vertex[DataWrapper]("i" + itemID, null) {

  def update(): Substep[DataWrapper] = {
    {
      /*
       * Initialize "value" for the crunch and no message to send.
       */
      value = FavoriteMap(new HashMap[ItemID, List[User]])
      List()
    } crunch ((v1, v2) => {
      (v1, v2) match {
        case (FavoriteMap(map1), FavoriteMap(map2)) => FavoriteMap(map1 ++ map2.map { case (k, v) => k -> (v ::: map1.getOrElse(k, List())) })
      }
    }) then {
      /*
       * Store the "FavoriteItemsMap" in "value" and no message to send.
       */
      value = incoming.head.value
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

      /*
       *  Prepare the hashmaps with the data received via messages.
       */

      for (message <- incoming) {
        message.value match {
          case MeanAndRatings(mean, ratings) => {
            val source = message.source
            // The first rating is the grade of this item because of the way we split the ratings.
            val weightedGrade = ratings.head._2 - mean
            selfGradesMap.put(source, weightedGrade)

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
      val similarities: List[(ItemID, Similarity)] = (
        for (
          (otherItemID, ratings) <- otherGradesMap if (ratings.length >= THRESHOLD_nMinimumRatings && {
            var num = 0d
            var de1 = 0d
            var de2 = 0d
            for ((source, otherGrade) <- ratings) {
              val selfGrade = selfGradesMap.get(source).get
              num += (otherGrade * selfGrade)
              de1 += (otherGrade * otherGrade)
              de2 += (selfGrade * selfGrade)
            }
            result = num / scala.math.sqrt(de1 + de2)
            result > THRESHOLD_minimalSimilarity
          })
        ) yield (otherItemID, result)).toList

      // println("Similarity between: " + itemID + " and " + otherItemID + " is: " + result) // DEBUG
      time_itemSubstepSimilarity += stopTimer // DEBUG
      startTimer // DEBUG

      /*
       * We prepare the messages for each user.
       * Important: As we compute later a weighted average, we weight the similarities right here to simplify things later.
       */
      val messagesMap = new HashMap[User, List[(ItemID, ItemID, Similarity)]]
      val favoriteItemsMap = value.asInstanceOf[FavoriteMap].map
      val selfFavoriteUserSet = favoriteItemsMap.get(itemID).get
      value = null
      
      for ((otherItemID, similarity) <- similarities) {
        // The user is interested by items similar to this item.
        for (destination <- selfFavoriteUserSet) {
          messagesMap.put(destination, (itemID, otherItemID, similarity) :: messagesMap.getOrElseUpdate(destination, List()))
        }
        // The user is interested by this item.
        for (destination <- favoriteItemsMap.get(otherItemID).get) {
          messagesMap.put(destination, (otherItemID, itemID, similarity) :: messagesMap.getOrElseUpdate(destination, List()))
        }
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