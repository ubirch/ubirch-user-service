# ubirch-user-service


## General Information

ubirch user management service


## Release History

### Version 0.6.3 (tbd)

* tbd

### Version 0.6.2 (2017-08-11)

* add `/user/$PROVIDER/$EXTERNAL_USER_ID` endpoing to REST client (see `UserServiceClientRest#userGET`)

### Version 0.6.1 (2017-08-09)

* bugfix: searching for groups based on an ownerId was broken since the queries had not been updated to the new field name (`ownerIds` instead of `ownerId`)
* fixed broken tests

### Version 0.6.0 (2017-08-07)

* add context _trackle_ to InitData
* refactored field `Group.ownerId: String|UUID` to a set (in DB and REST models)

### Version 0.5.1 (2017-07-31)

* fixed some scaladoc
* update to `com.ubirch.util:mongo-utils:0.3.6`
* update to `com.ubirch.util:mongo-test-utils:0.3.6`

### Version 0.5.0 (2017-07-28)

* refactor `UserServiceClientRest.deepCheck` to return `DeepCheckResponse` (without Option)
* refactor `UserServiceClientRest.deepCheck` responses to include a `[user-service]` prefix in all it's messages

### Version 0.4.21 (2017-07-28)

* change context name from _trackle-$env_ to _trackle-ui-$env_

### Version 0.4.20 (2017-07-27)

* add method `UserServiceClientRest#check`
* add method `UserServiceClientRest#deepCheck`

### Version 0.4.19 (2017-07-27)

* add `initUser.sh`
* update to `com.ubirch.util:json:0.4.3`
* update to `com.ubirch.util:deep-check-model:0.1.3`
* update to `com.ubirch.util:response-util:0.2.4`
* update to `com.ubirch.util:mongo-test-utils:0.3.5`
* update to `com.ubirch.util:mongo-utils:0.3.5`

### Version 0.4.18 (2017-07-24)

* revert to Akka 2.4.19
* refactor `UserServiceClientRest` to use Akka Http for the connection

### Version 0.4.17 (2017-07-18)

* update to Akka 2.4.19
* update to Play 2.5.3
* update _com.ubirch.util:mongo(-test)-utils_ to 0.3.4

### Version 0.4.16 (2017-07-17)

* change `com.ubirch.user.core.manager.GroupsManager.findByContextAndUser` to return only groups where the given user is the owner

### Version 0.4.15 (2017-07-17)

* update _com.ubirch.util:rest-akka-http(-test)_ to 0.3.8
* update _com.ubirch.util:response-util_ to 0.2.3

### Version 0.4.14 (2017-07-13)

* fixed bug in dev script
* update logging dependencies
* update logback configs
* update _com.ubirch.util:mongo(-test)-utils_ to 0.3.3

### Version 0.4.13 (2017-06-29)

* switch from dependency _play-ahc-ws-standalone:1.0.0-M10_ to _play-ws:2.4.11_ to fix a bug where we get runtime errors
  using the REST clients from user-service and key-service in the same project

### Version 0.4.12 (2017-06-29)

* add script `dev-scripts/resetDatabase.sh`
* refactored actors by adding a `props()` method
* updated to _com.ubirch.util:json:0.4.2_ and all ubirch util libs depending on it, too

### Version 0.4.11 (2017-06-22)

* bugfix (UBI-264): updates on Azure's CosmosDB show the behavior of an upsert()

### Version 0.4.10 (2017-06-19)

* update json4s to 3.5.2
* update to _com.ubirch.util:json:0.4.1_
* update to _com.ubirch.util:deep-check-model:0.1.1_
* update to _com.ubirch.util:mongo-test-utils:0.3.1_
* update to _com.ubirch.util:mongo-utils:0.3.1_
* update to _com.ubirch.util:response-util:0.2.1_

### Version 0.4.9 (2017-06-12)

* endpoint `/api/userService/v1/deepCheck` responds with http status 503 if deep check finds problems

### Version 0.4.8 (2016-06-09)

* migrate to _com.ubirch.util:deep-check-model:0.1.0_

### Version 0.4.7 (2017-06-08)

* update _com.ubirch.util:mongo-test-utils_ to 0.2.3
* update _com.ubirch.util:mongo-utils_ to 0.2.3
* introduce `DeepCheckManager`

### Version 0.4.6 (2017-06-07)

* introduce endpoint `/api/authService/v1/check`
* update to sbt 0.13.15
* update to _com.ubirch.util:json:0.4.0_
* update to _com.ubirch.util:response-util:0.1.6_
* introduce endpoint `/api/userService/v1/deepCheck`

### Version 0.4.5 (2017-05-31)

* added field `User.activeUser` to REST model

### Version 0.4.4 (2017-05-31)

* improve documentation
* fixed bug that broke `InitUsers`
* added activeUser flag to user 

### Version 0.4.3 (2017-05-31)

