//
// Copyright 2015-2022 New Zealand Institute of Language, Brain and Behaviour, 
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

package nzilbb.bas.test;
	      
import org.junit.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import nzilbb.bas.*;

/** Unit tests for LanguageTag utility class. */
public class TestLanguageTag {
  @Test public void PrimaryLanguages() {
    try {
      LanguageTag tagger = new LanguageTag();
	 
      assertEquals("3-letter", "eng", tagger.tag("eng"));
      assertEquals("3-letter wrong case", "eng", tagger.tag("ENG"));
      assertEquals("2-letter", "eng", tagger.tag("en"));
      assertEquals("2-letter wrong case", "eng", tagger.tag("EN"));
      assertEquals("name", "eng", tagger.tag("English"));
      assertEquals("name is case-insentitive", "eng", tagger.tag("ENGLISH"));
    } catch(Exception exception) {
      fail(exception.toString());
    }
  }

  @Test public void PrimaryLanguagesWithCountry() {
    try {
      LanguageTag tagger = new LanguageTag();
	 
      assertEquals("3-letter", "eng-NZ", tagger.tag("eng-NZ"));
      assertEquals("3-letter wrong case", "eng-NZ", tagger.tag("ENG-NZ"));
      assertEquals("2-letter", "eng-NZ", tagger.tag("en-NZ"));
      assertEquals("2-letter wrong case", "eng-NZ", tagger.tag("EN-NZ"));
      assertEquals("name", "eng-NZ", tagger.tag("English-NZ"));
      assertEquals("name is case-insentitive", "eng-NZ", tagger.tag("ENGLISH-NZ"));

      // TODO we could be more RFC 5646-compliant by validating script/region etc.
    } catch(Exception exception) {
      fail(exception.toString());
    }
  }

  @Test public void InvalidLanguages() {
    try {
      LanguageTag tagger = new LanguageTag();
	 
      assertEquals("Elvish", tagger.tag("Elvish"));
      assertEquals("eng-Hobbiton", tagger.tag("ENG-Hobbiton"));
      assertEquals("", tagger.tag(null));
    } catch(Exception exception) {
      fail(exception.toString());
    }
  }

  public static void main(String args[]) {
    org.junit.runner.JUnitCore.main("nzilbb.bas.TestLanguageTag");
  }
}
