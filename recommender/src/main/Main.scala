package main

import initialization._
import algorithm._

import java.io.File
import java.io.PrintStream
import java.io.FileOutputStream

/**
 * Algorithm:
 * 1) Create preference matrix - ToolRunner.run -> PreparePreferenceMatrixJob <- @L154
 *  a) Job itemIDIndex - MAP & REDUCE - Map: Create 2tuple (Index, ItemID) - Reduce: Search the minimum ItemID
 *  b) Job toUserVectors - MAP & REDUCE - Map: Create 2tuple (UserID, (ItemID, Preference)) - Reduce: (UserID, List((ItemID, Preference)))
 *  c) Job toItemVectors - MAP & REDUCE - Map: Create the vector array[ItemID] := Preference - Reduce: Merge all the vectors in a matrix
 *
 * 2) Create similarity matrix - ToolRunner.run -> RowSimilarityJob <- @176
 *  a) Job normsAndTranspose - MAP & REDUCE - Map: Compute the norm. I.e. ItemsID.EuclidianNorm = for(i<-Preference) yield +=i^2 - Reduce: Sum the norms
 *  b) Job pairwiseSimilarity - MAP & REDUCE - Map: Compute each item pair similiarity - Reduce: sum for each item pair the similarities value (?)
 *  c) Job asMatrix - Map & REDUCE - Map: Take the top k similarity of the user - Reduce: merge the similarities value and take the top k again.
 *
 * 3) Fill the similarity matrix - Jobs in RecommenderJob
 *  a) Job prePartialMultiply1 - MAP ONLY - Map: Map a row of the similarity matrix to a VectorOrPrefWritable - Solve self-similarity problem by putting NaN in the diagonal elements.
 *  b) Job prePartialMultiply2 - MAP ONLY - Map: Extract user/pref from the vector and transform the non significative preferences (aka. the zeros and all which are smallest than the top K) in NaN.
 *  c) Job partialMultiply - REDUCE ONLY - Reduce: Tranform the VectorOrPref to VectorAndPref.
 *  d) Job itemFiltering - MAP & REDUCE - (Intui.) Use the filterFile if option is active.
 *  e) Job aggregateAndRecommend - MAP & REDUCE - Map: For each user store his pref + the similarity column - Reduce: TADA ! Apply the formula cf. slides to predict preferences, return top K for each user.
 *
 *  Our implementation of 1) is done in the Parser and in the GraphReader.
 *
 *  Our implementation of 2) is the first x substeps of the update method in RecommanderVertex
 */
object Main {
  def main(args: Array[String]): Unit = {
        println("Getting started !")
//        makeDataset
//        0 / 0 // XD
//        println("sleepy");
//        Thread.sleep(3000)
//        println("notanymore!");
//        val parsedData = Parser(new File("dataset_test1.dat"))
        val parsedData = Parser(new File(args(0)))
        val graph = GraphReader(parsedData)
//        GraphReader.printGraph(graph)
        graph.start()
        println("Started !")
        val time = System.currentTimeMillis
        graph.iterate(4)
        graph.terminate()
//        println("SubstepTwo, data handling: " + time_itemSubstepData)
//        println("SubstepTwo, pairwise similarity: " + time_itemSubstepSimilarity)
        println("Total time of the algorithm: " + (System.currentTimeMillis - time) + "ms.")
//        println("Total of empty recommandations list: " + numberOfEmptyRecommandations)
        println("Finished !")
  }
  
  def makeDataset() {
    val file = new File("dataset_mod5_6000k.dat")
    file.createNewFile()
    Console.setOut(new PrintStream(new FileOutputStream(file)))

    val list = (1 to 3000000).map(x => ((math.random * 50000).toInt, (math.random * 1000).toInt)).toList.sortWith((x, y) => x._1 < y._1 || (x._1 == y._1 && x._2 < y._2)).distinct.map(x => (x._1, x._2, if(x._1 % 5 == x._2 % 5) (math.random * 5).toInt + 5 else (math.random * 5).toInt))
    list.foreach(x => println(x._1 + "," + x._2 + "," + x._3))
  }
}