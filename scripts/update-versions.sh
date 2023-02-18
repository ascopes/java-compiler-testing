#!/usr/bin/env bash
#
# Copyright (C) 2022 - 2023, the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

###
### Sets a new version in all POMs.
###

set -o errexit
set -o nounset

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"
cd "$(dirname "${BASH_SOURCE[0]}")/.."

function usage() {
  echo "USAGE: ${BASH_SOURCE[0]} [-h] -v <version>"
  echo "    -h             Show this message and exit."
  echo "    -v <version>   Set the version to apply."
  echo
}

version=""

while getopts "hv:" opt; do
  case "${opt}" in
    h) usage; exit 0;;
    v) version="${OPTARG}";;
    ?|*) err "Unrecognised argument"; usage; exit 1;;
  esac
done

if [ -z "${version}" ]; then err "Missing required argument"; usage; exit 1; fi

info "Updating versions"
run <<< "./mvnw -B -e versions:set -DnewVersion='${version}'"

info "Tracking all 'pom.xml' files with Git"
run <<< "find . -name 'pom.xml' -type f -exec git add -v {} \;"

success 'Complete!'
