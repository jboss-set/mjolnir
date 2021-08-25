#!/usr/bin/env bash

injected_dir=$1

echo "Copy ${injected_dir}/extensions/postconfigure.sh to ${JBOSS_HOME}/extensions/"
mkdir -p "${JBOSS_HOME}/extensions/"
cp -v "${injected_dir}/extensions/postconfigure.sh" "${JBOSS_HOME}/extensions/"

echo "Copy ${injected_dir}/extensions/preconfigure.sh to ${JBOSS_HOME}/extensions/"
cp -v "${injected_dir}/extensions/preconfigure.sh" "${JBOSS_HOME}/extensions/"