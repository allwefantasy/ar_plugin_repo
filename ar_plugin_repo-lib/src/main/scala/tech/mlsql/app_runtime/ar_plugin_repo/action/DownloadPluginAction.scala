package tech.mlsql.app_runtime.ar_plugin_repo.action

import net.csdn.common.exception.RenderFinish
import net.csdn.common.jline.ANSI.Renderer.RenderException
import net.csdn.modules.http.ViewType
import tech.mlsql.app_runtime.ar_plugin_repo.DownloadFileUtils
import tech.mlsql.app_runtime.ar_plugin_repo.quill_model.StorePluginType
import tech.mlsql.app_runtime.commons.Input
import tech.mlsql.common.utils.log.Logging
import tech.mlsql.serviceframework.platform.action.file.action.FileDownloadAction
import tech.mlsql.serviceframework.platform.action.{ActionContext, CustomAction, HttpContext}
import tech.mlsql.serviceframework.platform.{PluginItem, PluginType}

/**
 * 30/1/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class DownloadPluginAction extends CustomAction with Logging {

  import DownloadPluginAction._

  override def run(params: Map[String, String]): String = {
    // local/hdfs
    params.getOrElse("downloadType", "local") match {
      case "local" =>
        localDownLoad(params)
        throw new RenderException("")
      case "hdfs" =>
        new FileDownloadAction().hdfsDownload(params)
        throw new RenderException("")
    }
  }

  def localDownLoad(params: Map[String, String]) = {
    val HttpContext(request, response) = ActionContext.context().httpContext
    val pluginName = params(Params.PLUGIN_NAME.name)
    val version = params(Params.PLUGIN_VERSION.name)
    val pluginType = params.get(Params.PLUGIN_TYPE.name).map(f => StorePluginType.from(f)).getOrElse(StorePluginType.APP_RUNTIME_PLUGIN)

    val pluginInfoOpt = ArPluginRepoService.findPlugin(pluginName, version, pluginType)

    if (pluginInfoOpt.isEmpty) {
      render(response, 404, s"${pluginName}-${version} is not found")
    }
    val pluginInfo = pluginInfoOpt.head

    try {
      DownloadFileUtils.getFileByPath(response.httpServletResponse(), pluginInfo.path)
    } catch {
      case e: Exception if !e.isInstanceOf[RenderFinish] =>
        logError("download fail", e)
    }
    response.write(200, "", ViewType.stream)
    throw new RenderFinish
  }
}

object DownloadPluginAction {

  object Params {
    val PLUGIN_NAME = Input("pluginName", "")
    val PLUGIN_VERSION = Input("version", "")
    val PLUGIN_TYPE = Input("pluginType", "")
  }

  def action = "downloadPlugin"

  def plugin = PluginItem(DownloadPluginAction.action,
    classOf[DownloadPluginAction].getName, PluginType.action, None)
}
