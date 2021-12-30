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

package services

import config.AppConfig
import connectors.GatekeeperEmailConnector
import controllers.ComposeEmailForm
import models.EmailHttpResponse
import models.SendEmailRequest.createEmailRequest
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.http.HeaderCarrier
import views.html.{ComposeEmail, EmailSentConfirmation}

import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class ComposeEmailService @Inject()(emailConnector: GatekeeperEmailConnector)
                         (implicit val ec: ExecutionContext){

  def sendEmail(composeEmailForm: ComposeEmailForm)(implicit hc: HeaderCarrier): Future[Int] = {
    val result = emailConnector.sendEmail(composeEmailForm)
    result
  }

}
