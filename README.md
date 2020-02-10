# Prediction service

Issues predictions of machine learning models on a stream of incoming data.

## Assumptions

As the data is coming with labels, we guess that the purpose of the application is to evaluate model quality and
not to provide predictions for making decisions.

We assume that input data is read from a kafka topic (kafka is listed in job description). We will use kafka
 for receiving model updates and publishing prediction results and model metrics as well.

We will use JSON for representing input/output.

## Error handling

The service will quit on errors in expectation that it's run by a container service and will be configured to restart 
on error, triggering an ops alert.
