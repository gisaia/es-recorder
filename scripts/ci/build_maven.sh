#!/usr/bin/env bash
set -o errexit -o pipefail

RELEASE_VERSION=$1

SCRIPT_DIRECTORY="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"
PROJECT_ROOT_DIRECTORY="$SCRIPT_DIRECTORY"/../..

echo "=> Docker login"
echo "${DOCKER_PASSWORD}" | docker login -u ${DOCKER_USERNAME} --password-stdin

echo "=> Build es-recorder jar v${RELEASE_VERSION}"
echo "===> update project version to ${RELEASE_VERSION}"
docker run --rm \
        -w /opt/maven \
        -v $PWD:/opt/maven \
        -v $HOME/.m2:/root/.m2 \
        -e RELEASE_VERSION=${RELEASE_VERSION} \
        maven:3.8.5-openjdk-17 \
        mvn -q clean versions:set -DnewVersion=${RELEASE_VERSION}
sed -i.bak 's/\"API_VERSION\"/\"'${RELEASE_VERSION}'\"/' ${PROJECT_ROOT_DIRECTORY}/es-recorder-rest/src/main/java/com/gisaia/recorder/rest/service/RecorderRestService.java

echo "===> build es-recorder v${RELEASE_VERSION}"
docker run --rm \
    -w /opt/maven \
	-v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	maven:3.8.5-openjdk-17 \
	mvn -q clean install