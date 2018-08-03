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

#### Check If Account With Given Email Exists

    curl localhost:8092/api/userService/v1/user/emailExists/$EMAIL_ADDRESS

If user with given email address exists the response is:

    200

Otherwise:

    400
    {
      "version" : "1.0",
      "status" : "NOK",
      "errorType" : "QueryError",
      "errorMessage": "no user with given email address exists"
    }

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

### Registration

Users are independent of a context and it can happen that they have been registered in another context already. All user
specific information (user's and group' displayName) is dynamically determined based on information received from the
OpenID Connect provider).

To register a new user:

    curl -XPOST curl localhost:8092/api/userService/v1/register -H "Content-Type: application/json" -d '{
      "context": "ubirch-local",
      "providerId": "google",
      "userId": "asdf-1234",
      "userName": "some user name",
      "locale": "en-US"
    }'

If the registration is successful the response is:

    200
    {
      "displayName": "some user name",
      "myGroups": [
        {
          "id": "a4c08d88-7c43-4984-a568-0672b4431016", // UUID
          "displayName": "some user name" // same as displayName above
        }
      ],
      "allowedGroups": [] // being a new user it is impossible to have been added to another group
    }

If the registration fails the response is:

    400
    {
      "apiVersion": "1.0.0",
      "status": "NOK",
      "error": {
        "errorId": "RegistrationError", // errorId can be different
        "errorMessage": "failed to register new user" // errorMessage can be different
      }
    }


### User Info

#### Get

    curl curl localhost:8092/api/userService/v1/userInfo/$CONTEXT/$PROVIDER_ID/$EXTERNAL_USER_ID

If the query is successful the response is (user exists but is not registered not in this context):

    200
    {
      "displayName": "some string being displayed in frontend as my display name",
      "locale": "en",
      "myGroups": [],
      "allowedGroups": []
    }

If the query is successful the response is (user is registered in this context and has been added to other group
including the admin group of this context):

    200
    {
      "displayName": "some string being displayed in frontend as my display name",
      "myGroups": [
        {
          "id": "a4c08d88-7c43-4984-a568-0672b4431016", // UUID
          "displayName": "my-ubirch-group"
        }
      ],
      "allowedGroups": [
        {
          "id": "f2d4280d-336f-438d-9b2a-70337723a3e7", // UUID
          "displayName": "my-best-friends-ubirch-group"
        },
        {
          "id": "32c5c928-97a0-49b7-9d71-0b0517b7d13e", // UUID
          "displayName": "admin group",
          "adminGroup": true // means user has admin rights for this context
        }
      ]
    }

If no user is found the response is:

    400
    {
      "apiVersion": "1.0.0",
      "status": "NOK",
      "error": {
        "errorId": "NoUserInfoFound"
        "errorMessage": "failed to get user info"
      }
    }

If the query fails the response is:

    500
    {
      "apiVersion": "1.0.0",
      "status": "NOK",
      "error": {
        "errorId": "ServerError",
        "errorMessage": "failed to get user info"
      }
    }

#### Update

    curl -XPUT curl localhost:8092/api/userService/v1/userInfo -H "Content-Type: application/json" -d '{
      "simpleUserContext": {
        "context": "ubirch-local",
        "providerId": "google",
        "userId": "asdf-1234"
      },
      "update": {
        "displayName": "my new display name"
      }
    }'

If the query is successful the response is:

    200
    {
      "displayName": "my new display name",
      "locale": "en",
      "myGroups": [
        {
          "id": "a4c08d88-7c43-4984-a568-0672b4431016", // UUID
          "displayName": "my-ubirch-group"
        }
      ],
      "allowedGroups": [
        {
          "id": "f2d4280d-336f-438d-9b2a-70337723a3e7", // UUID
          "displayName": "my-best-friends-ubirch-group"
        },
        {
          "id": "32c5c928-97a0-49b7-9d71-0b0517b7d13e", // UUID
          "displayName": "another-friends-ubirch-group"
        }
      ]
    }

If the query fails the response is:

    400
    {
      "apiVersion": "1.0.0",
      "status": "NOK",
      "error": {
        "errorId": "UpdateError", // errorId can be different
        "errorMessage": "failed to update user" // errorMessage can be different
      }
    }

### Init Data

Creates all contexts (unless they already exist) for the given environment name and then create one admin user for each
context.

    curl localhost:8092/api/userService/v1/initData/$ENVIRONMENT_NAME

`$ENVIRONMENT_NAME`s can be:

* local
* dev
* demo
* ...
