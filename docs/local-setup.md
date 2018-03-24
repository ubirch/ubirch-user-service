## Local Setup

1. Start [MongoDB 3.4](https://www.mongodb.com/download-center?jmp=nav)

1. Delete Existing Data

*Running `dev-scripts/resetDatabase.sh` does everything in this step.*

    ./sbt "cmdtools/runMain com.ubirch.user.cmd.MongoDelete"

1. Create Contexts

You can get the providerId and externalId by logging in through the auth-service and then checking the Redis database.

    export ADMIN_PROVIDER_ID=google
    export ADMIN_EXTERNAL_ID=1234
    ./sbt server/run
    curl localhost:8092/api/userService/v1/initData/local

1. Create Test Users

Creates test users for the configured environment (see config key: _ubirchUserService.testUserContext_)

    export TEST_USER_CONTEXT=ubirch
    export ENV_NAME=local
    ./sbt "cmdtools/runMain com.ubirch.user.cmd.InitUsers"
