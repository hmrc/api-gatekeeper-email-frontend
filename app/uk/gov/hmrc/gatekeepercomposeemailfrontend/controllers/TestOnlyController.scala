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

// $COVERAGE-OFF$
package uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import views.html.ForbiddenView
import views.html.testonly.GKFEDummyForm

import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import uk.gov.hmrc.gatekeepercomposeemailfrontend.config.AppConfig
import uk.gov.hmrc.gatekeepercomposeemailfrontend.connectors.AuthConnector
import uk.gov.hmrc.gatekeepercomposeemailfrontend.models.GatekeeperRole
import uk.gov.hmrc.gatekeepercomposeemailfrontend.utils.GatekeeperAuthWrapper

@Singleton
class TestOnlyController @Inject() (
    mcc: MessagesControllerComponents,
    gkfeDummyForm: GKFEDummyForm,
    override val forbiddenView: ForbiddenView,
    override val authConnector: AuthConnector
  )(implicit val appConfig: AppConfig,
    val ec: ExecutionContext
  ) extends FrontendController(mcc) with GatekeeperAuthWrapper with Logging {

  def displaydummypage(): Action[AnyContent] = requiresAtLeast(GatekeeperRole.USER) { implicit request =>
    Future.successful(Ok(gkfeDummyForm()))
  }

}

// $COVERAGE-ON$
