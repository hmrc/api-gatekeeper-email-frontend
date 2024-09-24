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

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.http.Status.UNAUTHORIZED
import uk.gov.hmrc.http.test.HttpClientV2Support
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import uk.gov.hmrc.gatekeepercomposeemailfrontend.common.AsyncHmrcSpec
import uk.gov.hmrc.gatekeepercomposeemailfrontend.config.EmailConnectorConfig
import uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.{ComposeEmailForm, EmailPreviewForm}
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.{DevelopersEmailQuery, RegisteredUser, TestEmailRequest}
import uk.gov.hmrc.gatekeepercomposeemailfrontend.stubs.GatekeeperEmailStub

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
    WireMock.reset()
    super.afterEach()
  }

  override def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  val subject         = "Email subject"
  val emailUUIDString = UUID.randomUUID().toString()

  trait Setup extends HttpClientV2Support with GatekeeperEmailStub {

    val fakeEmailConnectorConfig = new EmailConnectorConfig {
      val emailBaseUrl                  = wireMockUrl
      override val emailSubject: String = subject
    }

    implicit val hc: HeaderCarrier = HeaderCarrier()

    lazy val underTest                     = new GatekeeperEmailConnector(httpClientV2, fakeEmailConnectorConfig)
    val composeEmailForm: ComposeEmailForm = ComposeEmailForm(subject, "emailBody La La LAS")
    val emailPreviewForm: EmailPreviewForm = EmailPreviewForm(emailUUIDString, composeEmailForm)
    val email                              = "example@example.com"
    val testEmailRequest                   = TestEmailRequest("example@example.com")
    val users                              = List(RegisteredUser(email, "first name", "last name", verified = true), RegisteredUser("example2@example2.com", "first name2", "last name2", true))
    val userSelectionQuery                 = new DevelopersEmailQuery(Some("topic-dev"), None, None, false, Some("apiVersionFilter"), false, None)

  }

  "emailConnector" when {

    "send email" should {
      "send gatekeeper email successfully" in new Setup {
        SendEmail.sucesss(emailUUIDString)

        await(underTest.sendEmail(emailPreviewForm))

        SendEmail.verify(emailUUIDString)

      }

      "fail to send gatekeeper email" in new Setup {
        SendEmail.failsWithStatus(emailUUIDString, UNAUTHORIZED)

        intercept[UpstreamErrorResponse] {
          await(underTest.sendEmail(emailPreviewForm))
        }

        SendEmail.verify(emailUUIDString)
      }
    }

    "send test email" should {
      "send gatekeeper test email successfully" in new Setup {
        SendTestMail.success(emailUUIDString)

        await(underTest.sendTestEmail(emailUUIDString, testEmailRequest))

        SendTestMail.verify(emailUUIDString)
      }

      "fail to send test gatekeeper email" in new Setup {
        SendTestMail.failsWithStatus(emailUUIDString, UNAUTHORIZED)

        intercept[UpstreamErrorResponse] {
          await(underTest.sendTestEmail(emailUUIDString, testEmailRequest))
        }

        SendTestMail.verify(emailUUIDString)
      }
    }

    "save email" should {
      "save gatekeeper email" in new Setup {

        SaveEmail.success(emailUUIDString)

        await(underTest.saveEmail(composeEmailForm, emailUUIDString, userSelectionQuery))

        SaveEmail.verify(emailUUIDString)
      }

      "fail to save gatekeeper email" in new Setup {
        SaveEmail.failsWithStatus(emailUUIDString, UNAUTHORIZED)
        intercept[UpstreamErrorResponse] {
          await(underTest.saveEmail(composeEmailForm, emailUUIDString, userSelectionQuery))
        }
        SaveEmail.verify(emailUUIDString)
      }
    }

    "update email" should {
      "update gatekeeper email" in new Setup {
        UpdateEmail.success(emailUUIDString)

        await(underTest.updateEmail(composeEmailForm, emailUUIDString, Some(userSelectionQuery)))

        UpdateEmail.verify(emailUUIDString)
      }

      "fail to update gatekeeper email" in new Setup {
        UpdateEmail.failsWithStatus(emailUUIDString, UNAUTHORIZED)

        intercept[UpstreamErrorResponse] {
          await(underTest.updateEmail(composeEmailForm, emailUUIDString, Some(userSelectionQuery)))
        }

        UpdateEmail.verify(emailUUIDString)
      }
    }

    "fetch email" should {

      "fetch email info" in new Setup {
        FetchEmail.success(emailUUIDString)

        await(underTest.fetchEmail(emailUUIDString))

        FetchEmail.verify(emailUUIDString)
      }

      "fail to fetch  email info " in new Setup {
        FetchEmail.failsWithStatus(emailUUIDString, UNAUTHORIZED)

        intercept[UpstreamErrorResponse] {
          await(underTest.fetchEmail(emailUUIDString))
        }

        FetchEmail.verify(emailUUIDString)
      }
    }
  }
}
