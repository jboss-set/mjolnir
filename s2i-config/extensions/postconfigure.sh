#!/usr/bin/env bash

#set -x
ENV_DIR=/etc/eap-environment

# Copy OID adapter configuration file
#echo Copying $ENV_DIR/secure-deployments to $JBOSS_HOME/standalone/configuration/secure-deployments
#cp $ENV_DIR/secure-deployments $JBOSS_HOME/standalone/configuration/secure-deployments

# Trigger Keycload adapter configuration
#echo Configuring Keycloack
#source $JBOSS_HOME/bin/launch/openshift-common.sh
#export CONFIG_FILE=$JBOSS_HOME/standalone/configuration/standalone-openshift.xml
#source $JBOSS_HOME/bin/launch/keycloak.sh
#configure_cli_keycloak

echo "Grepping for postgres":
grep postgres $JBOSS_HOME/standalone/configuration/standalone-openshift.xml

# Run EAP modifications CLI script
echo Configuring EAP
$JBOSS_HOME/bin/jboss-cli.sh --file=$ENV_DIR/eap-config.cli

#set +x


