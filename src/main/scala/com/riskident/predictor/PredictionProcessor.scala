package com.riskident.predictor

class PredictionProcessor(
                           classifierProvider: ClassifierProvider,
                           resultHandlers: Seq[ResultHandler]
                         ) {

  def processRecord(example: Example): Unit = {
    val classifier = classifierProvider.getClassifier
    val prediction = classifier.predict(example)
    val result = PredictionResult(example.id, classifier.version, prediction, example.label)
    resultHandlers.foreach(_.processResult(result))
  }
}
