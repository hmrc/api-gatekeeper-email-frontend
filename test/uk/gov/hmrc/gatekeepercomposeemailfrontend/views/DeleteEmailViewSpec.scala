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
import org.jsoup.nodes.Document
import views.html.DeleteEmail

import play.api.data.FormError
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.Html

import uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.DeleteEmailOptionForm
import uk.gov.hmrc.gatekeepercomposeemailfrontend.views.helpers.CommonViewSpec

class DeleteEmailViewSpec extends CommonViewSpec {

  trait Setup {
    val deleteEmail    = app.injector.instanceOf[DeleteEmail]
    val request        = FakeRequest().withCSRFToken
    val userSelection  = "To,Everyone"
    val emailUUid      = UUID.randomUUID().toString
    val formWithErrors = DeleteEmailOptionForm.form.withError(FormError("value", "error.required"))

    def validateStandardPageElements(document: Document) = {
      document.title() shouldBe "Are you sure you want to delete the email?"
      document.getElementById("page-heading").text() shouldBe "Are you sure you want to delete the email?"
      document.getElementById("are-you-sure-hint").text() shouldBe "Deleted emails cannot be recovered."
      document.getElementById("value-no").`val`() shouldBe "false"
      document.getElementById("value").`val`() shouldBe "true"
      document.getElementById("delete-action-button").text() shouldBe "Continue"
    }

    def validateErrorAndStandardElements(document: Document) = {
      val errorLinks = document.getElementsByClass("govuk-error-summary__list").first().getElementsByTag("a").asScala.toList
      errorLinks.size shouldBe 1
      errorLinks.head.text() should be("Select yes if you want to remove this file")
      document.getElementById("value-error").text() shouldBe "Error: Select yes if you want to remove this file"
      validateStandardPageElements(document)
    }
  }

  "DeleteEmail" should {

    "render the  page correctly" in new Setup {
      val page: Html = deleteEmail.render(DeleteEmailOptionForm.form.fill(DeleteEmailOptionForm("true")), emailUUid, userSelection, request, messages)
      validateStandardPageElements(Jsoup.parse(page.body))
    }

    "display errors correctly" in new Setup {
      val page: Html =
        deleteEmail.render(formWithErrors, emailUUid, userSelection, request, messages)
      validateErrorAndStandardElements(Jsoup.parse(page.body))
    }
  }

}
