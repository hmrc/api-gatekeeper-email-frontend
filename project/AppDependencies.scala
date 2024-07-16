import sbt._

object AppDependencies {

  lazy val bootstrapPlayVersion = "9.0.0"
  lazy val jsoupVersion         = "1.13.1"
  lazy val scalaCheckVersion    = "1.14.0"
  lazy val seleniumVersion      = "4.8.3"

  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  lazy val dependencies = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "9.0.0"
  )

  lazy val testDependencies: Seq[ModuleID] = Seq(
    "org.pegdown"             % "pegdown"                 % "1.6.0",
    "org.jsoup"               % "jsoup"                   % jsoupVersion,
    "org.seleniumhq.selenium" % "selenium-java"           % seleniumVersion,
    "org.seleniumhq.selenium" % "htmlunit-driver"         % seleniumVersion,
    "org.mockito"            %% "mockito-scala-scalatest" % "1.17.30",
    "org.scalacheck"         %% "scalacheck"              % scalaCheckVersion,
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"  % bootstrapPlayVersion
  ).map(_ % "test")
}
