#!/usr/bin/env bash
sbt clean
sbt compile
sbt "runMain com.riskident.predictor.PredictionService"
