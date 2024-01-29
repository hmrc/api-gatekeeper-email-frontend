import play.core.PlayVersion
import sbt._

object AppDependencies {

  lazy val bootstrapPlayVersion = "7.21.0"
  lazy val jsoupVersion = "1.13.1"
  lazy val scalaCheckVersion = "1.14.0"
  lazy val seleniumVersion = "4.8.3"

  def apply(): Seq[ModuleID] = dependencies ++ testDependencies

  lazy val dependencies = Seq(
    "uk.gov.hmrc"       %%  "bootstrap-frontend-play-28"    % bootstrapPlayVersion,
    "uk.gov.hmrc"       %%  "play-frontend-hmrc"            % "7.19.0-play-28"
  )

  lazy val testScopes = Seq(Test.name, IntegrationTest.name, "acceptance").mkString(",")

  lazy val testDependencies: Seq[ModuleID] = Seq(
    "org.scalatestplus.play"  %%  "scalatestplus-play"        % "5.1.0",
    "org.pegdown"             %   "pegdown"                   % "1.6.0",
    "org.jsoup"               %   "jsoup"                     % jsoupVersion,
    "com.github.tomakehurst"  %   "wiremock"                  % "1.58",
    "org.seleniumhq.selenium" %   "selenium-java"             % seleniumVersion,
    "org.seleniumhq.selenium" %   "htmlunit-driver"           % seleniumVersion,
    "org.mockito"             %%  "mockito-scala-scalatest"   % "1.17.29",
    "org.scalatest"           %%  "scalatest"                 % "3.2.17",
    "org.scalacheck"          %%  "scalacheck"                % scalaCheckVersion,
    "uk.gov.hmrc"             %%  "bootstrap-test-play-28"    % bootstrapPlayVersion,
    "com.vladsch.flexmark"    %   "flexmark-all"              % "0.62.2"
  ).map (_ % testScopes)
}