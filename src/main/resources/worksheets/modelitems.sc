package worksheets

//import com.hs.haystack.tachyon.constituent.recommendcontextstouser._

import breeze.stats.meanAndVariance
import breeze.stats.MeanAndVariance

object modelitems {
  println("sequence of trials that led to item properties addition for serving")
                                                  //> sequence of trials that led to item properties addition for serving
  
  val items = Map(1 -> new Item(None, "d1", "t1"),
  2 -> new Item(None, "d2", "t2"),
  3 -> new Item(None, "d3", "t3"),
  4 -> new Item(None, "d4", "t4"),
  5 -> new Item(None, "d5", "t5"),
  6 -> new Item(None, "d6", "t6")).withDefaultValue(new Item(None, "haystack.in", "POV"))
                                                  //> items  : scala.collection.immutable.Map[Int,worksheets.Item] = Map(5 -> Item
                                                  //| (None,d5,t5), 1 -> Item(None,d1,t1), 6 -> Item(None,d6,t6), 2 -> Item(None,d
                                                  //| 2,t2), 3 -> Item(None,d3,t3), 4 -> Item(None,d4,t4))
val item1 =  items(8).domain                      //> item1  : String = haystack.in


////////// next check combined val in serving now
var allScores: Array[ItemScore]  = Array(new ItemScore("1", 1.0, "d1", "t1"),
new ItemScore("2", 2.0, "d2", "t2"),
new ItemScore("3", 3.0, "d3", "t3"),
new ItemScore("4", 4.0, "d4", "t4"),
new ItemScore("5", 5.0, "d5", "t5"))              //> allScores  : Array[worksheets.ItemScore] = Array(ItemScore(1,1.0,d1,t1), Ite
                                                  //| mScore(2,2.0,d2,t2), ItemScore(3,3.0,d3,t3), ItemScore(4,4.0,d4,t4), ItemSco
                                                  //| re(5,5.0,d5,t5))


val predictedResults: Seq[PredictedResult]  = Seq(new PredictedResult(allScores))
                                                  //> predictedResults  : Seq[worksheets.PredictedResult] = List(PredictedResult([
                                                  //| Lworksheets.ItemScore;@2173f6d9))

val standard1: Seq[Array[ItemScore]] = predictedResults.map(_.itemScores)
                                                  //> standard1  : Seq[Array[worksheets.ItemScore]] = List(Array(ItemScore(1,1.0,
                                                  //| d1,t1), ItemScore(2,2.0,d2,t2), ItemScore(3,3.0,d3,t3), ItemScore(4,4.0,d4,
                                                  //| t4), ItemScore(5,5.0,d5,t5)))
val standard: Seq[Array[ItemScore]] = Seq(allScores)
                                                  //> standard  : Seq[Array[worksheets.ItemScore]] = List(Array(ItemScore(1,1.0,d
                                                  //| 1,t1), ItemScore(2,2.0,d2,t2), ItemScore(3,3.0,d3,t3), ItemScore(4,4.0,d4,t
                                                  //| 4), ItemScore(5,5.0,d5,t5)))
val combined = standard.flatten
								.groupBy(_.item)
                 .mapValues(itemScores => itemScores.map(_.score).reduce(_ + _))
                 .toArray
                 .sortBy(_._2)(Ordering.Double.reverse)
                 .take(4)
                 .map { case (k,v) => ItemScore(k, v, "", "") }
                                                  //> combined  : Array[worksheets.ItemScore] = Array(ItemScore(5,5.0,,), ItemSco
                                                  //| re(4,4.0,,), ItemScore(3,3.0,,), ItemScore(2,2.0,,))
 
 val populate = standard1.flatten.map (t => t.item -> t).toMap.withDefaultValue(new ItemScore("",4.0,"haystack.in","POV"))
                                                  //> populate  : scala.collection.immutable.Map[String,worksheets.ItemScore] = M
                                                  //| ap(4 -> ItemScore(4,4.0,d4,t4), 5 -> ItemScore(5,5.0,d5,t5), 1 -> ItemScore
                                                  //| (1,1.0,d1,t1), 2 -> ItemScore(2,2.0,d2,t2), 3 -> ItemScore(3,3.0,d3,t3))

var combinedWithOthers = List[ItemScore]()        //> combinedWithOthers  : List[worksheets.ItemScore] = List()

combined.foreach(e => {
					val domain = populate.get(e.item).get.domain
					val itemType = populate.get(e.item).get.itemType
					combinedWithOthers ::= new ItemScore(e.item, e.score, domain, itemType)
					})
 combinedWithOthers                               //> res0: List[worksheets.ItemScore] = List(ItemScore(2,2.0,d2,t2), ItemScore(3
                                                  //| ,3.0,d3,t3), ItemScore(4,4.0,d4,t4), ItemScore(5,5.0,d5,t5))
 
 standard1.foreach(e => println (e(0)))           //> ItemScore(1,1.0,d1,t1)
 
 for (e <- standard1) {
        // imagine this requires multiple lines
        for (u <- e) {
        println(u)                                //> ItemScore(1,1.0,d1,t1)
                                                  //| ItemScore(2,2.0,d2,t2)
                                                  //| ItemScore(3,3.0,d3,t3)
                                                  //| ItemScore(4,4.0,d4,t4)
                                                  //| ItemScore(5,5.0,d5,t5)
        }
        
      }
}





case class PredictedResult(
  itemScores: Array[ItemScore]
)

case class ItemScore(
  item: String,
  score: Double,
  domain: String,
  itemType: String
)

case class Item(
    val categories: Option[List[String]],
    val domain: String,
    val itemType: String)