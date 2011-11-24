package initialization

import algorithm._
import menthor.Graph
import menthor.Vertex
import scala.collection.mutable.HashMap

/**
 * Object used to create and read a graph.
 *
 * Input:
 * List[userID, List[(itemID, rating)]]
 *
 * Output:
 * Graph[DataWrapper] with two types of nodes:
 * UserNode (label = userID, data = userRatings)
 * ItemNode (label = itemID, data = null)
 * For each rating, the UserNode is connected to the ItemNode corresponding to the rating.
 */
object GraphReader {

  def apply(data: List[(Int, List[(Int, Int)])]): Graph[DataWrapper] = {

    val time = System.currentTimeMillis()

    val graph = new Graph[DataWrapper]
    // We use this to log the vertex creation in order to create each of them only once.
    val itemMap = new HashMap[Int, Vertex[DataWrapper]]()

    for ((userID, ratings) <- data) {
      val userVertex = graph.addVertex(new UserVertex(userID, Ratings(ratings)))
      for ((itemID, _) <- ratings) {
        val itemVertex = itemMap.getOrElseUpdate(itemID, graph.addVertex(ItemVertex(itemID)))
        userVertex.connectTo(itemVertex)
      }
    }
    println("Time to draw the graph: " + (System.currentTimeMillis() - time) + "ms.")
    graph
    //TODO: Do we have to synchronized/terminate the graph as in the pagerank example ??
  }

  def printGraph(graph: Graph[DataWrapper]) = {
    for (v <- graph.vertices) {
      println(v.label + ": " + v.value)
    }
  }

  def printGraphSamples(graph: Graph[DataWrapper], n: Int) = {
    val skip = graph.vertices.length / n
    println(n + " samples of the graph: ")
    for (i <- 0 until n) {
      var vertex = graph.vertices.drop(i * skip).head
      println(vertex.label + ": " + vertex.value)
    }
  }
}
