package com.riskident.predictor

class BlockingClassifierProvider extends ClassifierProvider {
  var currentClassifier: Classifier = _

  override def getClassifier: Classifier = {
    this.synchronized {
      while (currentClassifier == null) {
        wait()
      }
      return currentClassifier
    }
  }

  def update(serializedClassifier: String): Unit = {
    this.synchronized {
      currentClassifier = deserialize(serializedClassifier)
      notifyAll()
    }
  }

  def deserialize(serializedClassifier: String): Classifier = {
    import spray.json._
    import DefaultJsonProtocol._

    val classifierJson = serializedClassifier.parseJson.asJsObject

    val modelType = classifierJson.fields("model_type").convertTo[String]
    if (modelType != "LogReg") {
      throw new IllegalArgumentException("Unsupported model type: " + modelType)
    }

    val modelVersion = classifierJson.fields("model_version").convertTo[String]
    val featureWeights = classifierJson.fields("weights").convertTo[Map[String, Double]]
    val bias = classifierJson.fields("bias").convertTo[Double]

    new LogisticRegression(modelVersion, featureWeights, bias)
  }
}
