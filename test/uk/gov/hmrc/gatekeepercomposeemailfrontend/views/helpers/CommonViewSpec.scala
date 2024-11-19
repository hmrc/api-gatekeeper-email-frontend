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

package uk.gov.hmrc.gatekeepercomposeemailfrontend.views.helpers

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

import java.util.Locale
import scala.collection.JavaConverters.asScalaBufferConverter

import org.jsoup.nodes.Document
import org.scalatest.Assertion
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl, MessagesProvider}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.request.RequestAttrKey
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.{FakeRequest, StubMessagesFactory}
import play.test.Helpers

import uk.gov.hmrc.gatekeepercomposeemailfrontend.common.AsyncHmrcSpec
import uk.gov.hmrc.gatekeepercomposeemailfrontend.config.AppConfig
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.LoggedInUser
import uk.gov.hmrc.gatekeepercomposeemailfrontend.utils.WithCSRFAddToken

trait CommonViewSpec extends AsyncHmrcSpec with GuiceOneAppPerSuite {

  val mcc         = app.injector.instanceOf[MessagesControllerComponents]
  val messagesApi = mcc.messagesApi

  implicit val fakeRequest: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken

  implicit val userName: LoggedInUser             = LoggedInUser(Some("gate.keeper"))
  implicit val messagesProvider: MessagesProvider = MessagesImpl(Lang(Locale.ENGLISH), messagesApi)
  implicit val messages: Messages                 = messagesApi.preferred(fakeRequest)

  implicit val appConfig: AppConfig = mock[AppConfig]

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(("metrics.jvm", false))
      .build()

}
