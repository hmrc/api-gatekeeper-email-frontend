/*
 * Copyright 2023 HM Revenue & Customs
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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import uk.gov.hmrc.gatekeepercomposeemailfrontend.common.AsyncHmrcSpec
import uk.gov.hmrc.gatekeepercomposeemailfrontend.config.AppConfig
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.file_upload.UploadDocumentsWrapper
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.{OutgoingEmail, RegisteredUser}
import uk.gov.hmrc.gatekeepercomposeemailfrontend.services.ComposeEmailService

class UploadDocumentsConnectorSpec extends AsyncHmrcSpec with BeforeAndAfterEach with BeforeAndAfterAll with GuiceOneAppPerSuite {

  val subject        = "Email subject"
  val emailUUID      = "email-uuid"
  val emailBody      = "Body to be used in the email template"
  val selectionQuery = """{"topic":"topic-dev", "privateapimatch": false, "apiVersionFilter": "apiVersionFilter", "allUsers": false}""".stripMargin

  val outgoingEmail                                       =
    s"""
       |  {
       |    "emailUUID": "$emailUUID",
       |    "recipientTitle": "Team-Title",
       |    "recipients": [{"email": "", "firstName": "", "lastName": "", "verified": true}],
       |    "attachmentLink": "",
       |    "markdownEmailBody": "",
       |    "htmlEmailBody": "",
       |    "subject": "",
       |    "status": "",
       |    "composedBy": "auto-emailer",
       |    "approvedBy": "auto-emailer",
       |    "userSelectionQuery": $selectionQuery,
       |    "emailsCount": 1
       |  }
      """.stripMargin
  val mockComposeEmailConnector: GatekeeperEmailConnector = mock[GatekeeperEmailConnector]

  class ComposeEmailServiceStub extends ComposeEmailService(mockComposeEmailConnector) {

    override def fetchEmail(emailUUID: String)(implicit hc: HeaderCarrier): Future[OutgoingEmail] = {
      Future.successful(Json.parse(outgoingEmail).as[OutgoingEmail])
    }
  }
  val composEmailServieStub = new ComposeEmailServiceStub

  trait Setup {
    val httpClient = app.injector.instanceOf[HttpClient]

    implicit val hc        = HeaderCarrier()
    implicit val appConfig = mock[AppConfig]
    val CREATED            = 201
    val OK                 = 200

    class UploadDocumentsConnectorSuccess extends UploadDocumentsConnector(httpClient, composEmailServieStub) {

      override def actualPost(request: UploadDocumentsWrapper)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
        Future.successful(HttpResponse.apply(CREATED, "", Map[String, Seq[String]]("Location" -> Seq("/upload-documents"))))
      }
    }
    lazy val underTestSuccess = new UploadDocumentsConnectorSuccess

    class UploadDocumentsConnectorFailure extends UploadDocumentsConnector(httpClient, composEmailServieStub) {

      override def actualPost(request: UploadDocumentsWrapper)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
        Future.successful(HttpResponse.apply(OK, "", Map.empty[String, Seq[String]]))
      }
    }
    lazy val underTestFailure = new UploadDocumentsConnectorFailure

    val users = List(RegisteredUser("example@example.com", "first name", "last name", true), RegisteredUser("example2@example2.com", "first name2", "last name2", true))

  }

  "UploadDocumentsConnector" should {

    "send succesful initialise to UDF" in new Setup {
      val result = await(underTestSuccess.initializeNewFileUpload(emailUUID, true, true))
      result shouldBe Some("/upload-documents")
    }

    "send failed initialise to UDF" in new Setup {
      val result = await(underTestFailure.initializeNewFileUpload(emailUUID, true, true))
      result shouldBe None
    }
  }

}
