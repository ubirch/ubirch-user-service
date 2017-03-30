# ubirch-user-service


## General Information

ubirch user management service

TODO


## Release History

### Version 0.1.0 (tbd)

* initial release


## Scala Dependencies

### `cmdtools`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "cmdtools" % "0.1.0-SNAPSHOT"
)
```

### `config`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "config" % "0.1.0-SNAPSHOT"
)
```

### `core`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "core" % "0.1.0-SNAPSHOT"
)
```

### `model-db`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "model-db" % "0.1.0-SNAPSHOT"
)
```

### `model-rest`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "model-rest" % "0.1.0-SNAPSHOT"
)
```

### `server`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("hseeberger", "maven")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "server" % "0.1.0-SNAPSHOT"
)
```

### `util`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "util" % "0.1.0-SNAPSHOT"
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

    curl -XPUT localhost:8092/api/userService/v1/context -H "Content-Type: application/json" -d '{
      "displayName": "$DISPLAY_NAME" // string
    }'

Responds with the created context (including it's id which is a UUID).

#### Update

    curl -XPOST localhost:8092/api/userService/v1/context -H "Content-Type: application/json" -d '{
      "id": "$CONTEXT_ID", // UUID
      "displayName": "$DISPLAY_NAME" // string
    }'

Responds with the updated context (including it's id which is a UUID).

#### Delete

    curl -XDELETE localhost:8092/api/userService/v1/context/$CONTEXT_ID

### User Related

#### Create User

    curl -XPUT localhost:8092/api/userService/v1/user -H "Content-Type: application/json" -d '{
      "externalId": "$EXTERNAL_USER_ID", // string
      "providerId": "$PROVIDER_ID", // string
      "displayName": "$DISPLAY_NAME" // string
    }'

Responds with the created user (including it's id which is a UUID).

#### Find UserId

    curl localhost:8092/api/userService/v1/user/$PROVIDER/$EXTERNAL_USER_ID

Responds with the user if it exists (including it's id which is a UUID).

#### Update

    curl -XPOST localhost:8092/api/userService/v1/user -H "Content-Type: application/json" -d '{
      "id": "$USER_ID", // UUID
      "externalId": "$EXTERNAL_USER_ID", // string
      "providerId": "$PROVIDER_ID", // string
      "displayName": "$DISPLAY_NAME" // string
    }'

Responds with the updated user (including it's id which is a UUID).

#### Delete

    curl -XDELETE localhost:8092/api/userService/v1/user/$USER_ID

Responds with the deleted user (including it's id which is a UUID).

### Group Related

#### Create Group

    curl -XPUT localhost:8092/api/userService/v1/group -H "Content-Type: application/json" -d '{
      "ownerId": "$OWNER", // userId allowed to modify it
      "displayName": "$DISPLAY_NAME", // string
      "contextId": "$CONTEXT_ID", // UUID
      "allowedUsers": ["$USER_ID_1", "$USER_ID_2"] // ownerId always has access no matter if it's listed here
    }'

Responds with the created group (including it's id which is a UUID).

#### Update

    curl -XPOST localhost:8092/api/userService/v1/group -H "Content-Type: application/json" -d '{
      "id": "$GROUP_ID", // UUID
      "ownerId": "$OWNER", // userId allowed to modify it
      "displayName": "$DISPLAY_NAME", // string
      "contextId": "$CONTEXT_ID", // UUID
      "allowedUsers": ["$USER_ID_1", "$USER_ID_2"] // ownerId always has access no matter if it's listed here
    }'

Responds with the updated group (including it's id which is a UUID).

#### Delete

    curl -XDELETE localhost:8092/api/userService/v1/group/$GROUP_ID

Responds with the deleted group (including it's id which is a UUID).

#### Add Users to Group

This is a convenience method and the same result would be achieved by updating a group.

    curl -XPOST localhost:8092/api/userService/v1/group/allowedUsers -H "Content-Type: application/json" -d '{
      "groupId": "$GROUP_ID", // UUID
      "allowedUsers": ["$USER_ID_1", "$USER_ID_2"] // list(UUID)
    }'

#### Remove Users from Group

This is a convenience method and the same result would be achieved by updating a group.

    curl -XDELETE localhost:8092/api/userService/v1/group/allowedUsers -H "Content-Type: application/json" -d '{
      "groupId": "$GROUP_ID", // UUID
      "allowedUsers": ["$USER_ID_1", "$USER_ID_2"] // list(UUID)
    }'

### Get Groups

_$USER_ID_ and $CONTEXT_NAME are strings.

    curl localhost:8092/api/userService/v1/groups/$CONTEXT_NAME/$USER_ID


## Configuration

TODO


## Deployment Notes

This service depends on MongoDB.


## Automated Tests

run all tests

    ./sbt test

### generate coverage report

    ./sbt coverageReport

more details here: https://github.com/scoverage/sbt-scoverage


## Local Setup

TODO


## Create Docker Image

    ./sbt server/docker
