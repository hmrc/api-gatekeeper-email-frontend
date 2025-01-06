import com.typesafe.sbt.digest.Import.*
import com.typesafe.sbt.web.Import.*
import net.ground5hark.sbt.concat.Import.*
import uk.gov.hmrc.DefaultBuildSettings

lazy val appName = "api-gatekeeper-email-frontend"

Global / bloopAggregateSourceDependencies := true
Global / bloopExportJarClassifiers := Some(Set("sources"))

ThisBuild / scalaVersion := "2.13.12"
ThisBuild / majorVersion := 0
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    Concat.groups := Seq(
      "javascripts/apis-app.js" -> group(
        (baseDirectory.value / "app" / "assets" / "javascripts") ** "*.js"
      )
    ),
    pipelineStages := Seq(digest),
    Assets / pipelineStages := Seq(
      concat
    )
  )
  .settings(
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true
  )
  .settings(
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-eT")
  )
  .settings(ScoverageSettings())
  .settings(
    TwirlKeys.templateImports ++= Seq(
      "views.html.helper.CSPNonce",
      "uk.gov.hmrc.gatekeepercomposeemailfrontend.config.AppConfig"
    )
  )
  .settings(
    scalacOptions ++= Seq(
      "-Wconf:cat=unused&src=views/.*\\.scala:s",
      // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
      // suppress warnings in generated routes files
      "-Wconf:src=routes/.*:s"
    )
  )

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(
    name := "integration-tests",
    DefaultBuildSettings.itSettings()
  )



lazy val component = (project in file("component"))
  .dependsOn(microservice % "test->test")
  .settings(
    name := "component-tests",
    libraryDependencies ++= AppDependencies(),
    Test / unmanagedResourceDirectories += baseDirectory.value / "resources",
    DefaultBuildSettings.itSettings(),
    Test / testOptions := Seq(Tests.Argument(TestFrameworks.JUnit, "-a"))
  )

commands ++= Seq(
  Command.command("cleanAll") { state => "clean" :: "it/clean"  :: "component/clean" :: state },
  Command.command("fmtAll") { state => "scalafmtAll" :: "it/scalafmtAll"  :: "component/scalafmtAll" ::state },
  Command.command("fixAll") { state => "scalafixAll" :: "it/scalafixAll"  ::  "component/scalafixAll" ::state },
  Command.command("testAllIncludedInCoverage") { state => "testOnly * -- -l ExcludeFromCoverage" :: "it/test" :: "component/test" :: state },
  Command.command("testAllExcludedFromCoverage") { state => "testOnly * -- -n ExcludeFromCoverage" :: state },
  Command.command("testAll") { state => "test" :: "it/test" :: "component/test" :: state },
  Command.command("run-all-tests") { state => "testAll" :: state },
  Command.command("clean-and-test") { state => "cleanAll" :: "compile" :: "run-all-tests" :: state },
  Command.command("pre-commit") { state => "cleanAll" :: "fmtAll" :: "fixAll" :: "testAllExcludedFromCoverage" :: "coverage" :: "testAllIncludedInCoverage" :: "coverageOff" :: "coverageAggregate" :: state }
)
