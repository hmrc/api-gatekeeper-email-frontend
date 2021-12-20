/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers

import connectors.GatekeeperEmailConnector
import org.scalatest.matchers.should.Matchers
import play.api.Play.materializer
import play.api.http.Status
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.filters.csrf.CSRF.TokenProvider
import views.html.{ComposeEmail, EmailSentConfirmation, ForbiddenView}

import scala.concurrent.ExecutionContext.Implicits.global

class ComposeEmailControllerSpec extends ControllerBaseSpec with Matchers {

  trait Setup extends ControllerSetupBase {
    lazy val mockGatekeeperEmailConnector = mock[GatekeeperEmailConnector]
    private lazy val forbiddenView = app.injector.instanceOf[ForbiddenView]
    private lazy val emailView = app.injector.instanceOf[ComposeEmail]
    private lazy val emailSentConfirmationView = app.injector.instanceOf[EmailSentConfirmation]
    val controller = new ComposeEmailController(stubMessagesControllerComponents(), emailView, mockGatekeeperEmailConnector,
      emailSentConfirmationView, forbiddenView, mockAuthConnector)
    val csrfToken: (String, String) = "csrfToken" -> app.injector.instanceOf[TokenProvider].generateToken

    val loggedInRequest = FakeRequest("GET", "/email").withSession(csrfToken, authToken, userToken).withCSRFToken
    val fakeConfirmationGetRequest = FakeRequest("GET", "/sent-email").withSession(csrfToken, authToken, userToken).withCSRFToken
    val fakePostFormRequest = FakeRequest("POST", "/email").withSession(csrfToken, authToken, userToken).withCSRFToken
  }

  "GET /email" should {
    "return 200" in new Setup {
      givenTheGKUserIsAuthorisedAndIsANormalUser()
      val result = controller.email()(loggedInRequest)
      verifyAuthConnectorCalledForUser
      status(result) shouldBe Status.OK
      verifyZeroInteractions(mockGatekeeperEmailConnector)
    }

    "return HTML" in new Setup {
      givenTheGKUserIsAuthorisedAndIsANormalUser()
      val result = controller.email()(loggedInRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      verifyZeroInteractions(mockGatekeeperEmailConnector)
    }
  }

  "GET /sent-email" should {
    "return 200" in new Setup {
      givenTheGKUserIsAuthorisedAndIsANormalUser()
      val result = controller.sentEmailConfirmation(fakeConfirmationGetRequest)
      verifyAuthConnectorCalledForUser
      status(result) shouldBe Status.OK
      verifyZeroInteractions(mockGatekeeperEmailConnector)
    }

    "return HTML" in new Setup {
      givenTheGKUserIsAuthorisedAndIsANormalUser()
      val result = controller.sentEmailConfirmation(fakeConfirmationGetRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      verifyZeroInteractions(mockGatekeeperEmailConnector)
    }
  }

  "POST /email" should {
    "send an email upon receiving a valid form submission" in new Setup {
      givenTheGKUserIsAuthorisedAndIsANormalUser()
      val fakeRequest = FakeRequest("POST", "/email")
        .withSession(csrfToken, authToken, userToken)
        .withFormUrlEncodedBody("emailRecipient" -> "fsadfas%40adfas.com", "emailSubject" -> "dfasd", "emailBody" -> "asdfasf")
        .withCSRFToken
      val result = controller.sendEmail()(fakeRequest)
      status(result) shouldBe SEE_OTHER
      verify(mockGatekeeperEmailConnector).sendEmail(*)(*)
      verifyAuthConnectorCalledForUser
    }

    "reject a form submission with missing emailRecipient" in new Setup {
      givenTheGKUserIsAuthorisedAndIsANormalUser()
      val fakeRequest = FakeRequest("POST", "/email")
        .withSession(csrfToken, authToken, userToken)
        .withFormUrlEncodedBody("emailSubject" -> "dfasd", "emailBody" -> "asdfasf")
        .withCSRFToken
      val result = controller.sendEmail()(fakeRequest)
      status(result) shouldBe BAD_REQUEST
      verifyAuthConnectorCalledForUser
      verifyZeroInteractions(mockGatekeeperEmailConnector)
    }

    "reject a form submission with missing emailSubject" in new Setup {
      givenTheGKUserIsAuthorisedAndIsANormalUser()
      val fakeRequest = FakeRequest("POST", "/email")
        .withSession(csrfToken, authToken, userToken)
        .withFormUrlEncodedBody("emailRecipient" -> "fsadfas%40adfas.com", "emailBody" -> "asdfasf")
        .withCSRFToken
      val result = controller.sendEmail()(fakeRequest)
      status(result) shouldBe BAD_REQUEST
      verifyAuthConnectorCalledForUser
      verifyZeroInteractions(mockGatekeeperEmailConnector)
    }

    "reject a form submission with missing emailBody" in new Setup {
      givenTheGKUserIsAuthorisedAndIsANormalUser()
      val fakeRequest = FakeRequest("POST", "/email")
        .withSession(csrfToken, authToken, userToken)
        .withFormUrlEncodedBody("emailRecipient" -> "fsadfas%40adfas.com", "emailSubject" -> "dfasd")
        .withCSRFToken
      val result = controller.sendEmail()(fakeRequest)
      status(result) shouldBe BAD_REQUEST
      verifyAuthConnectorCalledForUser
      verifyZeroInteractions(mockGatekeeperEmailConnector)
    }
  }
}
