# ubirch-user-service


## General Information

ubirch user management service


## Configuration

TODO


## Deployment Notes

This service depends on MongoDB (tested with 2.6 and 3.4).


## Automated Tests

run all tests

    ./sbt test

### generate coverage report

    ./sbt coverage test coverageReport

more details here: https://github.com/scoverage/sbt-scoverage


## Create Docker Image

    ./sbt server/docker
