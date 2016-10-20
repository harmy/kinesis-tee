/*
 * Copyright (c) 2016 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.

 */
package com.snowplowanalytics.kinesistee.transformation

import com.snowplowanalytics.kinesistee.models.Content
import org.specs2.mutable.Specification
import org.specs2.scalaz.ValidationMatchers

import scalaz.{ Failure, Success}


class JavascriptTransformerSpec extends Specification with ValidationMatchers {


  val unstructJson =
    """{
    "schema": "iglu:com.snowplowanalytics.snowplow/contexts/jsonschema/1-0-0",
    "data": {
      "schema": "iglu:com.snowplowanalytics.snowplow/link_click/jsonschema/1-0-1",
      "data": {
        "targetUrl": "http://www.example.com",
        "elementClasses": ["foreground"],
        "elementId": "exampleLink"
      }
    }
  }"""

  val contextsJson =
    """{
    "schema": "iglu:com.snowplowanalytics.snowplow/contexts/jsonschema/1-0-0",
    "data": [
      {
        "schema": "iglu:org.schema/WebPage/jsonschema/1-0-0",
        "data": {
          "genre": "blog",
          "inLanguage": "en-US",
          "datePublished": "2014-11-06T00:00:00Z",
          "author": "Fred Blundun",
          "breadcrumb": [
            "blog",
            "releases"
          ],
          "keywords": [
            "snowplow",
            "javascript",
            "tracker",
            "event"
          ]
        }
      },
      {
        "schema": "iglu:org.w3/PerformanceTiming/jsonschema/1-0-0",
        "data": {
          "navigationStart": 1415358089861,
          "unloadEventStart": 1415358090270,
          "unloadEventEnd": 1415358090287,
          "redirectStart": 0,
          "redirectEnd": 0,
          "fetchStart": 1415358089870,
          "domainLookupStart": 1415358090102,
          "domainLookupEnd": 1415358090102,
          "connectStart": 1415358090103,
          "connectEnd": 1415358090183,
          "requestStart": 1415358090183,
          "responseStart": 1415358090265,
          "responseEnd": 1415358090265,
          "domLoading": 1415358090270,
          "domInteractive": 1415358090886,
          "domContentLoadedEventStart": 1415358090968,
          "domContentLoadedEventEnd": 1415358091309,
          "domComplete": 0,
          "loadEventStart": 0,
          "loadEventEnd": 0
        }
      }
    ]
  }"""

  val derivedContextsJson =
    """{
    "schema": "iglu:com.snowplowanalytics.snowplow\/contexts\/jsonschema\/1-0-1",
    "data": [
      {
        "schema": "iglu:com.snowplowanalytics.snowplow\/ua_parser_context\/jsonschema\/1-0-0",
        "data": {
          "useragentFamily": "IE",
          "useragentMajor": "7",
          "useragentMinor": "0",
          "useragentPatch": null,
          "useragentVersion": "IE 7.0",
          "osFamily": "Windows XP",
          "osMajor": null,
          "osMinor": null,
          "osPatch": null,
          "osPatchMinor": null,
          "osVersion": "Windows XP",
          "deviceFamily": "Other"
        }
      }
    ]
  }"""

