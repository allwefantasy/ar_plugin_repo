package tech.mlsql.app_runtime.plugin

import net.csdn.ServiceFramwork
import net.csdn.bootstrap.Application
import tech.mlsql.serviceframework.platform.runtime.BuildInAppRuntime

object App {
  def main(args: Array[String]): Unit = {
    BuildInAppRuntime.main(args, List(
      new DBPluginDesc,
      new UserPluginDesc,
      new ConsolePluginDesc
    ))
  }

}

class App {

}
