//
// Copyright 2016-2022 New Zealand Institute of Language, Brain and Behaviour, 
// University of Canterbury
// Written by Robert Fromont - robert.fromont@canterbury.ac.nz
//
//    This file is part of nzilbb.bas.
//
//    nzilbb.bas is free software; you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation; either version 3 of the License, or
//    (at your option) any later version.
//
//    nzilbb.bas is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with nzilbb.bas; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package nzilbb.bas;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Normalizes languages to produce a language tag that BAS will like.
 * <p> This is ostensibly a <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag
 * for identifying the language, but actually the service appears to accept only 3-letter 
 * (ISO639-2) not 2-letter (ISO639-1) language codes.
 * <p> For example, assuming that <code>tagger = new LanguageTag();</code>
 * <ul>
 *  <li> <code>tagger.tag("eng")</code> returns <tt>"eng"</tt> </li>
 *  <li> <code>tagger.tag("ENG")</code> returns <tt>"eng"</tt> </li>
 *  <li> <code>tagger.tag("en")</code> returns <tt>"eng"</tt> </li>
 *  <li> <code>tagger.tag("en-NZ")</code> returns <tt>"eng-NZ"</tt> </li>
 *  <li> <code>tagger.tag("eng")</code> returns <tt>"eng"</tt> </li>
 * </ul>
 * @author Robert Fromont robert.fromont@canterbury.ac.nz
 */
public class LanguageTag {
  // Attributes:

  /**
   * A map of ISO639-2 (3-letter) codes to language names.
   * @see #getIso639Map2ToName()
   */
  protected Properties iso639Map2ToName = new Properties();
  /**
   * Getter for {@link #iso639Map2ToName}: A map of ISO639-2 (3-letter) codes to language names.
   * @return A map of ISO639-2 (3-letter) codes to language names.
   */
  public Properties getIso639Map2ToName() { return iso639Map2ToName; }

  /**
   * A map of language names to ISO639-2 (3-letter) codes. Names are all lowercase and in English.
   * @see #getIso639MapNameTo2()
   */
  protected Properties iso639MapNameTo2 = new Properties();
  /**
   * Getter for {@link #iso639MapNameTo2}: A map of language names to ISO639-2 (3-letter) codes.
   * @return A map of language names to ISO639-2 (3-letter) codes. Names are all lowercase
   * and in English. 
   */
   
  /**
   * A map of ISO639-1 (2-letter) to ISO639-2 (3-letter) codes.
   * @see #getIso639Map1to2()
   */
  protected Properties iso639Map1To2 = new Properties();
  /**
   * Getter for {@link #iso639Map1To2}: A map of ISO639-1 (2-letter) to ISO639-2 (3-letter) codes.
   * @return A map of ISO639-1 (2-letter) to ISO639-2 (3-letter) codes.
   */
  public Properties getIso639Map1to2() { return iso639Map1To2; }

  /**
   * A map of ISO639-2 (3-letter) to ISO639-1 (2-letter) codes.
   * @see #getIso639Map1To2()
   */
  protected Properties iso639Map2To1 = new Properties();
  /**
   * Getter for {@link #iso639Map2To1}: A map of ISO639-2 (3-letter) to ISO639-1 (2-letter) codes.
   * @return A map of ISO639-2 (3-letter) to ISO639-1 (2-letter) codes.
   */
  public Properties getIso639Map1To2() { return iso639Map2To1; }
   
  // Methods:
   
  /**
   * Default constructor.
   * @throws IOException If the ISO 639 resources could be loaded.
   */
  public LanguageTag() throws IOException {
    try {
      InputStream in = getClass().getResource("ISO639-2.txt").openStream();
      iso639Map2ToName.load(in);
      in.close();
	 
      // build name lookup table
      for (Object oCode : iso639Map2ToName.keySet()) {
        String name = iso639Map2ToName.getProperty(oCode.toString());
        iso639MapNameTo2.setProperty(name.toLowerCase(), oCode.toString());
      } // next key
	 
      in = getClass().getResource("ISO639-2-to-1.txt").openStream();
      iso639Map2To1.load(in);
      in.close();
	 
      // build 2-letter code lookup table
      for (Object oCode : iso639Map2To1.keySet()) {
        String name = iso639Map2To1.getProperty(oCode.toString());
        iso639Map1To2.setProperty(name.toLowerCase(), oCode.toString());
      } // next key
	 
    } catch(NullPointerException exception) {
      throw new IOException("ISO639 resource file not found");
    }
  } // end of constructor

  /**
   * Returns the given tag, but converted to an RFC 5646 code with a three-letter primary
   * language code, if possible. 
   * @param language A language identifiter, which may be an RFC 5646 code, or a language name.
   * @return An RFC 5646 code with a three-letter primary language code, or the given tag
   * if normalization was not possible. 
   */
  public String tag(String language) {
    if (language == null) return "";
    String suffix = "";
    String code = language;
    int hyphen = language.indexOf('-');
    if (hyphen >= 0) {
      suffix = language.substring(hyphen);
      code = language.substring(0, hyphen);
    }
    if (!iso639Map2ToName.containsKey(code)) { // not a three-letter code
      // try for non-lowercase
      if (iso639Map2ToName.containsKey(code.toLowerCase()))
      {
        code = code.toLowerCase();
      }
      // try for a two-letter code
      if (iso639Map1To2.containsKey(code.toLowerCase()))
      {
        code = iso639Map1To2.getProperty(code.toLowerCase());
      }
      // try for a language name
      else if (iso639MapNameTo2.containsKey(code.toLowerCase()))
      {
        code = iso639MapNameTo2.getProperty(code.toLowerCase());
      }
    }
    return code + suffix;
  } // end of tag()

} // end of class LanguageTag
