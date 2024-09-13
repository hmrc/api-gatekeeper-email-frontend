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

package uk.gov.hmrc.gatekeepercomposeemailfrontend.utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.apache.pekko.stream.Materializer
import org.mockito.MockitoSugar
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.should.Matchers
import views.html._

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.gatekeepercomposeemailfrontend.common.ControllerBaseSpec
import uk.gov.hmrc.gatekeepercomposeemailfrontend.connectors.{AuthConnector, GatekeeperEmailConnector}
import uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.{ComposeEmailController, ComposeEmailForm, RemoveUploadedFileFormProvider}
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.{DevelopersEmailQuery, OutgoingEmail, RegisteredUser}
import uk.gov.hmrc.gatekeepercomposeemailfrontend.services.ComposeEmailService

object ComposeEmailControllerSpecHelpers extends ControllerBaseSpec with Matchers with GivenWhenThen
    with MockitoSugar {
  implicit val materializer: Materializer = app.materializer
  lazy val forbiddenView                  = app.injector.instanceOf[ForbiddenView]
  val errorTemplate: ErrorTemplate        = fakeApplication().injector.instanceOf[ErrorTemplate]

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.jvm"     -> false,
        "metrics.enabled" -> false
      )
      .build()

  val fakeGetRequest                               = FakeRequest("GET", "/email").withCSRFToken
  val fakeConfirmationGetRequest                   = FakeRequest("GET", "/sent-email").withCSRFToken
  val mockEmailConnector: GatekeeperEmailConnector = mock[GatekeeperEmailConnector]
  val mockWSClient: WSClient                       = mock[WSClient]
  lazy val composeEmailTemplateView                = app.injector.instanceOf[ComposeEmail]
  lazy val emailPreviewTemplateView                = app.injector.instanceOf[EmailPreview]
  lazy val emailSentTemplateView                   = app.injector.instanceOf[EmailSentConfirmation]
  lazy val deleteConfirmEmail                      = app.injector.instanceOf[EmailDeleteConfirmation]
  lazy val deleteEmail                             = app.injector.instanceOf[EmailDelete]
  lazy val formProvider                            = app.injector.instanceOf[RemoveUploadedFileFormProvider]
  val su                                           = List(RegisteredUser("sawd", "efef", "eff", true))
  val userSelectionQuery                           = new DevelopersEmailQuery(None, None, None, false, None, false, None)

  class ComposeEmailServiceTest extends ComposeEmailService(mock[GatekeeperEmailConnector]) {

    override def saveEmail(composeEmailForm: ComposeEmailForm, emailUUID: String, userSelectionQuery: DevelopersEmailQuery)(implicit hc: HeaderCarrier): Future[OutgoingEmail] =
      Future.successful(OutgoingEmail("srinivasalu.munagala@digital.hmrc.gov.uk", "Hello", "*test email body*", "", "", "", "", None, userSelectionQuery, 1))

    override def fetchEmail(emailUUID: String)(implicit hc: HeaderCarrier): Future[OutgoingEmail] = {
      Future.successful(OutgoingEmail("srinivasalu.munagala@digital.hmrc.gov.uk", "Hello", "*test email body*", "", "", "", "", None, userSelectionQuery, 1))
    }

    override def updateEmail(
        composeEmailForm: ComposeEmailForm,
        emailUUID: String,
        userSelectionQuery: Option[DevelopersEmailQuery]
      )(implicit hc: HeaderCarrier
      ): Future[OutgoingEmail] = {
      Future.successful(OutgoingEmail("srinivasalu.munagala@digital.hmrc.gov.uk", "Hello", "*test email body*", "", "", "", "", None, userSelectionQuery.get, 1))
    }

    override def deleteEmail(emailUUID: String)(implicit hc: HeaderCarrier): Future[Boolean] = Future.successful(true)
  }
  val mockGateKeeperService = new ComposeEmailServiceTest

  def buildController(mockGateKeeperService: ComposeEmailService, mockAuthConnector: AuthConnector): ComposeEmailController = {
    new ComposeEmailController(
      mcc,
      composeEmailTemplateView,
      emailPreviewTemplateView,
      mockGateKeeperService,
      emailSentTemplateView,
      deleteConfirmEmail,
      deleteEmail,
      forbiddenView,
      formProvider,
      mockAuthConnector
    )
  }

}
