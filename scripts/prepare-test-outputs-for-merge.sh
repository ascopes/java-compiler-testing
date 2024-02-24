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
### Script to run in CI to prefix any Surefire report and test case names with the Java version
### that the test applies to, and to rename the jacoco.xml files to match the Java version in use.
###

set -o errexit
set -o nounset

# shellcheck source=./scripts/common.sh
source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

function usage() {
  echo "USAGE: ${BASH_SOURCE[0]} [-h] -j <Java version> -o <OS name>"
  echo "    -h                 Show this message and exit."
  echo "    -j <Java version>  Specify the Java version to use."
  echo "    -o <OS name>       Specify the OS name to use."
}

ci_java_version=""
ci_os=""

while getopts ":hj:o:" opt; do
  case "${opt}" in
    h) usage; exit 0 ;;
    j) ci_java_version="${OPTARG}" ;;
    o) ci_os="${OPTARG}" ;;
    :) err "Missing required argument for option '-${OPTARG}'"; usage; exit 1 ;;
    ?) err "Unknown option '-${opt}'"; usage; exit 1 ;;
  esac
done

if [[ -z "${ci_java_version}" ]] || [[ -z "${ci_os}" ]]; then
  err "Missing required arguments"
  usage
  exit 1
fi

success "Found $(command -v xsltproc)"
run <<< "xsltproc --version"

info "Generating Surefire XSLT script..."
surefire_prefix_xslt_dir="$(mktemp -d)"

function tidy() {
  run <<< "rm -Rf '${surefire_prefix_xslt_dir}'"
}

trap 'tidy' EXIT SIGINT SIGTERM SIGQUIT
surefire_prefix_xslt="${surefire_prefix_xslt_dir}/surefire.xslt"

cat > "${surefire_prefix_xslt}" <<'EOF'
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

function xsltproc_surefire_report() {
  local prefix="${1}"
  local xslt="${2}"
  local input_report="${3}"
  local output_report="${4}"

  if ! run --no-group <<< "xsltproc --stringparam prefix '${prefix}' '${xslt}' '${input_report}' > '${output_report}'"; then
    err "Error invoking xsltproc! Erroneous report was:"
    dump "${input_report}"
    return 2
  fi

  rm "${input_report}"
}

info "Updating test reports..."
report_count=0
prefix="[Java-${ci_java_version}-${ci_os}]"
concurrency="$(($(nproc || echo 2) * 4))"

while read -r report; do
  report_count="$((report_count+1))"
  new_report="${report/.xml/-java-${ci_java_version}-${ci_os}.xml}"
  xsltproc_surefire_report "${prefix}" "${surefire_prefix_xslt}" "${report}" "${new_report}" &

  # Wait if we have the max number of jobs in the window running (cpu-count * 4)
  if [[ "$((report_count % concurrency))" -eq 0 ]]; then
    wait < <(jobs -p)
    info "Waited for up to ${concurrency} jobs to complete, will now continue..."
  fi
done < <(find . -name 'TEST*.xml' -print)

while read -r report; do
  report_count="$((report_count+1))"
  new_report="${report/.log/-java-${ci_java_version}-${ci_os}.log}"
  mv "${report}" "${new_report}"
done < <(find . -name build.log -print)

wait < <(jobs -p)

if [[ "${report_count}" -eq 0 ]]; then
  err "No test reports found..."
  exit 3
fi

success "Updated ${report_count} test reports"
success "Processing completed."
