package org.rawkintrevo.aiathon.asyncio

import com.google.gson.{Gson, JsonArray, JsonElement, JsonObject}
import org.apache.flink.runtime.concurrent.Executors
import org.apache.flink.streaming.api.functions.async.{AsyncFunction, ResultFuture}
import org.apache.flink.streaming.api.scala._

import collection.JavaConverters._
import org.apache.flink.api.java.tuple.{Tuple2, Tuple4}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}


class AsyncPingModels extends AsyncFunction[Tuple4[String,String, String, String],
  Tuple2[String, String]]{

  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.directExecutor())

  override def asyncInvoke(input: Tuple4[String,String, String, String],
                           resultFuture: ResultFuture[Tuple2[String, String]]): Unit = {

    val LOG = LoggerFactory.getLogger(classOf[AsyncPingModels])
    val deviceIdStr: String = input.f0;
    val deviceDataStr: String = input.f1;
    val lastAnalyticsStr: String = input.f2;
    val urlJsonStr: String = input.f3
      .replace("[","")
      .replace("]","")
      .replace("\"", "")


    if (urlJsonStr != "") {


      val model_name: String = urlJsonStr.split(":")(0).replace("{", "")
      val url: String = "https:" + urlJsonStr.split(":")(2).replace("}", "")


      case class Analytics(max: Double,
                           min: Double,
                           sum: Double,
                           avg: Double,
                           ssd: Double,
                           std: Double,
                           count: Integer,
                           devicetype: String,
                           id: String,
                           event: String)



      /*
       {"properies":
        {"std":{
          "peperoniArm3":15.702087617751836,
          "peperoniArm2":15.253414918196734,
          "peperoniArm1":10.456258094238748},
        "min":{
          "peperoniArm3":2.0,
          "peperoniArm2":36.0,
          "peperoniArm1":14.0},
        "avg":{"peperoniArm3":13.333333333333334,"peperoniArm2":24.0,"peperoniArm1":16.0},"max":{"peperoniArm3":38.0,"peperoniArm2":36.0,"peperoniArm1":34.0},"ssd":{"peperoniArm3":739.6666666666666,"peperoniArm2":698.0,"peperoniArm1":328.0},"sum":{"peperoniArm3":40.0,"peperoniArm2":72.0,"peperoniArm1":48.0}},"count":3,"devicetype":"pizza-machine","id":"01","event":"applyPeperonis"}
       */
      val analytics: Analytics = new Gson()
        .fromJson(lastAnalyticsStr, classOf[Analytics])

      val params = "?analytics=" + lastAnalyticsStr + "&device_data=" + deviceDataStr;
      //                "?max=" + analytics.max.toString +
      //                "&min=" + analytics.min.toString +
      //                "&sum=" + analytics.sum.toString +
      //                "&avg=" + analytics.avg.toString +
      //                "&ssd=" + analytics.ssd.toString +
      //                "&std=" + analytics.std.toString +
      //                "&cnt=" + analytics.count.toString;

      val resultFutureRequested: Future[String] = Future {
        val finalUrl = url + params.replace("{", "%7B")
            .replace("\"", "%22")
            .replace(":", "%3A")
            .replace("}", "%7D")

        val src = scala.io.Source.fromURL(finalUrl)
        val str = src.mkString
        src.close()
        str // put string in new output json object.
      }

      resultFutureRequested.onSuccess {
        case result: String => {
          val resultJson = new Gson().fromJson(result, classOf[JsonObject]);
          val outputJson = new JsonObject();
          outputJson.addProperty("analytics", lastAnalyticsStr);
          outputJson.addProperty("devicedata", deviceDataStr)
          outputJson.add(model_name + "_results", resultJson)
          resultFuture.complete(Iterable(new Tuple2(deviceIdStr, outputJson.toString)).asJavaCollection)
        };
      }
    }
   }
}
