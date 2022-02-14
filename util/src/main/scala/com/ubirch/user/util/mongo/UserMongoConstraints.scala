package com.ubirch.user.util.mongo

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.user.config.Config
import com.ubirch.util.mongo.MongoConstraintsBase
import reactivemongo.api.indexes.{Index, IndexType}


trait UserMongoConstraints extends MongoConstraintsBase with StrictLogging {

  val constraintsToCreate: Map[String, Set[Index]] = Map(
    Config.mongoCollectionUser -> Set(
      Index(name = Some("externalId_1_providerId_1"),
        key = Seq(("externalId", IndexType.Ascending), ("providerId", IndexType.Ascending)),
        unique = true)
    ),
    Config.mongoCollectionGroup -> Set(
      Index(name = Some("contextId_1_ownerIds_1"),
        key = Seq(("contextId", IndexType.Ascending), ("ownerIds", IndexType.Ascending))),
      Index(name = Some("allowedUsers_1_contextId_1"),
        key = Seq(("allowedUsers", IndexType.Ascending), ("contextId", IndexType.Ascending)))
    )
  )

  val constraintsToDrop: Map[String, Set[String]] = Map()

  val collections: Set[String] = Set(
    Config.mongoCollectionUser,
    Config.mongoCollectionGroup,
    Config.mongoCollectionContext
  )

}
