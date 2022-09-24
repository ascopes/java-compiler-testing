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

function log() {
  printf "\e[1;${1}m%s: \e[0;${1}m%s\e[0m\n" "${2}" "${3}" >&2
}

default_command="./mvnw clean package -B"

if ! command -v docker; then
  log 31 ERROR "docker is not installed on this system. Please ensure it is on your \$PATH, and then try again."
  exit 2
fi

function usage() {
  echo "USAGE: ${1} [-c <command>] [-h] -v <version-range>" >&2
  echo "    -c   <command>         The command to run. If unspecified, this will default to" >&2
  echo "                           ${default_command}" >&2
  echo "    -h                     Show this message and exit." >&2
  echo "    -v   <version-range>   Set the version range to build with." >&2
  echo >&2
  echo "  Version ranges can be a single digit (e.g. 17 for JDK 17), or a closed range, delimited" >&2
  echo "  with a hyphen \`-' character (e.g. 11-17 for JDK 11, 12, 13, ..., 16, and 17)." >&2
  echo >&2
  echo "Additional options can also be passed to Maven using the \$MAVEN_OPTS environment variable." >&2
  echo "The \$M2_HOME environment variable will be used to mount the local Maven repository. If this" >&2
  echo "is not defined, then ~/.m2 will be used on the host." >&2
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
      log 31 ERROR "Invalid range syntax for version."
      usage "${0}"
      exit 1
    else
      first_version="$(echo "${OPTARG}" | cut -d- -f 1)"
      last_version="$(echo "${OPTARG}" | cut -d- -f 2)"
    fi
    ;;
  esac
done

if [ -z ${first_version+undef} ]; then
  log 31 ERROR "Missing parameter '-v <version-range>'"
  usage "${0}"
  exit 1
fi

if [ -z ${M2_HOME+undef} ]; then
  export M2_HOME="${HOME:-${HOMEDIR}}/.m2"
  mkdir -v "${M2_HOME}" 2>/dev/null || true
fi 

log 32 INFO "Will mount M2_HOME as ${M2_HOME}"

workspace_dir="$(realpath "$(dirname "${BASH_SOURCE[0]:-$0}")"/..)"
container_workspace_dir="/src"
m2_container_dir=/m2

mkdir -v target 2>/dev/null || true

docker_flags=()
maven_opts=()

if [ -t 0 ] || [ -t 1 ]; then
  docker_flags+=("-t")
  maven_opts+=("-Dstyle.color=always")
fi

for version in $(seq "${first_version}" "${last_version}"); do
  # AWS Elastic Container Registry will not rate-limit us like DockerHub will, so prefer them
  # instead.
  image="public.ecr.aws/docker/library/openjdk:${version}"

  if [ -z "$(docker image ls "${image}" --quiet)" ]; then
    log 32 INFO "Pulling ${image} as it was not found on this system."
    docker pull "${image}"
  fi

  log 32 INFO "Running command ${command} in ${image} container..."

  docker run \
    -e JAVA_TOOL_OPTIONS \
    -e "MAVEN_OPTS=${MAVEN_OPTS} ${maven_opts[@]}" \
    -e "M2_HOME=${m2_container_dir}" \
    --init \
    --name "jct-build-in-containers-jdk-${version}" \
    -u "$(id -u "${USER}")" \
    -v "$(realpath "${M2_HOME}"):${m2_container_dir}" \
    -v "${workspace_dir}:${container_workspace_dir}" \
    -w "${container_workspace_dir}" \
    --rm \
    "${docker_flags[@]}" \
    "${image}" \
    ${command} \
    2>&1 |
    while read -r line; do
      log 33 "JDK ${version}" "${line}"
    done
done
