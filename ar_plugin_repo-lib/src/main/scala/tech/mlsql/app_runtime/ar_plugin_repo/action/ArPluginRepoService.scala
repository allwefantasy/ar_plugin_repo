package tech.mlsql.app_runtime.ar_plugin_repo.action

import tech.mlsql.app_runtime.ar_plugin_repo.PluginDB
import tech.mlsql.app_runtime.ar_plugin_repo.PluginDB.ctx
import tech.mlsql.app_runtime.ar_plugin_repo.PluginDB.ctx._
import tech.mlsql.app_runtime.ar_plugin_repo.quill_model.StorePluginType.StorePluginType
import tech.mlsql.app_runtime.ar_plugin_repo.quill_model.{PluginStoreItem, PluginUser, PluginUserRelateType}
import tech.mlsql.app_runtime.db.quill_model.DictType
import tech.mlsql.app_runtime.db.service.BasicDBService
import tech.mlsql.app_runtime.plugin.user.Session
import tech.mlsql.app_runtime.plugin.user.action.{IsLoginAction, UserQuery, UserSystemActionProxy}
import tech.mlsql.app_runtime.plugin.user.quill_model.User
import tech.mlsql.common.utils.serder.json.JSONTool

/**
 * 6/3/2020 WilliamZhu(allwefantasy@gmail.com)
 */
object ArPluginRepoService {

  object Config {
    val REPO_LOCATION_KEY = s"${PluginDB.plugin_name}__config__repoLocation"
    val DEFAULT_REPO_LOCATION = "/tmp/repo"
  }

  def isLogin(params: Map[String, String]) = {
    val isLoginStr = UserSystemActionProxy.proxy.run(IsLoginAction.action, params)
    val isLogin = JSONTool.parseJson[List[Session]](isLoginStr)
    isLogin.headOption
  }

  def repoLocation = {
    val repo = BasicDBService.fetch(
      ArPluginRepoService.Config.REPO_LOCATION_KEY,
      DictType.SYSTEM_CONFIG).map(f => f.value).getOrElse(ArPluginRepoService.Config.DEFAULT_REPO_LOCATION)
    repo
  }

  def listPlugins = {
    ctx.run(ctx.query[PluginStoreItem]).toList
  }

  def findPlugin(pluginName: String, version: String, pluginType: StorePluginType) = {
    ctx.run(findPluginCriteria(pluginName, version, pluginType)).headOption
  }


  def findPluginCriteria(pluginName: String, version: String, pluginType: StorePluginType) = {
    quote {
      ctx.query[PluginStoreItem].filter { item =>
        item.name == lift(pluginName) && item.version == lift(version) && item.pluginType == lift(pluginType.id)
      }
    }
  }

  def saveUploadInfo(user: String, pluginName: String, jarPath: String, version: String, pluginType: StorePluginType, extraParams: String = "{}") = {

    def findRelate(userId: Int, pluginId: Int) = {
      quote {
        ctx.query[PluginUser].filter { item =>
          item.userId == lift(userId) &&
            item.pluginId == lift(pluginId) &&
            item.relateType == lift(PluginUserRelateType.OWENER.id)
        }
      }
    }

    val criteria = findPluginCriteria(pluginName, version, pluginType)
    val userStr = UserSystemActionProxy.proxy.run(UserQuery.action, Map("userName" -> user))
    val userRef = JSONTool.parseJson[List[User]](userStr).head
    ctx.run(
      criteria
    ).headOption match {
      case Some(plugin) =>
        ctx.run(criteria.update(_.path -> lift(jarPath), _.extraParams -> lift(extraParams)))
      case None =>
        val pluginId = ctx.run(ctx.query[PluginStoreItem].insert(
          _.name -> lift(pluginName),
          _.path -> lift(jarPath),
          _.version -> lift(version),
          _.pluginType -> lift(pluginType.id),
          _.extraParams -> lift(extraParams)
        ).returningGenerated(_.id))

        ctx.run(ctx.query[PluginUser].insert(
          _.userId -> lift(userRef.id),
          _.relateType -> lift(PluginUserRelateType.OWENER.id),
          _.pluginId -> lift(pluginId)
        ))
    }
  }
}
