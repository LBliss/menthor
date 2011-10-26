package initialization

import menthor.Graph
import algorithm.RecommanderVertex

/**
 * Object used to create and read a graph.
 * 
 * Input: 
 * List[userID, List[(itemID, rating)]]
 * 
 * Output: 
 * Graph[List[(Int, Int)] with userID as vertex label
 */
object GraphReader {

  def apply(data: List[(Int, List[(Int, Int)])]): Graph[List[(Int, Int)]] = {
    val graph = new Graph[List[(Int, Int)]]
    
    data.foreach(userRatings => graph.addVertex(new RecommanderVertex(userRatings)))
    
    graph
  }
  
  def printGraph(graph: Graph[List[(Int, Int)]]) = {
    for (v <- graph.vertices) {
      println(v.label + ": " + v.value)
    }
  }
  
  def printGraphSamples(graph: Graph[List[(Int, Int)]], n: Int) = {
    val skip = graph.vertices.length / n
    println(n + " samples of the graph: ")
    for(i <- 0 until n) {
      var vertex = graph.vertices.drop(i*skip).head
      println(vertex.label + ": " + vertex.value)
    }
  }
}
