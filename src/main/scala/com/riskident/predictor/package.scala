package com.riskident

package object predictor {

  case class Example(
                      id: String, // unique ID for correlating output and logged data
                      features: Map[String, Double], // new model might support different features
                      label: Double // 1 or 0
                    )

  case class Prediction(
                         probability: Double, // useful to log for metrics like AUC
                         result: Boolean
                       )

  case class PredictionResult(
                               exampleId: String,
                               modelVersion: String,
                               prediction: Prediction,
                               actualLabel: Double
                             )

  trait Classifier {
    def predict(example: Example): Prediction

    def version: String // for logging; should identify both model structure and its weights
  }

  trait ClassifierProvider {
    def getClassifier: Classifier
  }

  trait ResultHandler {
    def processResult(predictionResult: PredictionResult)
  }
}
