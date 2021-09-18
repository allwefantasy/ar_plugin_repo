package tech.mlsql.app_runtime.ar_plugin_repo.action

import tech.mlsql.app_runtime.ar_plugin_repo.action.UploadPluginAction.Params
import tech.mlsql.app_runtime.ar_plugin_repo.quill_model.StorePluginType
import tech.mlsql.serviceframework.platform.form.{Editor, FormParams, Input, KV}
import tech.mlsql.app_runtime.user.action.BaseAction
import tech.mlsql.common.utils.log.Logging
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.serviceframework.platform.{PluginItem, PluginType}

/**
 * 26/4/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class CreateScriptAction extends BaseAction with Logging {
  override def _run(params: Map[String, String]): String = {
    val userName = params(Params.USER_NAME.name)
    val pluginName = params(Params.PLUGIN_NAME.name)
    val versionOpt = params.get(Params.PLUGIN_VERSION.name)
    val pluginType = StorePluginType.MLSQL_SCRIPT

    ArPluginRepoService.saveUploadInfo(userName, pluginName, "", versionOpt.getOrElse("0.1.0"), pluginType, buildExtraParams(params))
    JSONTool.toJsonStr(Map("msg" -> "Create success"))
  }

  def buildExtraParams(params: Map[String, String]) = {
    JSONTool.toJsonStr(params - Params.USER_NAME.name - "password")
  }

  override def _help(): String = {
    JSONTool.toJsonStr(FormParams.toForm(CreateScriptAction.Params).toList.reverse)
  }
}

object CreateScriptAction {

  object Params {
    val USER_NAME = Input("userName", "")
    val PLUGIN_NAME = Input("pluginName", "")
    val PLUGIN_VERSION = Input("version", "")
    val content = Editor("content", values = List(), valueProvider = Option(() => {
      List(KV(Option("sql"), Option("")))
    }))
  }

  def action = "createScript"

  def plugin = PluginItem(CreateScriptAction.action,
    classOf[CreateScriptAction].getName, PluginType.action, None)
}
