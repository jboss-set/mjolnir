#!/usr/bin/env bash

set -x
ENV_DIR=/etc/eap-environment

# Run EAP modifications CLI script
$JBOSS_HOME/bin/jboss-cli.sh --file=$ENV_DIR/eap-config.cli

set +x


