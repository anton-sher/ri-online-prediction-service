# Prediction service

Solution for the [Data Engineer coding challenge](./Data%20Engineer%20coding%20challenge.pdf).

Issues predictions of machine learning models on a stream of incoming data.

## Running the code

To run the code, following is needed:

- bash
- SBT
- python3 (for example generation code)

Tested on a mac OS 10.13.6, SBT from homebrew, python3 from pyenv.

Folder [demo](./demo) contains shell scripts with names starting with numbers. These can be run
in order to demonstrate the application using local kafka server. Some (most) scripts run the apps in foreground and
 should be run in their own terminal sessions.
 
```shell script
cd demo
./01-get-kafka.sh
# ...
./05-start-service.sh
# open new terminal window
# ...
```

These scripts push provided example models and generated examples to respective kafka topics.

Output of the application is written to two kafka topics, "predictions" and "statistics" and can be seen in respective
consumers.

## Design decisions for requirements

> At any point in time over its life cycle, the predictor should be able to make predictions, i.e. have a model available.
 
Predictor starts without a model, but listens on a kafka stream of models from the beginning.
 Before it reads a model from the stream it won't start serving predictions.
It tries to start with the latest model from the stream (implementation is a bit naive now).

> Furthermore, it should be able to switch to using an updated model without being restarted.
> To accomplish this, the predictor is listening on a second stream consisting of machine learning models. 
> As soon as a new model is available in the models stream, the component should stop predicting, switch over
> to the new model and then resume predicting using the new model

Switchover happens in a synchronized block. As soon as a model is available, the block is entered and the current
model cannot be retrieved until it's replaced.

Predictions happen in a separate thread, it will finish predicting with the current model and then wait until the 
newer one is loaded.

## Assumptions

As the data is coming with labels, we guess that the purpose of the application is to evaluate model quality and
 not to provide predictions for making decisions. Thus emphasis in design will be on processing everything, failing fast
 and logging as much as possible. The service will quit on errors in expectation that it's run by a container service
 and will be configured to restart on error, triggering an ops alert.
 
Real-world service, however, might instead need to skip erroneous input and log a metric. (It would also not use
hardcoded kafka endpoint on localhost and allow setting offsets and consumer groups).

We assume that input data is read from a kafka topic (kafka is listed in job description). We will use kafka
 for receiving model updates and publishing prediction results and model metrics as well.

The application uses same kafka consumer group for examples and unique consumer groups for models.

Like this, it can be scaled out to process examples faster, and every replica will see all models.

## Data model

We use JSON for representing input/output.

### Classifier model

Every model has a version that is logged together with prediction result for future analysis.
We use JSON to serialize the model and foresee a type field that tells the service how to load the model.
A logistic regression is described as below; more elaborate models (XGBoost or Tensorflow bundle) may have to be loaded
 from external storage, so an additional field with model URI could be added for such case.  

Example:

```json
{
  "model_version": "lr1",
  "model_type": "LogReg",
  "weights": {
    "x1": 6.08,
    "x2": 7.78,
    "x3": 6.34,
    "x4": 8.05,
    "x5": 3.14
  },
  "bias": 61.35
}
```

### Prediction examples

Every example carries an ID that is logged for later analysis. Feature values are represented as a dictionary so it's 
easier to add/remove features as model evolves without changing the interface. 

Example:

```json
{
  "id": "8",
  "label": 1.0,
  "features": {
    "x1": 52.410052582558805,
    "x2": 11.562737129397437,
    "x3": 73.41992488838524,
    "x4": 27.7229531133118,
    "x5": 22.015671801302574
  }
}
```

### Prediction results

Result contains identifiers of the model, of the example, predicted label and also predicted probability.
 Logging predicted probability would allow analyzing AUC in addition to other model performance metrics and
 tuning precision/recall tradeoff.

```json
{
  "exampleId": "5",
  "modelVersion": "lr5",
  "prediction": {
    "probability": 7.397488455717403E-88,
    "result": false
  },
  "actualLabel": 1.0
}
```

