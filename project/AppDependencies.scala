import sbt._

object AppDependencies {

  lazy val bootstrapPlayVersion = "10.7.0"
  lazy val jsoupVersion         = "1.21.1"
  lazy val scalaCheckVersion    = "1.14.0"
  lazy val apiDomainVersion     = "1.1.0-SNAPSHOT"
  lazy val hmrcFrontendVersion  = "12.32.0"
  lazy val mockitoScalaVersion  = "2.0.0"

  def apply(): Seq[ModuleID] = compile ++ test

  lazy val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % hmrcFrontendVersion,
    "uk.gov.hmrc" %% "api-platform-api-domain"    % apiDomainVersion
  )

  lazy val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"    %% "bootstrap-test-play-30"  % bootstrapPlayVersion,
    "org.jsoup"       % "jsoup"                   % jsoupVersion,
    "uk.gov.hmrc"    %% "ui-test-runner"          % "0.46.0",
    "org.mockito"    %% "mockito-scala-scalatest" % mockitoScalaVersion,
    "org.scalacheck" %% "scalacheck"              % scalaCheckVersion
  ).map(_ % "test")
}
