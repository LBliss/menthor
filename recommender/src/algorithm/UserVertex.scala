package algorithm

import menthor._

case class UserVertex(userID: Int, ratings: DataWrapper) extends Vertex[DataWrapper]("User #" + userID, ratings) {
  def update(): Substep[DataWrapper] = {
    {
      /*
         * Compute the mean of the ratings to normalize the grades.
         * Send the ratings and the mean to all ItemVertex concerned.
         */
      if (Math.random > 0.99) {
        println("Trolling");
      }
      value match {
        case Ratings(ratings) =>
          val mean = ratings.map(_._2).reduce(_ + _) / ratings.length.toDouble
          for (neighbor <- neighbors) yield Message(this, neighbor, MeanAndRatings(mean, ratings)) // TODO: Optimization

        case _ => sys.error("Internal error")
      }
    } then {
      /*
         * Nothing to do and no message to send
         */
      List()
    } then {
      List() // No outgoing messages
    } then {
      List() // No outgoing messages
    } then {
      List() // No outgoing messages
    }
  }
}