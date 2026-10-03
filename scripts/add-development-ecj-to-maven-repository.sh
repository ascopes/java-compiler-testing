#!/usr/bin/env bash
#
# Copyright (C) 2022 - 2024, the original author or authors.
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
### Shortcut to injecting a development version of ECJ's JAR into the local Maven registry.
###

set -o errexit
set -o nounset
[[ -n ${DEBUG+defined} ]] && set -o xtrace

if [[ $# -ne 2 ]]; then
  echo "USAGE: ${BASH_SOURCE[0]} <url-to-jar> <version>"
  echo "Inject the given URL to an ECJ JAR as the given version in the local Maven repository."
  echo ""
  echo "Arguments:"
  echo "    <url-to-jar>    The URL to the ECJ JAR to use."
  echo "    <version>       The version number to use for that JAR."
  echo ""
  exit 1
fi

url=$1
version=$2

maven_repository_dir=${M2_HOME:-${HOME}/.m2}/repository
target_dir=${maven_repository_dir}/org/eclipse/jdt/ecj/${version}

if [[ -d ${target_dir} ]]; then
  echo "Clearing existing directory out..."
  rm -Rvf "${target_dir}"
fi

echo "Making ECJ directory"
mkdir -pv "${target_dir}"

echo "Working out the latest ECJ POM to use..."
latest_published_version=$(curl --fail --silent https://repo1.maven.org/maven2/org/eclipse/jdt/ecj/maven-metadata.xml \
    | grep -oE "<version>.+?</version>" \
    | sed -E 's@</?version>@@g' \
    | tail -n 1)

echo "Making ECJ POM derived from the POM for v${latest_published_version}"
curl --fail --silent https://repo1.maven.org/maven2/org/eclipse/jdt/ecj/"${latest_published_version}"/ecj-"${latest_published_version}".pom \
    | sed 's@<version>'"${latest_published_version}"'</version>@<version>'"${version}"'</version>@g' \
    > "${target_dir}/ecj-${version}.pom"

echo "Downloading ECJ JAR"
curl --fail --silent "${url}" > "${target_dir}/ecj-${version}.jar"

echo "Computing SHA1 digest of ECJ JAR"
sha1sum < "${target_dir}/ecj-${version}.jar" | cut -d ' ' -f 1 > "${target_dir}/ecj-${version}.jar.sha1"

echo "Making dummy _remote.repositories file"

cat > "${target_dir}/_remote.repositories" <<-EOF
#NOTE: This is a Maven Resolver internal implementation file, its format can be changed without prior notice.
#$(date)
ecj-${version}.jar>central=
ecj-${version}.pom>central=
EOF

echo "Done."
