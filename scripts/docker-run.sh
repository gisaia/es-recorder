#!/bin/bash
set -e

SCRIPT_DIRECTORY="$(cd "$(dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd)"
PROJECT_ROOT_DIRECTORY="$(dirname "$SCRIPT_DIRECTORY")"
BUILD_OPTS="--no-build"
DOCKER_COMPOSE="${PROJECT_ROOT_DIRECTORY}/docker/docker-files/docker-compose.yml"

for i in "$@"
do
case $i in
  --build)
  BUILD_OPTS="--build"
  shift # past argument with no value
  ;;
  *)
    # unknown option
  ;;
esac
done

function clean_exit {
  ARG=$?
  exit $ARG
}
trap clean_exit EXIT

export ES_RECORDER_VERSION=`xmlstarlet sel -t -v /_:project/_:version pom.xml`

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/..

# PACKAGE
echo "===> compile ES RECORDER"
docker run --rm \
    -w /opt/maven \
	-v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	maven:3.8.5-openjdk-17 \
	mvn clean install -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
echo "es-recorder:${ES_RECORDER_VERSION}"

echo "===> start ES RECORDER stack"
docker-compose -f ${DOCKER_COMPOSE} --project-name esrecorder up -d ${BUILD_OPTS}

echo "===> wait for es-recorder up and running"
docker run --network esrecorder_default --rm busybox sh -c 'i=1; until nc -w 2 es-recorder 9997; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'
