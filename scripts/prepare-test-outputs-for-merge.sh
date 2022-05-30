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
set -o pipefail

CI_JAVA_VERSION=${1?Pass the Java version as the first argument to this script!}
CI_OS=${2?Pass the OS name as the second argument to this script!}

if ! command -v xsltproc >/dev/null 2>&1; then
  if [ -z ${CI+_} ]; then
    echo -e "\e[1;31mERROR\e[0m: xsltproc is not found -- make sure it is installed first."
    exit 2
  else
    echo -e "\e[1;33mWARN\e[0m: xsltproc is not installed, so I will install it now..."
    sudo apt-get install xsltproc -qy
    echo -e "\e[1;32m...done!\e[0m"
  fi
fi

echo -e "\e[1;35mUpdating Surefire reports...\e[0m"
surefire_prefix_xslt=$(mktemp --suffix=.xslt)
sed 's/^  //g' >"${surefire_prefix_xslt}" <<'EOF'
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

function find-all-surefire-reports() {
  find . -wholename '**/target/surefire-reports/TEST-*Test.xml' -print0 | xargs -0
}

function find-all-jacoco-reports() {
  # For now, we only want the one jacoco file for the main module, if it exists.
  local desired_jacoco_file="java-compiler-testing/target/site/jacoco/jacoco.xml"
  if [ -f "${desired_jacoco_file}" ]; then
    echo "${desired_jacoco_file}"
  fi
}

for surefire_report in $(find-all-surefire-reports); do
  echo -e "\e[1;34mAdding Java version to test case names in ${surefire_report}...\e[0m"
  new_surefire_report=${surefire_report/.xml/-java-${CI_JAVA_VERSION}-${CI_OS}.xml}
  xsltproc --stringparam prefix "[Java-${CI_JAVA_VERSION}-${CI_OS}]" \
    "${surefire_prefix_xslt}" "${surefire_report}" >"${new_surefire_report}"
  echo -e "\e[1;34mReplacing ${surefire_report} with ${new_surefire_report}\e[0m"
  rm "${surefire_report}"
done

rm "${surefire_prefix_xslt}"

echo -e "\e[1;35mUpdating Jacoco reports...\e[0m"
for jacoco_report in $(find-all-jacoco-reports); do
  new_jacoco_report="${jacoco_report/.xml/-java-${CI_JAVA_VERSION}-${CI_OS}.xml}"
  echo -e "\e[1;34mRenaming ${jacoco_report} to ${new_jacoco_report}\e[0m"
  mv "${jacoco_report}" "${new_jacoco_report}"
done

echo -e "\e[1;32mDone!\e[0m"
