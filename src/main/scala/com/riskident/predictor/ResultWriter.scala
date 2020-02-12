package com.riskident.predictor

import com.riskident.predictor.PredictorJsonProtocol._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import spray.json._

class ResultWriter extends ResultHandler {
  val props = new java.util.Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("key.serializer", classOf[StringSerializer].getName)
  props.put("value.serializer", classOf[StringSerializer].getName)
  val kafkaProducer = new KafkaProducer[String, String](props)

  override def processResult(predictionResult: PredictionResult): Unit = {
    kafkaProducer.send(new ProducerRecord("predictions", predictionResult.toJson.compactPrint))
  }
}
