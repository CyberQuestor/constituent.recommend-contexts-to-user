

package com.hs.haystack.tachyon.constituent.recommendcontextstouser

import org.apache.predictionio.controller.PAlgorithm
import org.apache.predictionio.controller.Params
import org.apache.predictionio.data.storage.BiMap

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.{Rating => MLlibRating}
import org.apache.spark.mllib.recommendation.ALSModel

import grizzled.slf4j.Logger


class LikeALSAlgorithm(ap: ALSAlgorithmParams)
  extends ALSAlgorithm(ap) {

  @transient lazy override val logger = Logger[this.type]

  if (ap.numIterations > 30) {
    logger.warn(
      s"ALSAlgorithmParams.numIterations > 30, current: ${ap.numIterations}. " +
      s"There is a chance of running to StackOverflowException." +
      s"To remedy it, set lower numIterations or checkpoint parameters.")
  }

  override
  def train(sc: SparkContext, data: PreparedData): ALSModel = {
    require(!data.likeEvents.take(1).isEmpty,
      s"likeEvents in PreparedData cannot be empty." +
      " Please check if DataSource generates TrainingData" +
      " and Preprator generates PreparedData correctly.")
    require(!data.users.take(1).isEmpty,
      s"users in PreparedData cannot be empty." +
      " Please check if DataSource generates TrainingData" +
      " and Preprator generates PreparedData correctly.")
    require(!data.items.take(1).isEmpty,
      s"items in PreparedData cannot be empty." +
      " Please check if DataSource generates TrainingData" +
      " and Preprator generates PreparedData correctly.")
      
      println("Training recommended items model through ALS likes")

    // create User and item's String ID to integer index BiMap
    val userStringIntMap = BiMap.stringInt(data.users.keys)
    val itemStringIntMap = BiMap.stringInt(data.items.keys)
    
    // collect Item as Map and convert ID to Int index
    val items: Map[Int, Item] = data.items.map { case (id, item) =>(itemStringIntMap.getOrElse(id, 0), item)
      case default => (0, Item("00000000-0000-0000-0000-000000000000", None,"haystack.in","POV","00000000-0000-0000-0000-000000000000"))
    }.collectAsMap.toMap
    
    val mllibRatings = data.likeEvents
      .map { r =>
        // Convert user and item String IDs to Int index for MLlib
        val uindex = userStringIntMap.getOrElse(r.user, -1)
        val iindex = itemStringIntMap.getOrElse(r.item, -1)

        if (uindex == -1)
          logger.info(s"Couldn't convert nonexistent user ID ${r.user}"
            + " to Int index.")

        if (iindex == -1)
          logger.info(s"Couldn't convert nonexistent item ID ${r.item}"
            + " to Int index.")

        // key is (uindex, iindex) tuple, value is (like, t) tuple
        ((uindex, iindex), (r.like, r.t))
      }.filter { case ((u, i), v) =>
        // keep events with valid user and item index
        (u != -1) && (i != -1)
      }.reduceByKey { case (v1, v2) => // MODIFIED
        // An user may like an item and change to dislike it later,
        // or vice versa. Use the latest value for this case.
        val (like1, t1) = v1
        val (like2, t2) = v2
        // keep the latest value
        if (t1 > t2) v1 else v2
      }.map { case ((u, i), (like, t)) => // MODIFIED
        // With ALS.trainImplicit(), we can use negative value to indicate
        // negative signal (ie. dislike)
        val r = if (like) 1 else -1
        // MLlibRating requires integer index for user and item
        MLlibRating(u, i, r)
      }
      .cache()
      
      // MLLib ALS cannot handle empty training data.
    require(!mllibRatings.take(1).isEmpty,
      s"mllibRatings cannot be empty." +
      " Please check if your events contain valid user and item ID.")
    // seed for MLlib ALS
    val seed = ap.seed.getOrElse(System.nanoTime)
    
    println("ready for spark")
    println("top 50")
    //mllibRatings.take(50).foreach(println)
    
    /*mllibRatings.take(50).foreach(e => {
      val i = items.getOrElse(e.product, "Item missing")
      println("Composition: " + e)
      println("Composition item: " + i)
    })*/
    
    val m = ALS.trainImplicit(
      ratings = mllibRatings,
      rank = ap.rank,
      iterations = ap.numIterations,
      lambda = ap.lambda,
      blocks = -1,
      alpha = 1.0,
      seed = seed)
      
      println("Model through ALS likes training complete")
      
      println("trained product features")
      // m.userFeatures.take(50).foreach(println)
      
    new ALSModel(
      rank = m.rank,
      userFeatures = m.userFeatures,
      productFeatures = m.productFeatures,
      userStringIntMap = userStringIntMap,
      itemStringIntMap = itemStringIntMap,
      items = items
    )

  }

}
