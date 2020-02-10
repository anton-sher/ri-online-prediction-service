package com.riskident.predictor

class ModelMetricsCalculator extends ResultHandler {
  override def processResult(predictionResult: PredictionResult): Unit = {
    // TODO: count TPR/FPR/AUC, publish to kafka
  }
}
