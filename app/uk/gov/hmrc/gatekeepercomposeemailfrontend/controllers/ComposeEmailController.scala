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

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

import views.html._

import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import uk.gov.hmrc.gatekeepercomposeemailfrontend.config.AppConfig
import uk.gov.hmrc.gatekeepercomposeemailfrontend.connectors.AuthConnector
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models._
import uk.gov.hmrc.gatekeepercomposeemailfrontend.services.EmailService
import uk.gov.hmrc.gatekeepercomposeemailfrontend.utils.GatekeeperAuthWrapper

@Singleton
class ComposeEmailController @Inject() (
    mcc: MessagesControllerComponents,
    composeEmail: ComposeEmail,
    emailService: EmailService,
    sentEmail: EmailSentConfirmation,
    deleteConfirmEmail: DeleteEmailConfirmation,
    deleteEmail: DeleteEmail,
    override val forbiddenView: ForbiddenView,
    override val authConnector: AuthConnector
  )(implicit val appConfig: AppConfig,
    val ec: ExecutionContext
  ) extends FrontendController(mcc) with GatekeeperAuthWrapper with Logging {

  def initialiseEmail: Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) { implicit request =>
    def persistEmailDetails(userSelectionQuery: DevelopersEmailQuery, userSelection: String): Future[Result] = {
      val emailUUID = UUID.randomUUID().toString
      for {
        email <- emailService.saveEmail(ComposeEmailForm("", ""), emailUUID, userSelectionQuery)
      } yield Ok(composeEmail(
        email.emailUUID,
        uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.ComposeEmailForm.form.fill(ComposeEmailForm("", "")),
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
                    case NonFatal(e) =>
                      logger.error("Email recipients not valid JSON", e)
                      Future.successful(BadRequest(JsErrorResponse(ErrorCode.INVALID_REQUEST_PAYLOAD, s"Request payload does not appear to be JSON: ${e.getMessage}")))
                  }
              }
          }
        case None                   => Future.successful(BadRequest(JsErrorResponse(ErrorCode.INVALID_REQUEST_PAYLOAD, s"Request payload does not contain gatekeeper user selected options")))
      }
    } catch {
      case _: Throwable =>
        logger.error("Error")
        Future.successful(BadRequest(JsErrorResponse(ErrorCode.INVALID_REQUEST_PAYLOAD, "Request payload was not a URL encoded form")))
    }
  }

  def sentEmailConfirmation(userSelection: String, users: Int): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      {
        val userSelectionMap: Map[String, String] = Json.parse(userSelection).as[Map[String, String]]
        Future.successful(Ok(sentEmail(userSelectionMap, users)))
      }
  }

  def deleteOption(emailUUID: String, userSelection: String): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      Future.successful(Ok(deleteEmail(DeleteEmailOptionForm.form, emailUUID, userSelection)))
  }

  def delete(emailUUID: String, userSelection: String): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) {
    implicit request =>
      DeleteEmailOptionForm.form.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(
            BadRequest(deleteEmail(formWithErrors, emailUUID, userSelection))
          )
        },
        form => {
          if (form.value.equalsIgnoreCase("true")) {
            emailService.deleteEmail(emailUUID) map {
              _ => Ok(deleteConfirmEmail())
            }
          } else {
            Future.successful(Ok(composeEmail(
              emailUUID,
              uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.ComposeEmailForm.form.fill(ComposeEmailForm("", "")),
              Json.parse(userSelection).as[Map[String, String]]
            )))
          }
        }
      )
  }

}
