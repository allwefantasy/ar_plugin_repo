package tech.mlsql.app_runtime.plugin

import tech.mlsql.app_runtime.ar_plugin_repo.action.{DownloadPluginAction, ListPluginAction, UploadPluginAction}
import tech.mlsql.serviceframework.platform.{PluginItem, _}

class PluginDesc extends Plugin {
  override def entries: List[PluginItem] = {
    List(
      UploadPluginAction.plugin,
      DownloadPluginAction.plugin,
      ListPluginAction.plugin
    )
  }

  def registerForTest() = {
    val pluginLoader = PluginLoader(Thread.currentThread().getContextClassLoader, this)
    entries.foreach { item =>
      AppRuntimeStore.store.registerAction(item.name, item.clzzName, pluginLoader)
    }
  }
}
