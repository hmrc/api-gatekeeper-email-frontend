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

import scala.concurrent.ExecutionContext.Implicits.global

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{verify => wireMockVerify, _}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import uk.gov.hmrc.gatekeepercomposeemailfrontend.common.AsyncHmrcSpec
import uk.gov.hmrc.gatekeepercomposeemailfrontend.config.EmailConnectorConfig
import uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.{ComposeEmailForm, EmailPreviewForm}
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.{DevelopersEmailQuery, RegisteredUser}

class GatekeeperEmailConnectorSpec extends AsyncHmrcSpec with BeforeAndAfterEach with BeforeAndAfterAll with GuiceOneAppPerSuite {

  val stubPort       = sys.env.getOrElse("WIREMOCK", "22222").toInt
  val stubHost       = "localhost"
  val wireMockUrl    = s"http://$stubHost:$stubPort"
  val wireMockServer = new WireMockServer(wireMockConfig().port(stubPort))

  override def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
    WireMock.configureFor(stubHost, stubPort)
  }

  override def afterEach(): Unit = {
    wireMockServer.resetMappings()
    super.afterEach()
  }

  override def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  val subject                = "Email subject"
  val emailUUID              = "email-uuid"
  val emailSendServicePath   = s"/gatekeeper-email/send-email/$emailUUID"
  val emailSaveServicePath   = s"/gatekeeper-email/save-email?emailUUID=$emailUUID"
  val emailUpdateServicePath = s"/gatekeeper-email/update-email?emailUUID=$emailUUID"
  val fetchEmailUrl          = s"/gatekeeper-email/fetch-email/$emailUUID"
  val emailBody              = "Body to be used in the email template"

  trait Setup {
    val httpClient = app.injector.instanceOf[HttpClientV2]

    val fakeEmailConnectorConfig = new EmailConnectorConfig {
      val emailBaseUrl                  = wireMockUrl
      override val emailSubject: String = subject
    }

    implicit val hc: HeaderCarrier = HeaderCarrier()

    lazy val underTest                     = new GatekeeperEmailConnector(httpClient, fakeEmailConnectorConfig)
    val composeEmailForm: ComposeEmailForm = ComposeEmailForm(subject, emailBody, true)
    val emailPreviewForm: EmailPreviewForm = EmailPreviewForm(emailUUID, composeEmailForm)
    val users                              = List(RegisteredUser("example@example.com", "first name", "last name", true), RegisteredUser("example2@example2.com", "first name2", "last name2", true))
    val userSelectionQuery                 = new DevelopersEmailQuery(Some("topic-dev"), None, None, false, Some("apiVersionFilter"), false, None)

  }

  trait WorkingHttp {
    self: Setup =>
    val selectionQuery = """{"topic":"topic-dev", "privateapimatch": false, "apiVersionFilter": "apiVersionFilter", "allUsers": false}""".stripMargin

    val outgoingEmail =
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

    stubFor(post(urlEqualTo(emailSendServicePath)).willReturn(aResponse()
      .withHeader("Content-type", "application/json")
      .withBody(outgoingEmail)
      .withStatus(OK)))
    stubFor(post(urlEqualTo(emailSaveServicePath)).willReturn(aResponse()
      .withHeader("Content-type", "application/json")
      .withBody(outgoingEmail)
      .withStatus(OK)))
    stubFor(post(urlEqualTo(emailUpdateServicePath)).willReturn(aResponse()
      .withHeader("Content-type", "application/json")
      .withBody(outgoingEmail)
      .withStatus(OK)))
    stubFor(get(urlEqualTo(fetchEmailUrl)).willReturn(aResponse()
      .withHeader("Content-type", "application/json")
      .withBody(outgoingEmail)
      .withStatus(OK)))
  }

  trait FailingHttp {
    self: Setup =>
    stubFor(post(urlEqualTo(emailSendServicePath)).willReturn(aResponse().withStatus(NOT_FOUND)))
    stubFor(post(urlEqualTo(emailSaveServicePath)).willReturn(aResponse().withStatus(NOT_FOUND)))
  }

  "emailConnector" should {

    "send gatekeeper email" in new Setup with WorkingHttp {
      await(underTest.sendEmail(emailPreviewForm))

      wireMockVerify(
        1,
        postRequestedFor(
          urlEqualTo(emailSendServicePath)
        )
      )
    }

    "fail to send gatekeeper email" in new Setup with FailingHttp {
      intercept[UpstreamErrorResponse] {
        await(underTest.sendEmail(emailPreviewForm))
      }
    }

    "save gatekeeper email" in new Setup with WorkingHttp {
      await(underTest.saveEmail(composeEmailForm, emailUUID, userSelectionQuery))

      wireMockVerify(
        1,
        postRequestedFor(
          urlEqualTo(emailSaveServicePath)
        )
      )
    }

    "fail to save gatekeeper email" in new Setup with FailingHttp {
      intercept[UpstreamErrorResponse] {
        await(underTest.saveEmail(composeEmailForm, emailUUID, userSelectionQuery))
      }
    }

    "update gatekeeper email" in new Setup with WorkingHttp {
      await(underTest.updateEmail(composeEmailForm, emailUUID, Some(userSelectionQuery)))

      wireMockVerify(
        1,
        postRequestedFor(
          urlEqualTo(emailUpdateServicePath)
        )
      )
    }

    "fail to update gatekeeper email" in new Setup with FailingHttp {
      intercept[UpstreamErrorResponse] {
        await(underTest.updateEmail(composeEmailForm, emailUUID, Some(userSelectionQuery)))
      }
    }

    "fetch email info" in new Setup with WorkingHttp {
      await(underTest.fetchEmail(emailUUID))

      wireMockVerify(
        1,
        getRequestedFor(
          urlEqualTo(fetchEmailUrl)
        )
      )
    }

    "fail to fetch  email info " in new Setup with FailingHttp {
      intercept[UpstreamErrorResponse] {
        await(underTest.fetchEmail(emailUUID))
      }
    }
  }
}
