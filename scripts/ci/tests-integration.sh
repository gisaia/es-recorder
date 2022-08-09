#!/bin/bash
set -o errexit -o pipefail

function clean_docker {
  ./scripts/docker-clean.sh
  echo "===> clean maven repository"
	docker run --rm \
		-w /opt/maven \
		-v $PWD:/opt/maven \
		-v $HOME/.m2:/root/.m2 \
		maven:3.8.5-openjdk-17 \
		mvn clean
}

function clean_exit {
  ARG=$?
  echo "===> Exit status = ${ARG}"
  echo "===> es-recorder logs"
  docker logs es-recorder
  clean_docker
  exit $ARG
}
trap clean_exit EXIT

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/../..

function start_stack() {
  export ARLAS_AUTH_PUBLIC_URIS=".*"
  ./scripts/docker-clean.sh
  ./scripts/docker-run.sh --build
}

function test_rest_server() {
    start_stack
    echo "===> run integration tests suite"
    docker run --rm \
        -w /opt/maven \
        -v $PWD:/opt/maven \
        -v $HOME/.m2:/root/.m2 \
        -e ES_RECORDER_HOST="es-recorder" \
        -e ES_RECORDER_PREFIX="es_recorder" \
        -e ES_RECORDER_APP_PATH=${ES_RECORDER_APP_PATH} \
        --network esrecorder_default \
        maven:3.8.5-openjdk-17 \
        mvn -Dit.test=RecorderIT verify -DskipTests=false -DfailIfNoTests=false
}

test_rest_server