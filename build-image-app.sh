#!/bin/bash
# Script Buid Image Appfluentd

echo "Script Buid Image Appfluentd"
echo "Preparazione Ambiente"


./gradlew clean build

mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)

echo "Build Image"

docker build  --build-arg DEPENDENCY=build/dependency -t localhost:5000/appfluentd/spam-fluentd:1.0 --rm=true .

echo "Push Image"

docker push localhost:5000/appfluentd/spam-fluentd:1.0


echo "Finito!!!"

docker images | grep 'appfluentd'
