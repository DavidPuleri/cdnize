package com.davidpuleri.cdnize.config

import com.typesafe.config.Config

class AppConfig(conf: Config) {
  val port: Int = conf.getInt("port")
  val baseFolder: String = conf.getString("baseFolder")
  val cacheFolder: String = conf.getString("cacheFolder")

}
