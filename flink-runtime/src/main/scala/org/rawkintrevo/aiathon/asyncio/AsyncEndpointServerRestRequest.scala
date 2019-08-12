package org.rawkintrevo.aiathon.asyncio

import java.util
import java.util.concurrent.TimeUnit
import java.util.{Calendar, Collection}

import org.apache.flink.runtime.concurrent.Executors
import org.apache.flink.streaming.api.functions.async.{AsyncFunction, ResultFuture}
import org.apache.flink.streaming.api.scala._

import collection.JavaConverters._
import org.apache.flink.api.java.tuple.Tuple2
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

class AsyncEndpointServerRestRequest extends AsyncFunction[Tuple2[String,String], Tuple2[String, String]] {

  /** The database specific client that can issue concurrent requests with callbacks */
  //  lazy val client: DatabaseClient = new DatabaseClient(host, post, credentials)

  /** The context used for the future callbacks */
  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.directExecutor())

  lazy val lastCallDt : Calendar = Calendar.getInstance()

  override def asyncInvoke(input: Tuple2[String,String] , resultFuture: ResultFuture[Tuple2[String, String]]): Unit = {
    val LOG = LoggerFactory.getLogger(classOf[AsyncEndpointServerRestRequest])

      // str - string coming from upstream,
      // resultFuture- what goes on down the line.
      val input_str: String = input.f0
      //    val param_str = "?type=" + input_str.split("_")(0) +
      //      "&id=" + input_str.split("_")(1) +
      //      "&event=" + input_str.split("_")(2);
      val param_str = "?key=" + input_str
      val url = input.f1 + param_str;
      LOG.info("called for urls list...")
      // issue the asynchronous request, receive a future for the result
      val resultFutureRequested: Future[String] = Future {
        LOG.info("ok i'm for seriously calling...")
        val src = scala.io.Source.fromURL(url)
        val str = src.mkString
        LOG.info("ok i actually called...")
        src.close()
        str
      }
      // set the callback to be executed once the request by the client is complete
      // the callback simply forwards the result to the result future
      resultFutureRequested.onSuccess {
        case result: String => {
          resultFuture.complete(Iterable(new Tuple2(input_str, result)).asJavaCollection);
          LOG.info("recieved urls list: " + result);
        }
      }




    }



}