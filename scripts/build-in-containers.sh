#!/usr/bin/env bash
###
### Copyright (C) 2022 Ashley Scopes
###
### Licensed under the Apache License, Version 2.0 (the "License");
### you may not use this desired_jacoco_file except in compliance with the License.
### You may obtain a copy of the License at
###
###    http://www.apache.org/licenses/LICENSE-2.0
###
### Unless required by applicable law or agreed to in writing, software
### distributed under the License is distributed on an "AS IS" BASIS,
### WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
### See the License for the specific language governing permissions and
### limitations under the License.
###
### Script that will build this project in the given range of JDK versions.
###

set -o errexit
set -o pipefail

default_command="./mvnw -T8C clean package"

if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: docker is not installed on this system. Please ensure it is on your "
  echo "  \$PATH, and then try again."
  exit 2
fi

function usage() {
  echo "USAGE: ${1} [-c <command>] [-h] -v <version-range>"
  echo "    -c   <command>         The command to run. If unspecified, this will default to"
  echo "                           ${default_command}"
  echo "    -h                     Show this message and exit."
  echo "    -v   <version-range>   Set the version range to build with."
  echo
  echo "  Version ranges can be a single digit (e.g. 17 for JDK 17), or a closed range, delimited"
  echo "  with a hyphen \`-' character (e.g. 11-17 for JDK 11, 12, 13, ..., 16, and 17)."
  echo
}

command="${default_command}"

while getopts ":c:v:h" opt; do
  case "${opt}" in
  c)
    command="${OPTARG}"
    ;;
  h)
    usage "${0}"
    exit 0
    ;;
  v)
    if ! echo "${OPTARG}" | grep -qE "^[0-9]+(-[0-9]+)?$"; then
      echo "ERROR: Invalid range syntax for version."
      usage "${0}"
      exit 1
    else
      first_version=$(echo "${OPTARG}" | cut -d- -f 1)
      last_version=$(echo "${OPTARG}" | cut -d- -f 2)
    fi
    ;;
  esac
done

if [ -z ${first_version:+undef} ]; then
  echo "ERROR: Missing parameter '-v <version-range>'"
  usage "${0}"
  exit 1
fi

if [ -z ${M2_HOME+undef} ]; then
  export M2_HOME="${HOME:-$HOMEDIR}/.m2"
fi

workspace_dir="$(realpath $(dirname ${BASH_SOURCE[0]:-$0})/..)"
container_workspace_dir="/src"
m2_container_dir=/m2

mkdir target 2>&1 || true

for version in $(seq ${first_version} ${last_version}); do
  # AWS Elastic Container Registry will not rate-limit us like DockerHub will, so prefer them
  # instead.
  image="public.ecr.aws/docker/library/openjdk:${version}"

  if [ -z "$(docker image ls "${image}" --quiet)" ]; then
    echo -e "\e[1;33mPulling \e[3;34m${image}\e[0;1;33m, as it was not found on this system.\e[0m"
    docker pull "${image}"
  fi

  echo -en "\e[0;1;33mRunning command \e[3;35m${command}\e[0;1;33m in "
  echo -e "\e[3;34m${image}\e[0;1;33m container...\e[0m"

  docker run \
    -e "MAVEN_OPTS=-Dstyle.color=always" \
    -e "M2_HOME=${m2_container_dir}" \
    --name "jct-build-in-containers-jdk-${version}" \
    -t \
    -u "$(id -u "${USER}")" \
    -v "$(realpath "${M2_HOME}"):${m2_container_dir}" \
    -v "${workspace_dir}:${container_workspace_dir}" \
    -w "${container_workspace_dir}" \
    --rm \
    "${image}" \
    ${command} \
    2>&1 |
    while read line; do
      printf "\e[1;33m[JDK %2d]\e[0m %s\n" "${version}" "${line}"
    done
done
