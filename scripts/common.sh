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
### Common functions.
###

# We use undefined as a placeholder elsewhere.
unset undefined &> /dev/null || true

# Helper to log a coloured message to stderr with a timestamp.
# Arg 1 = colour code (e.g. 33)
# Arg 2 = log level name (e.g. WARNING)
# Arg 3 = Message to display (e.g. "Something ain't right here")
function __log() {
  printf "\033[3;37m$(date "+%H:%M:%S.%3N") \033[0;37m| \033[0;1;${1}m%7s:\033[0m %s\033[0m\n" \
      "${2}" "${3}" >&2
}

# Log an error.
function err() {
  __log 31 ERROR "${@}"
}

# Log a success.
function success() {
  __log 32 SUCCESS "${@}"
}

# Log a warning.
function warn() {
  __log 33 WARNING "${@}"
}

# Log an info message.
function info() {
  __log 35 INFO "${@}"
}

# Ensure that each variable name provided is set in the current shell, otherwise print an error
# and fail at the end.
# Used by the deployment pipeline.
function ensure-set() {
  local return_code
  return_code=0

  for variable_name; do
    if [ "${!variable_name}" = "" ]; then
      err "Variable ${variable_name} was empty or not set"
      return_code=1
    fi
  done

  return "${return_code}"
}

# Dump the contents of a file.
function dump() {
  local name
  name="$(basename "${1?Provide a file to dump as the first argument}}")"
  while IFS=$'\r\n' read -r line; do __log 37 "${name}" "${line}"; done < "${1}"
}

# Visually run the command, showing the command being run, stdout, and stderr to the user.
# Pass the commands to run in via a heredoc as stdin. Incorrect usage will dump an error and
# terminate the current shell.
function run() {
  # Run in subshell to enable correct argument re-quoting.
  local file
  file="$(mktemp)"

  if [ "$#" -gt 0 ]; then
    err "Function <run> called incorrectly. Pass script to run via stdin rather than as arguments."
    err "Invocation: <${*}>"
    exit 1
  fi

  {
    echo "#!/usr/bin/env bash"
    echo "PS4=$'Running: \e[1;33m$ \e[0;3;33m'"
    echo "set -o errexit"
    echo "set -o nounset"
    echo "set -o xtrace"
    cat -
  } >> "${file}"

  if [[ ! -z ${CI+undefined} ]]; then echo "::group::$(head -n "${file}")"; fi

  set +e
  /usr/bin/env bash \
       1> >(while IFS=$'\r\n' read -r line; do __log 36 STDOUT "${line}"; done) \
       2> >(while IFS=$'\r\n' read -r line; do __log 37 STDERR "${line}"; done) \
       "${file}"
  local return_code="${?}"
  rm "${file}"
  set -e

  # Wait for stdout and stderr to flush for ~10ms
  sleep 0.05

  if [[ ! -z ${CI+undefined} ]]; then echo "::endgroup::"; fi

  return "${return_code}"
}
