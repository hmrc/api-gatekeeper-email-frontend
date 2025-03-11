import sbt._

object AppDependencies {

  lazy val bootstrapPlayVersion = "9.11.0"
  lazy val jsoupVersion         = "1.13.1"
  lazy val scalaCheckVersion    = "1.14.0"
  lazy val apiDomainVersion     = "0.19.1"
  lazy val hmrcFrontendVersion  = "11.11.0"

  def apply(): Seq[ModuleID] = compile ++ test

  lazy val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % hmrcFrontendVersion,
    "uk.gov.hmrc" %% "api-platform-api-domain"    % apiDomainVersion
  )

  lazy val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"          % bootstrapPlayVersion,
    "org.jsoup"               % "jsoup"                           % jsoupVersion,
    "uk.gov.hmrc"            %% "ui-test-runner"                  % "0.45.0",
    "org.mockito"            %% "mockito-scala-scalatest"         % "1.17.30",
    "org.scalacheck"         %% "scalacheck"                      % scalaCheckVersion
  ).map(_ % "test")

}
