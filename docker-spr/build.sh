#!/bin/bash

mkdir -p files
  
cp ../build/libs/*.war files/spam-fluentd.war 

. "`dirname $0`"/../config
[[ -r "`dirname $0`"/../config.local ]] && . "`dirname $0`"/../config.local  

RESIN_DEFAULT_IMAGE="${DOCKER_REGISTRY_BASE}/resin-polij"
RESIN_TAG="4.0.62"


IMAGE_NAME="spam-fluentd"
IMAGE_TAG="1.0"

if [[ -z "${IMAGE_NAME}" || -z "${IMAGE_TAG}" ]]; then
  usage
fi


if [[ -z "${RESIN_TAG}" ]]; then
  usage
fi

if [[ "${RESIN_TAG%:*}" == "${RESIN_TAG}" ]]; then
  RESIN_TAG="${RESIN_DEFAULT_IMAGE}:${RESIN_TAG}"
fi

LOCAL_TAG="${IMAGE_NAME}:${IMAGE_TAG}"
REMOTE_TAG="${DOCKER_REGISTRY_BASE}/apps/${LOCAL_TAG}"

echo "Building '${LOCAL_TAG}' from '${RESIN_TAG}'..."

(
  cd "`dirname $0`"

  docker build -t ${LOCAL_TAG} \
	       -t ${IMAGE_NAME}:latest \
	       -t ${REMOTE_TAG} \
	       -t ${DOCKER_REGISTRY_BASE}/apps/${IMAGE_NAME}:latest \
	       --build-arg BASE_IMAGE=${RESIN_TAG} \
	       --build-arg APPLICATION=${IMAGE_NAME} \
	       . || exit 1
  docker push ${REMOTE_TAG} || exit 1
  docker push ${DOCKER_REGISTRY_BASE}/apps/${IMAGE_NAME}:latest || exit 1


)



