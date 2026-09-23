import sbt._

object AppDependencies {

  lazy val bootstrapPlayVersion = "10.8.0"
  lazy val jsoupVersion         = "1.21.1"
  lazy val commonDomainVersion  = "1.4.0"
  lazy val apiDomainVersion     = "1.8.0"
  lazy val hmrcFrontendVersion  = "12.32.0"

  def apply(): Seq[ModuleID] = compile ++ test

  lazy val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % hmrcFrontendVersion,
    "uk.gov.hmrc" %% "api-platform-common-domain" % commonDomainVersion,
    "uk.gov.hmrc" %% "api-platform-api-domain"    % apiDomainVersion
  )

  lazy val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"    %% "bootstrap-test-play-30"  % bootstrapPlayVersion,
    "org.jsoup"       % "jsoup"                   % jsoupVersion,
    "uk.gov.hmrc"    %% "ui-test-runner"          % "0.53.0",
    "uk.gov.hmrc"    %% "api-platform-common-domain-fixtures" % commonDomainVersion
  ).map(_ % "test")
}
