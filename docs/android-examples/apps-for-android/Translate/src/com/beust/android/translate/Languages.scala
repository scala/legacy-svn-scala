/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.beust.android.translate

import android.app.Activity
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Button

import scala.collection.immutable.HashMap

/**
 * Language information for the Google Translate API.
 */
object Languages {
    
  /**
   * Reference at http://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
   */
  object Language extends Enumeration {

    case class Lang(shortName: String, longName: String, flag: Int)
    extends Val(shortName) {
      def this(shortName: String, longName: String) =
        this(shortName, longName, -1)
      override def toString: String = longName
      def name: String = longName
      def configureButton(activity: Activity, button: Button) {
        button setTag this
        button setText longName
        if (flag != -1) {
          val df = activity.getResources getDrawable flag
          button.setCompoundDrawablesWithIntrinsicBounds(df, null, null, null)
          button setCompoundDrawablePadding 5
        }
      }
    }
    object Lang {
      def apply(shortName: String, longName: String) =
        new Lang(shortName, longName)
    }

//  val AFRIKAANS = Lang("af", "Afrikaans", R.drawable.af)
//  val ALBANIAN = Lang("sq", "Albanian")
//  val AMHARIC = Lang("am", "Amharic", R.drawable.am),
//  val ARABIC = Lang("ar", "Arabic", R.drawable.ar),
//  val ARMENIAN = Lang("hy", "Armenian"),
//  val AZERBAIJANI = Lang("az", "Azerbaijani", R.drawable.az),
//  val BASQUE = Lang("eu", "Basque"),
//  val BELARUSIAN = Lang("be", "Belarusian", R.drawable.be),
//  val BENGALI = Lang("bn", "Bengali", R.drawable.bn),
//  val BIHARI = Lang("bh", "Bihari", R.drawable.bh),
        
    val BULGARIAN = Lang("bg", "Bulgarian", R.drawable.bg)

//  val BURMESE = Lang("my", "Burmese", R.drawable.my)
    val CATALAN = Lang("ca", "Catalan")
        
    val CHINESE = Lang("zh", "Chinese", R.drawable.cn)
    val CHINESE_SIMPLIFIED = Lang("zh-CN", "Chinese simplified", R.drawable.cn)
    val CHINESE_TRADITIONAL = Lang("zh-TW", "Chinese traditional", R.drawable.tw)
    val CROATIAN = Lang("hr", "Croatian", R.drawable.hr)
    val CZECH = Lang("cs", "Czech", R.drawable.cs)
        
    val DANISH = Lang("da", "Danish", R.drawable.dk)
//  val DHIVEHI = Lang("dv", "Dhivehi"),
        
    val DUTCH = Lang("nl", "Dutch", R.drawable.nl)
    val ENGLISH = Lang("en", "English", R.drawable.us)
        
//  val ESPERANTO = Lang("eo", "Esperanto")
//  val ESTONIAN = Lang("et", "Estonian", R.drawable.et)
    val FILIPINO = Lang("tl", "Filipino", R.drawable.ph)

    val FINNISH = Lang("fi", "Finnish", R.drawable.fi)
    val FRENCH = Lang("fr", "French", R.drawable.fr)
        
//  val GALICIAN = Lang("gl", "Galician", R.drawable.gl)
//  val GEORGIAN = Lang("ka", "Georgian")
        
    val GERMAN = Lang("de", "German", R.drawable.de)
        
    val GREEK = Lang("el", "Greek", R.drawable.gr)
//  val GUARANI = Lang("gn", "Guarani", R.drawable.gn)
//  val GUJARATI = Lang("gu", "Gujarati", R.drawable.gu)
//  val HEBREW = Lang("iw", "Hebrew", R.drawable.il)
//  val HINDI = Lang("hi", "Hindi")
//  val HUNGARIAN = Lang("hu", "Hungarian", R.drawable.hu)
//  val ICELANDIC = Lang("is", "Icelandic", R.drawable.is)
    val INDONESIAN = Lang("id", "Indonesian", R.drawable.id)
//  val INUKTITUT = Lang("iu", "Inuktitut")
        
