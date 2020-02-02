package tech.mlsql.app_runtime.ar_plugin_repo.action

import net.csdn.common.exception.RenderFinish
import net.csdn.common.jline.ANSI.Renderer.RenderException
import net.csdn.modules.http.ViewType
import tech.mlsql.app_runtime.ar_plugin_repo.DownloadFileUtils
import tech.mlsql.common.utils.log.Logging
import tech.mlsql.serviceframework.platform.action.file.action.FileDownloadAction
import tech.mlsql.serviceframework.platform.action.{ActionContext, CustomAction, HttpContext}
import tech.mlsql.serviceframework.platform.{PluginItem, PluginType}

/**
 * 30/1/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class DownloadPluginAction extends CustomAction with Logging {
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
    val pluginName = params("pluginName")
    val version = params("version")

    val pluginInfoOpt = ArPluginRepoService.findPlugin(pluginName, version)

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
  def action = "downloadPlugin"

  def plugin = PluginItem(DownloadPluginAction.action,
    classOf[DownloadPluginAction].getName, PluginType.action, None)
}
