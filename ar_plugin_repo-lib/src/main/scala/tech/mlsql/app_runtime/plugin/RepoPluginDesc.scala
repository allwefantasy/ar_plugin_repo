package tech.mlsql.app_runtime.plugin

import tech.mlsql.app_runtime.ar_plugin_repo.action._
import tech.mlsql.serviceframework.platform.{Plugin, PluginItem}

/**
 * 18/9/2021 WilliamZhu(allwefantasy@gmail.com)
 */
class RepoPluginDesc extends Plugin {
  override def entries: List[PluginItem] = {
    List(
      UploadPluginAction.plugin,
      DownloadPluginAction.plugin,
      ListPluginAction.plugin,
      GetPluginAction.plugin,
      CreateScriptAction.plugin
    )
  }
}
