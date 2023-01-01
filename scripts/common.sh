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
### Common functions.
###

unset undefined > /dev/null 2>&1 || true

function log() {
  printf "\033[3;37m$(date "+%H:%M:%S.%3N") \033[0;37m| \033[0;1;${1}m%s:\033[0;${1}m %s\033[0m\n" \
      "${2}" "${3}" >&2
}

function err()     { log 31 ERROR   "${@}"; }
function success() { log 32 SUCCESS "${@}"; }
function warn()    { log 33 WARNING "${@}"; }
function info()    { log 35 INFO    "${@}"; }

function in-path() {
  command -v "${1}" > /dev/null 2>&1
  return "${?}"
}

function run() {
  # Run in subshell to enable correct argument requoting.
  /usr/bin/env bash \
       1> >(while IFS=$"\r\n" read -r line; do log 36 "STDOUT" "${line}"; done) \
       2> >(while IFS=$"\r\n" read -r line; do log 37 "STDERR" "${line}"; done) \
       <<< "PS4=$'Running: \e[1;33m$ \e[0;3;33m'; set -euxo pipefail; ${@}"

  # Wait for stdout and stderr to flush for ~10ms
  sleep 0.01
}
