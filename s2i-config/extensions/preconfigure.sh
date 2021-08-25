#!/usr/bin/env bash

ENV_DIR=/etc/eap-environment

echo Copying $ENV_DIR/secure-deployments to $JBOSS_HOME/standalone/configuration/secure-deployments
cp $ENV_DIR/secure-deployments $JBOSS_HOME/standalone/configuration/secure-deployments