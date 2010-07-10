/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.{IOException, StringWriter, Writer}

import scala.collection.mutable.{HashMap, Map}

/**
 * <p>
 * Provides HTML and XML entity utilities.
 * </p>
 * 
 * @see <a href="http://hotwired.lycos.com/webmonkey/reference/special_characters/">ISO Entities</a>
 * @see <a href="http://www.w3.org/TR/REC-html32#latin1">HTML 3.2 Character Entities for ISO Latin-1</a>
 * @see <a href="http://www.w3.org/TR/REC-html40/sgml/entities.html">HTML 4.0 Character entity references</a>
 * @see <a href="http://www.w3.org/TR/html401/charset.html#h-5.3">HTML 4.01 Character References</a>
 * @see <a href="http://www.w3.org/TR/html401/charset.html#code-position">HTML 4.01 Code positions</a>
 * 
 * @author <a href="mailto:alex@purpletech.com">Alexander Day Chaffee</a>
 * @author <a href="mailto:ggregory@seagullsw.com">Gary Gregory</a>
 * @since 2.0
 * @version $Id: Entities.java 636641 2008-03-13 06:11:30Z bayard $
 */
object Entities {

  private final val BASIC_ARRAY = Array(
    Array("quot", "34"), // " - double-quote
    Array("amp", "38"),  // & - ampersand
    Array("lt", "60"),   // < - less-than
    Array("gt", "62")    // > - greater-than
  )

  private final val APOS_ARRAY = Array(
    Array("apos", "39")  // XML apostrophe
  )

  // package scoped for testing
  final val ISO8859_1_ARRAY = Array(
    Array("nbsp",   "160"), // non-breaking space
    Array("iexcl",  "161"), // inverted exclamation mark
    Array("cent",   "162"), // cent sign
    Array("pound",  "163"), // pound sign
    Array("curren", "164"), // currency sign
    Array("yen",    "165"), // yen sign = yuan sign
    Array("brvbar", "166"), // broken bar = broken vertical bar
    Array("sect",   "167"), // section sign
    Array("uml",    "168"), // diaeresis = spacing diaeresis
    Array("copy",   "169"), // - copyright sign
    Array("ordf",   "170"), // feminine ordinal indicator
    Array("laquo",  "171"), // left-pointing double angle quotation mark = left pointing guillemet
    Array("not",    "172"), // not sign
    Array("shy",    "173"), // soft hyphen = discretionary hyphen
    Array("reg",    "174"), // - registered trademark sign
    Array("macr",   "175"), // macron = spacing macron = overline = APL overbar
    Array("deg",    "176"), // degree sign
    Array("plusmn", "177"), // plus-minus sign = plus-or-minus sign
    Array("sup2",   "178"), // superscript two = superscript digit two = squared
    Array("sup3",   "179"), // superscript three = superscript digit three = cubed
    Array("acute",  "180"), // acute accent = spacing acute
    Array("micro",  "181"), // micro sign
    Array("para",   "182"), // pilcrow sign = paragraph sign
    Array("middot", "183"), // middle dot = Georgian comma = Greek middle dot
    Array("cedil",  "184"), // cedilla = spacing cedilla
    Array("sup1",   "185"), // superscript one = superscript digit one
    Array("ordm",   "186"), // masculine ordinal indicator
    Array("raquo",  "187"), // right-pointing double angle quotation mark = right pointing guillemet
    Array("frac14", "188"), // vulgar fraction one quarter = fraction one quarter
    Array("frac12", "189"), // vulgar fraction one half = fraction one half
    Array("frac34", "190"), // vulgar fraction three quarters = fraction three quarters
    Array("iquest", "191"), // inverted question mark = turned question mark
    Array("Agrave", "192"), //  - uppercase A, grave accent
    Array("Aacute", "193"), //  - uppercase A, acute accent
    Array("Acirc",  "194"), //  - uppercase A, circumflex accent
    Array("Atilde", "195"), //  - uppercase A, tilde
    Array("Auml",   "196"), //  - uppercase A, umlaut
    Array("Aring",  "197"), //  - uppercase A, ring
    Array("AElig",  "198"), //  - uppercase AE
    Array("Ccedil", "199"), //  - uppercase C, cedilla
    Array("Egrave", "200"), //  - uppercase E, grave accent
    Array("Eacute", "201"), //  - uppercase E, acute accent
    Array("Ecirc",  "202"), //  - uppercase E, circumflex accent
    Array("Euml",   "203"), //  - uppercase E, umlaut
    Array("Igrave", "204"), //  - uppercase I, grave accent
    Array("Iacute", "205"), //  - uppercase I, acute accent
    Array("Icirc",  "206"), //  - uppercase I, circumflex accent
    Array("Iuml",   "207"), //  - uppercase I, umlaut
    Array("ETH",    "208"), //  - uppercase Eth, Icelandic
    Array("Ntilde", "209"), //  - uppercase N, tilde
    Array("Ograve", "210"), //  - uppercase O, grave accent
    Array("Oacute", "211"), //  - uppercase O, acute accent
    Array("Ocirc",  "212"), //  - uppercase O, circumflex accent
    Array("Otilde", "213"), //  - uppercase O, tilde
    Array("Ouml",   "214"), //  - uppercase O, umlaut
    Array("times",  "215"), // multiplication sign
    Array("Oslash", "216"), //  - uppercase O, slash
    Array("Ugrave", "217"), //  - uppercase U, grave accent
    Array("Uacute", "218"), //  - uppercase U, acute accent
    Array("Ucirc",  "219"), //  - uppercase U, circumflex accent
    Array("Uuml",   "220"), //  - uppercase U, umlaut
    Array("Yacute", "221"), //  - uppercase Y, acute accent
    Array("THORN",  "222"), //  - uppercase THORN, Icelandic
    Array("szlig",  "223"), //  - lowercase sharps, German
    Array("agrave", "224"), //  - lowercase a, grave accent
    Array("aacute", "225"), //  - lowercase a, acute accent
    Array("acirc",  "226"), //  - lowercase a, circumflex accent
    Array("atilde", "227"), //  - lowercase a, tilde
    Array("auml",   "228"), //  - lowercase a, umlaut
    Array("aring",  "229"), //  - lowercase a, ring
    Array("aelig",  "230"), //  - lowercase ae
    Array("ccedil", "231"), //  - lowercase c, cedilla
    Array("egrave", "232"), //  - lowercase e, grave accent
    Array("eacute", "233"), //  - lowercase e, acute accent
    Array("ecirc",  "234"), //  - lowercase e, circumflex accent
    Array("euml",   "235"), //  - lowercase e, umlaut
    Array("igrave", "236"), //  - lowercase i, grave accent
    Array("iacute", "237"), //  - lowercase i, acute accent
    Array("icirc",  "238"), //  - lowercase i, circumflex accent
    Array("iuml",   "239"), //  - lowercase i, umlaut
    Array("eth",    "240"), //  - lowercase eth, Icelandic
    Array("ntilde", "241"), //  - lowercase n, tilde
    Array("ograve", "242"), //  - lowercase o, grave accent
    Array("oacute", "243"), //  - lowercase o, acute accent
    Array("ocirc",  "244"), //  - lowercase o, circumflex accent
    Array("otilde", "245"), //  - lowercase o, tilde
    Array("ouml",   "246"), //  - lowercase o, umlaut
    Array("divide", "247"), // division sign
    Array("oslash", "248"), //  - lowercase o, slash
    Array("ugrave", "249"), //  - lowercase u, grave accent
    Array("uacute", "250"), //  - lowercase u, acute accent
    Array("ucirc",  "251"), //  - lowercase u, circumflex accent
    Array("uuml",   "252"), //  - lowercase u, umlaut
    Array("yacute", "253"), //  - lowercase y, acute accent
    Array("thorn",  "254"), //  - lowercase thorn, Icelandic
    Array("yuml",   "255")  //  - lowercase y, umlaut
  )

