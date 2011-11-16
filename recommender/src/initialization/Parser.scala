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

  // TODO: Add ensuring.
  def apply(file: File): List[(Int, List[(Int, Int)])] = {
    val time = System.currentTimeMillis()

    val inIterator: Iterator[Array[Int]] = new BufferedSource(new FileInputStream(new File("ratings.dat"))).getLines().map(_.split(',').map(_.toInt))
    var outList: List[(Int, List[(Int, Int)])] = List()

    val firstLine = inIterator.next()
    var userID = firstLine(0)
    var rankings: List[(Int, Int)] = List( (firstLine(1), firstLine(2)) )

    for (line <- inIterator) {
      if (line(0) != userID) {
        outList ::= (userID, rankings)
        rankings = List()
        userID = line(0)
      }
      rankings ::= (line(1), line(2))
    }

    println("Time to parse the dataset: " + (System.currentTimeMillis() - time) + "ms.")

    (userID, rankings) :: outList
  }
}


//    inList.groupBy(x => {if(x(0) != userID) {userID = x(0); false} else true
//      
//    })
//    
//    while(!inList.isEmpty) {
//      userID = inList.head(0)
//      val split = inList.splitAt(inList.indexWhere(_(0)!=userID))
//      outList ::= (userID, split._1.map(x => (x(1), x(2))))
//      inList = split._2
//    }
    /*
     *  The foreach has a special meaning, as the iterator move on in the middle of it.
     *  It loops only on element with different userID.
     */
    //    inIterator.foreach(
    //      newUser => {
    //        val userID: Int = newUser(0)
    //        val ratings: List[(Int, Int)] = inIterator.takeWhile(_.head.equals(userID)).map(rating => (rating(1), rating(2))).toList
    //        outList ::= (userID, ratings)
    //        }
    //      )
    //          // The takeWhile method does the iteration

//    while (inIterator.hasNext) {
//      val next = inIterator.next()
//      if (next.head.equals(userID)) {
//        
//      } else {
//        userID = next(0)
//      }
//      val userID = inIterator.head(0)
//      val ratings: List[(Int, Int)] = inIterator.takeWhile(_.head.equals(userID)).map(rating => (rating(1), rating(2))).toList
//      outList ::= (userID, ratings)
//    }
    //          // The takeWhile participes to the iteration
    //    for (newUser <- inIterator) yield {
    //      val userID: Int = newUser(0)
    //      val ratings: List[(Int, Int)] = inIterator.takeWhile(_.head.equals(userID)).map(rating => (rating(1), rating(2))).toList
    //      (userID, ratings)
    //    }
    //
    //TODO: remove following line
//
////class CorrectBufferedIterator[+A](iter: BufferedIterator[A]) extends BufferedIterator[A] with Correc
//
//class CorrectBufferedIterator[+A](iter: BufferedIterator[A]) extends BufferedIterator[A] {
//  def head = iter.head
//  def hasNext = iter.hasNext
//  def next = iter.next
//
//  override def takeWhile(p: A => Boolean): Iterator[A] = new Iterator[A] {
//    val tail = iter
//    var headValid = p(tail.head)
//      
//    def hasNext = headValid || tail.hasNext && {
//      tail.next()
//      headValid = p(tail.head)
//      headValid
//    }
//    def next() =
//      if(hasNext) {
//        headValid = false
//        tail.head
//       } else Iterator.empty.next()
//  }
//}
//
//    ---> Old implementation, 40% slower <---
//
//    // The takeWhile method does the iteration
//    while (inIterator.hasNext) {
//      val userID: Int = inIterator.head(0)
//      val ratings: List[(Int, Int)] = inIterator.takeWhile(_.head.equals(userID)).map(rating => (rating(1), rating(2))).toList
//      outList ::= (userID, ratings)
//    }
//    