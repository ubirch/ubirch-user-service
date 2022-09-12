package com.ubirch.user.core.manager

import com.ubirch.user.core.manager.util.UserInfoUtil
import com.ubirch.user.model.db.User
import com.ubirch.user.model.rest.{SimpleUserContext, UserInfo, UserUpdate}
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-04-25
  */
object UserInfoManager {

  /**
    * @param simpleUserContext user is determined based on this context
    * @param mongo             mongo connection
    * @return None if no user is found; Some otherwise
    */
  def getInfo(simpleUserContext: SimpleUserContext)(implicit mongo: MongoUtil): Future[Option[UserInfo]] = {

    UserManager.findByProviderIdAndExternalId(
      providerId = simpleUserContext.providerId,
      externalUserId = simpleUserContext.userId
    ) flatMap {

      case None => Future(None)

      case Some(user: User) =>
        GroupsManager.findByContextAndUser(
          contextName = simpleUserContext.context,
          providerId = simpleUserContext.providerId,
          externalUserId = simpleUserContext.userId
        ) map { groups =>
          val myGroups = groups filter (_.ownerIds.contains(user.id))
          val allowedGroups = groups diff myGroups

          val info = UserInfo(
            displayName = user.displayName,
            locale = user.locale,
            activeUser = user.activeUser.getOrElse(false),
            myGroups = UserInfoUtil.toUserInfoGroups(myGroups),
            allowedGroups = UserInfoUtil.toUserInfoGroups(allowedGroups)
          )
          Some(info)

        }

    }

  }

  def update(simpleUserContext: SimpleUserContext, userUpdate: UserUpdate)(implicit
                                                                           mongo: MongoUtil): Future[Option[UserInfo]] = {

    UserManager.findByProviderIdAndExternalId(
      providerId = simpleUserContext.providerId,
      externalUserId = simpleUserContext.userId
    ) flatMap {

      case None => Future(None)

      case Some(user: User) =>
        val forUpdate = user.copy(displayName = userUpdate.displayName)
        if (user == forUpdate) {

          getInfo(simpleUserContext)

        } else {

          UserManager.update(forUpdate) flatMap {
            case None => Future(None)
            case Some(_: User) => getInfo(simpleUserContext)
          }

        }

    }

  }

}