  // http://www.w3.org/TR/REC-html40/sgml/entities.html
  // package scoped for testing
  final val HTML40_ARRAY = Array(
    // <!-- Latin Extended-B -->
    Array("fnof", "402"), // latin small f with hook = function= florin, U+0192 ISOtech -->
    // <!-- Greek -->
    Array("Alpha", "913"), // greek capital letter alpha, U+0391 -->
    Array("Beta", "914"), // greek capital letter beta, U+0392 -->
    Array("Gamma", "915"), // greek capital letter gamma,U+0393 ISOgrk3 -->
    Array("Delta", "916"), // greek capital letter delta,U+0394 ISOgrk3 -->
    Array("Epsilon", "917"), // greek capital letter epsilon, U+0395 -->
    Array("Zeta", "918"), // greek capital letter zeta, U+0396 -->
    Array("Eta", "919"), // greek capital letter eta, U+0397 -->
    Array("Theta", "920"), // greek capital letter theta,U+0398 ISOgrk3 -->
    Array("Iota", "921"), // greek capital letter iota, U+0399 -->
    Array("Kappa", "922"), // greek capital letter kappa, U+039A -->
    Array("Lambda", "923"), // greek capital letter lambda,U+039B ISOgrk3 -->
    Array("Mu", "924"), // greek capital letter mu, U+039C -->
    Array("Nu", "925"), // greek capital letter nu, U+039D -->
    Array("Xi", "926"), // greek capital letter xi, U+039E ISOgrk3 -->
    Array("Omicron", "927"), // greek capital letter omicron, U+039F -->
    Array("Pi", "928"), // greek capital letter pi, U+03A0 ISOgrk3 -->
    Array("Rho", "929"), // greek capital letter rho, U+03A1 -->
    // <!-- there is no Sigmaf, and no U+03A2 character either -->
    Array("Sigma", "931"), // greek capital letter sigma,U+03A3 ISOgrk3 -->
    Array("Tau", "932"), // greek capital letter tau, U+03A4 -->
    Array("Upsilon", "933"), // greek capital letter upsilon,U+03A5 ISOgrk3 -->
    Array("Phi", "934"), // greek capital letter phi,U+03A6 ISOgrk3 -->
    Array("Chi", "935"), // greek capital letter chi, U+03A7 -->
    Array("Psi", "936"), // greek capital letter psi,U+03A8 ISOgrk3 -->
    Array("Omega", "937"), // greek capital letter omega,U+03A9 ISOgrk3 -->
    Array("alpha", "945"), // greek small letter alpha,U+03B1 ISOgrk3 -->
    Array("beta", "946"), // greek small letter beta, U+03B2 ISOgrk3 -->
    Array("gamma", "947"), // greek small letter gamma,U+03B3 ISOgrk3 -->
    Array("delta", "948"), // greek small letter delta,U+03B4 ISOgrk3 -->
    Array("epsilon", "949"), // greek small letter epsilon,U+03B5 ISOgrk3 -->
    Array("zeta", "950"), // greek small letter zeta, U+03B6 ISOgrk3 -->
    Array("eta", "951"), // greek small letter eta, U+03B7 ISOgrk3 -->
    Array("theta", "952"), // greek small letter theta,U+03B8 ISOgrk3 -->
    Array("iota", "953"), // greek small letter iota, U+03B9 ISOgrk3 -->
    Array("kappa", "954"), // greek small letter kappa,U+03BA ISOgrk3 -->
    Array("lambda", "955"), // greek small letter lambda,U+03BB EntitiesISOgrk3 -->
    Array("mu", "956"), // greek small letter mu, U+03BC ISOgrk3 -->
    Array("nu", "957"), // greek small letter nu, U+03BD ISOgrk3 -->
    Array("xi", "958"), // greek small letter xi, U+03BE ISOgrk3 -->
    Array("omicron", "959"), // greek small letter omicron, U+03BF NEW -->
    Array("pi", "960"), // greek small letter pi, U+03C0 ISOgrk3 -->
    Array("rho", "961"), // greek small letter rho, U+03C1 ISOgrk3 -->
    Array("sigmaf",   "962"), // greek small letter final sigma,U+03C2 ISOgrk3 -->
    Array("sigma",    "963"), // greek small letter sigma,U+03C3 ISOgrk3 -->
    Array("tau",      "964"), // greek small letter tau, U+03C4 ISOgrk3 -->
    Array("upsilon",  "965"), // greek small letter upsilon,U+03C5 ISOgrk3 -->
    Array("phi",      "966"), // greek small letter phi, U+03C6 ISOgrk3 -->
    Array("chi",      "967"), // greek small letter chi, U+03C7 ISOgrk3 -->
    Array("psi",      "968"), // greek small letter psi, U+03C8 ISOgrk3 -->
    Array("omega",    "969"), // greek small letter omega,U+03C9 ISOgrk3 -->
    Array("thetasym", "977"), // greek small letter theta symbol,U+03D1 NEW -->
    Array("upsih",    "978"), // greek upsilon with hook symbol,U+03D2 NEW -->
    Array("piv",      "982"), // greek pi symbol, U+03D6 ISOgrk3 -->
    // <!-- General Punctuation -->
    Array("bull",    "8226"), // bullet = black small circle,U+2022 ISOpub -->
    // <!-- bullet is NOT the same as bullet operator, U+2219 -->
    Array("hellip",  "8230"), // horizontal ellipsis = three dot leader,U+2026 ISOpub -->
    Array("prime",   "8242"), // prime = minutes = feet, U+2032 ISOtech -->
    Array("Prime",   "8243"), // double prime = seconds = inches,U+2033 ISOtech -->
    Array("oline",   "8254"), // overline = spacing overscore,U+203E NEW -->
    Array("frasl",   "8260"), // fraction slash, U+2044 NEW -->
    // <!-- Letterlike Symbols -->
    Array("weierp",  "8472"), // script capital P = power set= Weierstrass p, U+2118 ISOamso -->
    Array("image",   "8465"), // blackletter capital I = imaginary part,U+2111 ISOamso -->
    Array("real",    "8476"), // blackletter capital R = real part symbol,U+211C ISOamso -->
    Array("trade",   "8482"), // trade mark sign, U+2122 ISOnum -->
    Array("alefsym", "8501"), // alef symbol = first transfinite cardinal,U+2135 NEW -->
    // <!-- alef symbol is NOT the same as hebrew letter alef,U+05D0 although the
    // same glyph could be used to depict both characters -->
    // <!-- Arrows -->
    Array("larr",   "8592"), // leftwards arrow, U+2190 ISOnum -->
    Array("uarr",   "8593"), // upwards arrow, U+2191 ISOnum-->
    Array("rarr",   "8594"), // rightwards arrow, U+2192 ISOnum -->
    Array("darr",   "8595"), // downwards arrow, U+2193 ISOnum -->
    Array("harr",   "8596"), // left right arrow, U+2194 ISOamsa -->
    Array("crarr",  "8629"), // downwards arrow with corner leftwards= carriage return, U+21B5 NEW -->
    Array("lArr",   "8656"), // leftwards double arrow, U+21D0 ISOtech -->
    // <!-- ISO 10646 does not say that lArr is the same as the 'is implied by'
    // arrow but also does not have any other character for that function.
    // So ? lArr canbe used for 'is implied by' as ISOtech suggests -->
    Array("uArr",   "8657"), // upwards double arrow, U+21D1 ISOamsa -->
    Array("rArr",   "8658"), // rightwards double arrow,U+21D2 ISOtech -->
    // <!-- ISO 10646 does not say this is the 'implies' character but does not
    // have another character with this function so ?rArr can be used for
    // 'implies' as ISOtech suggests -->
    Array("dArr",   "8659"), // downwards double arrow, U+21D3 ISOamsa -->
    Array("hArr",   "8660"), // left right double arrow,U+21D4 ISOamsa -->
    // <!-- Mathematical Operators -->
    Array("forall", "8704"), // for all, U+2200 ISOtech -->
    Array("part",   "8706"), // partial differential, U+2202 ISOtech -->
    Array("exist",  "8707"), // there exists, U+2203 ISOtech -->
    Array("empty",  "8709"), // empty set = null set = diameter,U+2205 ISOamso -->
    Array("nabla",  "8711"), // nabla = backward difference,U+2207 ISOtech -->
    Array("isin",   "8712"), // element of, U+2208 ISOtech -->
    Array("notin",  "8713"), // not an element of, U+2209 ISOtech -->
    Array("ni",     "8715"), // contains as member, U+220B ISOtech -->
    // <!-- should there be a more memorable name than 'ni'? -->
    Array("prod",   "8719"), // n-ary product = product sign,U+220F ISOamsb -->
    // <!-- prod is NOT the same character as U+03A0 'greek capital letter pi'
    // though the same glyph might be used for both -->
    Array("sum",    "8721"), // n-ary summation, U+2211 ISOamsb -->
    // <!-- sum is NOT the same character as U+03A3 'greek capital letter sigma'
    // though the same glyph might be used for both -->
    Array("minus",  "8722"), // minus sign, U+2212 ISOtech -->
    Array("lowast", "8727"), // asterisk operator, U+2217 ISOtech -->
    Array("radic",  "8730"), // square root = radical sign,U+221A ISOtech -->
    Array("prop",   "8733"), // proportional to, U+221D ISOtech -->
    Array("infin",  "8734"), // infinity, U+221E ISOtech -->
    Array("ang",    "8736"), // angle, U+2220 ISOamso -->
    Array("and",    "8743"), // logical and = wedge, U+2227 ISOtech -->
    Array("or",     "8744"), // logical or = vee, U+2228 ISOtech -->
    Array("cap",    "8745"), // intersection = cap, U+2229 ISOtech -->
    Array("cup",    "8746"), // union = cup, U+222A ISOtech -->
    Array("int",    "8747"), // integral, U+222B ISOtech -->
    Array("there4", "8756"), // therefore, U+2234 ISOtech -->
    Array("sim",    "8764"), // tilde operator = varies with = similar to,U+223C ISOtech -->
    // <!-- tilde operator is NOT the same character as the tilde, U+007E,although
    // the same glyph might be used to represent both -->
    Array("cong",   "8773"), // approximately equal to, U+2245 ISOtech -->
    Array("asymp",  "8776"), // almost equal to = asymptotic to,U+2248 ISOamsr -->
    Array("ne",     "8800"), // not equal to, U+2260 ISOtech -->
    Array("equiv",  "8801"), // identical to, U+2261 ISOtech -->
    Array("le",     "8804"), // less-than or equal to, U+2264 ISOtech -->
    Array("ge",     "8805"), // greater-than or equal to,U+2265 ISOtech -->
    Array("sub",    "8834"), // subset of, U+2282 ISOtech -->
    Array("sup",    "8835"), // superset of, U+2283 ISOtech -->
    // <!-- note that nsup, 'not a superset of, U+2283' is not covered by the
    // Symbol font encoding and is not included. Should it be, for symmetry?
    // It is in ISOamsn --> <!ENTITY nsub", "8836"},
    // not a subset of, U+2284 ISOamsn -->
    Array("sube",   "8838"), // subset of or equal to, U+2286 ISOtech -->
    Array("supe",   "8839"), // superset of or equal to,U+2287 ISOtech -->
    Array("oplus",  "8853"), // circled plus = direct sum,U+2295 ISOamsb -->
    Array("otimes", "8855"), // circled times = vector product,U+2297 ISOamsb -->
    Array("perp",   "8869"), // up tack = orthogonal to = perpendicular,U+22A5 ISOtech -->
    Array("sdot",   "8901"), // dot operator, U+22C5 ISOamsb -->
        // <!-- dot operator is NOT the same character as U+00B7 middle dot -->
        // <!-- Miscellaneous Technical -->
    Array("lceil",  "8968"), // left ceiling = apl upstile,U+2308 ISOamsc -->
    Array("rceil",  "8969"), // right ceiling, U+2309 ISOamsc -->
    Array("lfloor", "8970"), // left floor = apl downstile,U+230A ISOamsc -->
    Array("rfloor", "8971"), // right floor, U+230B ISOamsc -->
    Array("lang",   "9001"), // left-pointing angle bracket = bra,U+2329 ISOtech -->
        // <!-- lang is NOT the same character as U+003C 'less than' or U+2039 'single left-pointing angle quotation
        // mark' -->
    Array("rang",  "9002"), // right-pointing angle bracket = ket,U+232A ISOtech -->
        // <!-- rang is NOT the same character as U+003E 'greater than' or U+203A
        // 'single right-pointing angle quotation mark' -->
        // <!-- Geometric Shapes -->
    Array("loz",   "9674"), // lozenge, U+25CA ISOpub -->
        // <!-- Miscellaneous Symbols -->
    Array("spades", "9824"), // black spade suit, U+2660 ISOpub -->
        // <!-- black here seems to mean filled as opposed to hollow -->
    Array("clubs",  "9827"), // black club suit = shamrock,U+2663 ISOpub -->
    Array("hearts", "9829"), // black heart suit = valentine,U+2665 ISOpub -->
    Array("diams",  "9830"), // black diamond suit, U+2666 ISOpub -->

        // <!-- Latin Extended-A -->
    Array("OElig",   "338"), // -- latin capital ligature OE,U+0152 ISOlat2 -->
    Array("oelig",   "339"), // -- latin small ligature oe, U+0153 ISOlat2 -->
        // <!-- ligature is a misnomer, this is a separate character in some languages -->
    Array("Scaron",  "352"), // -- latin capital letter S with caron,U+0160 ISOlat2 -->
    Array("scaron",  "353"), // -- latin small letter s with caron,U+0161 ISOlat2 -->
    Array("Yuml",    "376"), // -- latin capital letter Y with diaeresis,U+0178 ISOlat2 -->
        // <!-- Spacing Modifier Letters -->
    Array("circ",    "710"), // -- modifier letter circumflex accent,U+02C6 ISOpub -->
    Array("tilde",   "732"), // small tilde, U+02DC ISOdia -->
        // <!-- General Punctuation -->
    Array("ensp",   "8194"), // en space, U+2002 ISOpub -->
    Array("emsp",   "8195"), // em space, U+2003 ISOpub -->
    Array("thinsp", "8201"), // thin space, U+2009 ISOpub -->
    Array("zwnj",   "8204"), // zero width non-joiner,U+200C NEW RFC 2070 -->
    Array("zwj",    "8205"), // zero width joiner, U+200D NEW RFC 2070 -->
    Array("lrm",    "8206"), // left-to-right mark, U+200E NEW RFC 2070 -->
    Array("rlm",    "8207"), // right-to-left mark, U+200F NEW RFC 2070 -->
    Array("ndash",  "8211"), // en dash, U+2013 ISOpub -->
    Array("mdash",  "8212"), // em dash, U+2014 ISOpub -->
    Array("lsquo",  "8216"), // left single quotation mark,U+2018 ISOnum -->
    Array("rsquo",  "8217"), // right single quotation mark,U+2019 ISOnum -->
    Array("sbquo",  "8218"), // single low-9 quotation mark, U+201A NEW -->
    Array("ldquo",  "8220"), // left double quotation mark,U+201C ISOnum -->
    Array("rdquo",  "8221"), // right double quotation mark,U+201D ISOnum -->
    Array("bdquo",  "8222"), // double low-9 quotation mark, U+201E NEW -->
    Array("dagger", "8224"), // dagger, U+2020 ISOpub -->
    Array("Dagger", "8225"), // double dagger, U+2021 ISOpub -->
    Array("permil", "8240"), // per mille sign, U+2030 ISOtech -->
    Array("lsaquo", "8249"), // single left-pointing angle quotation mark,U+2039 ISO proposed -->
    // <!-- lsaquo is proposed but not yet ISO standardized -->
    Array("rsaquo", "8250"), // single right-pointing angle quotation mark,U+203A ISO proposed -->
    // <!-- rsaquo is proposed but not yet ISO standardized -->
    Array("euro",   "8364")  // -- euro sign, U+20AC NEW -->
  )

