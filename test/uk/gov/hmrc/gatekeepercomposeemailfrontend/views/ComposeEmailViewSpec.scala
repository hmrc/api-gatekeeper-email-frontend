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

import scala.jdk.CollectionConverters._

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import views.html.ComposeEmail

import play.api.data.FormError
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.Html

import uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.ComposeEmailForm
import uk.gov.hmrc.gatekeepercomposeemailfrontend.views.helpers.CommonViewSpec

class ComposeEmailViewSpec extends CommonViewSpec {

  trait Setup {
    val composeEmail   = app.injector.instanceOf[ComposeEmail]
    val request        = FakeRequest().withCSRFToken
    val userSelection  = Map("To" -> "Everyone")
    val formWithErrors = ComposeEmailForm.form.withError(FormError("emailSubject", "email.subject.required")).withError(FormError("emailBody", "email.body.required"))
  }

  "ComposeEmail" should {

    "render the  page correctly" in new Setup {
      val page: Html =
        composeEmail.render("emailUUID", ComposeEmailForm.form.fill(ComposeEmailForm("Subject", "Email Body")), userSelection, request, messagesProvider.messages, appConfig)

      val document: Document = Jsoup.parse(page.body)

      document.title() shouldBe "- Compose Email"
      document.getElementById("page-heading").text() shouldBe "Compose email"
      document.getElementById("data-key-0").text() shouldBe "To"
      document.getElementById("data-value-0").text() shouldBe "Everyone"
      document.getElementById("emailSubject").text() shouldBe "Subject"
      document.getElementById("emailBody").text() shouldBe "Email Body"
    }

    "display errors correctly" in new Setup {

      val page: Html         =
        composeEmail.render("emailUUID", formWithErrors, userSelection, request, messagesProvider.messages, appConfig)
      val document: Document = Jsoup.parse(page.body)

      val errorLinks = document.getElementsByClass("govuk-error-summary__list").first().getElementsByTag("a").asScala.toList
      errorLinks.size shouldBe 2
      errorLinks.map(_.text()) should contain only ("Provide an email subject", "Provide an email body")
      document.getElementById("field-error-emailSubject").text() shouldBe "Error: Provide an email subject"
      document.getElementById("field-error-emailBody").text() shouldBe "Error: Provide an email body"
    }
  }

}
