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
import views.html.{ComposeEmail, ForbiddenView, PreviewEmail}

import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.apiplatform.modules.common.domain.models.Actors
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import uk.gov.hmrc.gatekeepercomposeemailfrontend.config.AppConfig
import uk.gov.hmrc.gatekeepercomposeemailfrontend.connectors.{AuthConnector, GatekeeperEmailConnector}
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.{GatekeeperRole, OutgoingEmail, TestEmailRequest}
import uk.gov.hmrc.gatekeepercomposeemailfrontend.services.EmailService
import uk.gov.hmrc.gatekeepercomposeemailfrontend.utils.GatekeeperAuthWrapper

@Singleton
class PreviewEmailController @Inject() (
    mcc: MessagesControllerComponents,
    composeEmail: ComposeEmail,
    emailService: EmailService,
    previewEmail: PreviewEmail,
    override val forbiddenView: ForbiddenView,
    override val authConnector: AuthConnector,
    emailConnector: GatekeeperEmailConnector
  )(implicit val appConfig: AppConfig,
    val ec: ExecutionContext
  ) extends FrontendController(mcc) with GatekeeperAuthWrapper with I18nSupport with Logging {

  def previewEmail(emailUUID: String, userSelection: String, previewSent: Boolean = false): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      val userSelectionMap: Map[String, String]                       = Json.parse(userSelection).as[Map[String, String]]
      def getFilledForm(email: OutgoingEmail): Form[PreviewEmailForm] = uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.PreviewEmailForm.form.fill(PreviewEmailForm(
        email.emailUUID,
        ComposeEmailForm(email.subject, email.markdownEmailBody)
      ))
      emailService.fetchEmail(emailUUID).map { email =>
        Ok(previewEmail(
          base64Decode(email.htmlEmailBody),
          getFilledForm(email),
          userSelectionMap,
          email.status,
          previewSent
        ))
      }
  }

  def previewEmailAction(emailUUID: String, userSelection: String): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      def handleValidForm(form: ComposeEmailForm) = {

        val userSelectionMap: Map[String, String] = Json.parse(userSelection).as[Map[String, String]]
        emailService.fetchEmail(emailUUID).flatMap { emailFetched =>
          val outgoingEmail = emailService.updateEmail(form, emailUUID, Some(emailFetched.userSelectionQuery), Actors.GatekeeperUser(request.name.get))
          outgoingEmail.map { email =>
            logger.info(s"Fetched email status:${emailFetched.status}")
            Ok(previewEmail(
              base64Decode(email.htmlEmailBody),
              uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.PreviewEmailForm.form.fill(uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.PreviewEmailForm(
                email.emailUUID,
                form
              )),
              userSelectionMap,
              emailFetched.status,
              previewSent = false
            ))
          }
        }
      }

      def handleInvalidForm(formWithErrors: Form[ComposeEmailForm]): Future[Result] = {
        logger.warn(s"Error in form: ${formWithErrors.errors}")
        Future.successful(BadRequest(composeEmail(emailUUID, formWithErrors, Map[String, String]().empty)))
      }

      ComposeEmailForm.form.bindFromRequest().fold(handleInvalidForm, handleValidForm)
  }

  def sendEmail(emailUUID: String, userSelection: String): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      for {
        fetchEmail    <- emailService.fetchEmail(emailUUID)
        outgoingEmail <- emailConnector.sendEmail(PreviewEmailForm(emailUUID, ComposeEmailForm(fetchEmail.subject, fetchEmail.htmlEmailBody)))
        emailsCount    = outgoingEmail.emailsCount
        _              = logger.info(s"outgoingEmail count is ${emailsCount}")
      } yield Redirect(uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.routes.ComposeEmailController.sentEmailConfirmation(userSelection, emailsCount))
  }

  def sendTestEmail(emailUUID: String, userSelection: String): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      {
        emailConnector.sendTestEmail(emailUUID, TestEmailRequest(request.email.get)).map(_ =>
          Redirect(uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.routes.PreviewEmailController.previewEmail(emailUUID, userSelection, true))
        )
      }
  }

  def editEmail(emailUUID: String, userSelection: String): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      {
        val userSelectionMap: Map[String, String] = Json.parse(userSelection).as[Map[String, String]]
        emailService.fetchEmail(emailUUID).map { email =>
          val txtEmailBody = base64Decode(email.markdownEmailBody).substring(1)
          Ok(composeEmail(
            emailUUID,
            uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.ComposeEmailForm.form.fill(ComposeEmailForm(email.subject, txtEmailBody)),
            userSelectionMap
          ))
        }
      }
  }

  private def base64Decode(result: String): String =
    new String(Base64.getDecoder.decode(result), Charsets.UTF_8)
}
