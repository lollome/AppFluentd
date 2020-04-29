#!/bin/bash

PROFILE=${1:-jdk8}

(
  cd "`dirname $0`"

  if [[ ! -r "config.${PROFILE}" ]]; then
    echo "'config.${PROFILE}' not found"
    exit 1
  fi

  . ../config
  [[ -r ../config.local ]] && . ../config.local

  . "config.${PROFILE}"

  RESIN_TAG=${RESIN_IMAGE_NAME}:${RESIN_VERSION}
  RESIN_REMOTE_TAG=${DOCKER_REGISTRY_BASE}/${RESIN_TAG}

  RESIN_SRC="resin-${RESIN_VERSION}-src.tar.gz"

  echo "Will build ${RESIN_TAG} with resin ${RESIN_VERSION}, openjdk ${OPENJDK_IMAGE}"

  mkdir -p files/resin-sources
  
  if [[ ! -f "files/resin-sources/${RESIN_SRC}" ]]; then
    wget "http://caucho.com/download/${RESIN_SRC}" -P files/resin-sources || exit 1
  fi


  docker build -t ${RESIN_TAG} \
	       -t ${RESIN_IMAGE_NAME}:latest \
	       -t ${RESIN_REMOTE_TAG} \
	       -t ${DOCKER_REGISTRY_BASE}/${RESIN_IMAGE_NAME}:latest \
	       --build-arg RESIN_VERSION=${RESIN_VERSION} \
	       --build-arg RESIN_SRC=${RESIN_SRC} \
	       --build-arg OPENJDK_IMAGE=${OPENJDK_IMAGE} \
	       --build-arg OPENJDK_BUILD_IMAGE=${OPENJDK_BUILD_IMAGE} \
	       . || exit 1
  docker push ${RESIN_REMOTE_TAG} || exit 1
  docker push ${DOCKER_REGISTRY_BASE}/${RESIN_IMAGE_NAME}:latest || exit 1
)

