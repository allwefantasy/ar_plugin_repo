package tech.mlsql.app_runtime.ar_plugin_repo.quill_model

case class PluginStoreItem(id: Int, name: String, path: String, version: String, pluginType: Int, extraParams: String)

case class PluginUser(id: Int, userId: Int, pluginId: Int, relateType: Int)


object StorePluginType extends Enumeration {
  type StorePluginType = Value
  val MLSQL_PLUGIN = Value(0)
  val APP_RUNTIME_PLUGIN = Value(1)

  def from(str: String) = {
    str match {
      case "MLSQL_PLUGIN" => StorePluginType.MLSQL_PLUGIN
      case "APP_RUNTIME_PLUGIN" => StorePluginType.APP_RUNTIME_PLUGIN
    }
  }
}

object PluginUserRelateType extends Enumeration {
  type DictType = Value
  val OWENER = Value(0)
}
