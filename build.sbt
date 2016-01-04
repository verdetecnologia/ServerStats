name := "Server Stats"

organization  := "br.com.verde"

version       := "0.0.1"

scalaVersion  := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

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

Revolver.settings

sources in doc in Compile := List()

logLevel in test := Level.Debug

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "junitxml")

enablePlugins(JavaServerAppPackaging)

packageSummary in Linux := "Server Stats"
packageSummary in Windows := "Server Stats"
packageDescription := "Servidor REST para prover serviços para monitorar um servidor"

maintainer in Windows := "Verde Tecnologia"
maintainer in Debian := "Verde Tecnologia <contato@verde.com.br>"