# SERVIÇOS REST DIRETAMENTE AO PONTO COM FINATRA

Saudações! Na Verde Tecnologia costumamos buscar tecnologias e práticas que nos permitam elevar a um patamar de inovação e já há alguns anos adotamos Scala como nossa principal linguagem em backend. Mais do que as vantagens de sintaxe da linguagem, aproveitamos a maturidade JVM, a extensa gama de Frameworks/Bibliotecas escritas em "bytecode" mas a real vantagem é abertura da mente pra novas ideias. O ambiente Scala propicia descobrir que existe mundo além dos **POJOs** e criar uma "dependência" naqueles que começam a se aventurar pelo paradigma Funcional. Essa proximidade com o mundo Java atraiu muitos projetos interessantes como por o **Akka** (ecossistema para programação distribuída baseada no Modelo de Atores) ou o **Apache Spark** (engine para BigData).

Mas nem só de complexidade vive a inovação! Esse post mostra de forma concisa como criar de rapidamente um servidor REST usando a biblioteca **Finatra**. Essa é uma das formas mais práticas de se construir um backend para aplicações móveis, web ou até mesmo como parte de um sistema de Microserviços.

O **Finatra** é uma biblioteca fundamentada sobre o **Finagle**, projeto de **RPC** Twitter altamente assíncrono para aplicações de alta concorrência. Se aproveitando disso, o **Finatra** usa uma interface muito simples para definir os Serviços REST de uma forma bem elegante. 

Site oficial: [Finatra](https://twitter.github.io/finatra/) 

## Requisitos

Utilizando o gerenciador de dependências/build SBT, devemos indicar o repositório do **Finagle** e quais bibliotecas vamos utilizar: 

### Arquivo build.sbt:

```scala
resolvers ++= Seq(
  "Local Maven Repository" at "" + Path.userHome.asFile.toURI.toURL + "/.m2/repository",
  "Twitter" at "http://maven.twttr.com"
)

libraryDependencies ++= {
  val finatraV = "2.1.1"
  Seq(
    "com.twitter.finatra"   %%   "finatra-http"    % finatraV,
    "com.twitter.finatra"   %%   "finatra-slf4j"   % finatraV,
    "com.twitter.inject"    %%   "inject-core"     % finatraV,
    
    "ch.qos.logback"        %    "logback-classic" % "1.0.13"
  )
}
```

Após baixar as dependências no SBT, definiremos os serviços. No exemplo foi definido um único serviço, via método **GET** que responde em formato **JSON** algumas métricas do servidor rodando na JVM: 

### Arquivo ServerStatsController.scala:

```scala
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
```

Um controlador que define o serviço deve estender a classe Controller. Na classe ServerStatsController utilizamos a injeção de dependência do Google Guice para criar o objeto Controller. Essa criação acontece de forma transparente a partir da anotação @Inject. A anotação @Singleton também do Guice garante que só existirá uma instância desse objeto na aplicação.

A definição do serviço ocorre na função *get* (pode ser usada outra função do HTTP como *post*, *delete*, etc.), onde o parâmetro define a rota (complemento da url de onde é acessado o serviço).

Os dados da requisição vem com o objeto *request*, de onde no caso podem ser obtidos os parâmetros passados pela url. Neste é lido onde existir um parâmetro get *onlyRam*, para modificar a resposta de acordo.

O retorno da função deve ser um objeto do tipo Response, que é construído a partir da função *response* fornecido pelo Controller. Utilizando o *response* construímos como será a resposta do HTTP, definindo o status e o corpo. No exemplo é utilizado o status *ok* e o corpo é no formato *json*.

Já definido o serviço, só precisamos de um servidor para botar no ar. O **Finagle** nos provê um da forma mais simples possível: 

### Arquivo AppServe.scala:

```scala
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
```

Definimos uma classe que estende HttpServer e sobrescrevemos a função *configureHttp* onde é adicionado o controlador. Ao indicar a classe *ServerStatsController* via função *add* é injetado no sistema de rotas os serviços definidos no controlador.

Além disso criamos um *object* para rodar nosso servidor. Pronto! Já temos nosso REST básico e sem complicações!

Ao rodar a classe AppServerMain já podemos utilizar nosso serviço via url/porta padrão: **localhost:8888/serverStats** ou podemos passar um parâmetro pro serviço para pegar somente os dados de memória do servidor: **localhost:8888/serverStats?onlyRam**

O código completo, com arquivos de configuração e plugins do SBT está disponível no GitHub: [github.com/verdetecnologia/ServerStats](https://github.com/verdetecnologia/ServerStats) 

## Fazendo o build e desenvolvendo com o SBT
Baixando o projeto pelo GitHub, já existem algumas configurações e plugins para facilitar o desenvolvimento. Utilize-os sem moderação, para ajudar nas tarefas ingratas de *setup* do projeto! Rode o SBT na pasta:
` $ sbt'

Para criar o projeto Eclipse:
`sbt> eclipse`

Para rodar/reiniciar o servidor após alterações:
`sbt> re-start`

Para parar o servidor
`sbt> re-stop`

Para gerar uma distribuição do servidor (É gerado na pasta **target/universal/stage**):
`sbt> stage`

Então temos um servidor REST de forma bem básica, mas porém poderosíssima e ágil que pode se adequar a várias aplicações de backend. O **Finatra** possui muito mais recursos que podem ser explorados dentro de sua documentação.

*Se divirtam!!*
