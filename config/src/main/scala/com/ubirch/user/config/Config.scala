package com.ubirch.user.config

import com.ubirch.util.config.ConfigBase

import scala.collection.JavaConverters._
/**
  * author: cvandrei
  * since: 2017-01-19
  */
object Config extends ConfigBase {

  /**
    * The interface the server runs on.
    *
    * @return interface
    */
  def interface: String = config.getString(ConfigKeys.INTERFACE)

  /**
    * Port the server listens on.
    *
    * @return port number
    */
  def port: Int = config.getInt(ConfigKeys.PORT)

  /**
    * Default server timeout.
    *
    * @return timeout in seconds
    */
  def timeout: Int = config.getInt(ConfigKeys.TIMEOUT)

  def goPipelineName: String = config.getString(ConfigKeys.GO_PIPELINE_NAME)
  def goPipelineLabel: String = config.getString(ConfigKeys.GO_PIPELINE_LABEL)
  def goPipelineRevision: String = config.getString(ConfigKeys.GO_PIPELINE_REVISION)

  /*
   * Akka Related
   ************************************************************************************************/

  /**
    * Default actor timeout.
    *
    * @return timeout in seconds
    */
  def actorTimeout: Int = config.getInt(ConfigKeys.ACTOR_TIMEOUT)

  def akkaNumberOfWorkers: Int = config.getInt(ConfigKeys.AKKA_NUMBER_OF_WORKERS)

  /*
   * Mongo Related
   ************************************************************************************************/

  def mongoCollectionContext: String = config.getString(ConfigKeys.COLLECTION_CONTEXT)

  def mongoCollectionUser: String = config.getString(ConfigKeys.COLLECTION_USER)

  def mongoCollectionGroup: String = config.getString(ConfigKeys.COLLECTION_GROUP)

  /*
   * Init Data
   ************************************************************************************************/

  def adminUserProviderId: String = config.getString(ConfigKeys.ADMIN_PROVIDER_ID)

  def adminUserExternalId: String = config.getString(ConfigKeys.ADMIN_EXTERNAL_ID)

  def testUserContext: String = config.getString(ConfigKeys.TEST_USER_CONTEXT)

  def contextPrefixList: List[String] = config.getStringList(ConfigKeys.CONTEXT_PREFIX_LIST).asScala.toList

  def providersWithUsersActivated: List[String] = config.getStringList(ConfigKeys.PROVIDERS_WITH_USERS_ACTIVATED).asScala.toList

}
