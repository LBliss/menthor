package algorithm

import menthor.{Vertex, Substep}
/*
 * 2) Create similarity matrix - ToolRunner.run -> RowSimilarityJob <- @176
 *  a) Job normsAndTranspose - MAP & REDUCE - Map: Compute the norm. I.e. ItemsID.EuclidianNorm = for(i<-Preference) yield +=i^2 - Reduce: Sum the norms
 *  b) Job pairwiseSimilarity - MAP & REDUCE - Map: Compute each item pair similiarity - Reduce: sum for each item pair the similarities value (?)
 *  c) Job asMatrix - Map & REDUCE - Map: Take the top k similarity of the user - Reduce: merge the similarities value and take the top k again.
 */
class RecommanderVertex(userRatings: (Int, List[(Int, Int)])) extends Vertex[List[(Int, Int)]](userRatings._1.toString, userRatings._2){ // eheh
	def update(): Substep[List[(Int, Int)]] = {
	  {
	    // Normalization, the norm is the square root of the squares of the rating
	    // Corresponds to Job a)
	    val norm = math.sqrt((for(rating <- value) yield rating._2^2).foldLeft(0)(_+_))
	    List() // No outgoing messages
	  } then {
	    // Compute the pairwise similarity
	    List() // No outgoing messages
	  } then {
	    List() // No outgoing messages
	  }
	}
}