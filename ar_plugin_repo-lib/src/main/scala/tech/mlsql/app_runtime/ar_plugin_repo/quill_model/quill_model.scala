package tech.mlsql.app_runtime.ar_plugin_repo.quill_model

case class PluginStoreItem(id: Int, name: String, path: String, version: String)

case class PluginUser(id: Int, userId: Int, pluginId: Int, relateType: Int)

object PluginUserRelateType extends Enumeration {
  type DictType = Value
  val OWENER = Value(0)
}
