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
### Shortcut to running the ECJ compiler.
###

set -o errexit
set -o nounset

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ecj_dir="${project_dir}/target/ecj"
if [[ ! -d "${ecj_dir}" ]] || [[ ! "$(ls -A "${ecj_dir}")" ]]; then
  mkdir -p "${ecj_dir}"

  echo "[[Determining ECJ version to use, please wait...]]" >&2
  ecj_version="$("${project_dir}/mvnw" -f "${project_dir}/pom.xml" help:evaluate \
      --offline \
      --quiet \
      -Dexpression="ecj.version" \
      -DforceStdout)"

  echo "[[Downloading ECJ ${ecj_version} artifact, please wait...]]" >&2
  "${project_dir}/mvnw" dependency:get \
      --quiet \
      -Dartifact="org.eclipse.jdt:ecj:${ecj_version}"

  echo "[[Copying ECJ ${ecj_version} artifact into ${ecj_dir}, please wait...]]" >&2
  "${project_dir}/mvnw" dependency:copy \
      --offline \
      --quiet \
      -Dartifact="org.eclipse.jdt:ecj:${ecj_version}" \
      -DoutputDirectory="${ecj_dir}" \
      -Dtransitive=true

  echo "[[Completed download of ECJ ${ecj_version}.]]" >&2
fi

java -jar "$(find "${ecj_dir}" -type f -name "*.jar" -print | head -n 1)" "${@}"
