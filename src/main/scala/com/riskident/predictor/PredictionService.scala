package com.riskident.predictor

import java.util
import java.util.Properties

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException

import scala.util.Random
import collection.JavaConverters._
import PredictorJsonProtocol._
import org.apache.kafka.common.protocol.types.Field.UUID
import spray.json._

object PredictionService {
  def main(args: Array[String]): Unit = {
    val classifierProvider = new BlockingClassifierProvider()
    val resultWriter = new ResultWriter()
    val metricsCalculator = new ModelMetricsCalculator()
    val predictionProcessor: PredictionProcessor = new PredictionProcessor(
      classifierProvider,
      Seq(resultWriter, metricsCalculator)
    )

    new Thread(new Runnable {
      override def run(): Unit = {
        val props: Properties = commonConsumerProperties
        // we want examples topic balanced between consumers
        props.put("group.id", "predictor-examples")
        // consume all available examples
        props.put("auto.offset.reset", "earliest")
        val consumer = new KafkaConsumer[String, String](props)

        try {
          consumer.subscribe(util.Arrays.asList("examples"))

          println("Start consuming examples")
          while (true) {
            val records = consumer.poll(Long.MaxValue).asScala
            records.foreach(r => {
              val example = r.value().parseJson.convertTo[Example]
              predictionProcessor.processRecord(example)
            })
          }
        } catch {
          case e: WakeupException =>
          case t: Throwable => {
            t.printStackTrace()
            System.exit(1)
          }
        } finally {
          consumer.close()
        }
      }
    }, "examples-consumer").start()

    new Thread(new Runnable {
      override def run(): Unit = {
        val props: Properties = commonConsumerProperties
        // we want every consumer get the entire models topic
        props.put("group.id", "predictor-models-" + util.UUID.randomUUID().toString)
        // start from the newest model
        props.put("auto.offset.reset", "latest")
        val consumer = new KafkaConsumer[String, String](props)

        try {
          consumer.subscribe(util.Arrays.asList("models"))
          println("Start consuming models")
          while (true) {
            val records = consumer.poll(Long.MaxValue).asScala
            records.foreach(r => {
              val modelAsString = r.value()
              println("New model: " + modelAsString)
              classifierProvider.update(modelAsString)
            })
          }
        } catch {
          case e: WakeupException =>
          case t: Throwable => {
            t.printStackTrace()
            System.exit(1)
          }
        } finally {
          consumer.close()
        }
      }
    }, "models-consumer").start()
  }

  private def commonConsumerProperties = {
    import org.apache.kafka.common.serialization.StringDeserializer

    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.deserializer", classOf[StringDeserializer].getName)
    props.put("value.deserializer", classOf[StringDeserializer].getName)
    props
  }
}
