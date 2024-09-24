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

package uk.gov.hmrc.apigatekeeperemailfrontend.stubs

import java.util.UUID

import com.github.tomakehurst.wiremock.client.WireMock.{verify => wireMockVerify, _}

import play.api.http.Status.OK

trait GatekeeperEmailStub {
  val selectionQuery = """{"topic":"topic-dev", "privateapimatch": false, "apiVersionFilter": "apiVersionFilter", "allUsers": false}""".stripMargin

  def responseBody(emailUUIDString: String) =
    s"""
       |  {
       |    "emailUUID": "$emailUUIDString",
       |    "recipientTitle": "Team-Title",
       |    "recipients": [{"email": "", "firstName": "", "lastName": "", "verified": true}],
       |    "attachmentLink": "",
       |    "markdownEmailBody": "",
       |    "htmlEmailBody": "",
       |    "subject": "",
       |    "status": "",
       |    "composedBy": "auto-emailer",
       |    "approvedBy": "auto-emailer",
       |    "userSelectionQuery": $selectionQuery,
       |    "emailsCount": 1
       |  }
      """.stripMargin

  def verifyPostToURL(url: String): Unit = {
    wireMockVerify(
      1,
      postRequestedFor(
        urlEqualTo(url)
      )
    )
  }

  def verifyGetFromURL(url: String): Unit = {
    wireMockVerify(
      1,
      getRequestedFor(
        urlEqualTo(url)
      )
    )
  }

  def stubPost(emailUUID: String, url: String) = {
    stubFor(post(urlEqualTo(url)).willReturn(aResponse()
      .withHeader("Content-type", "application/json")
      .withBody(responseBody(emailUUID))
      .withStatus(OK)))
  }

  object SendEmail {
    val path = s"/gatekeeper-email/send-email/"

    def sucesss(emailUUid: String) = {
      stubPost(emailUUid, s"$path$emailUUid")
    }

    def failsWithStatus(emailUUid: String, status: Int) = {
      stubFor(post(urlEqualTo(s"$path$emailUUid")).willReturn(aResponse().withStatus(status)))
    }

    def verify(emailUUid: String) = {
      verifyPostToURL(s"$path$emailUUid")
    }
  }

  object SendTestMail {
    val path = s"/gatekeeper-email/send-test-email/"

    def success(emailUUid: String) = {
      stubPost(emailUUid, s"$path$emailUUid")
    }

    def failsWithStatus(emailUUid: String, status: Int) = {
      stubFor(post(urlEqualTo(s"$path$emailUUid")).willReturn(aResponse().withStatus(status)))
    }

    def verify(emailUUid: String) = {
      verifyPostToURL(s"$path$emailUUid")
    }
  }

  object SaveEmail {
    val path = s"/gatekeeper-email/save-email?emailUUID="

    def success(emailUUid: String) = {
      stubPost(emailUUid, s"$path$emailUUid")
    }

    def success() = {
      stubFor(post(urlPathMatching("/gatekeeper-email/save-email.*")).willReturn(aResponse()
        .withHeader("Content-type", "application/json")
        .withBody(responseBody(UUID.randomUUID().toString))
        .withStatus(OK)))
    }

    def failsWithStatus(emailUUid: String, status: Int) = {
      stubFor(post(urlEqualTo(s"$path$emailUUid")).willReturn(aResponse().withStatus(status)))
    }

    def verify(emailUUid: String) = {
      verifyPostToURL(s"$path$emailUUid")
    }
  }

  object UpdateEmail {
    val path = s"/gatekeeper-email/update-email?emailUUID="

    def success(emailUUid: String) = {
      stubPost(emailUUid, s"$path$emailUUid")
    }

    def failsWithStatus(emailUUid: String, status: Int) = {
      stubFor(post(urlEqualTo(s"$path$emailUUid")).willReturn(aResponse().withStatus(status)))
    }

    def verify(emailUUid: String) = {
      verifyPostToURL(s"$path$emailUUid")
    }
  }

  object FetchEmail {

    val path = s"/gatekeeper-email/fetch-email/"

    def success(emailUUid: String) = {
      stubFor(get(urlEqualTo(s"$path$emailUUid")).willReturn(aResponse()
        .withHeader("Content-type", "application/json")
        .withBody(responseBody(UUID.randomUUID().toString))
        .withStatus(OK)))
    }

    def failsWithStatus(emailUUid: String, status: Int) = {
      stubFor(get(urlEqualTo(s"$path$emailUUid")).willReturn(aResponse().withStatus(status)))
    }

    def verify(emailUUid: String) = {
      verifyGetFromURL(s"$path$emailUUid")
    }

  }

}