  /**
   * <p>
   * The set of entities supported by standard XML.
   * </p>
   */
  final val XML: Entities = new Entities();
    {
      XML addEntities BASIC_ARRAY
      XML addEntities APOS_ARRAY
    }

  /**
   * <p>
   * The set of entities supported by HTML 3.2.
   * </p>
   */
  final val HTML32: Entities = new Entities();
    {
      HTML32 addEntities BASIC_ARRAY
      HTML32 addEntities ISO8859_1_ARRAY
    }

  /**
   * <p>
   * The set of entities supported by HTML 4.0.
   * </p>
   */
  final val HTML40: Entities = new Entities();
    {
      HTML40 addEntities BASIC_ARRAY
      HTML40 addEntities ISO8859_1_ARRAY
      HTML40 addEntities HTML40_ARRAY
   }

  trait EntityMap {
    /**
     * <p>
     * Add an entry to this entity map.
     * </p>
     * 
     * @param name
     *            the entity name
     * @param value
     *            the entity value
     */
    def add(name: String, value: Int)

    /**
     * <p>
     * Returns the name of the entity identified by the specified value.
     * </p>
     * 
     * @param value
     *            the value to locate
     * @return entity name associated with the specified value
     */
    def name(value: Int): String

    /**
     * <p>
     * Returns the value of the entity identified by the specified name.
     * </p>
     * 
     * @param name
     *            the name to locate
     * @return entity value associated with the specified name
     */
    def value(name: String): Int

  }

