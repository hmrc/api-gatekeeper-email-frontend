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

package uk.gov.hmrc.gatekeepercomposeemailfrontend.common

import java.net.URL
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxOptions, FirefoxProfile}
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.{Dimension, WebDriver}

import scala.util.{Properties, Try}

trait Env {
  val driver: WebDriver = createWebDriver
  lazy val port = 6001
  lazy val windowSize = new Dimension(1024, 800)

  lazy val createWebDriver: WebDriver = {
    Properties.propOrElse("test_driver", "chrome") match {
      case "chrome" => createChromeDriver()
      case "firefox" => createFirefoxDriver()
      case "remote-chrome" => createRemoteChromeDriver()
      case "remote-firefox" => createRemoteFirefoxDriver()
      case other => throw new IllegalArgumentException(s"target browser $other not recognised")
    }
  }

  def createRemoteChromeDriver() = {
    val driver = new RemoteWebDriver(new URL(s"http://localhost:4444/wd/hub"), new ChromeOptions() )
    driver.manage().window().setSize(windowSize)
    driver
  }

  def createRemoteFirefoxDriver() = {
    new RemoteWebDriver(new URL(s"http://localhost:4444/wd/hub"), new FirefoxOptions())
  }

  def createChromeDriver(): WebDriver = {
    val options = new ChromeOptions()
    options.addArguments("--headless")
    options.addArguments("--proxy-server='direct://'")
    options.addArguments("--proxy-bypass-list=*")
    val driver = new ChromeDriver(options)
    driver.manage().deleteAllCookies()
    driver.manage().window().setSize(windowSize)
    driver
  }

  def createFirefoxDriver(): WebDriver = {
    val profile = new FirefoxProfile
    profile.setAcceptUntrustedCertificates(true)
    val firefoxOptions: FirefoxOptions = new FirefoxOptions
    firefoxOptions.setProfile(profile)
    new FirefoxDriver(firefoxOptions)
  }

  def shutdown = Try(driver.quit())

  sys addShutdownHook {
    shutdown
  }
}

object Env extends Env

class AfterHook {
  Env.shutdown
}
