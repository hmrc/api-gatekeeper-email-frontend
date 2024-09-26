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

package uk.gov.hmrc.apigatekeeperemailfrontend.specs

import uk.gov.hmrc.apigatekeeperemailfrontend.pages.{ComposeEmailPage, DummyStartPage, PreviewEmailPage}
import uk.gov.hmrc.apigatekeeperemailfrontend.stubs.GatekeeperEmailStub

class PreviewEmailSpec extends ApiGatekeeperEmailBaseSpec with GatekeeperEmailStub {

  def verifyPage() = {
    verifyTextById("page-heading", "Email preview")
    verifyTextById("delete-email-button", "Delete email")
    verifyTextById("send-email-button", "Send email")
  }

  def primePageForTesting() = {
    signInGatekeeper(app)
    SaveEmail.success()

    And("I've selected to compose an email")
    on(DummyStartPage)
    DummyStartPage.clickSubmit()
    on(ComposeEmailPage)

    ComposeEmailPage.writeInSubjectField("Api Version Change")
    ComposeEmailPage.writeInBodyField("Times they are a changin")

    FetchEmail.success()
    UpdateEmail.success()
    ComposeEmailPage.clickPreviewSubmit()
  }

  Feature("Preview Email Page") {
    info("AS A Product Owner")
    info("I WANT The SDST (Software Developer Support Team) to be able to compose and send emails")
    info("SO THAT The SDST can notify users of Api changes")

    Scenario("Ensure a user can see the preview email page") {
      Given("I have successfully logged in to the API Gatekeeper")
      primePageForTesting()

      Then("I am successfully navigated to the Compose email page")
      on(PreviewEmailPage)

      And("The page renders the posted form data correctly")
      verifyPage()
    }

  }

}
