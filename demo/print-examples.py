#!/usr/bin/env python3
import json
import random
import time

for i in range(1, 1000):
    features = {
        'x' + str(k): random.random() * 200 - 100 for k in range(1,6)
    }
    print(json.dumps({'id' : str(i), 'label': random.choice([0.0, 1.0]), 'features': features}), flush=True)

    time.sleep(0.1)
