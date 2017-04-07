#!/usr/bin/env bash

echo ============== creating context
curl -XPOST localhost:8092/api/userService/v1/context -H "Content-Type: application/json" -d '{"displayName": "trackle-dev"}'
echo
echo ============== creating test-user-1
curl -XPOST localhost:8092/api/userService/v1/user -H "Content-Type: application/json" -d '{ "externalId": "1234", "providerId": "google", "displayName": "test-user-1"}'
echo
echo ============== creating test-user-2
curl -XPOST localhost:8092/api/userService/v1/user -H "Content-Type: application/json" -d '{ "externalId": "1235", "providerId": "google", "displayName": "test-user-2"}'
echo
echo ============== creating test-user-3
curl -XPOST localhost:8092/api/userService/v1/user -H "Content-Type: application/json" -d '{ "externalId": "1236", "providerId": "google", "displayName": "test-user-3"}'
echo


echo "\n============== created one context and three users. please remember to create a group."

###################################
## this last section depends on UUID returned by the above calls:
##   * contextId
##   * ownerId (can be test-user-1)
##   * allowedUsers (can be test-user-2 and test-user-3)
####################################

#curl -XPOST localhost:8092/api/userService/v1/group -H "Content-Type: application/json" -d '{
#  "ownerId": "d98bf96c-fee5-4452-b7b4-ec20cc165a15",
#  "displayName": "test-user-1-group",
#  "contextId": "65c6fec5-961b-4299-8fca-159acb465623",
#  "allowedUsers": ["5f279916-d16c-402f-87aa-51ed5222bca5", "e082ff34-a4b2-4736-a2f4-a091aa5c5a6b"]
#}'
