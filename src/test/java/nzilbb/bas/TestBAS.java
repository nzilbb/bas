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

package nzilbb.bas;
	      
import org.junit.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;

/** Unit tests for the BAS API class. */
public class TestBAS {
  @Test public void MAUSBasic() {
    try {
      BAS bas = new BAS();
	 
      File signal = new File(getDir(), "test.wav");
      File text = new File(getDir(), "test.txt");
	 
      BASResponse response = bas.MAUSBasic("eng-NZ", signal, text);
      System.out.println(response.getOutput());
      if (response.getWarnings() != null) System.out.println(response.getWarnings());
      assertEquals(true, response.getSuccess());
      assertNotNull(response.getDownloadLink());
      File result = new File(getDir(), "result.TextGrid");
      response.saveDownload(result);
      // we can't really validate the result, as it may change as the implementation changes
      result.delete();
    } catch(Exception exception) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      exception.printStackTrace(pw);
      try { sw.close(); }
      catch(IOException x) {}
      pw.close();	
      fail(exception.toString() + "\n" + sw);
    }
  }

  @Test public void G2P() {
    try {
      BAS bas = new BAS();
	 
      File text = new File(getDir(), "test.txt");
	 
      BASResponse response = bas.G2P(
        "eng", text, "txt", "maus-sampa", "extended", "bpf", true, true, true, false, "no");
      System.out.println(response.getOutput());
      if (response.getWarnings() != null) System.out.println(response.getWarnings());
      assertTrue(response.getSuccess());
      assertNotNull(response.getDownloadLink());
      File result = new File(getDir(), "result.txt");
      response.saveDownload(result);
      // we can't really validate the result, as it may change as the implementation changes
      result.delete();
    } catch(Exception exception) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      exception.printStackTrace(pw);
      try { sw.close(); }
      catch(IOException x) {}
      pw.close();	
      fail(exception.toString() + "\n" + sw);
    }
  }

  @Test public void MAUSWithNulls() {
    try {
      BAS bas = new BAS();
	 
      File wav = new File(getDir(), "test.wav");
      File bpf = new File(getDir(), "test.par");
	 
      BASResponse response = bas.MAUS("eng-NZ", wav, bpf, "emuDB", "ipa");
      System.out.println(response.getOutput());
      if (response.getWarnings() != null) System.out.println(response.getWarnings());
      assertEquals(true, response.getSuccess());
      assertNotNull(response.getDownloadLink());
      File result = new File(getDir(), "result.emuDB");
      response.saveDownload(result);
      // we can't really validate the result, as it may change as the implementation changes
      result.delete();
	 
    } catch(Exception exception) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      exception.printStackTrace(pw);
      try { sw.close(); }
      catch(IOException x) {}
      pw.close();	
      fail(exception.toString() + "\n" + sw);
    }
  }

  @Test public void Phon2Syl() {
    try {
      BAS bas = new BAS();
	 
      File bpf = new File(getDir(), "test.par");
	 
      BASResponse response = bas.Pho2Syl("eng-NZ", bpf, "KAN", true, "tg", 22050);
      System.out.println(response.getOutput());
      if (response.getWarnings() != null) System.out.println(response.getWarnings());
      assertEquals(true, response.getSuccess());
      assertNotNull(response.getDownloadLink());
      File result = new File(getDir(), "result.TextGrid");
      response.saveDownload(result);
      // we can't really validate the result, as it may change as the implementation changes
      result.delete();
	 
    } catch(Exception exception) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      exception.printStackTrace(pw);
      try { sw.close(); }
      catch(IOException x) {}
      pw.close();	
      fail(exception.toString() + "\n" + sw);
    }
  }

  /*TODO @Test*/ public void TTS() {
    try {
      BAS bas = new BAS();
	 
      BASResponse response = bas.TTS("Hallo Welt");
      System.out.println(response.getOutput());
      if (response.getWarnings() != null) System.out.println(response.getWarnings());
      assertTrue(response.getSuccess());
      assertNotNull(response.getDownloadLink());
      File result = new File(getDir(), "result.wav");
      response.saveDownload(result);
      // we can't really validate the result, as it may change as the implementation changes
      result.delete();
	 
    } catch(Exception exception) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      exception.printStackTrace(pw);
      try { sw.close(); }
      catch(IOException x) {}
      pw.close();	
      fail(exception.toString() + "\n" + sw);
    }
  }

  @Test public void TextAlign() {
    try {
      BAS bas = new BAS();

      File csv = new File(getDir(), "test.csv");
	 
      BASResponse response = bas.TextAlign(csv, "g2p_nze");
      System.out.println(response.getOutput());
      if (response.getWarnings() != null) System.out.println(response.getWarnings());
      assertEquals(true, response.getSuccess());
      assertNotNull(response.getDownloadLink());
      File result = new File(getDir(), "result.zip");
      response.saveDownload(result);
      // we can't really validate the result, as it may change as the implementation changes
      result.delete();
	 
    } catch(Exception exception) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      exception.printStackTrace(pw);
      try { sw.close(); }
      catch(IOException x) {}
      pw.close();	
      fail(exception.toString() + "\n" + sw);
    }
  }

  /**
   * Directory for text files.
   * @see #getDir()
   * @see #setDir(File)
   */
  protected File fDir;
  /**
   * Getter for {@link #fDir}: Directory for text files.
   * @return Directory for text files.
   */
  public File getDir() { 
    if (fDir == null) {
      try {
        URL urlThisClass = getClass().getResource(getClass().getSimpleName() + ".class");
        File fThisClass = new File(urlThisClass.toURI());
        fDir = fThisClass.getParentFile();
      } catch(Throwable t) {
        System.out.println("" + t);
      }
    }
    return fDir; 
  }
  
  /**
   * Setter for {@link #fDir}: Directory for text files.
   * @param fNewDir Directory for text files.
   */
  public void setDir(File fNewDir) { fDir = fNewDir; }

  public static void main(String args[]) {
    org.junit.runner.JUnitCore.main("nzilbb.bas.TestBAS");
  }
}