  class PrimitiveEntityMap extends EntityMap {
    private val mapNameToValue = new HashMap[String, Int]()
    private val mapValueToName = new HashMap[Int, String]()

    /**
     * {@inheritDoc}
     */
    def add(name: String, value: Int) {
      mapNameToValue += name -> value
      mapValueToName += value -> name
    }

    /**
     * {@inheritDoc}
     */
    def name(value: Int): String = mapValueToName(value)

    /**
     * {@inheritDoc}
     */
    def value(name: String): Int =
      mapNameToValue get name match {
        case None => -1
        case Some(value) => value
      }
  }

  abstract class MapIntMap extends Entities.EntityMap {
    protected var mapNameToValue: Map[String, Int] = _
    protected var mapValueToName: Map[Int, String] = _

    /**
     * {@inheritDoc}
     */
    def add(name: String, value: Int) {
      mapNameToValue += name -> value
      mapValueToName += value -> name
    }

    /**
     * {@inheritDoc}
     */
    def name(value: Int): String = mapValueToName(value)

    /**
     * {@inheritDoc}
     */
    def value(name: String): Int =
      mapNameToValue get name match {
        case None => -1
        case Some(value) => value
      }

  }

  class HashEntityMap extends MapIntMap {
    mapNameToValue = new HashMap[String, Int]()
    mapValueToName = new HashMap[Int, String]()
  }
/*
  class TreeEntityMap extends MapIntMap {
    mapNameToValue = new TreeMap[String, Int]()
    mapValueToName = new TreeMap[Int, String]()
  }
*/
  class LookupEntityMap extends PrimitiveEntityMap {
    private lazy val lookupTable = createLookupTable()
    private val LOOKUP_TABLE_SIZE = 256

