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

package uk.gov.hmrc.gatekeepercomposeemailfrontend.connectors

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions, UpstreamErrorResponse}

import uk.gov.hmrc.gatekeepercomposeemailfrontend.config.EmailConnectorConfig
import uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.{ComposeEmailForm, EmailPreviewForm}
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.EmailRequest.{createEmailRequest, updateEmailRequest}
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.file_upload.UploadedFile
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.{DevelopersEmailQuery, EmailRequest, OutgoingEmail}
import uk.gov.hmrc.gatekeepercomposeemailfrontend.utils.ApplicationLogger

@Singleton
class GatekeeperEmailConnector @Inject() (http: HttpClient, config: EmailConnectorConfig)(implicit ec: ExecutionContext)
    extends HttpErrorFunctions
    with ApplicationLogger {

  lazy val serviceUrl = config.emailBaseUrl

  def saveEmail(composeEmailForm: ComposeEmailForm, emailUUID: String, userSelectionQuery: DevelopersEmailQuery)(implicit hc: HeaderCarrier): Future[OutgoingEmail] = {
    postSaveEmail(createEmailRequest(composeEmailForm, userSelectionQuery), emailUUID)
  }

  def updateEmail(
      composeEmailForm: ComposeEmailForm,
      emailUUID: String,
      userSelectionQuery: Option[DevelopersEmailQuery],
      attachmentDetails: Option[Seq[UploadedFile]] = None
    )(implicit hc: HeaderCarrier
    ): Future[OutgoingEmail] = {
    postUpdateEmail(updateEmailRequest(composeEmailForm, userSelectionQuery, attachmentDetails), emailUUID)
  }

  def fetchEmail(emailUUID: String)(implicit hc: HeaderCarrier): Future[OutgoingEmail] = {
    val url = s"$serviceUrl/gatekeeper-email/fetch-email/$emailUUID"
    http.GET[OutgoingEmail](url)
  }

  def deleteEmail(emailUUID: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = s"$serviceUrl/gatekeeper-email/delete-email/$emailUUID"
    http.POSTEmpty[Boolean](url)
  }

  def sendEmail(emailPreviewForm: EmailPreviewForm)(implicit hc: HeaderCarrier): Future[OutgoingEmail] = {
    http.POSTEmpty[OutgoingEmail](s"$serviceUrl/gatekeeper-email/send-email/${emailPreviewForm.emailUUID}")
  }

  private def postSaveEmail(request: EmailRequest, emailUUID: String)(implicit hc: HeaderCarrier) = {
    http.POST[EmailRequest, Either[UpstreamErrorResponse, OutgoingEmail]](s"$serviceUrl/gatekeeper-email/save-email?emailUUID=$emailUUID", request)
      .map {
        case resp @ Right(_) => resp.value
        case Left(err)       => throw err
      }
  }

  private def postUpdateEmail(request: EmailRequest, emailUUID: String)(implicit hc: HeaderCarrier) = {
    http.POST[EmailRequest, Either[UpstreamErrorResponse, OutgoingEmail]](s"$serviceUrl/gatekeeper-email/update-email?emailUUID=$emailUUID", request)
      .map {
        case resp @ Right(_) => resp.value
        case Left(err)       => throw err
      }
  }

}
