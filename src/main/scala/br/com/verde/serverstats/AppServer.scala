package br.com.verde.serverstats

import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.logging.modules.Slf4jBridgeModule

object AppServerMain extends AppServer

class AppServer extends HttpServer {
  
  override def modules = Seq(Slf4jBridgeModule)
  
  override def configureHttp(router: HttpRouter) {
    router
    .filter[CommonFilters]
    .add[ServerStatsController]
  }
}