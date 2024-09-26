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
import views.html.{DeleteEmail, DeleteEmailConfirmation}

import play.api.data.FormError
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.Html

import uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.DeleteEmailOptionForm
import uk.gov.hmrc.gatekeepercomposeemailfrontend.views.helpers.CommonViewSpec

class DeleteEmailConfirmationSpec extends CommonViewSpec {

  trait Setup {
    val deleteEmailConfirmation = app.injector.instanceOf[DeleteEmailConfirmation]
    val request                 = FakeRequest().withCSRFToken

    def validateStandardPageElements(document: Document) = {
      document.title() shouldBe "- Email Deleted"
      document.getElementById("page-heading").text() shouldBe "You deleted the email"
      document.getElementById("back-link").text() shouldBe "Back to Emails"
    }

  }

  "DeleteEmailConfirmation" should {

    "render the  page correctly" in new Setup {
      val page: Html = deleteEmailConfirmation.render(request, messages, appConfig)
      validateStandardPageElements(Jsoup.parse(page.body))
    }

  }

}
