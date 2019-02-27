

package com.hs.haystack.tachyon.constituent.recommendcontextstouser

import org.apache.predictionio.controller.EngineFactory
import org.apache.predictionio.controller.Engine

case class Query(
  aim: String,
  users: List[String],
  num: Int
)

case class PredictedResult(
  itemScores: Array[ItemScore],
  vehicleScores: Array[VehicleScore]
)

case class ActualResult(
  ratings: Array[Rating]
)

case class ItemScore(
  item: String,
  score: Double,
  domain: String,
  itemType: String
)

case class VehicleScore(
  vehicleType: String,
  aggregateScore: Double,
  frequency: Double
)

object RecommendationEngine extends EngineFactory {
  def apply() = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
      Map("als" -> classOf[ALSAlgorithm],
          "likealgo" -> classOf[LikeALSAlgorithm]), // ADDED),
      classOf[Serving])
  }
}
