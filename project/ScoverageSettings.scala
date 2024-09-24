import scoverage.ScoverageKeys

object ScoverageSettings {
  def apply() = Seq(
    ScoverageKeys.coverageMinimumStmtTotal := 82,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    ScoverageKeys.coverageExcludedPackages := Seq(
      "<empty>",
      ".*definition.*",
      "prod",
      "testOnlyDoNotUseInAppConf",
      "app",
      "uk.gov.hmrc.BuildInfo"
    ).mkString(";")
  )
}
