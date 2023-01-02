#!/usr/bin/env bash
#
# Copyright (C) 2022 - 2023 Ashley Scopes
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
### Reapplies the license headers globally.
###

set -o errexit
set -o nounset
set -o pipefail

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

cd "$(dirname "${BASH_SOURCE[0]}")/.."

mvn_flags=(-B -e)

function usage() {
  echo "USAGE: ${BASH_SOURCE[0]} [-d] [-h]"
  echo "    -d   Show debugging output from Maven."
  echo "    -h   Show this message and exit."
}

function help() {
  usage
  exit 0
}

function unknown-option() {
  err "Unknown option '${1}'"
  usage
  exit 1
}

while getopts "dh" opt; do
  case "${opt}" in
    d) mvn_flags+=(-x) ;;
    h) help ;;
    *) unknown-option "${opt}" ;;
  esac
done

info "Cleaning workspace..."
run <<< "./mvnw --quiet ${mvn_flags[*]} clean"

info "Reapplying licenses across workspace..."
run <<< "./mvnw ${mvn_flags[*]} license:remove license:format"

info "Running verification (skipping tests)"
run <<<  "./mvnw ${mvn_flags[*]} verify -DskipTests"

success "Completed!"