  val nvPairs = List(
    "app_id" -> "angry-birds",
    "platform" -> "web",
    "etl_tstamp" -> "2017-01-26 00:01:25.292",
    "collector_tstamp" -> "2013-11-26 00:02:05",
    "dvce_created_tstamp" -> "2013-11-26 00:03:57.885",
    "event" -> "page_view",
    "event_id" -> "c6ef3124-b53a-4b13-a233-0088f79dcbcb",
    "txn_id" -> "41828",
    "name_tracker" -> "cloudfront-1",
    "v_tracker" -> "js-2.1.0",
    "v_collector" -> "clj-tomcat-0.1.0",
    "v_etl" -> "serde-0.5.2",
    "user_id" -> "jon.doe@email.com",
    "user_ipaddress" -> "92.231.54.234",
    "user_fingerprint" -> "2161814971",
    "domain_userid" -> "bc2e92ec6c204a14",
    "domain_sessionidx" -> "3",
    "network_userid" -> "ecdff4d0-9175-40ac-a8bb-325c49733607",
    "geo_country" -> "US",
    "geo_region" -> "TX",
    "geo_city" -> "New York",
    "geo_zipcode" -> "94109",
    "geo_latitude" -> "37.443604",
    "geo_longitude" -> "-122.4124",
    "geo_region_name" -> "Florida",
    "ip_isp" -> "FDN Communications",
    "ip_organization" -> "Bouygues Telecom",
    "ip_domain" -> "nuvox.net",
    "ip_netspeed" -> "Cable/DSL",
    "page_url" -> "http://www.snowplowanalytics.com",
    "page_title" -> "On Analytics",
    "page_referrer" -> "",
    "page_urlscheme" -> "http",
    "page_urlhost" -> "www.snowplowanalytics.com",
    "page_urlport" -> "80",
    "page_urlpath" -> "/product/index.html",
    "page_urlquery" -> "id=GTM-DLRG",
    "page_urlfragment" -> "4-conclusion",
    "refr_urlscheme" -> "",
    "refr_urlhost" -> "",
    "refr_urlport" -> "",
    "refr_urlpath" -> "",
    "refr_urlquery" -> "",
    "refr_urlfragment" -> "",
    "refr_medium" -> "",
    "refr_source" -> "",
    "refr_term" -> "",
    "mkt_medium" -> "",
    "mkt_source" -> "",
    "mkt_term" -> "",
    "mkt_content" -> "",
    "mkt_campaign" -> "",
    "contexts" -> contextsJson,
    "se_category" -> "",
    "se_action" -> "",
    "se_label" -> "",
    "se_property" -> "",
    "se_value" -> "",
    "unstruct_event" -> unstructJson,
    "tr_orderid" -> "",
    "tr_affiliation" -> "",
    "tr_total" -> "",
    "tr_tax" -> "",
    "tr_shipping" -> "",
    "tr_city" -> "",
    "tr_state" -> "",
    "tr_country" -> "",
    "ti_orderid" -> "",
    "ti_sku" -> "",
    "ti_name" -> "",
    "ti_category" -> "",
    "ti_price" -> "",
    "ti_quantity" -> "",
    "pp_xoffset_min" -> "",
    "pp_xoffset_max" -> "",
    "pp_yoffset_min" -> "",
    "pp_yoffset_max" -> "",
    "useragent" -> "",
    "br_name" -> "",
    "br_family" -> "",
    "br_version" -> "",
    "br_type" -> "",
    "br_renderengine" -> "",
    "br_lang" -> "",
    "br_features_pdf" -> "1",
    "br_features_flash" -> "0",
    "br_features_java" -> "",
    "br_features_director" -> "",
    "br_features_quicktime" -> "",
    "br_features_realplayer" -> "",
    "br_features_windowsmedia" -> "",
    "br_features_gears" -> "",
    "br_features_silverlight" -> "",
    "br_cookies" -> "",
    "br_colordepth" -> "",
    "br_viewwidth" -> "",
    "br_viewheight" -> "",
    "os_name" -> "",
    "os_family" -> "",
    "os_manufacturer" -> "",
    "os_timezone" -> "",
    "dvce_type" -> "",
    "dvce_ismobile" -> "",
    "dvce_screenwidth" -> "",
    "dvce_screenheight" -> "",
    "doc_charset" -> "",
    "doc_width" -> "",
    "doc_height" -> "",
    "tr_currency" -> "",
    "tr_total_base" -> "",
    "tr_tax_base" -> "",
    "tr_shipping_base" -> "",
    "ti_currency" -> "",
    "ti_price_base" -> "",
    "base_currency" -> "",
    "geo_timezone" -> "",
    "mkt_clickid" -> "",
    "mkt_network" -> "",
    "etl_tags" -> "",
    "dvce_sent_tstamp" -> "",
    "refr_domain_userid" -> "",
    "refr_device_tstamp" -> "",
    "derived_contexts" -> derivedContextsJson,
    "domain_sessionid" -> "2b15e5c8-d3b1-11e4-b9d6-1681e6b88ec1",
    "derived_tstamp" -> "2013-11-26 00:03:57.886",
    "event_vendor" -> "com.snowplowanalytics.snowplow",
    "event_name" -> "link_click",
    "event_format" -> "jsonschema",
    "event_version" -> "1-0-0",
    "event_fingerprint" -> "e3dbfa9cca0412c3d4052863cefb547f",
    "true_tstamp" -> "2013-11-26 00:03:57.886"
  )

  val eventValues = nvPairs.unzip._2.mkString("\t")

  val jsSuccessTransform =
    """
      |function transform(row) {
      |   return row
      |}
    """.stripMargin

  val jsFailTransform =
    """
      |function transform(row) {
      |   return false
      |}
    """.stripMargin

  "A valid JS Transform" should {

    "with a js function that successfully transforms, returns content" in {
      val strategy = new JavascriptTransformer(jsSuccessTransform)
      strategy.transform(Content(eventValues, "p")) must beSuccessful(Content(eventValues, "p"))
    }

    "with a js function that fails transforming, returns failure" in {
      val strategy = new JavascriptTransformer(jsFailTransform)
      strategy.transform(Content(eventValues, "p")) must beFailing
    }

  }

  "An invalid js transform" should {

    "fail if js is not well formed" in {
      val badlyFormedJs =
        """
          |function transform(row) {
        """.stripMargin

      val expectedError =
        """<eval>:3:8 Expected } but found eof
          |
          |        ^ in <eval> at line number 3 at column number 8""".stripMargin

      scala.util.Try(new JavascriptTransformer(badlyFormedJs)) match {
        case scala.util.Success(_) => ko("Badly formed JS did not generate exception")
        case scala.util.Failure(f) => f.getMessage.replaceAll("\\s", "") mustEqual expectedError.replaceAll("\\s", "")
      }
    }

    "fail if the js is missing a 'transform' function" in {
      val missingTransformFunc =
        """
          |function banana(row) {
          |   return "papaya"
          |}
        """.stripMargin

      val strategy = new JavascriptTransformer(missingTransformFunc)
      strategy.transform(Content("abc", "p")) match {
        case Success(_) => ko("Transform cannot succeed without a 'transform' function")
        case Failure(f) => f.toString() mustEqual "NonEmptyList(java.lang.NoSuchMethodException: No such function transform)"
      }
    }

    "fail if the js has a runtime error" in {
      val runtimeBloop =
        """
          |function transform(row) { return someKey; }
        """.stripMargin
      val strategy = new JavascriptTransformer(runtimeBloop)
      strategy.transform(Content("BeepBoop", "p")) must beFailing
    }
  }
}