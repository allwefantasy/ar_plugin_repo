package tech.mlsql.app_runtime.ar_plugin_repo.action

import java.io.File

import org.apache.commons.fileupload.FileItem
import org.apache.commons.io.FileUtils
import tech.mlsql.app_runtime.ar_plugin_repo.PluginDB
import tech.mlsql.app_runtime.ar_plugin_repo.PluginDB.ctx
import tech.mlsql.app_runtime.ar_plugin_repo.PluginDB.ctx._
import tech.mlsql.app_runtime.ar_plugin_repo.quill_model.{PluginStoreItem, PluginUser, PluginUserRelateType}
import tech.mlsql.app_runtime.db.quill_model.DictType
import tech.mlsql.app_runtime.db.service.BasicDBService
import tech.mlsql.app_runtime.plugin.user.Session
import tech.mlsql.app_runtime.plugin.user.action.{IsLogin, UserQuery, UserSystemActionProxy}
import tech.mlsql.app_runtime.plugin.user.quill_model.User
import tech.mlsql.common.utils.log.Logging
import tech.mlsql.common.utils.path.PathFun
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.serviceframework.platform.action.{ActionContext, CustomAction}
import tech.mlsql.serviceframework.platform.{PluginItem, PluginType}

import scala.collection.JavaConverters._

/**
 * 21/1/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class UploadPluginAction extends CustomAction with Logging {
  override def run(params: Map[String, String]): String = {
    val userName = params("name")
    val pluginName = params("pluginName")
    val versionOpt = params.get("version")
    val actionContext = ActionContext.context()
    val sessionOpt = ArPluginRepoService.isLogin(params)
    if (sessionOpt.isEmpty) render(actionContext.httpContext.response, 400, JSONTool.toJsonStr(Map("msg" -> "user is not login")))
    val repoLocation = ArPluginRepoService.repoLocation

    val items = actionContext.others(ActionContext.Config.formItems).asInstanceOf[java.util.List[FileItem]]
    try {

      items.asScala.filterNot(f => f.isFormField).map {
        item =>
          val fileContent = item.getInputStream()
          val tempFilePath = PathFun(repoLocation).add(userName).add(item.getFieldName).toPath
          val dir = new File(repoLocation)
          if (!dir.exists()) {
            dir.mkdirs()
          }
          val targetPath = new File(tempFilePath)
          FileUtils.copyInputStreamToFile(fileContent, targetPath)
          fileContent.close()
          val version = versionOpt.getOrElse(tempFilePath.split("/").last.split("-").last.dropRight(4))
          ArPluginRepoService.saveUploadInfo(userName, pluginName, tempFilePath, version)
          tempFilePath

      }
    } catch {
      case e: Exception =>
        throw e
    }
    JSONTool.toJsonStr(Map("msg" -> "Upload success"))

  }
}


object UploadPluginAction {
  def action = "uploadPlugin"

  def plugin = PluginItem(UploadPluginAction.action,
    classOf[UploadPluginAction].getName, PluginType.action, None)
}

object ArPluginRepoService {

  object Config {
    val REPO_LOCATION_KEY = s"${PluginDB.plugin_name}__config__repoLocation"
    val DEFAULT_REPO_LOCATION = "/tmp/repo"
  }

  def isLogin(params: Map[String, String]) = {
    val isLoginStr = UserSystemActionProxy.proxy.run(IsLogin.action, params)
    val isLogin = JSONTool.parseJson[List[Session]](isLoginStr)
    isLogin.headOption
  }

  def repoLocation = {
    val repo = BasicDBService.fetch(
      ArPluginRepoService.Config.REPO_LOCATION_KEY,
      DictType.SYSTEM_CONFIG).map(f => f.value).getOrElse(ArPluginRepoService.Config.DEFAULT_REPO_LOCATION)
    repo
  }

  def findPlugin(pluginName: String, version: String) = {
    ctx.run(ctx.query[PluginStoreItem].filter { item =>
      item.name == lift(pluginName) && item.version == lift(version)
    }).headOption
  }

  def saveUploadInfo(user: String, pluginName: String, jarPath: String, version: String) = {

    def findPlugin() = {
      quote {
        ctx.query[PluginStoreItem].filter { item =>
          item.name == lift(pluginName) && item.version == lift(version)
        }
      }
    }

    def findRelate(userId: Int, pluginId: Int) = {
      quote {
        ctx.query[PluginUser].filter { item =>
          item.userId == lift(userId) &&
            item.pluginId == lift(pluginId) &&
            item.relateType == lift(PluginUserRelateType.OWENER.id)
        }
      }
    }

    val userStr = UserSystemActionProxy.proxy.run(UserQuery.action, Map("name" -> user))
    val userRef = JSONTool.parseJson[List[User]](userStr).head
    ctx.run(
      findPlugin()
    ).headOption match {
      case Some(plugin) =>
        ctx.run(findPlugin().update(_.path -> lift(jarPath)))
      case None =>
        val pluginId = ctx.run(ctx.query[PluginStoreItem].insert(
          _.name -> lift(pluginName),
          _.path -> lift(jarPath),
          _.version -> lift(version)
        ).returningGenerated(_.id))

        ctx.run(ctx.query[PluginUser].insert(
          _.userId -> lift(userRef.id),
          _.relateType -> lift(PluginUserRelateType.OWENER.id),
          _.pluginId -> lift(pluginId)
        ))
    }
  }
}
