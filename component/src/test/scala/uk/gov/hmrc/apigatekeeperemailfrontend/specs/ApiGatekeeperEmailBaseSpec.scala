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

package uk.gov.hmrc.apigatekeeperemailfrontend.specs

import org.scalatest.GivenWhenThen
import org.scalatest.matchers.should.Matchers

import uk.gov.hmrc.apigatekeeperemailfrontend.common.{BaseSpec, SignInSugar, WebPage}
import uk.gov.hmrc.apigatekeeperemailfrontend.matchers.CustomMatchers
import uk.gov.hmrc.apigatekeeperemailfrontend.utils.UrlEncoding

class ApiGatekeeperEmailBaseSpec
    extends BaseSpec
    with SignInSugar
    with Matchers
    with CustomMatchers
    with GivenWhenThen
    with UrlEncoding {

  def isCurrentPage(page: WebPage): Unit = {
    page.heading shouldBe page.pageHeading
  }
}
