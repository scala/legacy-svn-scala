/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.example.android.apis.os

/** Object that implements the text to morse code coversion */
object MorseCodeConverter {
  private final val SPEED_BASE = 100L
  final val DOT = SPEED_BASE
  final val DASH = SPEED_BASE * 3
  final val GAP = SPEED_BASE
  final val LETTER_GAP = SPEED_BASE * 3
  final val WORD_GAP = SPEED_BASE * 7

  /** The characters from 'A' to 'Z' */
  private final val LETTERS = Array(
    /* A */ Array( DOT, GAP, DASH ),
    /* B */ Array( DASH, GAP, DOT, GAP, DOT, GAP, DOT ),
    /* C */ Array( DASH, GAP, DOT, GAP, DASH, GAP, DOT ),
    /* D */ Array( DASH, GAP, DOT, GAP, DOT ),
    /* E */ Array( DOT ),
    /* F */ Array( DOT, GAP, DOT, GAP, DASH, GAP, DOT ),
    /* G */ Array( DASH, GAP, DASH, GAP, DOT ),
    /* H */ Array( DOT, GAP, DOT, GAP, DOT, GAP, DOT ),
    /* I */ Array( DOT, GAP, DOT ),
    /* J */ Array( DOT, GAP, DASH, GAP, DASH, GAP, DASH ),
    /* K */ Array( DASH, GAP, DOT, GAP, DASH ),
    /* L */ Array( DOT, GAP, DASH, GAP, DOT, GAP, DOT ),
    /* M */ Array( DASH, GAP, DASH ),
    /* N */ Array( DASH, GAP, DOT ),
    /* O */ Array( DASH, GAP, DASH, GAP, DASH ),
    /* P */ Array( DOT, GAP, DASH, GAP, DASH, GAP, DOT ),
    /* Q */ Array( DASH, GAP, DASH, GAP, DOT, GAP, DASH ),
    /* R */ Array( DOT, GAP, DASH, GAP, DOT ),
    /* S */ Array( DOT, GAP, DOT, GAP, DOT ),
    /* T */ Array( DASH ),
    /* U */ Array( DOT, GAP, DOT, GAP, DASH ),
    /* V */ Array( DOT, GAP, DOT, GAP, DASH ),
    /* W */ Array( DOT, GAP, DASH, GAP, DASH ),
    /* X */ Array( DASH, GAP, DOT, GAP, DOT, GAP, DASH ),
    /* Y */ Array( DASH, GAP, DOT, GAP, DASH, GAP, DASH ),
    /* Z */ Array( DASH, GAP, DASH, GAP, DOT, GAP, DOT )
  )

  /** The characters from '0' to '9' */
  private final val NUMBERS = Array(
    /* 0 */ Array( DASH, GAP, DASH, GAP, DASH, GAP, DASH, GAP, DASH ),
    /* 1 */ Array( DOT, GAP, DASH, GAP, DASH, GAP, DASH, GAP, DASH ),
    /* 2 */ Array( DOT, GAP, DOT, GAP, DASH, GAP, DASH, GAP, DASH ),
    /* 3 */ Array( DOT, GAP, DOT, GAP, DOT, GAP, DASH, GAP, DASH ),
    /* 4 */ Array( DOT, GAP, DOT, GAP, DOT, GAP, DOT, GAP, DASH ),
    /* 5 */ Array( DOT, GAP, DOT, GAP, DOT, GAP, DOT, GAP, DOT ),
    /* 6 */ Array( DASH, GAP, DOT, GAP, DOT, GAP, DOT, GAP, DOT ),
    /* 7 */ Array( DASH, GAP, DASH, GAP, DOT, GAP, DOT, GAP, DOT ),
    /* 8 */ Array( DASH, GAP, DASH, GAP, DASH, GAP, DOT, GAP, DOT ),
    /* 9 */ Array( DASH, GAP, DASH, GAP, DASH, GAP, DASH, GAP, DOT )
  )

  private final val ERROR_GAP = Array( GAP )

  /** Return the pattern data for a given character */
  def pattern(c: Char): Array[Long] =
    if (c >= 'A' && c <= 'Z')
      LETTERS(c - 'A')
    else if (c >= 'a' && c <= 'z')
      LETTERS(c - 'a')
    else if (c >= '0' && c <= '9')
      NUMBERS(c - '0')
    else
      ERROR_GAP

  def pattern(str: String): Array[Long] = {
    val strlen = str.length()

    // Calculate how long our array needs to be.
    var len = 1
    var lastWasWhitespace = true
    for (i <- 0 until strlen) {
      val c = str charAt i
      if (Character.isWhitespace(c)) {
        if (!lastWasWhitespace) {
          len += 1
          lastWasWhitespace = true
        }
      } else {
        if (!lastWasWhitespace) {
          len += 1
        }
        lastWasWhitespace = false
        len += pattern(c).length
      }
    }

    // Generate the pattern array.  Note that we put an extra element of 0
    // in at the beginning, because the pattern always starts with the pause,
    // not with the vibration.
    val result = new Array[Long](len+1)
    result(0) = 0
    var pos = 1
    lastWasWhitespace = true
    for (i <- 0 until strlen) {
      val c = str.charAt(i)
      if (Character.isWhitespace(c)) {
        if (!lastWasWhitespace) {
          result(pos) = WORD_GAP
          pos += 1
          lastWasWhitespace = true
        }
      } else {
        if (!lastWasWhitespace) {
          result(pos) = LETTER_GAP
          pos += 1
        }
        lastWasWhitespace = false
        val letter = pattern(c)
        System.arraycopy(letter, 0, result, pos, letter.length)
        pos += letter.length
      }
    }
    result
  }
}
