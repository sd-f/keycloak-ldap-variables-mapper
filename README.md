# Keycloak Custom Attribute IDP Linking

![Build](https://github.com/sd-f/keycloak-ldap-variables-mapper/actions/workflows/maven-build.yml/badge.svg)
![Release](https://github.com/sd-f/keycloak-ldap-variables-mapper/actions/workflows/maven-publish.yml/badge.svg)

## Development

```shell
mvn clean install
```

```shell
docker-compose up
```

Update Plugin in container by running ```mvn install```.

Attach remote jvm debug session on port 5015.

## Installation

Tested on Keycloak `15.0.2`.

### Keycloak <= 15.0.2

Copy or mount plugin in your keycloak installation depending on your environment (k8s, compose, gke).
For example in `/opt/jboss/keycloak/standalone/deployments/` (see file docker-compose.yml). You should see something like
following in your keycloak log:

```shell
...
WFLYSRV0010: Deployed "keycloak-ldap-variables-mapper-1.0.0.jar" (runtime-name : "keycloak-ldap-variables-mapper-1.0.0.jar")
...
```

Now you can use `Mapper` in your LDAP User Federation Configuration.