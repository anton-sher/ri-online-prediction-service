package com.riskident.predictor

class PredictionService() {
  def run(): Unit = {
    // TODO run kafka consumer for examples
    // TODO run kafka consumer for models
  }
}

object PredictionService {
  def main(args: Array[String]): Unit = {
    new PredictionService().run()
  }
}
