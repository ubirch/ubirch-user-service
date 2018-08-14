## Scala Dependencies

### `client-rest`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "client-rest" % "0.12.2"
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
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "cmdtools" % "0.12.2"
)
```

### `config`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "config" % "0.12.2"
)
```

### `core`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "core" % "0.12.2"
)
```

### `model-db`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "model-db" % "0.12.2"
)
```

### `model-rest`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "model-rest" % "0.12.2"
)
```

### `server`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.bintrayRepo("hseeberger", "maven")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "server" % "0.12.2"
)
```

### `util`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.user" %% "util" % "0.12.2"
)
```
