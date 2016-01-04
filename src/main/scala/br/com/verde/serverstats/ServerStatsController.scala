package br.com.verde.serverstats

import com.twitter.finatra.http.Controller
import com.google.inject.Inject
import com.twitter.finatra.validation._
import com.google.inject.Singleton
import com.twitter.finagle.http.Request
import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean
import java.io.File

@Singleton
class ServerStatsController @Inject() extends Controller {

  val runtime = Runtime.getRuntime

  val osBean = ManagementFactory.getOperatingSystemMXBean
  val runtimeBean = ManagementFactory.getRuntimeMXBean
  val memoryBean = ManagementFactory.getMemoryMXBean

  val megabyte = 1024 * 1024
  val second = 1000 // milisecond

  get("/serverStats") {
    request: Request =>

      val jvmTotalRam = runtime.maxMemory
      val jvmFreeRam = runtime.freeMemory
      val availableProcessors = osBean.getAvailableProcessors

      val freeSpace = File.listRoots.head.getFreeSpace

      val serverSystemLoad = osBean.getSystemLoadAverage
      val serverUptime = runtimeBean.getUptime

      val param = request.getParam("onlyRam")
      if (param == null) {
        response
          .ok
          .json(s"""{ "jvm_total_ram": "${jvmTotalRam / megabyte} Mb",
          "jvm_free_ram": "${jvmFreeRam / megabyte} Mb",
          "free_space": "${freeSpace / megabyte} Mb",
          "server_system_load": "${serverSystemLoad} %",
          "server_uptime": "${serverUptime / second} s",
          "available_processors": "${availableProcessors}" }""")
      } else {
        response
          .ok
          .json(s"""{ "jvm_total_ram": "${jvmTotalRam / megabyte} Mb",
          "jvm_free_ram": "${jvmFreeRam / megabyte} Mb" }""")
      }
  }
}