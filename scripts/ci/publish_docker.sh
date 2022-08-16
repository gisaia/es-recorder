#!/usr/bin/env bash
set -o errexit -o pipefail

echo "=> DOCKER PUSH"

RELEASE_VERSION=$1

SCRIPT_DIRECTORY="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"
PROJECT_ROOT_DIRECTORY="$SCRIPT_DIRECTORY"/../..
echo "=> RELEASE_VERSION:${RELEASE_VERSION}"
# Update project version and build
${SCRIPT_DIRECTORY}/build_maven.sh ${RELEASE_VERSION}

echo "=> Build es-recorder:${RELEASE_VERSION} docker image"
docker build -t docker.cloudsmith.io/gisaia/public/es-recorder:${RELEASE_VERSION} -f ${PROJECT_ROOT_DIRECTORY}/docker/docker-files/Dockerfile .

echo "=> Docker login cloudsmith"
echo "${DOCKER_CLOUDSMITH_PASSWORD}" | docker login docker.cloudsmith.io -u ${DOCKER_CLOUDSMITH_USERNAME} --password-stdin

echo "=> Push es-recorder:${RELEASE_VERSION} docker images"
docker push docker.cloudsmith.io/gisaia/public/es-recorder:${RELEASE_VERSION}

IFS='-' # - is set as delimiter
read -ra SEMVER_PARTS <<< "$RELEASE_VERSION" # $RELEASE_VERSION is read into an array as tokens separated by IFS
if [ "${#SEMVER_PARTS[@]}" -eq "1" ]; then
  # no pre-release found in semantic version => it's a release
  echo "=> Tag es-recorder:latest docker image"
  docker tag docker.cloudsmith.io/gisaia/public/es-recorder:${RELEASE_VERSION} docker.cloudsmith.io/gisaia/public/es-recorder:latest
  docker push docker.cloudsmith.io/gisaia/public/es-recorder:latest
fi