    /**
     * {@inheritDoc}
     */
    override def name(value: Int): String =
      if (value < LOOKUP_TABLE_SIZE) lookupTable(value)
      else super.name(value)

    /**
     * <p>
     * Creates an entity lookup table of LOOKUP_TABLE_SIZE elements,
     * initialized with entity names.
     * </p>
     */
    private def createLookupTable(): Array[String] = {
      val a = new Array[String](LOOKUP_TABLE_SIZE)
      for (i <- 0 until LOOKUP_TABLE_SIZE) {
        a(i) = super.name(i)
      }
      a
    }

  }

  class ArrayEntityMap(protected val growBy: Int = 100) extends EntityMap {
    protected var size = 0
    protected var names = new Array[String](growBy)
    protected var values = new Array[Int](growBy)

    /**
     * {@inheritDoc}
     */
    def add(name: String, value: Int) {
      ensureCapacity(size + 1)
      names(size) = name
      values(size) = value
      size += 1
    }

    /**
     * Verifies the capacity of the entity array, adjusting the size if necessary.
     * 
     * @param capacity
     *            size the array should be
     */
    protected def ensureCapacity(capacity: Int) {
      if (capacity > names.length) {
        val newSize = math.max(capacity, size + growBy)
        val newNames = new Array[String](newSize)
        System.arraycopy(names, 0, newNames, 0, size)
        names = newNames
        val newValues = new Array[Int](newSize)
        System.arraycopy(values, 0, newValues, 0, size)
        values = newValues
      }
    }

