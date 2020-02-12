package com.riskident.predictor

import java.util.concurrent.ArrayBlockingQueue

import com.riskident.predictor.PredictorJsonProtocol._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import spray.json._

import scala.collection.mutable.ListBuffer

// Doing this in the service may not be optimal. It might be better to process the result stream
// with e.g. spark streaming jobs to allow more analysis (e.g. breakdown by model version, different
// metrics, different analysis windows).
class ModelMetricsCalculator(
                              flushInterval: Int, // calculate and send out statistics every so many examples
                              calculationWindow: Int // it might be better to calculate statistics over longer interval than flushing
                            ) extends ResultHandler {
  val props = new java.util.Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("key.serializer", classOf[StringSerializer].getName)
  props.put("value.serializer", classOf[StringSerializer].getName)
  val kafkaProducer = new KafkaProducer[String, String](props)

  val buffer = new ListBuffer[Tuple2[Double, Double]]
  var flushCounter = 0

  // decouple potentially expensive statistics calculation into a dedicated thread over a queue
  val workQueue = new ArrayBlockingQueue[Array[(Double, Double)]](10)

  // Should be replaced with e.g. executor service in real project
  new Thread(new Runnable {
    override def run(): Unit = {
      println("Listening for statistical tasks")
      while (true) {
        val predictedAndActualLabels = workQueue.take()
        // calculate confusion matrix as basis for other metrics
        val cm = predictedAndActualLabels.map {
          case (0.0, 0.0) => "TN"
          case (0.0, 1.0) => "FN"
          case (1.0, 0.0) => "FP"
          case (1.0, 1.0) => "TP"
        }.groupBy(identity).mapValues(_.length)

        // calculate accuracy
        val accuracy = (cm.getOrElse("TN", 0) + cm.getOrElse("TP", 0)) / (cm.values.sum).asInstanceOf[Double]

        println("Confusion matrix: " + cm + ", accuracy: " + accuracy)

        val stats = StatisticsRecord(accuracy)
        kafkaProducer.send(new ProducerRecord("statistics", stats.toJson.compactPrint))
      }
    }
  }, "metrics calculator").start()

  override def processResult(predictionResult: PredictionResult): Unit = {
    this.synchronized {
      buffer += Tuple2(if (predictionResult.prediction.result) 1.0 else 0.0, predictionResult.actualLabel)
      while (buffer.size > calculationWindow) {
        buffer.remove(0, buffer.size - calculationWindow)
      }
      flushCounter += 1
      if (flushCounter >= flushInterval) {
        flushCounter = 0
        val copyForProcessing = new Array[(Double, Double)](buffer.size)
        buffer.copyToArray(copyForProcessing)
        workQueue.put(copyForProcessing)
      }
    }
  }
}
