package initialization

import scala.io.BufferedSource
import java.io.FileInputStream
import java.io.File

/**
 * Object used to parse a formatted dataset.
 *
 * Input:
 * File with lines formatted: userID,itemID,ratings
 *
 * Output:
 * List[userID, List[(itemID, rating)]]
 *
 * Dataset:
 * http://www.occamslab.com/petricek/data/
 */
object Parser {

  // TODO: discuss if we factorize the separator and the type
  def apply(file: File): List[(Int, List[(Int, Int)])] = {
    // TODO: remove following line
    val time = System.currentTimeMillis()

    val separator = ","
    val inIterator: BufferedIterator[Array[Int]] = new BufferedSource(new FileInputStream(new File("ratings.dat"))).getLines().map(_.split(separator).map(_.toInt)).buffered
    var outList: List[(Int, List[(Int, Int)])] = List()

    /*
     *  The foreach has a special meaning, as the iterator move on in the middle of it.
     *  It loops only on element with different userID.
     */
    inIterator.foreach(
      newUser => {
        val userID: Int = newUser(0)
        val ratings: List[(Int, Int)] = inIterator.takeWhile(_.head.equals(userID)).map(rating => (rating(1), rating(2))).toList
        outList ::= (userID, ratings)
        }
      )

    //TODO: remove following line
    println("Time to parse the dataset: " + (System.currentTimeMillis() - time) + "ms.")
    outList
  }
}

//    ---> Old implementation, 40% slower <---
//
//    // The takeWhile method does the iteration
//    while (inIterator.hasNext) {
//      val userID: Int = inIterator.head(0)
//      val ratings: List[(Int, Int)] = inIterator.takeWhile(_.head.equals(userID)).map(rating => (rating(1), rating(2))).toList
//      outList ::= (userID, ratings)
//    }
//    