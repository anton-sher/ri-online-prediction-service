package com.riskident.predictor

class BlockingClassifierProviderTest extends org.scalatest.FunSuite {
  def testDeserialize(): Unit = {
    val provider = new BlockingClassifierProvider()
    val classifier = provider.deserialize(
      """{"model_version":"123", "weights": {"a": 1.0, "b": 2.0, "c": 3.0}, "bias": 4.0}"""
    )

    assert(classifier.version == "123")
    val prediction = classifier.predict(Example("123", Map("a" -> 1.0, "b" -> 2.0, "c" -> 3.0), 0.0))
    assert(Math.log(prediction.probability / (1 - prediction.probability)) == 18.0)
    assert(!prediction.result)
  }
}
