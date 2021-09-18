package tech.mlsql.app_runtime.ar_plugin_repo.action

import tech.mlsql.serviceframework.platform.form.FormParams
import tech.mlsql.app_runtime.user.action.BaseAction
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.serviceframework.platform.{PluginItem, PluginType}

/**
 * 6/3/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class ListPluginAction extends BaseAction {
  override def _run(params: Map[String, String]): String = {
    JSONTool.toJsonStr(ArPluginRepoService.listPlugins.map(f => f.copy(path = "")))
  }

  override def _help(): String = {
    JSONTool.toJsonStr(FormParams.toForm(DownloadPluginAction.Params).toList.reverse)
  }
}

object ListPluginAction {

  object Params {

  }

  def action = "listPlugins"

  def plugin = PluginItem(ListPluginAction.action,
    classOf[ListPluginAction].getName, PluginType.action, None)
}
