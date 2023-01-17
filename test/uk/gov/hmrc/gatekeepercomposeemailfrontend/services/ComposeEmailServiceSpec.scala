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

package uk.gov.hmrc.gatekeepercomposeemailfrontend.services

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.mockito.ArgumentMatchersSugar
import org.mockito.scalatest.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.gatekeepercomposeemailfrontend.connectors.GatekeeperEmailConnector
import uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.ComposeEmailForm
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.{DevelopersEmailQuery, OutgoingEmail, RegisteredUser, User}

class ComposeEmailServiceSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ArgumentMatchersSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  trait Setup {
    val mockEmailConnector = mock[GatekeeperEmailConnector]
    val underTest          = new ComposeEmailService(mockEmailConnector)
    val su                 = List(RegisteredUser("sawd", "efef", "eff", true))
    val emailUUID          = "emailUUID"
    val userSelectionQuery = new DevelopersEmailQuery(None, None, None, false, None, false, None)

  }

  "saveEmail" should {
    "handle saving an email successfully" in new Setup {
      when(mockEmailConnector.saveEmail(*, *, *)(*)).thenReturn(Future.successful(OutgoingEmail("", "", None, "", "", "", "", "", None, userSelectionQuery, 1)))
      val result = await(underTest.saveEmail(new ComposeEmailForm("", "", true), "", userSelectionQuery))
      result shouldBe OutgoingEmail("", "", None, "", "", "", "", "", None, userSelectionQuery, 1)
    }
  }

  "fetchEmail" should {
    "handle fetching an email successfully" in new Setup {
      when(mockEmailConnector.fetchEmail(*)(*)).thenReturn(Future.successful(OutgoingEmail("", "", None, "", "", "", "", "", None, userSelectionQuery, 1)))
      val result = await(underTest.fetchEmail(emailUUID = emailUUID))
      result shouldBe OutgoingEmail("", "", None, "", "", "", "", "", None, userSelectionQuery, 1)
    }
  }

  "deleteEmail" should {
    "handle deleting an email successfully" in new Setup {
      when(mockEmailConnector.deleteEmail(*)(*)).thenReturn(Future.successful(true))
      val result = await(underTest.deleteEmail(emailUUID = emailUUID))
      result shouldBe true
    }
  }

  "updateEmail" should {
    "handle updating an email successfully" in new Setup {
      when(mockEmailConnector.updateEmail(*, *, *, *)(*)).thenReturn(Future.successful(OutgoingEmail("", "", None, "", "", "", "", "", None, userSelectionQuery, 1)))
      val result = await(underTest.updateEmail(new ComposeEmailForm("", "", true), "", Some(userSelectionQuery)))
      result shouldBe OutgoingEmail("", "", None, "", "", "", "", "", None, userSelectionQuery, 1)
    }
  }
}
