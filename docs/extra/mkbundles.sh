#!/bin/bash

#
# Script to create bundles for [Sonatype](https://oss.sonatype.org).
#
# 2024-01-06
#

projectId="uel"
modules="uel-type uel-rule uel-sat uel-asp uel-core uel-ui uel-protege"
bundle="bundle.jar"

mvn clean install -DperformRelease=true

for module in ${modules}; do
  cd "${module}/target" || exit 1
  jar -cf ${bundle} ${projectId}-*
  cd ../..
done

cd target || exit 1
jar -cf ${bundle} ${projectId}-parent-*
cd ..

