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

package uk.gov.hmrc.gatekeepercomposeemailfrontend.models.file_upload

import play.api.libs.json.{Json, OFormat}

import uk.gov.hmrc.gatekeepercomposeemailfrontend.config.AppConfig

case class UploadDocumentsWrapper(config: UploadDocumentsConfig, existingFiles: Seq[UploadedFile])

object UploadDocumentsWrapper {

  def createPayload(
      nonce: Nonce,
      emailUUID: String,
      searched: Boolean,
      multipleUpload: Boolean,
      attachmentDetails: Option[Seq[UploadedFile]]
    )(implicit appConfig: AppConfig
    ): UploadDocumentsWrapper = {
    val continueUrl = uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.routes.ComposeEmailController.emailPreview(emailUUID, "")
    val backLinkUrl = uk.gov.hmrc.gatekeepercomposeemailfrontend.controllers.routes.ComposeEmailController.upload(emailUUID, "")
    val callBack    = "/gatekeeperemail/updatefiles"

    val attachments =
      if (attachmentDetails.isDefined) {
        attachmentDetails.get
      } else Seq.empty

    UploadDocumentsWrapper(
      config = UploadDocumentsConfig(
        nonce = nonce,
        initialNumberOfEmptyRows = Some(1),
        continueUrl = s"${appConfig.fileUploadContinueUrlPrefix}$continueUrl",
        backlinkUrl = s"${appConfig.fileUploadContinueUrlPrefix}$backLinkUrl",
        callbackUrl = s"${appConfig.fileUploadCallbackUrlPrefix}$callBack",
        cargo = UploadCargo(emailUUID),
//        content = Some(UploadDocumentsContent(
//          serviceName = Some(appConfig.fileUploadServiceName),
//          serviceUrl = Some(appConfig.homepage),
//          accessibilityStatementUrl = Some(appConfig.fileUploadAccessibilityUrl),
//          phaseBanner = Some(appConfig.fileUploadPhase),
//          phaseBannerUrl = Some(appConfig.fileUploadPhaseUrl),
//          userResearchBannerUrl = Some(appConfig.helpMakeGovUkBetterUrl),
//          contactFrontendServiceId = Some(appConfig.contactFrontendServiceId)
//        )),
        content = None,
//        features = Some(UploadDocumentsFeatures(Some(multipleUpload)))
        features = None
      ),
      existingFiles = attachments
    )
  }

  implicit val format: OFormat[UploadDocumentsWrapper] = Json.format[UploadDocumentsWrapper]
}