    /**
     * {@inheritDoc}
     */
    def name(value: Int): String = {
      for (i <- 0 until size) {
        if (values(i) == value) {
          return names(i)
        }
      }
      null
    }

    /**
     * {@inheritDoc}
     */
    def value(name: String): Int = {
      for (i <- 0 until size) {
        if (names(i) equals name) {
         return values(i)
        }
      }
      -1
    }

  }

  class BinaryEntityMap(growBy: Int) extends ArrayEntityMap(growBy) {

    /**
     * Performs a binary search of the entity array for the specified key.
     * This method is based on code in {@link java.util.Arrays}.
     * 
     * @param key
     *            the key to be found
     * @return the index of the entity array matching the specified key
     */
    private def binarySearch(key: Int): Int = {
      var low = 0
      var high = size - 1

      while (low <= high) {
        val mid = (low + high) >>> 1
        val midVal = values(mid)

        if (midVal < key) {
          low = mid + 1
        } else if (midVal > key) {
          high = mid - 1
        } else {
          return mid // key found
        }
      }
      -(low + 1) // key not found.
    }

    /**
     * {@inheritDoc}
     */
    override def add(name: String, value: Int) {
      ensureCapacity(size + 1)
      var insertAt = binarySearch(value)
      if (insertAt > 0) {
        return; // note: this means you can't insert the same value twice
      }
      insertAt = -(insertAt + 1) // binarySearch returns it negative and off-by-one
      System.arraycopy(values, insertAt, values, insertAt + 1, size - insertAt)
      values(insertAt) = value
      System.arraycopy(names, insertAt, names, insertAt + 1, size - insertAt)
      names(insertAt) = name
      size += 1
    }

