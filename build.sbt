import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.uglify.Import._
import com.typesafe.sbt.web.Import._
import net.ground5hark.sbt.concat.Import._
import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.routes.RoutesKeys.routesGenerator
import sbt.Keys.{baseDirectory, unmanagedSourceDirectories, _}
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.versioning.SbtGitVersioning
import bloop.integrations.sbt.BloopDefaults

lazy val appName = "gatekeeper-compose-email-frontend"

scalaVersion := "2.13.8"

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .settings(
    Concat.groups := Seq(
      "javascripts/apis-app.js" -> group(
        (baseDirectory.value / "app" / "assets" / "javascripts") ** "*.js"
      )
    ),
    uglifyCompressOptions := Seq(
      "unused=false",
      "dead_code=true"
    ),
    uglify / includeFilter := GlobFilter("apis-*.js"),
    pipelineStages := Seq(digest),
    Assets / pipelineStages := Seq(
      concat,
      uglify
    ),
    routesImport += "uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.binders._"
  )
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(SilencerSettings(): _*)
  .settings(
    name:= appName,
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    routesGenerator := InjectedRoutesGenerator,
    shellPrompt := (_ => "> "),
    majorVersion := 0,
    routesImport += "uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.binders._",
    Test / testOptions := Seq(Tests.Argument(TestFrameworks.ScalaTest, "-eT")),
    Test / unmanagedSourceDirectories += baseDirectory.value / "testCommon",
    Test / unmanagedSourceDirectories += baseDirectory.value / "test"
  )
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    IntegrationTest / Keys.fork := false,
    IntegrationTest / parallelExecution := false,
    IntegrationTest / testOptions := Seq(Tests.Argument(TestFrameworks.ScalaTest, "-eT")),
    IntegrationTest / unmanagedSourceDirectories += baseDirectory.value / "testCommon",
    IntegrationTest / unmanagedSourceDirectories += baseDirectory.value / "it"
  )
  .configs(AcceptanceTest)
  .settings(inConfig(AcceptanceTest)(Defaults.testSettings): _*)
  .settings(inConfig(AcceptanceTest)(BloopDefaults.configSettings))
  .settings(headerSettings(AcceptanceTest) ++ automateHeaderSettings(AcceptanceTest))
  .settings(
    AcceptanceTest / Keys.fork := false,
    AcceptanceTest / parallelExecution := false,
    AcceptanceTest / testOptions := Seq(Tests.Argument("-l", "SandboxTest", "-eT")),
    AcceptanceTest / testOptions += Tests.Cleanup((loader: java.lang.ClassLoader) => loader.loadClass("uk.gov.hmrc.gatekeepercomposeemailfrontend.common.AfterHook").newInstance),
    AcceptanceTest / unmanagedSourceDirectories += baseDirectory.value / "testCommon",
    AcceptanceTest / unmanagedSourceDirectories += baseDirectory.value / "acceptance"
  )
  .configs(SandboxTest)
  .settings(inConfig(SandboxTest)(Defaults.testSettings): _*)
  .settings(inConfig(AcceptanceTest)(BloopDefaults.configSettings))
  .settings(
    SandboxTest / Keys.fork := false,
    SandboxTest / parallelExecution := false,
    SandboxTest / testOptions := Seq(Tests.Argument("-l", "NonSandboxTest"), Tests.Argument("-n", "SandboxTest", "-eT")),
    SandboxTest / testOptions += Tests.Cleanup((loader: java.lang.ClassLoader) => loader.loadClass("uk.gov.hmrc.gatekeepercomposeemailfrontend.common.AfterHook").newInstance),
    SandboxTest / unmanagedSourceDirectories += baseDirectory(_ / "acceptance").value
  )
  .settings(
    resolvers ++= Seq(
      Resolver.typesafeRepo("releases")
    ),
    TwirlKeys.templateImports ++= Seq(
    "views.html.helper.CSPNonce",
    "uk.gov.hmrc.gatekeepercomposeemailfrontend.config.AppConfig"
    )
  )
  .settings(
    scalacOptions ++= Seq(
      "-Wconf:cat=unused&src=views/.*\\.scala:s",
      "-Wconf:cat=unused&src=.*RoutesPrefix\\.scala:s",
      "-Wconf:cat=unused&src=.*Routes\\.scala:s",
      "-Wconf:cat=unused&src=.*ReverseRoutes\\.scala:s"
    )
  )
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)


lazy val AcceptanceTest = config("acceptance") extend Test
lazy val SandboxTest = config("sandbox") extend Test

coverageMinimumStmtTotal := 77
coverageFailOnMinimum := true
coverageExcludedPackages := Seq(
  "<empty>",
  "com.kenshoo.play.metrics",
  ".*definition.*",
  "prod",
  "testOnlyDoNotUseInAppConf",
  "app",
  "uk.gov.hmrc.BuildInfo"
).mkString(";")
