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
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

import org.apache.pekko.stream.Materializer
import org.scalatest.matchers.should.Matchers
import views.html.{ComposeEmail, ForbiddenView, PreviewEmail}

import play.api.Application
import play.api.http.Status
import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, OK, SEE_OTHER}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.Helpers.status
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import play.filters.csrf.CSRF.TokenProvider
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.gatekeepercomposeemailfrontend.connectors.GatekeeperEmailConnector
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.OutgoingEmail
import uk.gov.hmrc.gatekeepercomposeemailfrontend.services.EmailService
import uk.gov.hmrc.gatekeepercomposeemailfrontend.utils.ComposeEmailControllerSpecHelpers.mcc
import uk.gov.hmrc.gatekeepercomposeemailfrontend.utils.WithCSRFAddToken

class PreviewEmailControllerSpec extends ControllerBaseSpec with Matchers with FutureAwaits with DefaultAwaitTimeout with WithCSRFAddToken {
  implicit val materializer: Materializer = app.materializer

  trait Setup extends ControllerSetupBase {
    val emailUUID                                                      = UUID.randomUUID().toString
    lazy val forbiddenView                                             = app.injector.instanceOf[ForbiddenView]
    lazy val previewEmail                                              = app.injector.instanceOf[PreviewEmail]
    val csrfToken: (String, String)                                    = "csrfToken" -> app.injector.instanceOf[TokenProvider].generateToken
    private val mockGatekeeperEmailConnector: GatekeeperEmailConnector = mock[GatekeeperEmailConnector]
    val mockEmailService: EmailService                                 = mock[EmailService]
    private lazy val composeEmailTemplateView                          = app.injector.instanceOf[ComposeEmail]
    val loggedInRequest                                                = FakeRequest("GET", "/email").withSession(csrfToken, authToken, userToken)

    val request = FakeRequest().withCSRFToken

    val controller                 = new PreviewEmailController(
      mcc,
      composeEmailTemplateView,
      mockEmailService,
      previewEmail,
      forbiddenView,
      mockAuthConnector,
      mockGatekeeperEmailConnector
    )
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val selectionQuery             = """{"topic":"topic-dev", "privateapimatch": false, "apiVersionFilter": "apiVersionFilter", "allUsers": false}""".stripMargin

    val outgoingEmail =
      s"""
         |  {
         |    "emailUUID": "emailId",
         |    "recipientTitle": "Team-Title",
         |    "recipients": [{"email": "", "firstName": "", "lastName": "", "verified": true}],
         |    "attachmentLink": "",
         |    "markdownEmailBody": "SGVsbG8=",
         |    "htmlEmailBody": "SGVsbG8=",
         |    "subject": "emailSubject",
         |    "status": "IN_PROGRESS",
         |    "composedBy": "auto-emailer",
         |    "approvedBy": "auto-emailer",
         |    "userSelectionQuery": $selectionQuery,
         |    "emailsCount": 1
         |  }
      """.stripMargin

    when(mockGatekeeperEmailConnector.sendEmail(*)(*))
      .thenReturn(successful(Json.parse(outgoingEmail).as[OutgoingEmail]))

    when(mockGatekeeperEmailConnector.sendTestEmail(*, *)(*))
      .thenReturn(successful(Json.parse(outgoingEmail).as[OutgoingEmail]))

    when(mockGatekeeperEmailConnector.fetchEmail(*)(*))
      .thenReturn(successful(Json.parse(outgoingEmail).as[OutgoingEmail]))

    when(mockEmailService.fetchEmail(*)(*))
      .thenReturn(successful(Json.parse(outgoingEmail).as[OutgoingEmail]))

    when(mockGatekeeperEmailConnector.updateEmail(*, *, *)(*))
      .thenReturn(successful(Json.parse(outgoingEmail).as[OutgoingEmail]))

    def fakeApplication(): Application =
      new GuiceApplicationBuilder()
        .configure(
          "metrics.jvm"     -> false,
          "metrics.enabled" -> false
        )
        .build()
  }

  "GET /preview-email/emailUUID" should {

    "return 200" in new Setup {
      givenTheGKUserIsAuthorisedAndIsANormalUser()
      val result = await(addToken(controller.previewEmail(emailUUID, "{}"))(loggedInRequest))
      verifyAuthConnectorCalledForUser
      result.header.status shouldBe OK
      result.body.contentType shouldBe Some("text/html; charset=utf-8")

    }

    "be forbidden if unauthorised" in new Setup {
      givenTheGKUserHasInsufficientEnrolments()
      val result = await(addToken(controller.previewEmail(emailUUID, "{}"))(loggedInRequest))
      verifyAuthConnectorCalledForUser
      result.header.status shouldBe FORBIDDEN
    }
  }

