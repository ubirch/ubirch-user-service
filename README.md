# ubirch-user-service

## General Information

ubirch user management service

## Configuration

TODO

## Run the service

enter the server module and run:

    mvn compile exec:java -Dexec.mainClass="com.ubirch.user.server.Boot"

## Deployment Notes

This service depends on MongoDB (tested with 2.6 and 3.4).

## Automated Tests

run all tests

    mvn test

### generate coverage report

    mvn surefire-report:report test

more details here: https://github.com/scoverage/sbt-scoverage


## Create Docker Image

    mvn surefire-report:report deploy --update-snapshots -U -B -Ddockerfile.dockerConfigFile=/.docker/config.json -Duser.home=/build -Ddockerfile.tag=${GO_PIPELINE_LABEL} -Dbuild.number=${GO_PIPELINE_LABEL}
