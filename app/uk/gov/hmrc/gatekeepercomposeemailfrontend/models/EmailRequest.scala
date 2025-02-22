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

package uk.gov.hmrc.gatekeepercomposeemailfrontend.models

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.apiplatform.modules.common.domain.models.Actors

import uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.ComposeEmailForm

case class EmailData(emailSubject: String, emailBody: String)

case class EmailRequest(
    to: List[RegisteredUser],
    templateId: String,
    emailData: EmailData,
    force: Boolean = false,
    auditData: Map[String, String] = Map.empty,
    eventUrl: Option[String] = None,
    userSelectionQuery: Option[DevelopersEmailQuery] = None,
    composedBy: Actors.GatekeeperUser
  )

object EmailRequest {
  implicit val emailDataFmt: OFormat[EmailData]           = Json.format[EmailData]
  implicit val userFmt: OFormat[RegisteredUser]           = Json.format[RegisteredUser]
  implicit val sendEmailRequestFmt: OFormat[EmailRequest] = Json.format[EmailRequest]

  def createEmailRequest(form: ComposeEmailForm, developersEmailQuery: DevelopersEmailQuery, composedBy: Actors.GatekeeperUser) = {

    EmailRequest(
      to = List(),
      templateId = "gatekeeper",
      EmailData(form.emailSubject, form.emailBody),
      userSelectionQuery = Some(developersEmailQuery),
      composedBy = composedBy
    )
  }

  def updateEmailRequest(composeEmailForm: ComposeEmailForm, developersEmailQuery: Option[DevelopersEmailQuery], composedBy: Actors.GatekeeperUser) = {

    EmailRequest(
      List(),
      templateId = "gatekeeper",
      EmailData(composeEmailForm.emailSubject, composeEmailForm.emailBody),
      userSelectionQuery = developersEmailQuery,
      composedBy = composedBy
    )
  }

}
