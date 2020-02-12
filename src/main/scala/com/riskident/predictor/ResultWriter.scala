package com.riskident.predictor

class ResultWriter extends ResultHandler {
  override def processResult(predictionResult: PredictionResult): Unit = {
    // TODO: write to kafka
    println("Received prediction result: " + predictionResult)
  }
}