  "POST /preview-email" should {

    "reject a form submission with missing emailSubject" in new Setup {
      givenTheGKUserIsAuthorisedAndIsANormalUser()

      val result = controller.previewEmailAction(emailUUID, "{}")(FakeRequest().withFormUrlEncodedBody("emailBody" -> "SomeBody").withCSRFToken)
      status(result) shouldBe BAD_REQUEST
      verifyAuthConnectorCalledForUser
      verifyZeroInteractions(mockEmailService)
    }

    "reject a form submission with missing emailBody" in new Setup {
      givenTheGKUserIsAuthorisedAndIsANormalUser()

      val result = controller.previewEmailAction(emailUUID, "{}")(FakeRequest().withFormUrlEncodedBody("emailSubject" -> "SomeSubject").withCSRFToken)
      status(result) shouldBe BAD_REQUEST
      verifyAuthConnectorCalledForUser
      verifyZeroInteractions(mockEmailService)
    }

    "be forbidden if unauthorised" in new Setup {
      givenTheGKUserHasInsufficientEnrolments()
      val result = controller.previewEmailAction(emailUUID, "{}")(FakeRequest().withFormUrlEncodedBody("emailSubject" -> "SomeSubject").withCSRFToken)
      status(result) shouldBe FORBIDDEN
      verifyAuthConnectorCalledForUser
      verifyZeroInteractions(mockEmailService)
    }

  }

  "POST /send-email" should {

    "send an email upon receiving a valid form submission" in new Setup {
      givenTheGKUserIsAuthorisedAndIsANormalUser()
      val fakeRequest = FakeRequest("POST", s"/send-email/$emailUUID")
        .withSession(csrfToken, authToken, userToken).withCSRFToken
      val result      = controller.sendEmail(emailUUID, "{}")(fakeRequest)
      status(result) shouldBe SEE_OTHER
    }

    "redirect to login page for a user that is not authenticated" in new Setup {
      givenFailedLogin()
      val fakeRequest = FakeRequest("POST", s"/send-email/$emailUUID")
        .withSession(csrfToken, authToken, userToken).withCSRFToken
      val result      = controller.sendEmail(emailUUID, "{}")(fakeRequest)
      status(result) shouldBe SEE_OTHER
    }
    "deny user with incorrect privileges" in new Setup {
      givenTheGKUserHasInsufficientEnrolments()
      val fakeRequest = FakeRequest("POST", s"/send-email/$emailUUID")
        .withSession(csrfToken, authToken, userToken).withCSRFToken
      val result      = controller.sendEmail(emailUUID, "{}")(fakeRequest)
      status(result) shouldBe Status.FORBIDDEN
    }
  }

  "POST /send-test-email" should {

    "send an email upon receiving a valid form submission" in new Setup {
      givenTheGKUserIsAuthorisedAndIsANormalUser()
      val fakeRequest = FakeRequest("POST", s"/send-test-email/$emailUUID")
        .withSession(csrfToken, authToken, userToken).withCSRFToken
      val result      = controller.sendTestEmail(emailUUID, "{}")(fakeRequest)
      status(result) shouldBe SEE_OTHER
    }

    "redirect to login page for a user that is not authenticated" in new Setup {
      givenFailedLogin()
      val fakeRequest = FakeRequest("POST", s"/send-test-email/$emailUUID")
        .withSession(csrfToken, authToken, userToken).withCSRFToken
      val result      = controller.sendTestEmail(emailUUID, "{}")(fakeRequest)
      status(result) shouldBe SEE_OTHER
    }
    "deny user with incorrect privileges" in new Setup {
      givenTheGKUserHasInsufficientEnrolments()
      val fakeRequest = FakeRequest("POST", s"/send-test-email/$emailUUID")
        .withSession(csrfToken, authToken, userToken).withCSRFToken
      val result      = controller.sendTestEmail(emailUUID, "{}")(fakeRequest)
      status(result) shouldBe Status.FORBIDDEN
    }
  }

  "POST /edit-email" should {

    "edit email submits valid form to compose email" in new Setup {
      givenTheGKUserIsAuthorisedAndIsANormalUser()
      val fakeRequest = FakeRequest("POST", "/edit-email")
        .withFormUrlEncodedBody("emailUUID" -> "emailId", "composeEmailForm.emailSubject" -> "emailSubject", "composeEmailForm.emailBody" -> "emailBody")
        .withSession(csrfToken, authToken, userToken).withCSRFToken

      val result = controller.editEmail(emailUUID, "{}")(fakeRequest)
      status(result) shouldBe OK
    }
    "redirect to login page for a user that is not authenticated" in new Setup {
      givenFailedLogin()
      val fakeRequest = FakeRequest("POST", "/edit-email")
        .withFormUrlEncodedBody("emailUUID" -> "emailId", "composeEmailForm.emailSubject" -> "emailSubject", "composeEmailForm.emailBody" -> "emailBody")
        .withSession(csrfToken, authToken, userToken).withCSRFToken

      val result = controller.editEmail(emailUUID, "{}")(fakeRequest)
      status(result) shouldBe SEE_OTHER
    }
    "deny user with incorrect privileges" in new Setup {
      givenTheGKUserHasInsufficientEnrolments()
      val fakeRequest = FakeRequest("POST", "/edit-email")
        .withFormUrlEncodedBody("emailUUID" -> "emailId", "composeEmailForm.emailSubject" -> "emailSubject", "composeEmailForm.emailBody" -> "emailBody")
        .withSession(csrfToken, authToken, userToken).withCSRFToken

      val result = controller.editEmail(emailUUID, "{}")(fakeRequest)
      status(result) shouldBe Status.FORBIDDEN
    }
  }
}
