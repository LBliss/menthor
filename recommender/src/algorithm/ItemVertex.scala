package algorithm

import menthor._

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

case class ItemVertex(itemID: ItemID) extends Vertex[DataWrapper]("i" + itemID, null) {

  def update(): Substep[DataWrapper] = {
    {
      /*
       * Nothing to do and no message to send.
       */
      List()
    } then {
      val tmp = System.currentTimeMillis // Time measurement.

      /*
       * The most important substep, two phases:
       *  - First, we do some data handling (mostly filling hashmaps with data from the messages we received from the UserVertex).
       *  - Then, we compute the pairwise similarity between this item and all the other that have an ID greater (because of the way we split the ratings).
       *    and we store them in this.value in order to use them later in a crunch (reduce).
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
//      time_itemSubstepData += stopTimer // Time measurement.
//      startTimer // Time measurement.

      /*
       * Formula used to compute the pairwise similarity.
       * sum(r(i)*r(j) / sqrt(sum(r(i)^2) + sum(r(j)^2))
       */
      var result: Double = 0d
      val similarities: List[(ItemID, Double)] = ((
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
            result = num / (math.sqrt(de1) * math.sqrt(de2))
            result > THRESHOLD_minimalSimilarity
          })
        ) yield (otherItemID, result)).toList)

      // println("Similarity between: " + itemID + " and " + otherItemID + " is: " + result) // DEBUG

      val map = new HashMap[ItemID, List[(ItemID, Similarity)]]

      for ((otherItemID, sim) <- similarities) {
        map.put(itemID, (otherItemID, sim) :: map.get(itemID).getOrElse(List()))
        map.put(otherItemID, (itemID, sim) :: map.get(otherItemID).getOrElse(List()))
      }

      /*
       * We store the similarities.
       */
      value = Similarities(map)

      time_itemSubstepSimilarity += (System.currentTimeMillis - tmp) // Time measurement.
//      
//      count += 1 // Progress measurement.
//      progression += (numberOfItems - itemID) // Progress measurement.
//      println((progression / (numberOfItems * (numberOfItems + 1)) * 200).round + " % - " + count + " of " + numberOfItems + " done. ItemID " + itemID + ".") // Progress measurement.

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
       * No message to send.
       */
      List()
    }
  }
}