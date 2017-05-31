# ubirch-user-service


## General Information

ubirch user management service


## Release History

### Version 0.4.4 (2017-05-31)

* improve documentation
* fixed bug that broke `InitUsers`

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
  Resolver.sonatypeRepo("releases"),
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "client-rest" % "0.4.4"
)
```

#### Configuration

| Config Item                         | Mandatory  | Description                                                       |
|:------------------------------------|:-----------|:------------------------------------------------------------------|
| ubirchUserService.client.rest.host  | yes        | user-service host                                                 |

#### Play Configs

| Config Item                         | Mandatory  | Description                                                       |
|:------------------------------------|:-----------|:------------------------------------------------------------------|
| play.ws.compressionEnabled          | no         | use gzip/deflater encoding if true (default: false)               |
| play.ws.useragent                   | no         | to configure the User-Agent header field                          |
| play.ws.timeout.connection          | no         | connection timeout (default: 120 seconds)                         |
| play.ws.timeout.idle                | no         | maximum idle time (connection established but waiting for more data) (default: 120 seconds) |
| play.ws.timeout.request             | no         | request timeout (default: 120 seconds)                            |

#### SSL Configuration

See https://www.playframework.com/documentation/2.5.x/WSQuickStart for more details.

For a single PEM file:

    play.ws.ssl {
      trustManager = {
        stores = [
          { type = "PEM", path = "/path/to/cert/globalsign.crt" }
        ]
      }
    }

For a Java key store file:

    play.ws.ssl {
      trustManager = {
        stores = [
          { type = "JKS", path = "exampletrust.jks" }
        ]
      }
    }

#### Usage

See `com.ubirch.user.client.rest.UserServiceClientRestDebug` for an example usage.

### `cmdtools`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "cmdtools" % "0.4.4"
)
```

### `config`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "config" % "0.4.4"
)
```

### `core`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "core" % "0.4.4"
)
```

### `model-db`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "model-db" % "0.4.4"
)
```

### `model-rest`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "model-rest" % "0.4.4"
)
```

### `server`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.bintrayRepo("hseeberger", "maven")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "server" % "0.4.4"
)
```

### `util`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "util" % "0.4.4"
)
```


## REST Methods

### Welcome / Health

    curl localhost:8092/
    curl localhost:8092/api/userService/v1

If healthy the server response is:

    200 {"version":"1.0","status":"OK","message":"Welcome to the ubirchUserService"}

If not healthy the server response is:

    400 {"version":"1.0","status":"NOK","message":"$ERROR_MESSAGE"}

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
      "ownerId": "$OWNER", // UUID: userId allowed to modify it
      "displayName": "$DISPLAY_NAME", // string
      "contextId": "$CONTEXT_ID", // UUID
      "allowedUsers": ["$USER_ID_1", "$USER_ID_2"], // (optional) UUID: ownerId always has access no matter if it's listed here
      "adminGroup": true // (optional) only an admin group if set to true, otherwise not
    }'

Responds with the created group (including it's id which is a UUID).

#### Update

    curl -XPUT localhost:8092/api/userService/v1/group -H "Content-Type: application/json" -d '{
      "id": "$GROUP_ID", // UUID
      "ownerId": "$OWNER", // UUID: userId allowed to modify it
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