    val ITALIAN = Lang("it", "Italian", R.drawable.it)
    val JAPANESE = Lang("ja", "Japanese", R.drawable.jp)
        
//  val KANNADA = Lang("kn", "Kannada", R.drawable.kn)
//  val KAZAKH = Lang("kk", "Kazakh")
//  val KHMER = Lang("km", "Khmer", R.drawable.km)
        
    val KOREAN = Lang("ko", "Korean", R.drawable.kr)
        
//  val KURDISH = Lang("ky", "Kurdish", R.drawable.ky)
//  val LAOTHIAN = Lang("lo", "Laothian")
//  val LATVIAN = Lang("la", "Latvian", R.drawable.la)
    val LITHUANIAN = Lang("lt", "Lithuanian", R.drawable.lt)
//  val MACEDONIAN = Lang("mk", "Macedonian", R.drawable.mk)
//  val MALAY = Lang("ms", "Malay", R.drawable.ms)
//  val MALAYALAM = Lang("ml", "Malayalam", R.drawable.ml)
//  val MALTESE = Lang("mt", "Maltese", R.drawable.mt)
//  val MARATHI = Lang("mr", "Marathi", R.drawable.mr)
//  val MONGOLIAN = Lang("mn", "Mongolian", R.drawable.mn)
//  val NEPALI = Lang("ne", "Nepali", R.drawable.ne)
        
    val NORWEGIAN = Lang("no", "Norwegian", R.drawable.no)
        
//  val ORIYA = Lang("or", "Oriya")
//  val PASHTO = Lang("ps", "Pashto", R.drawable.ps)
//  val PERSIAN = Lang("fa", "Persian")
        
    val POLISH = Lang("pl", "Polish", R.drawable.pl)
    val PORTUGUESE = Lang("pt", "Portuguese", R.drawable.pt)
        
//  val PUNJABI = Lang("pa", "Punjabi", R.drawable.pa)
        
    val ROMANIAN = Lang("ro", "Romanian", R.drawable.ro)
    val RUSSIAN = Lang("ru", "Russian", R.drawable.ru)
        
//  val SANSKRIT = Lang("sa", "Sanskrit", R.drawable.sa)
    val SERBIAN = Lang("sr", "Serbian", R.drawable.sr)
//  val SINDHI = Lang("sd", "Sindhi", R.drawable.sd)
//  val SINHALESE = Lang("si", "Sinhalese", R.drawable.si)
    val SLOVAK = Lang("sk", "Slovak", R.drawable.sk)
    val SLOVENIAN = Lang("sl", "Slovenian", R.drawable.sl)
        
    val SPANISH = Lang("es", "Spanish", R.drawable.es)
        
//  val SWAHILI = Lang("sw", "Swahili"),
        
    val SWEDISH = Lang("sv", "Swedish", R.drawable.sv)
        
//  val TAJIK = Lang("tg", "Tajik", R.drawable.tg)
//  val TAMIL = Lang("ta", "Tamil")
        
    val TAGALOG = Lang("tl", "Tagalog", R.drawable.ph)
//  val TELUGU = Lang("te", "Telugu")
//  val THAI = Lang("th", "Thai", R.drawable.th)
//  val TIBETAN = Lang("bo", "Tibetan", R.drawable.bo)
//  val TURKISH = Lang("tr", "Turkish", R.drawable.tr)
    val UKRAINIAN = Lang("uk", "Ukrainian", R.drawable.ua)
//  val URDU = Lang("ur", "Urdu"),
//  val UZBEK = Lang("uz", "Uzbek", R.drawable.uz),
//  val UIGHUR = Lang("ug", "Uighur", R.drawable.ug)

    private val mLongNameToShortName =
      HashMap.empty[String, String] ++
      (values map { case v: Language.Lang => (v.longName, v.shortName) })

    private val mShortNameToLanguage =
      HashMap.empty[String, Language] ++
      (values map { case v: Language.Lang => (v.shortName, v) })

    def findLanguageByShortName(shortName: String): Language =
       mShortNameToLanguage(shortName)

    def getShortName(longName: String): String =
      Language.mLongNameToShortName(longName)

    private def log(s: String) {
      Log.d(TranslateActivity.TAG, "[Languages] " + s)
    }

    def apply(s: String): Language =
      super.withName(s).asInstanceOf[Language]
      
  }

  type Language = Language.Lang

}

