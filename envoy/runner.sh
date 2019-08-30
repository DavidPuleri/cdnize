#!/usr/bin/env bash

java -jar $1 & envoy -c service.yaml --service-cluster service$2

