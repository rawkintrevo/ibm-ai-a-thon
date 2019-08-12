package org.rawkintrevo.aiathon.coprocessfn


import org.apache.flink.runtime.concurrent.Executors
import org.apache.flink.streaming.api.functions.co.CoProcessFunction
import org.apache.flink.util.Collector

import scala.concurrent.ExecutionContext
import org.slf4j.{Logger, LoggerFactory}
// Shit at the wall...
import org.apache.flink.streaming.api.scala._
import collection.JavaConverters._

class JoinRecordAndAnalyticsCoProcess extends CoProcessFunction[
  (String, String),               // Stream 1: Original Tuples
  (String, String),               // Stream 2: Analytics JSON
  (String, String, String)] {     // Output: Tuple3[ "Key_like_this", "{ tuple: {...}} ", "{analytics: {...}}" ]


  var lastAnalyticsMap:scala.collection.mutable.Map[String, String] = scala.collection.mutable.Map[String, String]();

  // Shit at the wall...
  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.directExecutor())

  override def processElement1(
    in1: (String, String),
    context: CoProcessFunction[ Tuple2[String, String],
                                Tuple2[String, String],
                                Tuple3[String, String, String]]#Context,
    collector: Collector[Tuple3[String, String, String]]): Unit = {

    val LOG = LoggerFactory.getLogger(classOf[JoinRecordAndAnalyticsCoProcess])

    if (lastAnalyticsMap.contains(in1._1)) {
      collector.collect(Tuple3(in1._1, in1._2, lastAnalyticsMap(in1._1)));
    } else {
      LOG.info("Threw away event for key '" + in1._1 + "' haven't seen anay analytics for that yet. " )
    }

  }

  override def processElement2(
    in2: Tuple2[String, String],
    context: CoProcessFunction[ Tuple2[String, String],
      Tuple2[String, String],
      Tuple3[String, String, String]]#Context,
    collector: Collector[Tuple3[String, String, String]]): Unit = {

    lastAnalyticsMap(in2._1) = in2._2
  }
};
