/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.gatekeepercomposeemailfrontend.config

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig)
    extends ServicesConfig(config) with EmailConnectorConfig {
  val welshLanguageSupportEnabled = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  val appName      = "HMRC API Gatekeeper"
  val title        = "HMRC API Gatekeeper"
  val emailBaseUrl = baseUrl("gatekeeper-email")
  val emailSubject = getString("emailSubject")

  def gatekeeperSuccessUrl: String = getString("api-gatekeeper-email-success-url")

  def strideLoginUrl: String = s"${baseUrl("stride-auth-frontend")}/stride/sign-in"

  val authBaseUrl = baseUrl("auth")

  def adminRole: String = getString("roles.admin")

  def superUserRole: String = getString("roles.super-user")

  def userRole: String = getString("roles.user")

  lazy val registerCdsUrl: String               = config.get[String]("urls.cdsRegisterUrl")
  lazy val subscribeCdsUrl: String              = config.get[String]("urls.cdsSubscribeUrl")
  lazy val loginUrl: String                     = config.get[String]("urls.login")
  lazy val loginContinueUrl: String             = config.get[String]("urls.loginContinue")
  lazy val homepage: String                     = config.get[String]("urls.homepage")
  lazy val claimServiceUrl: String              = config.get[String]("urls.claimService")
  lazy val apiGatekeeperFrontendUrl: String     = config.get[String]("urls.apiGatekeeperFrontendUrl")
  lazy val apiGatekeeperApisFrontendUrl: String = config.get[String]("urls.apiGatekeeperApisFrontendUrl")
  lazy val signOutUrl: String                   = config.get[String]("urls.signOut")

  lazy val feedbackService                  = config.getOptional[String]("feedback.url").getOrElse("/feedback") +
    config.getOptional[String]("feedback.source").getOrElse("/CDS-FIN")
  lazy val contactFrontendServiceId: String = config.get[String]("contact-frontend.serviceId")

  lazy val helpMakeGovUkBetterUrl: String = config.get[String]("urls.helpMakeGovUkBetterUrl")

  lazy val selfUrl: String = servicesConfig.getString("self.url")

  lazy val timeout: Int      = config.get[Int]("timeout.timeout")
  lazy val countdown: Int    = config.get[Int]("timeout.countdown")
  lazy val itemsPerPage: Int = config.get[Int]("pagination.itemsPerPage")

  lazy val emailFrontendUrl: String = config.get[String]("urls.emailFrontend")

}

trait EmailConnectorConfig {
  val emailBaseUrl: String
  val emailSubject: String
}
