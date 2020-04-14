# Deploying the Application on OpenShift

## Building an Application Image

Import the EAP 7.2 OpenShift image:

```shell script
oc import-image jboss-eap-7/eap72-openshift --from=registry.redhat.io/jboss-eap-7/eap72-openshift --confirm --scheduled
```

Create a push secret for your external docker repository and update it's name in `bc.yaml`
(replace `images-paas-pull-secret`). Alternatively you could build into an image stream instead of pushing
into a remote repo.

Create a build config object:

```shell script
oc create -f bc.yaml
```

and let the build complete.

## Deploying the Image

Create a secret containing following keystores (named e.g. "mjolnir-secret"):

* keystore.jks (for HTTPS configuration),
* jgroups.jceks (for JGroups configuration),
* keystore-saml.jks (for Keycloak configuration)

(See [documentation](https://access.redhat.com/documentation/en-us/red_hat_single_sign-on_continuous_delivery/2/html/red_hat_single_sign-on_for_openshift/get_started#deploy_binary_build_of_eap_6_4_7_0_jsp_service_invocation_application_that_authenticates_using_red_hat_single_sign_on)
for details about how to create the keystores.)
 
Create a file with deployment parameters according to the `template.params` template. Name it e.g. `mjolnir.params`.

Import the application image from external repo:

```shell script
oc import-image images.paas.redhat.com/thofman/mjolnir:latest --confirm --scheduled
```

Create a deployment template:

```shell script
oc create -f template.json
```

Process a template into a list of OpenShift objects:

```shell script
oc process mjolnir-eap72 --param-file=mjolnir.params > processed-template.json
```

Review the `processed-template.json` file. If it looks OK, create the OpenShift objects:

```shell script
oc create -f processed-template.json
```

Trigger a deployment.