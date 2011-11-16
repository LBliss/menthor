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
      /*
	     * 
	     */
      if(Math.random > 0.99) {
        println("Updating");
      }
      val similarityHashMap = new HashMap[Int, (List[(Double, Vertex[DataWrapper])])] // First list is ratings, second is userID to send back the results.
      val selfRatingMap = new HashMap[Vertex[DataWrapper], Double]
      val similarities = new HashSet[(List[Vertex[DataWrapper]], (Int, Double))]
      val messagesMap = new HashMap[Vertex[DataWrapper], List[(Int, Double)]]
      //TEST
      incoming match {
        case _: List[MeanAndRatings] => println("COOL")
        case _: List[DataWrapper] => println("NOT COOL")
        case _ => println("LOL")
      }
     //ENDTEST
      
      // Prepare the hashmap with the data received via messages
      for (message <- incoming) {
    	  message.value match {
    	    case MeanAndRatings(mean, ratings) => {
    	      for (rating <- ratings) {
    	        val otherItemID = rating._1
    	        val weightedRating = rating._2 - mean
    	        if (otherItemID == itemID) {
    	          selfRatingMap.put(message.source, weightedRating)
    	        } else {
    	          var entry = similarityHashMap.getOrElseUpdate(itemID, List())
    	              entry ::= (weightedRating, message.source)
    	              similarityHashMap.put(itemID, entry)    	          
    	        }
    	      }
    	    }
    	    case _ => sys.error("Internal error")
    	  }
      }
      /*
       * Magical formula :P
       * sum(r(i)*r(j) / sqrt(sum(r(i)^2) + sum(r(j)^2))
       */
      for(similarity <- similarityHashMap) {
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
        val THRESHOLD = 1
        if(ratings.length >= THRESHOLD && ratings.length > 0) {
          val destination = ratings.map(_._2)
          val result = num / (de1 + de2)
          val otherItemID = similarity._1
          similarities.add(destination, (otherItemID, result))
        }
      }
      /*
       * Send messages back with the similarity computed.
       */
      for(similarity <- similarities) {
        for(destination <- similarity._1) {
          messagesMap.put(destination, similarity._2 :: messagesMap.getOrElseUpdate(destination, List()))
        }
      }
      for(message <- messagesMap.toList) yield Message(this, message._1, Similarities(message._2))
    } then {
      List() // No outgoing messages
    } then {
      List() // No outgoing messages
    } then {
      List() // No outgoing messages
    }
  }
}