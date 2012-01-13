import menthor._

package object algorithm {
  // Typedef
  type ItemID = Int
  type UserID = Int
  type Grade = Int
  type WeightedGrade = Double
  type Similarity = Double
  type User = Vertex[DataWrapper]
  
  // Used to measure the progression of the algorithm.
//  var count = 0
//  var progression = 0d
//  val numberOfItems = 1000
//  var numberOfEmptyRecommandations = 0
  
  // Used to do some time measurement.
  var time_itemSubstepData = 0l
  var time_itemSubstepSimilarity = 0l
  private var temp_time = 0l
  def startTimer() = temp_time = System.currentTimeMillis
  def stopTimer() = System.currentTimeMillis - temp_time
  
  // Number of items in each user that will be considered to find the recommended items.
  val THRESHOLD_nFavoriteItems = 10000
  // Number of item in the favoriteItems list that a recommended item must be similar to.
  val THRESHOLD_nSimilarRequired = 1
  // Minimum number of ratings to take the similarity into account. MUST BE BIGGER THAN 0.
  val THRESHOLD_nMinimumRatings = 1
  // Minimum similarity that two items have to be taken into account. Should be 0 or bigger.
  // Note that max is maxGrade/sqrt(2), as we weight the grade, the max will likely be near 3 for grade from 0 to 9.
  val THRESHOLD_minimalSimilarity = -100
  // Number of recommended item that we want
  val TOP_K = 200
  // To know if we have to sort the ratings
  val DATA_SORTED = false
}