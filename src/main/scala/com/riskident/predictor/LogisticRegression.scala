package com.riskident.predictor

class LogisticRegression(
                          modelVersion: String,
                          featureWeights: Map[String, Double],
                          bias: Double
                        ) extends Classifier {

  override def predict(example: Example): Prediction = {
    val linear = bias + featureWeights.keys
      .map(k => featureWeights(k) * example.features.getOrElse(k, 0.0))
      .sum
    val probability = activation(linear)
    Prediction(probability, probability > 0.5)
  }

  override def version: String = modelVersion

  def activation(x: Double): Double = 1.0 / (1.0 + Math.exp(-x))
}
