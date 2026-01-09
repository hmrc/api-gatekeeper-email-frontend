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

package uk.gov.hmrc.gatekeepercomposeemailfrontend.views.include

import scala.jdk.CollectionConverters._

import org.jsoup.Jsoup
import views.html.include.SiteHeader

import play.api.mvc.RequestHeader
import play.api.test.FakeRequest

import uk.gov.hmrc.gatekeepercomposeemailfrontend.config.AppConfig
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.LoggedInUser
import uk.gov.hmrc.gatekeepercomposeemailfrontend.views.helpers.CommonViewSpec

class SiteHeaderSpec extends CommonViewSpec {

  trait Setup {
    val mockAppConfig = mock[AppConfig]
    val siteHeader    = app.injector.instanceOf[SiteHeader]

    val apiGatekeeperUrl     = "http://localhost:9684/api-gatekeeper"
    val apiGatekeeperApisUrl = "http://localhost:9682/api-gatekeeper-apis"

    val expectedMenuItems = Map(
      "APIs"          -> apiGatekeeperApisUrl,
      "Applications"  -> s"$apiGatekeeperUrl/applications",
      "Developers"    -> s"$apiGatekeeperUrl/developers",
      "Terms of use"  -> s"$apiGatekeeperUrl/terms-of-use",
      "Email"         -> s"$apiGatekeeperUrl/emails",
      "API Approvals" -> s"$apiGatekeeperUrl/pending",
      "XML"           -> s"$apiGatekeeperUrl/xml-organisations"
    )

    when(mockAppConfig.apiGatekeeperFrontendUrl).thenReturn(apiGatekeeperUrl)
    when(mockAppConfig.apiGatekeeperApisFrontendUrl).thenReturn(apiGatekeeperApisUrl)
  }

  "SiteHeader" should {

    "render correctly" in new Setup {
      val component  = siteHeader.render(messagesProvider.messages, fakeRequest)
      val document   = Jsoup.parse(component.body)
      val navigation = document.getElementById("navigation")

      val actualMenuItems = navigation.select("a")
        .asScala
        .map(i => (i.text(), i.attr("href"))).toMap

      actualMenuItems shouldBe expectedMenuItems
    }
  }

}
