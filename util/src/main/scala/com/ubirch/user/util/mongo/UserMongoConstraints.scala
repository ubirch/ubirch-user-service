package com.ubirch.user.util.mongo

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.user.config.Config
import com.ubirch.util.mongo.MongoConstraintsBase
import reactivemongo.api.indexes.{Index, IndexType}


trait UserMongoConstraints extends MongoConstraintsBase with StrictLogging {

  val constraintsToCreate: Map[String, Set[Index.Default]] = Map(
    Config.mongoCollectionUser -> Set(
      Index(name = Some("_id_"), key = Seq(("_id", IndexType.Ascending)), unique = true),
      Index(name = Some("externalId_1_providerId_1"),
        key = Seq(("externalId", IndexType.Ascending), ("providerId", IndexType.Ascending)),
        unique = true),
      Index(name = Some("user_created"), key = Seq(("created", IndexType.Ascending)), unique = false),
    ),
    Config.mongoCollectionGroup -> Set(
      Index(name = Some("_id_"), key = Seq(("_id", IndexType.Ascending)), unique = true),
      Index(name = Some("contextId_1_ownerIds_1"),
        key = Seq(("contextId", IndexType.Ascending), ("ownerIds", IndexType.Ascending))),
      Index(name = Some("allowedUsers_1_contextId_1"),
        key = Seq(("allowedUsers", IndexType.Ascending), ("contextId", IndexType.Ascending)))
    ),
    Config.mongoCollectionContext -> Set(
      Index(name = Some("_id_"), key = Seq(("_id", IndexType.Ascending)), unique = true)
    ),

  )

  val constraintsToDrop: Map[String, Set[String]] = Map()

  val collections: Set[String] = Set(
    Config.mongoCollectionUser,
    Config.mongoCollectionGroup,
    Config.mongoCollectionContext
  )

}
