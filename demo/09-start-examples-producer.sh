#!/usr/bin/env bash
./print-examples.py | kafka_2.12-2.4.0/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic examples
