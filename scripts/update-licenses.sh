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

if [ "$#" -gt 0 ] && ("${1}" = "--help" ] || [ "${1}" = "-h" ]); then
  echo "USAGE: ${0} [-h | --help] [--debug]"
  echo "Reapply license headers across this repository."
  echo ""
  echo "    -h | --help    Show this message and exit."
  echo "    --debug        Show verbose Maven output."
  echo
  exit 0
fi

cd "$(dirname "${BASH_SOURCE[0]}")/.."

mvn_flags=(-B -e)

if [ "$#" -gt 0 ] && [ "${1}" = "--debug" ]; then
  mvn_flags+=(-x)
else
  mvn_flags+=()
fi

info "Cleaning workspace..."
run "./mvnw --quiet ${mvn_flags[@]} clean"

info "Reapplying licenses across workspace..."
run "./mvnw ${mvn_flags[@]} license:remove license:format"

info "Running verification (skipping tests)"
run "./mvnw ${mvn_flags[@]} verify -DskipTests"

success "Completed!"
