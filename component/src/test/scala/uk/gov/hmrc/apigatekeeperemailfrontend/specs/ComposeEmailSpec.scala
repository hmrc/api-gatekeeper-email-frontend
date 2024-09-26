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

import uk.gov.hmrc.apigatekeeperemailfrontend.pages.{ComposeEmailPage, DummyStartPage, EmailPreviewPage}
import uk.gov.hmrc.apigatekeeperemailfrontend.stubs.GatekeeperEmailStub

class ComposeEmailSpec extends ApiGatekeeperEmailBaseSpec with GatekeeperEmailStub {

  def verifyPage() = {
    verifyText("email-summary-key", "API")
    verifyText("email-summary-key", "Topic", 1)
    verifyText("email-summary-data", "Agent Authorisation")
    verifyText("email-summary-data", "Business and policy", 1)
  }

  def primePageForTesting() = {
    signInGatekeeper(app)
    SaveEmail.success()

    And("I've selected to compose an email")
    on(DummyStartPage)
    DummyStartPage.clickSubmit()
  }

  Feature("Compose Email Page") {
    info("AS A Product Owner")
    info("I WANT The SDST (Software Developer Support Team) to be able to compose and send emails")
    info("SO THAT The SDST can notify users of Api changes")

    Scenario("Ensure a user can see the compose email page") {
      Given("I have successfully logged in to the API Gatekeeper")
      primePageForTesting()

      Then("I am successfully navigated to the Compose email page")
      on(ComposeEmailPage)

      And("The page renders the posted form data correctly")
      verifyPage()
    }

    Scenario("Ensure a user can enter data the compose email page") {
      Given("I have successfully logged in to the API Gatekeeper and on the compose email page")
      primePageForTesting()
      on(ComposeEmailPage)

      Then("The page renders the posted form data correctly")
      verifyPage()

      Then("The user can fill in `Api Version Change` in the emailSubject field")
      ComposeEmailPage.writeInSubjectField("Api Version Change")

      And("The user can fill in `Times they are a changin` in the emailBody field")
      ComposeEmailPage.writeInBodyField("Times they are a changin")

      When("The user clicks on the preview button")
      FetchEmail.success()
      UpdateEmail.success()
      ComposeEmailPage.clickPreviewSubmit()

      Then("They are navigated to preview email page")

      on(EmailPreviewPage)
    }
  }

}