* deleted `group/byName/$GROUP_NAME` routes

### Version 0.4.2 (2017-05-30)

* fixed http response codes and error messages in `GroupRoute`
* updated documentation in README

### Version 0.4.1 (2017-05-29)

* update `DataHelpers` in module `test-tools-ext` to allow creation of admin groups

### Version 0.4.0 (2017-05-29)

* add field _adminGroup_ to REST and DB model of `Group`

### Version 0.3.2 (2017-05-24)

* `InitData` inits all users with a group now (before only user1 had a group)
* replaced `InitData` with `InitUsers`
* introduced endpoint `/initData/$ENV_NAME`

### Version 0.3.1 (2017-05-22)

* update dependency `rest-akka-http` to 0.3.7
* update dependency `rest-akka-http-test` to 0.3.7
* replace path `/groups` with `/group/memberOf`

### Version 0.3.0 (2017-05-18)

* update dependency `rest-akka-http` to 0.3.6
* update dependency `rest-akka-http-test` to 0.3.6
* update dependency `response-util` to 0.1.4
* update dependency `mongo-utils` to 0.2.2
* update dependency `mongo-test-utils` to 0.2.2
* update to Akka HTTP 10.0.6
* update to Akka 2.4.18
* add module `client-rest`

### Version 0.2.0 (2017-05-05)

* update all models to use `String` instead of `UUID`

### Version 0.1.1 (2017-05-02)

* try to fix problem where a config key is not found

### Version 0.1.0 (2017-05-02)

* initial release


## Scala Dependencies

### `client-rest`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "client-rest" % "0.6.3-SNAPSHOT"
)
```

#### Configuration

| Config Item                         | Mandatory  | Description                                                       |
|:------------------------------------|:-----------|:------------------------------------------------------------------|
| ubirchUserService.client.rest.host  | yes        | user-service host                                                 |

#### Usage

See `com.ubirch.user.client.rest.UserServiceClientRestDebug` for an example usage.

### `cmdtools`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "cmdtools" % "0.6.3-SNAPSHOT"
)
```

### `config`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "config" % "0.6.3-SNAPSHOT"
)
```

### `core`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "core" % "0.6.3-SNAPSHOT"
)
```

### `model-db`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "model-db" % "0.6.3-SNAPSHOT"
)
```

### `model-rest`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "model-rest" % "0.6.3-SNAPSHOT"
)
```

### `server`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("hseeberger", "maven")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "server" % "0.6.3-SNAPSHOT"
)
```

### `util`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "util" % "0.6.3-SNAPSHOT"
)
```


## REST Methods

### Welcome / Health / Check

    curl localhost:8092/
    curl localhost:8092/api/userService/v1
    curl localhost:8092/api/userService/v1/check

If healthy the server response is:

    200 {"version":"1.0","status":"OK","message":"Welcome to the ubirchUserService ( $GO_PIPELINE_NAME / $GO_PIPELINE_LABEL / $GO_PIPELINE_REVISION )"}

If not healthy the server response is:

    500 {"version":"1.0","status":"NOK","message":"$ERROR_MESSAGE"}

### Deep Check / Server Health

    curl localhost:8092/api/userService/v1/deepCheck

If healthy the response is:

    200 {"status":true,"messages":[]}

If not healthy the status is `false` and the `messages` array not empty:

    503 {"status":false,"messages":["unable to connect to the database"]}


### Context Related

#### Create

    curl -XPOST localhost:8092/api/userService/v1/context -H "Content-Type: application/json" -d '{
      "displayName": "$DISPLAY_NAME" // string
    }'

Responds with the created context (including it's id which is a UUID).

#### Update

    curl -XPUT localhost:8092/api/userService/v1/context -H "Content-Type: application/json" -d '{
      "id": "$CONTEXT_ID", // UUID
      "displayName": "$DISPLAY_NAME" // string
    }'

