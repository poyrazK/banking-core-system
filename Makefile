.PHONY: real-it-tests infra-it-tests all-it-tests

real-it-tests:
	bash scripts/run-real-it-tests.sh

infra-it-tests:
	mvn -pl cbs-api-gateway -am test -Dtest=ApiGatewayIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false
	mvn -pl cbs-config-server -am test -Dtest=ConfigServerIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false
	mvn -pl cbs-discovery-server -am test -Dtest=DiscoveryServerIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false

all-it-tests: infra-it-tests real-it-tests
