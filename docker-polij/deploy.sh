#!/bin/bash

if [[ $# -lt 3 ]]; then
  echo "Usage: $0 <environment> <application> <tag>"
  exit 1
fi

. "`dirname $0`"/../config

ENVIRONMENT="$1"
IMAGE_NAME="$2"
IMAGE_TAG="${DOCKER_REGISTRY_BASE}/apps/${IMAGE_NAME}:$3"

case "${ENVIRONMENT}" in
  "test" | "prod")
    ;;
  *)
    echo "environment must be either 'test' or 'prod'"
    exit 1
    ;;
esac   

DEPLOYMENT=deployment.v1.apps/${IMAGE_NAME/_/}-${ENVIRONMENT}
CONTAINER=${IMAGE_NAME/_/}-${ENVIRONMENT}

set -x

kubectl set image ${DEPLOYMENT} ${CONTAINER}=${IMAGE_TAG}
kubectl rollout status --watch ${DEPLOYMENT}