    /**
     * {@inheritDoc}
     */
    override def name(value: Int): String = {
      val index = binarySearch(value)
      if (index < 0) null else names(index)
    }

  }

}

class Entities {
  import Entities._  // companion object

  // package scoped for testing
  private val map: EntityMap = new LookupEntityMap()

  /**
   * <p>
   * Adds entities to this entity.
   * </p>
   * 
   * @param entityArray
   *            array of entities to be added
   */
  def addEntities(entityArray: Array[Array[String]]) {
    for (i <- 0 until entityArray.length) {
      addEntity(entityArray(i)(0), java.lang.Integer.parseInt(entityArray(i)(1)))
    }
  }

  /**
   * <p>
   * Add an entity to this entity.
   * </p>
   * 
   * @param name
   *            name of the entity
   * @param value
   *            vale of the entity
   */
  def addEntity(name: String, value: Int) {
    map.add(name, value)
  }

  /**
   * <p>import scala.collection.mutable.HashMap

   * Returns the name of the entity identified by the specified value.
   * </p>
   * 
   * @param value
   *            the value to locate
   * @return entity name associated with the specified value
   */
  def entityName(value: Int): String = map name value

  /**
   * <p>
   * Returns the value of the entity identified by the specified name.
   * </p>
   * 
   * @param name
   *            the name to locate
   * @return entity value associated with the specified name
   */
  def entityValue(name: String): Int = map value name

  /**
   * <p>
   * Escapes the characters in a <code>String</code>.
   * </p>
   * 
   * <p>
   * For example, if you have called addEntity(&quot;foo&quot;, 0xA1), escape(&quot;\u00A1&quot;) will return
   * &quot;&amp;foo;&quot;
   * </p>
   * 
   * @param str
   *            The <code>String</code> to escape.
   * @return A new escaped <code>String</code>.
   */
  def escape(str: String): String = {
    val stringWriter = createStringWriter(str)
    try {
      this.escape(stringWriter, str)
    } catch {
      case e: IOException =>
        // This should never happen because ALL the StringWriter methods
        // called by #escape(Writer, String) do not throw IOExceptions.
        throw new RuntimeException(e)
    }
    stringWriter.toString
  }

  /**
   * <p>
   * Escapes the characters in the <code>String</code> passed and writes the
   * result to the <code>Writer</code> passed.
   * </p>
   * 
   * @param writer
   *            The <code>Writer</code> to write the results of the escaping to.
   *            Assumed to be a non-null value.
   * @param str
   *            The <code>String</code> to escape. Assumed to be a non-null value.
   * @throws IOException
   *             when <code>Writer</code> passed throws the exception from
   *             calls to the {@link Writer#write(int)} methods.
   * 
   * @see #escape(String)
   * @see Writer
   */
  @throws(classOf[IOException])
  def escape(writer: Writer, str: String) {
    val len = str.length
    for (i <- 0 until len) {
      val c = str charAt i
      val entityName = this.entityName(c)
      if (entityName == null) {
        if (c > 0x7F) {
          writer write "&#"
          writer write java.lang.Integer.toString(c, 10)
          writer write ';'
        } else {
          writer write(c)
        }
      } else {
        writer write '&'
        writer write entityName
        writer write ';'
      }
    }
  }

