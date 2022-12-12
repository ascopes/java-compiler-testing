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

###
### Script to run in CI to prefix any Surefire report and test case names with the Java version
### that the test applies to, and to rename the jacoco.xml files to match the Java version in use.
###

set -o errexit
set -o noclobber
set -o nounset
set -o pipefail

unset undefined > /dev/null 2>&1 || true
ci_java_version="${1?Pass the Java version as the first argument to this script!}"
ci_os="${2?Pass the OS name as the second argument to this script!}"

function log {
  printf "\033[1;${1}m%s:\033[0;${1}m %s\033[0m\n" "${2}" "${3}" >&2
}

function err       { log 31 ERROR   "${@}"; }
function success   { log 32 SUCCESS "${@}"; }
function warn      { log 33 WARNING "${@}"; }
function info      { log 34 INFO    "${@}"; }
function stage     { log 35 STAGE   "${@}"; }

function in-path {
  command -v "${1}" > /dev/null 2>&1
  return "${?}"
}

stage "Looking for xsltproc binary..."

# If we don't have xsltproc installed, try to resolve it first.
# If we are in CI, always attempt to install it so we ensure that it
# is up-to-date first.
if [[ -z ${CI+undefined} ]] && in-path xsltproc; then
  info "xsltproc appears to be installed, and this is not a CI run"
elif [[ "${OSTYPE}" = "darwin"* ]] && in-path brew; then
  info "Installing xsltproc from homebrew"
  brew install libxslt
  info "Giving the brew xsltproc binary precedence over the default MacOS one..."
  export PATH="/usr/local/opt/libxslt/bin:${PATH}"
elif [[ "${OSTYPE}" =~ /win.*|mingw|msys|cygwin/ ]] && in-path choco; then
  info "Installing xsltproc from choco"
  choco install xsltproc
elif [[ "${OSTYPE}" = "linux"* ]] && in-path apt-get; then
  info "Installing xsltproc using apt-get"
  sudo apt-get install -qy xsltproc
elif [[ "${OSTYPE}" = "linux"* ]] && in-path dnf; then
  info "Installing xsltproc using dnf"
  sudo dnf install -qy xsltproc
elif [[ "${OSTYPE}" = "linux"* ]] && in-path yum; then
  info "Installing xsltproc using yum"
  sudo yum install -qy xsltproc
else
  err "Cannot find xsltproc, nor can I find a suitable package manager to install it with."
  err "Please install xsltproc manually and then try again."
  exit 2
fi

success "Found $(command -v xsltproc) $(xsltproc --version 2>/dev/null || true)"

stage "Generating Surefire XSLT script..."
surefire_prefix_xslt_dir="$(mktemp -d)"

function tidy-up {
  info "Destroying ${surefire_prefix_xslt_dir}"
  rm -Rf "${surefire_prefix_xslt_dir}"
}

trap tidy-up EXIT SIGINT SIGTERM SIGQUIT
surefire_prefix_xslt="${surefire_prefix_xslt_dir}/surefire.xslt"

sed 's/^  //g' > "${surefire_prefix_xslt}" <<'EOF'
  <?xml version="1.0" encoding="UTF-8"?>
  <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <!--
     | XSLT transformation to add a prefix to the start of the 'testcase' names in Surefire reports.
     | This allows us to prefix each test with the Java version before we combine them into a single
     | test report.
     |-->
    <xsl:param name="prefix"/>
    <xsl:output indent="yes"/>
    <xsl:template name="identity" match="node()|@*">
      <xsl:copy>
        <xsl:apply-templates select="node()|@*"/>
      </xsl:copy>
    </xsl:template>
    <xsl:template name="prefix-testcase-names" match="testcase/@name">
      <xsl:attribute name="name" namespace="{namespace-uri()}">
        <xsl:value-of select="concat($prefix, ' ', ../@name)"/>
      </xsl:attribute>
    </xsl:template>
  </xsl:stylesheet>
EOF

info "Generated XSLT script at ${surefire_prefix_xslt}"

function find-all-surefire-reports {
  info "Discovering Surefire test reports"
  find . -wholename '**/target/surefire-reports/TEST-*Test.xml' -print0 | xargs -0
}

function find-all-jacoco-reports {
  info "Discovering JaCoCo coverage reports"
  # For now, we only want the one jacoco file for the main module, if it exists.
  local desired_jacoco_file="java-compiler-testing/target/site/jacoco/jacoco.xml"
  if [ -f "${desired_jacoco_file}" ]; then
    echo "${desired_jacoco_file}"
  fi
}

stage "Updating Surefire reports..."
for surefire_report in $(find-all-surefire-reports); do
  info "Adding Java version to test case names in ${surefire_report}..."
  new_surefire_report=${surefire_report/.xml/-java-${ci_java_version}-${ci_os}.xml}
  xsltproc --stringparam prefix "[Java-${ci_java_version}-${ci_os}]" \
    "${surefire_prefix_xslt}" "${surefire_report}" >"${new_surefire_report}"
  info "Replacing ${surefire_report} with ${new_surefire_report}"
  rm "${surefire_report}"
done

stage "Updating JaCoCo reports..."
for jacoco_report in $(find-all-jacoco-reports); do
  new_jacoco_report="${jacoco_report/.xml/-java-${ci_java_version}-${ci_os}.xml}"
  info "Renaming ${jacoco_report} to ${new_jacoco_report}"
  mv "${jacoco_report}" "${new_jacoco_report}"
done

success "Processing completed. Goodbye!"
