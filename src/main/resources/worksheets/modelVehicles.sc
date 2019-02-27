package worksheets

object modelVehicles {
  println("sequence of trials that led to vehicle aggregate addition for serving")
                                                  //> sequence of trials that led to vehicle aggregate addition for serving
  
  
  var allScores: List[ItemScore]  = List(new ItemScore("1", 1.0, "d1", "Announcement"),
		new ItemScore("2", 2.8, "d2", "Newsletter"),
		new ItemScore("3", -3.4, "d1", "Survey"),
		new ItemScore("4", 2.1, "d1", "Announcement"),
		new ItemScore("5", -1.2, "d2", "Survey"))
                                                  //> allScores  : List[worksheets.ItemScore] = List(ItemScore(1,1.0,d1,Announceme
                                                  //| nt), ItemScore(2,2.8,d2,Newsletter), ItemScore(3,-3.4,d1,Survey), ItemScore(
                                                  //| 4,2.1,d1,Announcement), ItemScore(5,-1.2,d2,Survey))
  var totalOccurrences = 0                        //> totalOccurrences  : Int = 0
  
  var combinedWithOthers = List[ItemScore]()      //> combinedWithOthers  : List[worksheets.ItemScore] = List()

	allScores.foreach(e => {
					//val domain = populate.get(e.item).get.domain
					//val itemType = populate.get(e.item).get.itemType
					totalOccurrences += 1
					combinedWithOthers ::= new ItemScore(e.item, e.score, e.domain, e.itemType)
					})
 combinedWithOthers                               //> res0: List[worksheets.ItemScore] = List(ItemScore(5,-1.2,d2,Survey), ItemSco
                                                  //| re(4,2.1,d1,Announcement), ItemScore(3,-3.4,d1,Survey), ItemScore(2,2.8,d2,N
                                                  //| ewsletter), ItemScore(1,1.0,d1,Announcement))
 totalOccurrences                                 //> res1: Int = 5
 
 
 
 var vehicularScoreMap = allScores.groupBy(e => e.itemType).mapValues(_.foldLeft(0.0)(_ + _.score))
                                                  //> vehicularScoreMap  : scala.collection.immutable.Map[String,Double] = Map(Ann
                                                  //| ouncement -> 3.1, Newsletter -> 2.8, Survey -> -4.6)
 
  var vehicularMap = allScores.groupBy(e => e.itemType).mapValues(_.size)
                                                  //> vehicularMap  : scala.collection.immutable.Map[String,Int] = Map(Announcemen
                                                  //| t -> 2, Newsletter -> 1, Survey -> 2)
  val total = vehicularMap.values.sum.toDouble    //> total  : Double = 5.0
  // vehicularMap.mapValues(_ / total)
  var vehicularSegmentMap = vehicularMap.map({ case (k, v) => k -> VehicleScore(k, vehicularScoreMap(k), (v / total)) })
                                                  //> vehicularSegmentMap  : scala.collection.immutable.Map[String,worksheets.Veh
                                                  //| icleScore] = Map(Announcement -> VehicleScore(Announcement,3.1,0.4), Newsle
                                                  //| tter -> VehicleScore(Newsletter,2.8,0.2), Survey -> VehicleScore(Survey,-4.
                                                  //| 6,0.4))
	vehicularSegmentMap.values.toArray        //> res2: Array[worksheets.VehicleScore] = Array(VehicleScore(Announcement,3.1,
                                                  //| 0.4), VehicleScore(Newsletter,2.8,0.2), VehicleScore(Survey,-4.6,0.4))
}


// facet[0].template.templateId
case class VehicleScore(
  itemType: String,
  aggregateScore: Double,
  frequency: Double
)