Responds with the updated context (including it's id which is a UUID).

#### Find by Id

    curl localhost:8092/api/userService/v1/context/$CONTEXT_ID

#### Find by Name

    curl localhost:8092/api/userService/v1/context/byName/$CONTEXT_NAME

Responds with the context if it exists (including it's id which is a UUID).

#### Delete by Id

    curl -XDELETE localhost:8092/api/userService/v1/context/$CONTEXT_ID

Responds with the deleted context (including it's id which is a UUID).

#### Delete by Name

    curl -XDELETE localhost:8092/api/userService/v1/context/byName/$CONTEXT_NAME

Responds with the deleted context (including it's id which is a UUID).

### User Related

#### Create

    curl -XPOST localhost:8092/api/userService/v1/user -H "Content-Type: application/json" -d '{
      "externalId": "$EXTERNAL_USER_ID", // string
      "providerId": "$PROVIDER_ID", // string
      "displayName": "$DISPLAY_NAME", // string
      "locale": "$LOCALE" // language specifier (e.g. en)
    }'

Responds with the created user (including it's id which is a UUID).

#### Update

    curl -XPUT localhost:8092/api/userService/v1/user/$PROVIDER/$EXTERNAL_USER_ID -H "Content-Type: application/json" -d '{
      "externalId": "$EXTERNAL_USER_ID", // string
      "providerId": "$PROVIDER_ID", // string
      "displayName": "$DISPLAY_NAME", // string
      "locale": "$LOCALE" // language specifier (e.g. en)
    }'

Responds with the updated user (including it's id which is a UUID).

#### Find

    curl localhost:8092/api/userService/v1/user/$PROVIDER/$EXTERNAL_USER_ID

Responds with the user if it exists (including it's id which is a UUID).

#### Delete

    curl -XDELETE localhost:8092/api/userService/v1/user/$PROVIDER/$EXTERNAL_USER_ID

Responds with "OK" if user was deleted.

### Group Related

#### Create

    curl -XPUT localhost:8092/api/userService/v1/group -H "Content-Type: application/json" -d '{
      "ownerIds": ["$OWNER"], // UUID: userIds allowed to modify it
      "displayName": "$DISPLAY_NAME", // string
      "contextId": "$CONTEXT_ID", // UUID
      "allowedUsers": ["$USER_ID_1", "$USER_ID_2"], // (optional) UUID: ownerId always has access no matter if it's listed here
      "adminGroup": true // (optional) only an admin group if set to true, otherwise not
    }'

Responds with the created group (including it's id which is a UUID).

#### Update

    curl -XPUT localhost:8092/api/userService/v1/group -H "Content-Type: application/json" -d '{
      "id": "$GROUP_ID", // UUID
      "ownerIds": ["$OWNER"], // UUID: userIds allowed to modify it
      "displayName": "$DISPLAY_NAME", // string
      "contextId": "$CONTEXT_ID", // UUID
      "allowedUsers": ["$USER_ID_1", "$USER_ID_2"], // UUID: ownerId always has access no matter if it's listed here
      "adminGroup": true // (optional) only an admin group if set to true, otherwise not
    }'

Responds with the updated group (including it's id which is a UUID).

#### Get

    curl localhost:8092/api/userService/v1/group/$GROUP_ID

Responds with the related group if it exists

#### Delete

    curl -XDELETE localhost:8092/api/userService/v1/group/$GROUP_ID

Responds with the deleted group (including it's id which is a UUID).

#### Add Users to Group

This is a convenience method and the same result would be achieved by updating a group.

    curl -XPUT localhost:8092/api/userService/v1/group/allowedUsers -H "Content-Type: application/json" -d '{
      "groupId": "$GROUP_ID", // UUID
      "allowedUsers": ["$USER_ID_1", "$USER_ID_2"] // list(UUID)
    }'

#### Remove Users from Group

This is a convenience method and the same result would be achieved by updating a group.

    curl -XDELETE localhost:8092/api/userService/v1/group/allowedUsers -H "Content-Type: application/json" -d '{
      "groupId": "$GROUP_ID", // UUID
      "allowedUsers": ["$USER_ID_1", "$USER_ID_2"] // list(UUID)
    }'

### Groups Related

#### Find Groups A User is a Member Of

_$CONTEXT_NAME_, _$PROVIDER_ID_ and _$EXTERNAL_USER_ID_ are strings.

    curl localhost:8092/api/userService/v1/group/memberOf/$CONTEXT_NAME/$PROVIDER_ID/$EXTERNAL_USER_ID

Responds with a list of groups associated to the given contextName and (providerId, externalUserId).

### Init Data

Creates all contexts (unless they already exist) for the given environment name and then create one admin user for each
context.

    curl localhost:8092/api/userService/v1/initData/$ENVIRONMENT_NAME

`$ENVIRONMENT_NAME`s can be:

* local
* dev
* demo
* ...

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


## Local Setup

1. Start [MongoDB 3.4](https://www.mongodb.com/download-center?jmp=nav)

1. Delete Existing Data

*Running `dev-scripts/resetDatabase.sh` does everything in this step.*

    ./sbt "cmdtools/runMain com.ubirch.user.cmd.MongoDelete"

1. Create Contexts

You can get the providerId and externalId by logging in through the auth-service and then checking the Redis datbaase.

    export ADMIN_PROVIDER_ID=google
    export ADMIN_EXTERNAL_ID=1234
    ./sbt server/run
    curl localhost:8092/api/userService/v1/initData/local

1. Create Test Users

Creates test users for the configured environment (see config key: _ubirchUserService.testUserContext_)

    export TEST_USER_CONTEXT=ubirch-admin-ui
    export ENV_NAME=local
    ./sbt "cmdtools/runMain com.ubirch.user.cmd.InitUsers"


## Create Docker Image

    ./sbt server/docker
