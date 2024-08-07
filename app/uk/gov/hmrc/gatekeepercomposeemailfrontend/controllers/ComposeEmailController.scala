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

import java.util.{Base64, UUID}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

import com.google.common.base.Charsets
import views.html._

import play.api.Logging
import play.api.data.Form
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import uk.gov.hmrc.gatekeepercomposeemailfrontend.config.AppConfig
import uk.gov.hmrc.gatekeepercomposeemailfrontend.connectors.AuthConnector
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models._
import uk.gov.hmrc.gatekeepercomposeemailfrontend.services.ComposeEmailService
import uk.gov.hmrc.gatekeepercomposeemailfrontend.utils.GatekeeperAuthWrapper

@Singleton
class ComposeEmailController @Inject() (
    mcc: MessagesControllerComponents,
    composeEmail: ComposeEmail,
    emailPreview: EmailPreview,
    emailService: ComposeEmailService,
    sentEmail: EmailSentConfirmation,
    deleteConfirmEmail: EmailDeleteConfirmation,
    deleteEmail: RemoveEmailView,
    override val forbiddenView: ForbiddenView,
    formProvider: RemoveUploadedFileFormProvider,
    override val authConnector: AuthConnector
  )(implicit val appConfig: AppConfig,
    val ec: ExecutionContext
  ) extends FrontendController(mcc) with GatekeeperAuthWrapper with Logging with WithUnsafeDefaultFormBinding {

  def initialiseEmail: Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) { implicit request =>
    def persistEmailDetails(userSelectionQuery: DevelopersEmailQuery, userSelection: String): Future[Result] = {
      val emailUUID = UUID.randomUUID().toString
      for {
        email <- emailService.saveEmail(ComposeEmailForm("", "", false), emailUUID, userSelectionQuery)
      } yield Ok(composeEmail(
        email.emailUUID,
        uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.ComposeEmailForm.form.fill(ComposeEmailForm("", "", false)),
        Json.parse(userSelection).as[Map[String, String]]
      ))
    }

    try {
      val body: Option[Map[String, Seq[String]]] = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].asFormUrlEncoded
      body.map(elems => elems.get("user-selection")).head match {
        case Some(userSelectedData) => Json.parse(userSelectedData.head).validate[Map[String, String]] match {
            case JsSuccess(userSelection: Map[String, String], _) =>
              body.map(elems => elems.get("user-selection-query")).head match {
                case Some(userSelectionQuery) =>
                  try {
                    Json.parse(userSelectionQuery.head).validate[DevelopersEmailQuery] match {
                      case JsSuccess(value: DevelopersEmailQuery, _) =>
                        persistEmailDetails(value, Json.toJson(userSelection).toString())
                      case JsError(errors)                           => Future.successful(BadRequest(JsErrorResponse(
                          ErrorCode.INVALID_REQUEST_PAYLOAD,
                          s"""Request payload does not contain gatekeeper user selected query data: ${errors.mkString(", ")}"""
                        )))
                    }
                  } catch {
                    case NonFatal(e) => {
                      logger.error("Email recipients not valid JSON", e)
                      Future.successful(BadRequest(JsErrorResponse(ErrorCode.INVALID_REQUEST_PAYLOAD, s"Request payload does not appear to be JSON: ${e.getMessage}")))
                    }
                  }
              }
          }
        case None                   => Future.successful(BadRequest(JsErrorResponse(ErrorCode.INVALID_REQUEST_PAYLOAD, s"Request payload does not contain gatekeeper user selected options")))
      }
    } catch {
      case _: Throwable => {
        logger.error("Error")
        Future.successful(BadRequest(JsErrorResponse(ErrorCode.INVALID_REQUEST_PAYLOAD, "Request payload was not a URL encoded form")))
      }
    }
  }

  def sentEmailConfirmation(userSelection: String, users: Int): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      {
        val userSelectionMap: Map[String, String] = Json.parse(userSelection).as[Map[String, String]]
        Future.successful(Ok(sentEmail(userSelectionMap, users)))
      }
  }

  def emailPreview(emailUUID: String, userSelection: String, previewSent: Boolean = false): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      val userSelectionMap: Map[String, String] = Json.parse(userSelection).as[Map[String, String]]
      val fetchEmail: Future[OutgoingEmail]     = emailService.fetchEmail(emailUUID)
      fetchEmail.map { email =>
        Ok(emailPreview(
          base64Decode(email.htmlEmailBody),
          uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.EmailPreviewForm.form.fill(EmailPreviewForm(
            email.emailUUID,
            ComposeEmailForm(email.subject, email.markdownEmailBody, true)
          )),
          userSelectionMap,
          email.status,
          previewSent
        ))
      }
  }

  def upload(emailUUID: String, userSelection: String): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      def handleValidForm(form: ComposeEmailForm) = {
        val fetchEmail: Future[OutgoingEmail]     = emailService.fetchEmail(emailUUID)
        val userSelectionMap: Map[String, String] = Json.parse(userSelection).as[Map[String, String]]
        fetchEmail.flatMap { emailFetched =>
          val outgoingEmail = emailService.updateEmail(form, emailUUID, Some(emailFetched.userSelectionQuery), emailFetched.attachmentDetails)
          outgoingEmail.map { email =>
            if (form.attachFiles) {
              Redirect(uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.routes.FileUploadController.start(emailUUID, false, true))
            } else {
              logger.info(s"Fetched email status:${emailFetched.status}")
              Ok(emailPreview(
                base64Decode(email.htmlEmailBody),
                uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.EmailPreviewForm.form.fill(uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.EmailPreviewForm(
                  email.emailUUID,
                  form
                )),
                userSelectionMap,
                emailFetched.status,
                false
              ))
            }
          }
        }
      }

      def handleInvalidForm(formWithErrors: Form[ComposeEmailForm]) = {
        logger.warn(s"Error in form: ${formWithErrors.errors}")
        Future.successful(BadRequest(composeEmail(emailUUID, formWithErrors, Map[String, String]().empty)))
      }

      ComposeEmailForm.form.bindFromRequest().fold(handleInvalidForm(_), handleValidForm(_))
  }

  def deleteOption(emailUUID: String, userSelection: String): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      Future.successful(Ok(deleteEmail(formProvider(), submitLink(emailUUID, userSelection))))
  }

  def delete(emailUUID: String, userSelection: String): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      formProvider().bindFromRequest().fold(
        formWithErrors => {
          Future.successful(
            BadRequest(deleteEmail(formWithErrors, submitLink(emailUUID, userSelection)))
          )
        },
        value => {
          if (value) {
            emailService.deleteEmail(emailUUID) map {
              result => Ok(deleteConfirmEmail())
            }
          } else {
            Future.successful(Ok(composeEmail(
              emailUUID,
              uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.ComposeEmailForm.form.fill(ComposeEmailForm("", "", false)),
              Json.parse(userSelection).as[Map[String, String]]
            )))
          }
        }
      )
  }

  private def base64Decode(result: String): String =
    new String(Base64.getDecoder.decode(result), Charsets.UTF_8)

  private def submitLink(emailUUID: String, userSelection: String) =
    uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.routes.ComposeEmailController.delete(emailUUID, userSelection)
}
