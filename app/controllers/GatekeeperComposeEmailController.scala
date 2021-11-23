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

package controllers

import config.AppConfig
import connectors.GatekeeperEmailConnector
import play.api.i18n.I18nSupport
import views.html.HelloWorldPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GatekeeperComposeEmailController @Inject()(
  mcc: MessagesControllerComponents,
  emailConnector: GatekeeperEmailConnector,
  helloWorldPage: HelloWorldPage
  )(implicit val appConfig: AppConfig, val ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  val sendEmail: Action[AnyContent] = Action.async { implicit request =>
    val emailTo: String = "test.user@digital.hmrc.gov.uk"
    val params: Map[String, String] = Map("subject" -> appConfig.emailSubject,
                                          "fromAddress" -> "gateKeeper",
                                          "body" -> "Body to be used in the email template",
                                          "service" -> "gatekeeper")

    emailConnector.sendEmail(emailTo, params)
    Future.successful(Ok(helloWorldPage()))
  }

}