package com.ubirch.user.config

/**
  * author: cvandrei
  * since: 2017-01-19
  */
object ConfigKeys {

  final val CONFIG_PREFIX: String = "ubirchUserService"

  /*
   * general server configs
   *********************************************************************************************/

  final val INTERFACE: String = s"$CONFIG_PREFIX.interface"
  final val PORT: String = s"$CONFIG_PREFIX.port"
  final val TIMEOUT: String = s"$CONFIG_PREFIX.timeout"

  final val GO_PIPELINE_NAME: String = s"$CONFIG_PREFIX.gopipelinename"
  final val GO_PIPELINE_LABEL: String = s"$CONFIG_PREFIX.gopipelinelabel"
  final val GO_PIPELINE_REVISION: String = s"$CONFIG_PREFIX.gopipelinerev"

  /*
   * Akka related configs
   *********************************************************************************************/

  final val akkaPrefix: String = s"$CONFIG_PREFIX.akka"

  final val ACTOR_TIMEOUT: String = s"$akkaPrefix.actorTimeout"
  final val AKKA_NUMBER_OF_WORKERS: String = s"$akkaPrefix.numberOfWorkers"

  /*
   * Mongo
   *********************************************************************************************/

  final val MONGO_PREFIX: String = s"$CONFIG_PREFIX.mongo"

  private final val mongoCollection: String = s"$MONGO_PREFIX.collection"

  final val COLLECTION_CONTEXT: String = s"$mongoCollection.context"
  final val COLLECTION_USER: String = s"$mongoCollection.user"
  final val COLLECTION_GROUP: String = s"$mongoCollection.group"

  /*
   * Init Data
   *********************************************************************************************/

  private final val adminPrefix: String = s"$CONFIG_PREFIX.adminUser"
  final val ADMIN_PROVIDER_ID: String = s"$adminPrefix.providerId"
  final val ADMIN_EXTERNAL_ID: String = s"$adminPrefix.externalId"

  private final val testUserPrefix: String = s"$CONFIG_PREFIX.testUser"
  final val TEST_USER_CONTEXT: String = s"$testUserPrefix.context"

  final val CONTEXT_PREFIX_LIST: String = s"$CONFIG_PREFIX.contextPrefixList"
  final val PROVIDERS_WITH_USERS_ACTIVATED: String = s"$CONFIG_PREFIX.providersWithUsersActivated"

  final val RETRIEVE_RESOURCE_LIMIT: String = s"$CONFIG_PREFIX.retrieveResourceLimit"

}
