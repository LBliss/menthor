package initialization

import scala.io.BufferedSource
import java.io.FileInputStream
import java.io.File

/**
 * Object used to parse a formatted dataset.
 *
 * Input:
 * File with lines formatted as follow:
 * userID[separator]itemID[separator]grade
 * 
 * Important:
 *  - Both userID and itemID are integer.
 *  - The [separator] is optional (default: ",").
 *  - No two line have both the same userID and itemID (only one grade per user for a specific item).
 *  - The input must me sorted by userID.
 *
 * Output:
 * List[userID, List[(itemID, grade)]] also known as List[userID, List[ranking]].
 * 
 * Note:
 *  - The list is ordered if the input is sorted by userID and then by itemID.
 *
 * Original dataset:
 * http://www.occamslab.com/petricek/data/
 */
object Parser {

  def apply(file: File, separator: String = ","): List[(Int, List[(Int, Int)])] = {

    val time = System.currentTimeMillis

    val inIterator: Iterator[Array[Int]] = new BufferedSource(new FileInputStream(file)).getLines.map(_.split(separator).map(_.toInt))
    var outList: List[(Int, List[(Int, Int)])] = List()

    val firstLine = inIterator.next
    var userID = firstLine(0)
    var rankings: List[(Int, Int)] = List( (firstLine(1), firstLine(2)) )

    for (line <- inIterator) {
      if (line(0) != userID) {
        outList ::= (userID, rankings.reverse)
        rankings = List()
        userID = line(0)
      }
      rankings ::= (line(1), line(2))
    }

    println("Time to parse the dataset: " + (System.currentTimeMillis - time) + "ms.")

    (userID, rankings.reverse) :: outList
  }
}