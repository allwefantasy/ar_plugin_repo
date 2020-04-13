package tech.mlsql.app_runtime.ar_plugin_repo.action

import tech.mlsql.app_runtime.ar_plugin_repo.PluginDB.ctx
import tech.mlsql.app_runtime.ar_plugin_repo.PluginDB.ctx._
import tech.mlsql.app_runtime.ar_plugin_repo.quill_model.{PluginStoreItem, StorePluginType}
import tech.mlsql.app_runtime.commons.{FormParams, Input, KV, Select}
import tech.mlsql.app_runtime.plugin.user.action.BaseAction
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.serviceframework.platform.{PluginItem, PluginType}

/**
 * 6/3/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class GetPluginAction extends BaseAction {
  override def _run(params: Map[String, String]): String = {
    import GetPluginAction._
    val pluginName = params(Params.PLUGIN_NAME.name)
    val versionOpt = params.get(Params.PLUGIN_VERSION.name)
    val pluginType = params.get(Params.PLUGIN_TYPE.name).map(f => StorePluginType.from(f)).getOrElse(StorePluginType.APP_RUNTIME_PLUGIN)

    val query = versionOpt match {
      case Some(version) =>
        quote {
          ctx.query[PluginStoreItem].filter { item =>
            item.name == lift(pluginName) && item.version == lift(version) && item.pluginType == lift(pluginType.id)
          }
        }
      case None =>
        quote {
          ctx.query[PluginStoreItem].filter { item =>
            item.name == lift(pluginName) && item.pluginType == lift(pluginType.id)
          }
        }
    }

    JSONTool.toJsonStr(ctx.run(query).map(f => f.copy(path = "")))
  }

  override def _help(): String = {
    JSONTool.toJsonStr(FormParams.toForm(GetPluginAction.Params).toList.reverse)
  }
}

object GetPluginAction {

  object Params {
    val PLUGIN_NAME = Input("pluginName", "")
    val PLUGIN_TYPE = Select("pluginType", List(), valueProvider = Option(() => {
      List(
        KV(Option("MLSQL"), Option("MLSQL_PLUGIN")),
        KV(Option("APP_RUNTIME"), Option("APP_RUNTIME_PLUGIN"))
      )
    }))
    val PLUGIN_VERSION = Input("version", "")
  }

  def action = "getPlugin"

  def plugin = PluginItem(GetPluginAction.action,
    classOf[GetPluginAction].getName, PluginType.action, None)
}


