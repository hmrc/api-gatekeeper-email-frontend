/*
 * Copyright 2022 HM Revenue & Customs
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

package services

import connectors.GatekeeperEmailConnector
import controllers.{ComposeEmailForm, EmailPreviewForm}
import models.OutgoingEmail
import org.mockito.ArgumentMatchersSugar
import org.mockito.scalatest.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._
import play.mvc.Http.Status
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ComposeEmailServiceSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ArgumentMatchersSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  trait Setup {
    val mockEmailConnector = mock[GatekeeperEmailConnector]
    val underTest = new ComposeEmailService(mockEmailConnector)
  }

  "sendEmail" should {
    "handle sending an email successfully" in new Setup {
      when(mockEmailConnector.saveEmail(*)(*)).thenReturn(Future.successful(OutgoingEmail("", "", List(""), None, "", "", "", "", None)))
      val result = await(underTest.sendEmail(new EmailPreviewForm("","")))
      result shouldBe OutgoingEmail("", "", List(""), None, "", "", "", "", None)
    }
  }
}
