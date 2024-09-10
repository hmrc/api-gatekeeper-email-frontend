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

package uk.gov.hmrc.gatekeepercomposeemailfrontend.services

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.gatekeepercomposeemailfrontend.connectors.GatekeeperEmailConnector
import uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.ComposeEmailForm
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.{DevelopersEmailQuery, OutgoingEmail}

class ComposeEmailService @Inject() (emailConnector: GatekeeperEmailConnector)(implicit val ec: ExecutionContext) {

  def saveEmail(composeEmailForm: ComposeEmailForm, emailUUID: String, userSelectionQuery: DevelopersEmailQuery)(implicit hc: HeaderCarrier): Future[OutgoingEmail] = {
    emailConnector.saveEmail(composeEmailForm, emailUUID, userSelectionQuery)
  }

  def fetchEmail(emailUUID: String)(implicit hc: HeaderCarrier): Future[OutgoingEmail] = {
    emailConnector.fetchEmail(emailUUID)
  }

  def deleteEmail(emailUUID: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    emailConnector.deleteEmail(emailUUID)
  }

  def updateEmail(
      composeEmailForm: ComposeEmailForm,
      emailUUID: String,
      userSelectionQuery: Option[DevelopersEmailQuery]
    )(implicit hc: HeaderCarrier
    ): Future[OutgoingEmail] = {
    emailConnector.updateEmail(composeEmailForm, emailUUID, userSelectionQuery)
  }

}
