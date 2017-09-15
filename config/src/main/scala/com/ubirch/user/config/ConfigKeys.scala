package com.ubirch.user.config

/**
  * author: cvandrei
  * since: 2017-01-19
  */
object ConfigKeys {

  final val CONFIG_PREFIX = "ubirchUserService"

  /*
   * general server configs
   *********************************************************************************************/

  final val INTERFACE = s"$CONFIG_PREFIX.interface"
  final val PORT = s"$CONFIG_PREFIX.port"
  final val TIMEOUT = s"$CONFIG_PREFIX.timeout"

  final val GO_PIPELINE_NAME = s"$CONFIG_PREFIX.gopipelinename"
  final val GO_PIPELINE_LABEL = s"$CONFIG_PREFIX.gopipelinelabel"
  final val GO_PIPELINE_REVISION = s"$CONFIG_PREFIX.gopipelinerev"

  /*
   * Akka related configs
   *********************************************************************************************/

  private val akkaPrefix = s"$CONFIG_PREFIX.akka"

  final val ACTOR_TIMEOUT = s"$akkaPrefix.actorTimeout"
  final val AKKA_NUMBER_OF_WORKERS = s"$akkaPrefix.numberOfWorkers"

  /*
   * Mongo
   *********************************************************************************************/

  final val MONGO_PREFIX = s"$CONFIG_PREFIX.mongo"

  private final val mongoCollection = s"$MONGO_PREFIX.collection"

  final val COLLECTION_CONTEXT = s"$mongoCollection.context"
  final val COLLECTION_USER = s"$mongoCollection.user"
  final val COLLECTION_GROUP = s"$mongoCollection.group"

  /*
   * Init Data
   *********************************************************************************************/

  private val adminPrefix = s"$CONFIG_PREFIX.adminUser"
  final val ADMIN_PROVIDER_ID = s"$adminPrefix.providerId"
  final val ADMIN_EXTERNAL_ID = s"$adminPrefix.externalId"

  private val testUserPrefix = s"$CONFIG_PREFIX.testUser"
  final val TEST_USER_CONTEXT = s"$testUserPrefix.context"

  final val CONTEXT_PREFIX_LIST = s"$CONFIG_PREFIX.contextPrefixList"
  final val PROVIDERS_WITH_USERS_ACTIVATED = s"$CONFIG_PREFIX.providersWithUsersActivated"

}