  /**
   * <p>
   * Unescapes the entities in a <code>String</code>.
   * </p>
   * 
   * <p>
   * For example, if you have called addEntity(&quot;foo&quot;, 0xA1), unescape(&quot;&amp;foo;&quot;) will return
   * &quot;\u00A1&quot;
   * </p>
   * 
   * @param str
   *            The <code>String</code> to escape.
   * @return A new escaped <code>String</code>.
   */
  def unescape(str: String): String = {
    val firstAmp = str indexOf '&'
    if (firstAmp < 0) {
      str
    } else {
      val stringWriter = createStringWriter(str)
      try {
        this.doUnescape(stringWriter, str, firstAmp)
      } catch {
        case e: IOException =>
          // This should never happen because ALL the StringWriter methods
          // called by #escape(Writer, String) 
          // do not throw IOExceptions.
          throw new RuntimeException(e)
      }
      stringWriter.toString
    }
  }

  /**
   * Make the StringWriter 10% larger than the source String to avoid growing
   * the writer
   *
   * @param str The source string
   * @return A newly created StringWriter
   */
  private def createStringWriter(str: String): StringWriter =
    new StringWriter((str.length + str.length * 0.1).toInt)

  /**
   * <p>
   * Unescapes the escaped entities in the <code>String</code> passed and
   * writes the result to the <code>Writer</code> passed.
   * </p>
   * 
   * @param writer
   *            The <code>Writer</code> to write the results to; assumed to be non-null.
   * @param str
   *            The source <code>String</code> to unescape; assumed to be non-null.
   * @throws IOException
   *             when <code>Writer</code> passed throws the exception from
   *             calls to the {@link Writer#write(int)} methods.
   * 
   * @see #escape(String)
   * @see Writer
   */
  @throws(classOf[IOException])
  def unescape(writer: Writer, str: String) {
    val firstAmp = str indexOf '&'
    if (firstAmp < 0) {
      writer write str
    } else {
      doUnescape(writer, str, firstAmp)
    }
  }

  /**
   * Underlying unescape method that allows the optimisation of not starting
   * from the 0 index again.
   *
   * @param writer
   *            The <code>Writer</code> to write the results to; assumed to be non-null.
   * @param str
   *            The source <code>String</code> to unescape; assumed to be non-null.
   * @param firstAmp
   *            The <code>int</code> index of the first ampersand in the source String.
   * @throws IOException
   *             when <code>Writer</code> passed throws the exception from
   *             calls to the {@link Writer#write(int)} methods.
   */
  @throws(classOf[IOException])
  private def doUnescape(writer: Writer, str: String, firstAmp: Int) {
    writer.write(str, 0, firstAmp)
    val len = str.length
    var i = firstAmp
    while (i < len) {
      val c = str charAt i
      if (c == '&') {
        val nextIdx = i + 1
        var skip = false
        val semiColonIdx = str.indexOf(';', nextIdx)
        if (semiColonIdx == -1) {
           writer write c
           skip = true
        }
        if (!skip) {
          val amphersandIdx = str.indexOf('&', nextIdx)
          if (amphersandIdx != -1 && amphersandIdx < semiColonIdx) {
            // Then the text looks like &...&...;
            writer write c
            skip = true
          }
        }
        if (!skip) {
        val entityContent = str.substring(nextIdx, semiColonIdx)
        var entityValue = -1
        val entityContentLen = entityContent.length
        if (entityContentLen > 0) {
          if (entityContent.charAt(0) == '#') { // escaped value content is an integer (decimal or
            // hexidecimal)
            if (entityContentLen > 1) {
              val isHexChar = entityContent charAt 1
              try {
                isHexChar match {
                  case 'X' | 'x' =>
                    entityValue = java.lang.Integer.parseInt(entityContent.substring(2), 16)
                  case _ =>
                    entityValue = java.lang.Integer.parseInt(entityContent.substring(1), 10);
                }
                if (entityValue > 0xFFFF) {
                   entityValue = -1
                }
              } catch {
                case e: NumberFormatException =>
                  entityValue = -1
              }
            }
          } else { // escaped value content is an entity name
            entityValue = this.entityValue(entityContent)
          }
        }

        if (entityValue == -1) {
          writer write '&'
          writer write entityContent
          writer write ';'
        } else {
          writer write entityValue
        }
        i = semiColonIdx // move index up to the semicolon
        } //!skip
      } else {
        writer write c
      }
      i += 1
    } //while
  }

}
