#!/bin/bash
set -e

SCRIPT_DIRECTORY="$(cd "$(dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd)"
PROJECT_ROOT_DIRECTORY="$(dirname "$SCRIPT_DIRECTORY")"
DOCKER_COMPOSE="${PROJECT_ROOT_DIRECTORY}/docker/docker-files/docker-compose.yml"
DOCKER_COMPOSE_ES="${PROJECT_ROOT_DIRECTORY}/docker/docker-files/docker-compose-elasticsearch.yml"

function clean_exit {
  ARG=$?
  exit $ARG
}
trap clean_exit EXIT

echo "===> stop arlas RECORDER stack"
docker-compose -f ${DOCKER_COMPOSE} -f ${DOCKER_COMPOSE_ES} --project-name esrecorder down -v