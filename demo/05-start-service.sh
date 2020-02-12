#!/usr/bin/env bash
cd ..
sbt clean
sbt compile
sbt "runMain com.riskident.predictor.PredictionService"
