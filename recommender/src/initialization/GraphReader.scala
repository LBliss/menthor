package initialization

import menthor._

import algorithm._
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

/**
 * Object used to create and read a graph.
 *
 * Input:
 * List[userID, List[(itemID, grade)]]
 *
 * Output:
 * Graph[DataWrapper] with two types of nodes:
 * UserNode (label = userID, data = ratings)
 * ItemNode (label = itemID, data = null)
 * For each rating, the UserNode is connected to the ItemNode corresponding.
 * 
 * Important:
 * The nodes are nicely divided in the graph in order to get the maximum performances. // TODO: improve this sentence
 * 
 * Also contains methods to print samples or the whole graph.
 */
object GraphReader {

  def apply(data: List[(Int, List[(Int, Int)])]): Graph[DataWrapper] = {

    val time = System.currentTimeMillis

    val graph = new Graph[DataWrapper]
    // We use the following map to log the vertex creation in order to create each only once.
    val itemMap = new HashMap[Int, Vertex[DataWrapper]]
    // We use these lists to partition the vertices nicely.
    var userList: HashSet[UserVertex] = new HashSet[UserVertex]
    
    for ((userID, ratings) <- data) {
      val userVertex = UserVertex(userID, ratings)
      userList.addEntry(userVertex)
      for ((itemID, _) <- ratings) {
        val itemVertex = itemMap.getOrElseUpdate(itemID, ItemVertex(itemID))
        userVertex.connectTo(itemVertex)
      }
    }
    val userSize = userList.size
    val itemSize = itemMap.size
    val size = (userSize + itemMap.size)
    var userCount = 0d
    var itemCount = 0d
    val itemIter = scala.util.Random.shuffle(itemMap.values).iterator
    val userIter = userList.iterator
    for (i <- 0 until size) {
      if(userCount/userSize >= itemCount/itemSize) {
        itemCount += 1
        graph.addVertex(itemIter.next)
      } else {
    	userCount += 1
        graph.addVertex(userIter.next)
      }
    }
    println("Total number of vertex: " + (itemCount + userCount))
    println("Time to draw the graph: " + (System.currentTimeMillis - time) + "ms.")
    graph
  }

  def printGraph(graph: Graph[DataWrapper]) = {
    for (v <- graph.vertices) {
      println(v.label)
    }
  }

  def printGraphSamples(graph: Graph[DataWrapper], n: Int) = {
    val skip = graph.vertices.length / n
    println(n + " samples of the graph: ")
    for (i <- 0 until n) {
      var vertex = graph.vertices.drop(i * skip).head
      println(vertex.label)
    }
  }
}
