package tech.mlsql.app_runtime.ar_plugin_repo.action

import java.io.File

import org.apache.commons.fileupload.FileItem
import org.apache.commons.io.FileUtils
import tech.mlsql.app_runtime.ar_plugin_repo.action.UploadPluginAction.Params
import tech.mlsql.app_runtime.ar_plugin_repo.quill_model.StorePluginType
import tech.mlsql.app_runtime.commons.{FormParams, Input}
import tech.mlsql.app_runtime.plugin.user.action.BaseAction
import tech.mlsql.common.utils.log.Logging
import tech.mlsql.common.utils.path.PathFun
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.serviceframework.platform.action.ActionContext
import tech.mlsql.serviceframework.platform.{PluginItem, PluginType}

import scala.collection.JavaConverters._

/**
 * 21/1/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class UploadPluginAction extends BaseAction with Logging {
  override def _run(params: Map[String, String]): String = {
    import UploadPluginAction._
    val userName = params(Params.USER_NAME.name)
    val pluginName = params(Params.PLUGIN_NAME.name)
    val versionOpt = params.get(Params.PLUGIN_VERSION.name)
    val pluginType = params.get(Params.PLUGIN_TYPE.name).map(f => StorePluginType.from(f)).getOrElse(StorePluginType.APP_RUNTIME_PLUGIN)
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
          ArPluginRepoService.saveUploadInfo(userName, pluginName, tempFilePath, version, pluginType, buildExtraParams(params))
          tempFilePath

      }
    } catch {
      case e: Exception =>
        throw e
    }
    JSONTool.toJsonStr(Map("msg" -> "Upload success"))

  }

  def buildExtraParams(params: Map[String, String]) = {
    val pluginType = params.get(Params.PLUGIN_TYPE.name).map(f => StorePluginType.from(f)).getOrElse(StorePluginType.APP_RUNTIME_PLUGIN)
    pluginType match {
      case StorePluginType.APP_RUNTIME_PLUGIN => "{}"
      case StorePluginType.MLSQL_PLUGIN =>
        JSONTool.toJsonStr(params - Params.USER_NAME.name - "password")
    }


  }


  override def _help(): String = {
    JSONTool.toJsonStr(FormParams.toForm(UploadPluginAction.Params).toList.reverse)
  }
}


object UploadPluginAction {

  object Params {
    val USER_NAME = Input("userName", "")
    val PLUGIN_NAME = Input("pluginName", "")
    val PLUGIN_TYPE = Input("pluginType", "")
    val PLUGIN_VERSION = Input("version", "")
  }

  def action = "uploadPlugin"

  def plugin = PluginItem(UploadPluginAction.action,
    classOf[UploadPluginAction].getName, PluginType.action, None)
}


