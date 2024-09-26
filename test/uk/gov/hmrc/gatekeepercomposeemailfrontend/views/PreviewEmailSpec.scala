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

package uk.gov.hmrc.gatekeepercomposeemailfrontend.views

import java.util.UUID
import scala.jdk.CollectionConverters._

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import views.html.PreviewEmail

import play.api.data.{Form, FormError}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments}

import uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.{ComposeEmailForm, PreviewEmailForm}
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.LoggedInRequest
import uk.gov.hmrc.gatekeepercomposeemailfrontend.views.helpers.CommonViewSpec

class PreviewEmailSpec extends CommonViewSpec {

  trait Setup {
    val previewEmail: PreviewEmail               = app.injector.instanceOf[PreviewEmail]
    val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken

    val adminEnrolment: Enrolment        = Enrolment(key = "admin-role", Seq.empty, state = "Activated")
    val superUserEnrolment: Enrolment    = Enrolment(key = "super-user-role", Seq.empty, state = "Activated")
    val userEnrolment: Enrolment         = Enrolment(key = "user-role", Seq.empty, state = "Activated")
    val authorisedEnrolments: Enrolments = Enrolments(Set(adminEnrolment, superUserEnrolment, userEnrolment))
    val emailAddr: String                = "testemail@test.com"

    val loggedInRequestWithEmail: LoggedInRequest[AnyContentAsEmpty.type]    = LoggedInRequest(None, Some(emailAddr), authorisedEnrolments, request)
    val loggedInRequestWithOutEmail: LoggedInRequest[AnyContentAsEmpty.type] = LoggedInRequest(None, None, authorisedEnrolments, request)

    val userSelection: Map[String, String] = Map("To" -> "Everyone")

    val formWithErrors: Form[PreviewEmailForm] =
      PreviewEmailForm.form.withError(FormError("emailSubject", "email.subject.required")).withError(FormError("emailBody", "email.body.required")).withError(
        "emailUUID",
        "email.uid.required"
      )
    val emailUUidStr: String                   = UUID.randomUUID().toString

    def validateStardardPageElements(document: Document) = {
      document.title() shouldBe "HMRC API Gatekeeper - Email preview"
      document.getElementById("page-heading").text() shouldBe "Email preview"
    }

    def validateSendTestElement(document: Document) = {
      document.getElementById("send-test-email-button") shouldBe "something"
    }

    def validateNotificationBanner(document: Document) = {
      document.getElementById("email-sent-banner").text() shouldBe "Test email sent"
      document.getElementById("email-sent-text").text() shouldBe s"We have sent a test email to $emailAddr"
    }

  }

  "PreviewEmail" should {

    "render the  page correctly when preview sent is false" in new Setup {
      val previewSent = false
      val page: Html  = previewEmail.apply(
        emailUUidStr,
        PreviewEmailForm.form.fill(PreviewEmailForm(emailUUidStr, ComposeEmailForm("Subject", "Email Body"))),
        userSelection,
        "status",
        previewSent
      )(loggedInRequestWithOutEmail, messages)

      val document = Jsoup.parse(page.body)
      validateStardardPageElements(document)
      Option(document.getElementById("send-test-email-button")) shouldBe None

    }

    "render the  page correctly when preview sent is true" in new Setup {
      val previewSent = true
      val page: Html  = previewEmail.apply(
        emailUUidStr,
        PreviewEmailForm.form.fill(PreviewEmailForm(emailUUidStr, ComposeEmailForm("Subject", "Email Body"))),
        userSelection,
        "status",
        previewSent
      )(
        loggedInRequestWithEmail,
        messages
      )

      val document = Jsoup.parse(page.body)
      validateStardardPageElements(document)
      validateNotificationBanner(document)

      document.getElementById("send-test-email-button").text() shouldBe "Send test email"

    }

    "display errors correctly" in new Setup {

      val page: Html         = previewEmail.render("emailUUID", formWithErrors, userSelection, "status", false, loggedInRequestWithEmail, messages)
      val document: Document = Jsoup.parse(page.body)

      val errorLinks: List[Element] = document.getElementsByClass("govuk-error-summary__list").first().getElementsByTag("a").asScala.toList
      errorLinks.size shouldBe 3
      errorLinks.map(_.text()).sorted shouldBe List("Email UUID is required", "Provide an email body", "Provide an email subject")

    }
  }

}
