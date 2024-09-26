import scoverage.ScoverageKeys

object ScoverageSettings {
  def apply() = Seq(
    ScoverageKeys.coverageMinimumStmtTotal := 88,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    ScoverageKeys.coverageExcludedPackages := Seq(
      "<empty>",
      ".*definition.*",
      ".*testonly.*",
      "prod",
      "testOnlyDoNotUseInAppConf",
      "app",
      "uk.gov.hmrc.BuildInfo"
    ).mkString(";")
  )
}
