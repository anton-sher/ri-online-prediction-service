for model_json in \
 '{"model_version" : "lr1", "model_type":"LogReg", "weights": {"x1": 6.08, "x2": 7.78, "x3": 6.34, "x4": 8.05, "x5": 3.14}, "bias": 61.35}' \
 '{"model_version" : "lr2", "model_type":"LogReg", "weights": {"x1": 8.46, "x2": 1.74, "x3": 6.08, "x4": 4.25, "x5": 1.92}, "bias": 71.37}' \
 '{"model_version" : "lr3", "model_type":"LogReg", "weights": {"x1": 6.53, "x2": 5.46, "x3": 0.0, "x4": 9.95, "x5": 6.29}, "bias": 43.3}' \
 '{"model_version" : "lr4", "model_type":"LogReg", "weights": {"x1": 3.2, "x2": 7.32, "x3": 1.46, "x4": 2.29, "x5": 4.26}, "bias": 94.81}' \
 '{"model_version" : "lr5", "model_type":"LogReg", "weights": {"x1": 2.71, "x2": 0.82, "x3": 8.54, "x4": 0.21, "x5": 2.1}, "bias": 66.25}'
do
  echo $model_json
  sleep 5
done
