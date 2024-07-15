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

package uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers

import java.util.Base64
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import com.google.common.base.Charsets
import views.html.{ComposeEmail, ForbiddenView}

import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import uk.gov.hmrc.gatekeepercomposeemailfrontend.config.AppConfig
import uk.gov.hmrc.gatekeepercomposeemailfrontend.connectors.{AuthConnector, GatekeeperEmailConnector}
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.{GatekeeperRole, OutgoingEmail, TestEmailRequest}
import uk.gov.hmrc.gatekeepercomposeemailfrontend.services.ComposeEmailService
import uk.gov.hmrc.gatekeepercomposeemailfrontend.utils.GatekeeperAuthWrapper

@Singleton
class EmailPreviewController @Inject() (
    mcc: MessagesControllerComponents,
    composeEmail: ComposeEmail,
    emailService: ComposeEmailService,
    override val forbiddenView: ForbiddenView,
    override val authConnector: AuthConnector,
    emailConnector: GatekeeperEmailConnector
  )(implicit val appConfig: AppConfig,
    val ec: ExecutionContext
  ) extends FrontendController(mcc) with GatekeeperAuthWrapper with I18nSupport with Logging with WithUnsafeDefaultFormBinding {

  def sendEmail(emailUUID: String, userSelection: String): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      {
        val outgoingEmail = for {
          fetchEmail    <- emailService.fetchEmail(emailUUID)
          outgoingEmail <- emailConnector.sendEmail(EmailPreviewForm(emailUUID, ComposeEmailForm(fetchEmail.subject, fetchEmail.htmlEmailBody, true)))

          _ = logger.info(s"outgoingEmail count is ${outgoingEmail.emailsCount}")
        } yield outgoingEmail
        outgoingEmail.map(e =>
          Redirect(uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.routes.ComposeEmailController.sentEmailConfirmation(userSelection, e.emailsCount))
        )
      }
  }

  def sendTestEmail(emailUUID: String, userSelection: String): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      {
        emailConnector.sendTestEmail(emailUUID, TestEmailRequest(request.email.get)).map(_ =>
          Redirect(uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.routes.ComposeEmailController.emailPreview(emailUUID, userSelection, true))
        )
      }
  }

  def editEmail(emailUUID: String, userSelection: String): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      {
        val userSelectionMap: Map[String, String] = Json.parse(userSelection).as[Map[String, String]]
        val fetchEmail: Future[OutgoingEmail]     = emailService.fetchEmail(emailUUID)
        fetchEmail.map { email =>
          val txtEmailBody = base64Decode(email.markdownEmailBody).substring(1)
          Ok(composeEmail(
            emailUUID,
            uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.ComposeEmailForm.form.fill(ComposeEmailForm(email.subject, txtEmailBody, true)),
            userSelectionMap
          ))
        }
      }
  }

  private def base64Decode(result: String): String =
    new String(Base64.getDecoder.decode(result), Charsets.UTF_8)
}
