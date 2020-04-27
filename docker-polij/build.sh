#!/bin/bash

. "`dirname $0`"/../config
[[ -r "`dirname $0`"/../config.local ]] && . "`dirname $0`"/../config.local  

RESIN_DEFAULT_IMAGE="${DOCKER_REGISTRY_BASE}/resin-polij-jdk11"
RESIN_TAG="latest"

declare -a ARGS

usage() {
  echo "Usage: $0 [-d|--deploy <environment>] [-R|--resin-image <resin_image>] [-r|--resin-tag <resin_tag>] <app_name> <app_tag>"
  exit 1
}

while [[ $# -gt 0  ]]; do
  case "$1" in
  -d | --deploy)
    DEPLOY=1
    shift
    DEPLOY_ENV="$1"
    ;;
  -R | --resin-image)
    shift
    RESIN_DEFAULT_IMAGE="$1"
    ;;
  -r | --resin-tag)
    shift
    RESIN_TAG="$1"
    ;;
  *)
    ARGS+=($1)
    ;;
  esac
  shift
done

IMAGE_NAME="${ARGS[0]}"
IMAGE_TAG="${ARGS[1]}"
DEPLOYMENT_NAME="${IMAGE_NAME/_/}-${DEPLOY_ENV}"

if [[ -z "${IMAGE_NAME}" || -z "${IMAGE_TAG}" ]]; then
  usage
fi
  
if [[ ! -d "`dirname $0`/files/${IMAGE_NAME}" ]]; then
  echo "No files dir found for application ${IMAGE_NAME}"
  exit 1
fi

if [[ -z "${RESIN_TAG}" ]]; then
  usage
fi

if [[ "${DEPLOY}" == 1 ]]; then
  if [[ -z "${DEPLOY_ENV}" ]]; then
    usage
  fi

  # check that the target deployment is actually there
  if ! kubectl get deployment "${DEPLOYMENT_NAME}" &>/dev/null; then
    echo "Deployment '${DEPLOYMENT_NAME}' not found -- if this is a new application, create the deployment before using this script"
    exit 1
  fi
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
  docker login -u "${DOCKER_USER}" --password-stdin "${DOCKER_REGISTRY_HOST}" <<< "${DOCKER_PASSWORD}" || exit 1
  docker push ${REMOTE_TAG} || exit 1
  docker push ${DOCKER_REGISTRY_BASE}/apps/${IMAGE_NAME}:latest || exit 1

  if [[ "${DEPLOY}" == 1 ]]; then
    ./deploy.sh "${DEPLOY_ENV}" "${IMAGE_NAME}" "${IMAGE_TAG}"
  fi

)



