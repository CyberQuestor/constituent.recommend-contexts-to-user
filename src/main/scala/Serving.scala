

package com.hs.haystack.tachyon.constituent.recommendcontextstouser

import org.apache.predictionio.controller.LServing
import breeze.stats.meanAndVariance
import breeze.stats.MeanAndVariance

class Serving
  extends LServing[Query, PredictedResult] {

  override
  def serve(query: Query,
    predictedResults: Seq[PredictedResult]): PredictedResult = {
    val prediction = query.aim match {
      case "item" => serveItems(query, predictedResults)
      case "vehicle" => serveVehicles(query, predictedResults)
      case _ => PredictedResult(Array(), Array())
    }
    prediction
  }
  
  def serveItems(query: Query,
    predictedResults: Seq[PredictedResult]): PredictedResult = {
    val combinedWithOthers = serveCommonItems(query, predictedResults)
    PredictedResult(combinedWithOthers.toArray, Array())
  }
  
  def serveVehicles(query: Query,
    predictedResults: Seq[PredictedResult]): PredictedResult = {
    
    val combinedWithOthers = serveCommonItems(query, predictedResults)
					
		var vehicularScoreMap = combinedWithOthers.groupBy(e => e.itemType).mapValues(_.foldLeft(0.0)(_ + _.score))
		var vehicularMap = combinedWithOthers.groupBy(e => e.itemType).mapValues(_.size)
		val total = vehicularMap.values.sum.toDouble
		val vehicularSegmentMap = vehicularMap.map { case (k,v) => VehicleScore(k, vehicularScoreMap(k), (v / total)) }
					
    PredictedResult(combinedWithOthers.toArray, vehicularSegmentMap.toArray)
  }
  
  def serveCommonItems(query: Query,
    predictedResults: Seq[PredictedResult]): List[ItemScore] = {
    
    // MODFIED
    val standard: Seq[Array[ItemScore]] = if (query.num == 1) {
      // if query 1 item, don't standardize
      predictedResults.map(_.itemScores)
    } else {
      // Standardize the score before combine
      val mvList: Seq[MeanAndVariance] = predictedResults.map { pr =>
        meanAndVariance(pr.itemScores.map(_.score))
      }

      predictedResults.zipWithIndex
        .map { case (pr, i) =>
          pr.itemScores.map { is =>
            // standardize score (z-score)
            // if standard deviation is 0 (when all items have the same score,
            // meaning all items are ranked equally), return 0.
            val score = if (mvList(i).stdDev == 0) {
              0
            } else {
              (is.score - mvList(i).mean) / mvList(i).stdDev
            }

            ItemScore(is.item, score, is.domain, is.itemType)
          }
        }
    }
    
    //println("being served is")
    //standard.take(8).foreach(println)
    
    val populate = standard.flatten.map (t => t.item -> t).toMap.withDefaultValue(new ItemScore("",4.0,"haystack.in","POV"))
    
    // sum the standardized score if same item
    val combined = standard.flatten // Array of ItemScore
      .groupBy(_.item) // groupBy item id
      .mapValues(itemScores => itemScores.map(_.score).reduce(_ + _))
      .toArray // array of (item id, score)
      .sortBy(_._2)(Ordering.Double.reverse)
      .take(query.num)
      .map { case (k,v) => ItemScore(k, v, "", "") }
    
    //println("and that being combined")
    //combined.take(8).foreach(println)
    
    var combinedWithOthers = List[ItemScore]()     
    
    combined.foreach(e => {
					val domain = populate.get(e.item).get.domain
					val itemType = populate.get(e.item).get.itemType
					combinedWithOthers ::= new ItemScore(e.item, e.score, domain, itemType)
					})
		combinedWithOthers
  }
 
}
