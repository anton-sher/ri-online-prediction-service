name := "ri-online-prediction-service"

version := "0.1"

scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.1.0" % "test",
  "io.spray" %%  "spray-json" % "1.3.4",
  "org.apache.kafka" % "kafka-clients" % "2.4.0",
  "org.apache.kafka" %% "kafka" % "2.4.0" % "test"
)
