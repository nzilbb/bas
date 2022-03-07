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

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

/**
 * Class exposing the BAS web services API for various speech processing and annotation tasks.
 * <p> <a href="https://clarin.phonetik.uni-muenchen.de/BASWebServices/#/services">
 *      https://clarin.phonetik.uni-muenchen.de/BASWebServices/#/services</a> 
 * <p> For service discovery, links are like 
 *  <a href="http://clarin.phonetik.uni-muenchen.de/BASWebServices/BAS_Webservices.cmdi.xml">
 *   http://clarin.phonetik.uni-muenchen.de/BASWebServices/BAS_Webservices.cmdi.xml</a>
 * <p> The services supported here are:
 * <ul>
 *  <li><em>G2P</em> for converting orthographic transcript into phonemic transcription </li>
 *  <li><em>MAUS</em> for forced alignment given a WAV file and a phonemic transcription </li>
 *  <li><em>MAUSBasic</em> combines G2P and MAUS for forced alignment given a WAV file and
 *      a plain text orthrogaphic transcript </li> 
 *  <li><em>Pho2Syl</em> adding syllabification to phonemic transcriptions </li>
 *  <li><em>TTS</em> for transforming a transcript of German text into an audio file
 *      (Text-to-Speech) </li> 
 *  <li><em>TextAlign</em> for aligning two representations of text, e.g. letters in
 *       orthographic transcript with phonemes in a phonemic transcription. </li> 
 * </ul>
 * <p> To use the API, the code is something like this:
 * <pre>
 * BAS bas = new BAS();
 * {@link BASResponse} response = bas.MAUSBasic(
 *   "eng-NZ", new File("recording.wav"), new File("transcript.txt"));
 * if (response.getSuccess()) {
 *   response.saveDownload(new File("Praat.TextGrid"));
 * } else {
 *   System.out.println(response.getWarnings());
 * }
 * </pre>
 * <p> Input files can be supplied using an InputStream or a File.  In some cases, a
 * String can also be used as input. 
 * @author Robert Fromont robert@fromont.net.nz
 */
public class BAS {
  // Attributes:
   
  /**
   * Version of the BAS services this API is designed for.
   * @return Version of the BAS services this API is designed for.
   */
  public String getVersion() { return "2.10"; }

  /**
   * URL for the MAUSBasic service - default: 
   * https://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runMAUSBasic
   * @see #getMAUSBasicUrl()
   * @see #setMAUSBasicUrl(String)
   */
  protected String MAUSBasicUrl = "https://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runMAUSBasic";
  /**
   * Getter for {@link #MAUSBasicUrl}: URL for the MAUSBasic service - default: 
   * https://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runMAUSBasic
   * @return URL for the MAUSBasic service.
   */
  public String getMAUSBasicUrl() { return MAUSBasicUrl; }
  /**
   * Setter for {@link #MAUSBasicUrl}: URL for the MAUSBasic service.
   * @param newMAUSBasicUrl URL for the MAUSBasic service.
   */
  public void setMAUSBasicUrl(String newMAUSBasicUrl) { MAUSBasicUrl = newMAUSBasicUrl; }
   
  /**
   * URL for the G2P service - default: 
   * http://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runG2P
   * @see #getG2PUrl()
   * @see #setG2PUrl(String)
   */
  protected String G2PUrl = "https://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runG2P";
  /**
   * Getter for {@link #G2PUrl}: URL for the G2P service - default: 
   * http://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runG2P
   * @return URL for the G2P service.
   */
  public String getG2PUrl() { return G2PUrl; }
  /**
   * Setter for {@link #G2PUrl}: URL for the G2P service.
   * @param newG2PUrl URL for the G2P service.
   */
  public void setG2PUrl(String newG2PUrl) { G2PUrl = newG2PUrl; }

  /**
   * URL for the MAUS service - default: 
   * https://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runMAUS
   * @see #getMAUSUrl()
   * @see #setMAUSUrl(String)
   */
  protected String MAUSUrl = "https://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runMAUS";
  /**
   * Getter for {@link #MAUSUrl}: URL for the MAUS service - default: 
   * https://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runMAUS
   * @return URL for the MAUS service.
   */
  public String getMAUSUrl() { return MAUSUrl; }
  /**
   * Setter for {@link #MAUSUrl}: URL for the MAUS service.
   * @param newMAUSUrl URL for the MAUS service.
   */
  public void setMAUSUrl(String newMAUSUrl) { MAUSUrl = newMAUSUrl; }

  /**
   * URL from the Pho2Syl service - default: 
   * http://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runPho2Syl
   * @see #getPho2SylUrl()
   * @see #setPho2SylUrl(String)
   */
  protected String Pho2SylUrl = "http://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runPho2Syl";
  /**
   * Getter for {@link #Pho2SylUrl}: URL from the Pho2Syl service - default: 
   * http://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runPho2Syl
   * @return URL from the Pho2Syl service.
   */
  public String getPho2SylUrl() { return Pho2SylUrl; }
  /**
   * Setter for {@link #Pho2SylUrl}: URL from the Pho2Syl service.
   * @param newPho2SylUrl URL from the Pho2Syl service.
   */
  public void setPho2SylUrl(String newPho2SylUrl) { Pho2SylUrl = newPho2SylUrl; }

  /**
   * URL for MaryTTS service - default: 
   * https://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runTTSFile
   * @see #getTTSUrl()
   * @see #setTTSUrl(String)
   */
  protected String TTSUrl = "https://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runTTSFile";
  /**
   * Getter for {@link #TTSUrl}: URL for MaryTTS service - default: 
   * https://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runTTSFile
   * @return URL for MaryTTS service.
   */
  public String getTTSUrl() { return TTSUrl; }
  /**
   * Setter for {@link #TTSUrl}: URL for MaryTTS service.
   * @param newTTSUrl URL for MaryTTS service.
   */
  public void setTTSUrl(String newTTSUrl) { TTSUrl = newTTSUrl; }

  /**
   * URL for the TextAlign service - default: 
   * http://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runTextAlign
   * @see #getTextAlignUrl()
   * @see #setTextAlignUrl(String)
   */
  protected String TextAlignUrl = "http://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runTextAlign";
  /**
   * Getter for {@link #TextAlignUrl}: URL for the TextAlign service - default: 
   * http://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runTextAlign
   * @return URL for the TextAlign service.
   */
  public String getTextAlignUrl() { return TextAlignUrl; }
  /**
   * Setter for {@link #TextAlignUrl}: URL for the TextAlign service.
   * @param newTextAlignUrl URL for the TextAlign service.
   */
  public void setTextAlignUrl(String newTextAlignUrl) { TextAlignUrl = newTextAlignUrl; }

  private CloseableHttpClient httpclient;
  private LanguageTag languageTagger = new LanguageTag();
   
  // Methods:
   
  /**
   * Default constructor.
   * @throws IOException If the ISO 639 resources could be loaded.
   */
  public BAS() throws IOException {
    httpclient = HttpClients.createDefault();
    languageTagger = new LanguageTag();
  } // end of constructor

  /**
   * Invokes the MAUSBasic service, which combines G2P and MAUS for forced alignment given
   * a WAV file and a plain text orthrogaphic transcript. 
   * @param LANGUAGE <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for
   * identifying the language. 
   * @param SIGNAL The signal, in WAV format.
   * @param TEXT The transcription of the text.
   * @return The result of the call.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could
   * not be configured. 
   */
  public BASResponse MAUSBasic(String LANGUAGE, File SIGNAL, File TEXT)
    throws IOException, ParserConfigurationException {
    return MAUSBasic(LANGUAGE, new FileInputStream(SIGNAL), new FileInputStream(TEXT));
  }
  
  /**
   * Invokes the MAUSBasic service, which combines G2P and MAUS for forced alignment given
   * a WAV file and a plain text orthrogaphic transcript. 
   * @param LANGUAGE <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for
   * identifying the language. 
   * @param SIGNAL The signal, in WAV format.
   * @param TEXT The transcription of the text.
   * @return The result of the call.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could
   * not be configured. 
   */
  public BASResponse MAUSBasic(String LANGUAGE, InputStream SIGNAL, InputStream TEXT)
    throws IOException, ParserConfigurationException {
    HttpPost request = new HttpPost(getMAUSBasicUrl());	       
    HttpEntity entity = MultipartEntityBuilder
      .create()
      .addTextBody("LANGUAGE", languageTagger.tag(LANGUAGE))
      .addBinaryBody("SIGNAL", SIGNAL, ContentType.create("audio/wav"), "BAS.wav")
      .addBinaryBody("TEXT", TEXT, ContentType.create("text/plain"), "BAS.txt")
      .build();
    request.setEntity(entity);
    HttpResponse httpResponse = httpclient.execute(request);
    HttpEntity result = httpResponse.getEntity();
    return new BASResponse(result.getContent());
  } // end of MAUSBasic()
   
  /**
   * Invokes the G2P service for converting orthography into phonemic transcription.
   * <p> This convenience method takes a String as the text, and assumes <var>iform</var> = "txt",
   * use {@link #G2P(String,InputStream,String,String,int,String,String,String,boolean,boolean,boolean,boolean,String)} 
   * for full set of options.
   * @param lng <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for
   * identifying the language. 
   * @param txt The text to transform as a String.
   * @param outsym Ouput phoneme symbol inventory:
   *  <ul>
   *   <li> "sampa" - language-specific SAMPA variant is the default. </li> 
   *   <li> "x-sampa" - language independent X-SAMPA and IPA can be chosen. </li> 
   *   <li> "maus-sampa" - maps the output to a language-specific phoneme subset that
   *        WEBMAUS can process. </li>  
   *   <li> "ipa" - Unicode-encoded IPA. </li> 
   *   <li> "arpabet" - supported for eng-US only </li>
   * </ul>
   * @param featset - Feature set used for grapheme-phoneme conversion. 
   *  <ul>
   *   <li> "standard" comprises a letter window centered on the grapheme to be converted. </li> 
   *   <li> "extended" set additionally includes part of speech and morphological analyses. </li>
   *  </ul>
   * @param oform Output format:
   *  <ul>
   *   <li> "bpf" indicates the BAS Partitur Format (BPF) file with a KAN tier. </li> 
   *   <li> "bpfs" differs from "bpf" only in that respect, that the phonemes are
   *        separated by blanks. In case of TextGrid input, both "bpf" and "bpfs" require
   *        the additional parameters "tgrate" and "tgitem". The content of the TextGrid
   *        tier "tgitem" is stored as a word chunk segmentation in the partiture tier TRN. </li>  
   *   <li> "txt" indicates a replacement of the input words by their transcriptions;
   *        single line output without punctuation, where phonemes are separated by blanks
   *        and words by tabulators. </li>  
   *   <li> "tab" returns the grapheme phoneme conversion result in form of a table with
   *        two columns. The first column comprises the words, the second column their
   *        blank-separated transcriptions. </li>   
   *   <li> "exttab" results in a 5-column table. The columns contain from left to right:
   *        words, transcriptions, part of speech, morpheme segmentations, and morpheme
   *        class segmentations. </li>  
   *   <li> "lex" transforms the table to a lexicon, i.e. words are unique and sorted. </li> 
   *   <li> "extlex" provides the same information as "exttab" in a unique and sorted
   *        manner. For all lex and tab outputs columns are separated by ';'. </li>  
   *   <li> "exttcf" which is currently available for German and English only additionally
   *        adds part of speech (STTS tagset), morphs, and morph classes. </li> 
   *   <li> With "tg" and "exttg" TextGrid output is produced. </li>
   *  </ul>
   * @param syl whether or not word stress is to be added to the output transcription. 
   * @param stress whether or not the output transcription is to be syllabified. 
   * @return The result of this call.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could
   * not be configured. 
   */
  public BASResponse G2P(
    String lng, String txt, String outsym, String featset, String oform, boolean syl,
    boolean stress)
    throws IOException, ParserConfigurationException {
    return G2P(lng, new ByteArrayInputStream(txt.getBytes("UTF-8")), "txt", "", 16000, outsym, featset, oform, syl, stress, true, false, "no");
  }
  
  /**
   * Invokes the G2P service for converting orthography into phonemic transcription.
   * <p>This method cannot have <var>iform</var> set to "tg", use 
   * {@link #G2P(String,InputStream,String,String,int,String,String,String,boolean,boolean,boolean,boolean,String)}
   *  for full set of options.
   * @param lng <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for
   * identifying the language. 
   * @param i The text to transform.
   * @param iform The format of <var>i</var> -
   * <ul>
   *  <li> "txt" indicates connected text input, which will be tokenized before the
   *       conversion. </li>  
   *  <li> "list" indicates a sequence of unconnected words, that does not need to be tokenized. Furthermore, "list" requires a different part-of-speech tagging strategy than "txt" for the extraction of the "extended" feature set (see Parameter <var>featset</var>). </li> 
   *  <li> "tcf" indicates, that the input format is TCF containing at least a tokenization dominated by the element "tokens". </li> 
   *  <li> Input format "bpf" indicates BAS partitur file input containing an ORT tier to be transcribed. </li>
   * </ul>
   * @param outsym Ouput phoneme symbol inventory:
   *  <ul>
   *   <li> "sampa" - language-specific SAMPA variant is the default. </li> 
   *   <li> "x-sampa" - language independent X-SAMPA and IPA can be chosen. </li> 
   *   <li> "maus-sampa" - maps the output to a language-specific phoneme subset that
   *        WEBMAUS can process. </li>  
   *   <li> "ipa" - Unicode-encoded IPA. </li> 
   *   <li> "arpabet" - supported for eng-US only </li>
   * </ul>
   * @param featset - Feature set used for grapheme-phoneme conversion. 
   *  <ul>
   *   <li> "standard" comprises a letter window centered on the grapheme to be converted. </li> 
   *   <li> "extended" set additionally includes part of speech and morphological analyses. </li>
   *  </ul>
   * @param oform Output format:
   *  <ul>
   *   <li> "bpf" indicates the BAS Partitur Format (BPF) file with a KAN tier. </li> 
   *   <li> "bpfs" differs from "bpf" only in that respect, that the phonemes are
   *        separated by blanks. In case of TextGrid input, both "bpf" and "bpfs" require the
   *        additional parameters "tgrate" and "tgitem". The content of the TextGrid tier
   *        "tgitem" is stored as a word chunk segmentation in the partiture tier TRN. </li>  
   *   <li> "txt" indicates a replacement of the input words by their transcriptions;
   *        single line output without punctuation, where phonemes are separated by blanks and
   *        words by tabulators. </li>  
   *   <li> "tab" returns the grapheme phoneme conversion result in form of a table with
   *        two columns. The first column comprises the words, the second column their
   *        blank-separated transcriptions. </li>  
   *   <li> "exttab" results in a 5-column table. The columns contain from left to right:
   *        words, transcriptions, part of speech, morpheme segmentations, and morpheme class
   *        segmentations. </li>  
   *   <li> "lex" transforms the table to a lexicon, i.e. words are unique and sorted. </li> 
   *   <li> "extlex" provides the same information as "exttab" in a unique and sorted
   *        manner. For all lex and tab outputs columns are separated by ';'. </li>  
   *   <li> "exttcf" which is currently available for German and English only additionally
   *        adds part of speech (STTS tagset), morphs, and morph classes. </li> 
   *   <li> With "tg" and "exttg" TextGrid output is produced. </li>
   *  </ul>
   * @param syl whether or not word stress is to be added to the output transcription. 
   * @param stress whether or not the output transcription is to be syllabified. 
   * @param nrm Detects and expands 22 non-standard word types.
   * @param com whether &lt;*&gt; strings should be treated as annotation markers. If
   * true, then strings of this type are considered as annotation markers that are not
   * processed but passed on to the output. 
   * @param align "yes", "no", or "sym" decision whether or not the transcription is to be
   * letter-aligned. Syllable boundaries and word stress are not part of the output of
   * this 'sym' alignment. 
   * @return The result of this call.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could not be configured.
   */
  public BASResponse G2P(
    String lng, File i, String iform, String outsym, String featset, String oform, boolean syl,
    boolean stress, boolean nrm, boolean com, String align)
    throws IOException, ParserConfigurationException {
    return G2P(lng, new FileInputStream(i), iform, "", 16000, outsym, featset, oform, syl, stress, nrm, com, align);
  }
  
  /**
   * Invokes the G2P service for converting orthography into phonemic transcription.
   * <p>This method cannot have <var>iform</var> set to "tg", use
   *  {@link #G2P(String,InputStream,String,String,int,String,String,String,boolean,boolean,boolean,boolean,String)}
   * for full set of options.
   * @param lng <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for
   * identifying the language. 
   * @param i The text to transform.
   * @param iform The format of <var>i</var> -
   * <ul>
   *  <li> "txt" indicates connected text input, which will be tokenized before the
   *       conversion. </li>  
   *  <li> "list" indicates a sequence of unconnected words, that does not need to be
   *       tokenized. Furthermore, "list" requires a different part-of-speech tagging strategy
   *       than "txt" for the extraction of the "extended" feature set (see Parameter
   * <var>featset</var>). </li>  
   *  <li> "tcf" indicates, that the input format is TCF containing at least a
   *       tokenization dominated by the element "tokens". </li>  
   *  <li> Input format "bpf" indicates BAS partitur file input containing an ORT tier to
   *       be transcribed. </li> 
   * </ul>
   * @param outsym Ouput phoneme symbol inventory:
   *  <ul>
   *   <li> "sampa" - language-specific SAMPA variant is the default. </li> 
   *   <li> "x-sampa" - language independent X-SAMPA and IPA can be chosen. </li> 
   *   <li> "maus-sampa" - maps the output to a language-specific phoneme subset that
   *        WEBMAUS can process. </li>  
   *   <li> "ipa" - Unicode-encoded IPA. </li> 
   *   <li> "arpabet" - supported for eng-US only </li>
   * </ul>
   * @param featset - Feature set used for grapheme-phoneme conversion. 
   *  <ul>
   *   <li> "standard" comprises a letter window centered on the grapheme to be converted. </li> 
   *   <li> "extended" set additionally includes part of speech and morphological analyses. </li>
   *  </ul>
   * @param oform Output format:
   *  <ul>
   *   <li> "bpf" indicates the BAS Partitur Format (BPF) file with a KAN tier. </li> 
   *   <li> "bpfs" differs from "bpf" only in that respect, that the phonemes are
   *        separated by blanks. In case of TextGrid input, both "bpf" and "bpfs" require the
   *        additional parameters "tgrate" and "tgitem". The content of the TextGrid tier
   *        "tgitem" is stored as a word chunk segmentation in the partiture tier TRN. </li>  
   *   <li> "txt" indicates a replacement of the input words by their transcriptions;
   *        single line output without punctuation, where phonemes are separated by blanks and
   *        words by tabulators. </li>  
   *   <li> "tab" returns the grapheme phoneme conversion result in form of a table with
   *        two columns. The first column comprises the words, the second column their
   *        blank-separated transcriptions. </li>  
   *   <li> "exttab" results in a 5-column table. The columns contain from left to right:
   *        words, transcriptions, part of speech, morpheme segmentations, and morpheme class
   *        segmentations. </li>  
   *   <li> "lex" transforms the table to a lexicon, i.e. words are unique and sorted. </li> 
   *   <li> "extlex" provides the same information as "exttab" in a unique and sorted
   *        manner. For all lex and tab outputs columns are separated by ';'. </li>  
   *   <li> "exttcf" which is currently available for German and English only additionally
   *        adds part of speech (STTS tagset), morphs, and morph classes. </li> 
   *   <li> With "tg" and "exttg" TextGrid output is produced. </li>
   *  </ul>
   * @param syl whether or not word stress is to be added to the output transcription. 
   * @param stress whether or not the output transcription is to be syllabified. 
   * @param nrm Detects and expands 22 non-standard word types.
   * @param com whether &lt;*&gt; strings should be treated as annotation markers. If
   * true, then strings of this type are considered as annotation markers that are not
   * processed but passed on to the output. 
   * @param align "yes", "no", or "sym" decision whether or not the transcription is to be
   * letter-aligned. Syllable boundaries and word stress are not part of the output of
   * this 'sym' alignment. 
   * @return The result of this call.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could not be configured.
   */
  public BASResponse G2P(
    String lng, InputStream i, String iform, String outsym, String featset, String oform,
    boolean syl, boolean stress, boolean nrm, boolean com, String align)
    throws IOException, ParserConfigurationException {
    return G2P(lng, i, iform, "", 16000, outsym, featset, oform, syl, stress, nrm, com, align);
  }
  
  /**
   * Invokes the G2P service for converting orthography into phonemic transcription.
   * @param lng <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for
   * identifying the language. 
   * @param i The text to transform.
   * @param iform The format of <var>i</var> -
   * <ul>
   *  <li> "txt" indicates connected text input, which will be tokenized before the
   *       conversion. </li>  
   *  <li> "list" indicates a sequence of unconnected words, that does not need to be
   *       tokenized. Furthermore, "list" requires a different part-of-speech tagging strategy
   *       than "txt" for the extraction of the "extended" feature set (see Parameter
   * <var>featset</var>). </li>  
   *  <li> "tg" indicates TextGrid input. Long and short format is supported. For TextGrid
   *       input additionally the name of the item containing the words to be transcribed is to
   *       be specified by the parameter "tgname". In combination with "bpf" output format "tg"
   *       input additionally requires the specification of the sample rate by the parameter
   *       "tgrate". </li> 
   *  <li> "tcf" indicates, that the input format is TCF containing at least a
   *       tokenization dominated by the element "tokens". </li>  
   *  <li> Input format "bpf" indicates BAS partitur file input containing an ORT tier to
   *       be transcribed. </li> 
   * </ul>
   * @param tgitem Only needed, if <var>iform</var> is "tg". Name of the TextGrid item,
   * that contains the words to be transcribed. In case of TextGrid output, this item is
   * the reference for the added items.  
   * @param tgrate Only needed, if <var>iform</var> is "tg" and oform is "bpf(s)". Sample
   * rate to convert time values from TextGrid to sample values in BAS partiture file.  
   * @param outsym Ouput phoneme symbol inventory:
   *  <ul>
   *   <li> "sampa" - language-specific SAMPA variant is the default. </li> 
   *   <li> "x-sampa" - language independent X-SAMPA and IPA can be chosen. </li> 
   *   <li> "maus-sampa" - maps the output to a language-specific phoneme subset that
   *        WEBMAUS can process. </li>  
   *   <li> "ipa" - Unicode-encoded IPA. </li> 
   *   <li> "arpabet" - supported for eng-US only </li>
   * </ul>
   * @param featset - Feature set used for grapheme-phoneme conversion. 
   *  <ul>
   *   <li> "standard" comprises a letter window centered on the grapheme to be converted. </li> 
   *   <li> "extended" set additionally includes part of speech and morphological analyses. </li>
   *  </ul>
   * @param oform Output format:
   *  <ul>
   *   <li> "bpf" indicates the BAS Partitur Format (BPF) file with a KAN tier. </li> 
   *   <li> "bpfs" differs from "bpf" only in that respect, that the phonemes are
   *        separated by blanks. In case of TextGrid input, both "bpf" and "bpfs" require the
   *        additional parameters "tgrate" and "tgitem". The content of the TextGrid tier
   *        "tgitem" is stored as a word chunk segmentation in the partiture tier TRN. </li>  
   *   <li> "txt" indicates a replacement of the input words by their transcriptions;
   *        single line output without punctuation, where phonemes are separated by blanks and
   *        words by tabulators. </li>  
   *   <li> "tab" returns the grapheme phoneme conversion result in form of a table with
   *        two columns. The first column comprises the words, the second column their
   *        blank-separated transcriptions. </li>  
   *   <li> "exttab" results in a 5-column table. The columns contain from left to right:
   *        words, transcriptions, part of speech, morpheme segmentations, and morpheme class
   *        segmentations. </li>  
   *   <li> "lex" transforms the table to a lexicon, i.e. words are unique and sorted. </li> 
   *   <li> "extlex" provides the same information as "exttab" in a unique and sorted
   *        manner. For all lex and tab outputs columns are separated by ';'. </li>  
   *   <li> "exttcf" which is currently available for German and English only additionally
   *        adds part of speech (STTS tagset), morphs, and morph classes. </li> 
   *   <li> With "tg" and "exttg" TextGrid output is produced. </li>
   *  </ul>
   * @param syl whether or not word stress is to be added to the output transcription. 
   * @param stress whether or not the output transcription is to be syllabified. 
   * @param nrm Detects and expands 22 non-standard word types.
   * @param com whether &lt;*&gt; strings should be treated as annotation markers. If
   * true, then strings of this type are considered as annotation markers that are not
   * processed but passed on to the output. 
   * @param align "yes", "no", or "sym" decision whether or not the transcription is to be
   * letter-aligned. Syllable boundaries and word stress are not part of the output of
   * this 'sym' alignment. 
   * @return The result of this call. 
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could not be configured.
   */
  public BASResponse G2P(
    String lng, InputStream i, String iform, String tgitem, int tgrate, String outsym,
    String featset, String oform, boolean syl, boolean stress, boolean nrm, boolean com,
    String align)
    throws IOException, ParserConfigurationException {
    HttpPost request = new HttpPost(getG2PUrl());	       
    HttpEntity entity = MultipartEntityBuilder
      .create()
      .addTextBody("lng", languageTagger.tag(lng))
      .addBinaryBody("i", i, ContentType.create("text/plain"), "BAS." + iform)
      .addTextBody("iform", iform)
      .addTextBody("tgitem", tgitem)
      .addTextBody("tgrate", ""+tgrate)
      .addTextBody("outsym", outsym)
      .addTextBody("featset", featset)
      .addTextBody("oform", oform)
      .addTextBody("syl", syl?"yes":"no")
      .addTextBody("stress", stress?"yes":"no")
      .addTextBody("nrm", nrm?"yes":"no")
      .addTextBody("com", nrm?"yes":"no")
      .addTextBody("align", align)
      .build();
    request.setEntity(entity);
    HttpResponse httpResponse = httpclient.execute(request);
    HttpEntity result = httpResponse.getEntity();
    return new BASResponse(result.getContent());
  } // end of G2P()
   
  /**
   * Invoke the general MAUS service, with mostly default options, for forced alignment
   * given a WAV file and a phonemic transcription. 
   * @param LANGUAGE <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for
   * identifying the language. 
   * @param SIGNAL The signal, in WAV format.
   * @param BPF Phonemic transcription of the utterance to be segmented. Format is a 
   * <a href="http://www.bas.uni-muenchen.de/forschung/Bas/BasFormatseng.html">
   *  BAS Partitur Format (BPF)</a> file with a KAN tier.
   * @param OUTFORMAT Defines the output format:
   *  <ul>
   *   <li> "TextGrid" - a praat compatible TextGrid file </li> 
   *   <li> "par" or "mau-append" - the input BPF file with a new (or replaced) tier MAU </li>
   *   <li> "csv" or "mau" - only the BPF MAU tier (CSV table) </li> 
   *   <li> "legacyEMU" - a file with extension *.EMU that contains in the first part the
   *        Emu hlb file (*.hlb) and in the second part the Emu phonetic segmentation
   *        (*.phonetic) </li> 
   *   <li> "emuR" - an Emu compatible *_annot.json file </li>
   *  </ul>
   * @param OUTSYMBOL Defines the encoding of phonetic symbols in output. 
   *  <ul>
   *   <li> "sampa" - (default), phonetic symbols are encoded in language specific SAM-PA
   *        (with some coding differences to official SAM-PA </li> 
   *   <li> "ipa" - the service produces UTF-8 IPA output. </li> 
   *   <li> "manner" - the service produces IPA manner of articulation for each segment;
   *        possible values are: silence, vowel, diphthong, plosive, nasal, fricative,
   *        affricate, approximant, lateral-approximant, ejective. </li> 
   *   <li> "place" - the service produces IPA place of articulation for each segment;
   *        possible values are: silence, labial, dental, alveolar, post-alveolar, palatal,
   *        velar, uvular, glottal, front, central, back. </li> </ul>
   * @return The response to the request.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could
   * not be configured. 
   */
  public BASResponse MAUS(
    String LANGUAGE, File SIGNAL, File BPF, String OUTFORMAT, String OUTSYMBOL)
    throws IOException, ParserConfigurationException {
    return MAUS(LANGUAGE, new FileInputStream(SIGNAL), new FileInputStream(BPF), OUTFORMAT, OUTSYMBOL, null, null, null, null, null, null, null, null, null, null, null, null);
  }
  /**
   * Invoke the general MAUS service, for forced alignment given a WAV file and a phonemic
   * transcription. 
   * @param LANGUAGE <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for
   * identifying the language. 
   * @param SIGNAL The signal, in WAV format.
   * @param BPF Phonemic transcription of the utterance to be segmented. Format is a 
   * <a href="http://www.bas.uni-muenchen.de/forschung/Bas/BasFormatseng.html">
   *  BAS Partitur Format (BPF)</a> file with a KAN tier.
   * @param MINPAUSLEN Controls the behaviour of optional inter-word silence. If set to 1,
   * maus will detect all inter-word silence intervals that can be found (minimum length
   * for a silence interval is then 10 msec = 1 frame). If set to values n&gt;1, the
   * minimum length for an inter-word silence interval to be detected is set to n&times;10
   * msec. 
   * @param STARTWORD If set to a value n&gt;0, this option causes maus to start the
   * segmentation with the word number n (word numbering in BPF starts with 0). 
   * @param ENDWORD If set to a value n&lt;999999, this option causes maus to end the
   * segmentation with the word number n (word numbering in BPF starts with 0).  
   * @param RULESET MAUS rule set file; UTF-8 encoded; one rule per line; two different
   * file types defined by the extension: '*.nrul' : phonological rule set without
   * statistical information 
   * @param OUTFORMAT Defines the output format:
   *  <ul>
   *   <li> "TextGrid" - a praat compatible TextGrid file </li> 
   *   <li> "par" or "mau-append" - the input BPF file with a new (or replaced) tier MAU </li>
   *   <li> "csv" or "mau" - only the BPF MAU tier (CSV table) </li> 
   *   <li> "legacyEMU" - a file with extension *.EMU that contains in the first part the
   *        Emu hlb file (*.hlb) and in the second part the Emu phonetic segmentation
   *        (*.phonetic) </li> 
   *   <li> emuR - an Emu compatible *_annot.json file </li>
   *  </ul>
   * @param MAUSSHIFT If set to n, this option causes the calculated MAUS segment
   * boundaries to be shifted by n msec (default: 10) into the future. 
   * @param INSPROB The option INSPROB influences the probability of deletion of
   * segments. It is a constant factor (a constant value added to the log likelihood
   * score) after each segment. Therefore, a higher value of INSPROB will cause the
   * probability of segmentations with more segments go up, thus decreasing the
   * probability of deletions (and increasing the probability of insertions, which are
   * rarely modelled in the rule sets). 
   * @param INSKANTEXTGRID Switch to create an additional tier in the TextGrid output file
   * with a word segmentation labelled with the canonic phonemic transcript. 
   * @param INSORTTEXTGRID Switch to create an additional tier ORT in the TextGrid output
   * file with a word segmentation labelled with the orthographic transcript (taken from
   * the input ORT tier) 
   * @param USETRN  If set to true, the service searches the input BPF for a TRN tier. The
   * synopsis for a TRN entry is: 'TRN: (start-sample) (duration-sample) (word-link-list)
   * (label)', e.g. 'TRN: 23654 56432 0,1,2,3,4,5,6 sentence1' (the speech within the
   * recording 'sentence1' starts with sample 23654, last for 56432 samples and covers the
   * words 0-6). If only one TRN entry is found, the segmentation is restricted within a
   * time range given by this TRN tier entry. 
   * @param OUTSYMBOL Defines the encoding of phonetic symbols in output. 
   *  <ul>
   *   <li> "sampa" - (default), phonetic symbols are encoded in language specific SAM-PA
   *        (with some coding differences to official SAM-PA </li> 
   *   <li> "ipa" - the service produces UTF-8 IPA output. </li> 
   *   <li> "manner" - the service produces IPA manner of articulation for each segment;
   *        possible values are: silence, vowel, diphthong, plosive, nasal, fricative,
   *        affricate, approximant, lateral-approximant, ejective. </li> 
   *   <li> "place" - the service produces IPA place of articulation for each segment;
   *        possible values are: silence, labial, dental, alveolar, post-alveolar, palatal,
   *        velar, uvular, glottal, front, central, back. </li> </ul> 
   * @param NOINITIALFINALSILENCE Switch to suppress the automatic modeling on a
   * leading/trailing silence interval.  
   * @param WEIGHT weights the influence of the statistical pronunciation model against
   * the acoustical scores. More precisely WEIGHT is multiplied to the pronunciation model
   * score (log likelihood) before adding the score to the acoustical score within the
   * search. Since the pronunciation model in most cases favors the canonical
   * pronunciation, increasing WEIGHT will at some point cause MAUS to choose always the
   * canonical pronunciation; lower values of WEIGHT will favor less probable paths be
   * selected according to acoustic evidence 
   * @param MODUS Operation modus of MAUS: 
   *  <ul>
   *   <li> "standard" (default) - the segmentation and labelling using the MAUS technique
   *        as described in Schiel ICPhS 1999. </li>  
   *   <li> "align" - a forced alignment is performed on the input SAM-PA string defined
   *        in the KAN tier of the BPF. </li> 
   * </ul>
   * @return The response to the request.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could
   * not be configured. 
   */
  public BASResponse MAUS(
    String LANGUAGE, File SIGNAL, File BPF, String OUTFORMAT, String OUTSYMBOL,
    Integer MINPAUSLEN, Integer STARTWORD, Integer ENDWORD, File RULESET, Integer MAUSSHIFT,
    Double INSPROB, Boolean INSKANTEXTGRID, Boolean INSORTTEXTGRID, Boolean USETRN,
    Boolean NOINITIALFINALSILENCE, Double WEIGHT, String MODUS)
    throws IOException, ParserConfigurationException {
    return MAUS(LANGUAGE, new FileInputStream(SIGNAL), new FileInputStream(BPF), OUTFORMAT, OUTSYMBOL, MINPAUSLEN, STARTWORD, ENDWORD, RULESET==null?null:new FileInputStream(RULESET), MAUSSHIFT, INSPROB, INSKANTEXTGRID, INSORTTEXTGRID, USETRN, NOINITIALFINALSILENCE, WEIGHT, MODUS);
  }
  /**
   * Invoke the general MAUS service, for forced alignment given a WAV file and a phonemic
   * transcription. 
   * @param LANGUAGE <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for
   * identifying the language. 
   * @param SIGNAL The signal, in WAV format.
   * @param BPF Phonemic transcription of the utterance to be segmented. Format is a 
   * <a href="http://www.bas.uni-muenchen.de/forschung/Bas/BasFormatseng.html">
   *  BAS Partitur Format (BPF)</a> file with a KAN tier.
   * @param MINPAUSLEN Controls the behaviour of optional inter-word silence. If set to 1,
   * maus will detect all inter-word silence intervals that can be found (minimum length
   * for a silence interval is then 10 msec = 1 frame). If set to values n&gt;1, the
   * minimum length for an inter-word silence interval to be detected is set to n&times;10
   * msec. 
   * @param STARTWORD If set to a value n&gt;0, this option causes maus to start the
   * segmentation with the word number n (word numbering in BPF starts with 0). 
   * @param ENDWORD If set to a value n&lt;999999, this option causes maus to end the
   * segmentation with the word number n (word numbering in BPF starts with 0).  
   * @param RULESET MAUS rule set file; UTF-8 encoded; one rule per line; two different
   * file types defined by the extension: '*.nrul' : phonological rule set without
   * statistical information 
   * @param OUTFORMAT Defines the output format:
   *  <ul>
   *   <li> "TextGrid" - a praat compatible TextGrid file </li> 
   *   <li> "par" or "mau-append" - the input BPF file with a new (or replaced) tier MAU </li>
   *   <li> "csv" or "mau" - only the BPF MAU tier (CSV table) </li> 
   *   <li> "legacyEMU" - a file with extension *.EMU that contains in the first part the
   *        Emu hlb file (*.hlb) and in the second part the Emu phonetic segmentation
   *        (*.phonetic) </li> 
   *   <li> emuR - an Emu compatible *_annot.json file </li>
   *  </ul>
   * @param MAUSSHIFT If set to n, this option causes the calculated MAUS segment
   * boundaries to be shifted by n msec (default: 10) into the future. 
   * @param INSPROB The option INSPROB influences the probability of deletion of
   * segments. It is a constant factor (a constant value added to the log likelihood
   * score) after each segment. Therefore, a higher value of INSPROB will cause the
   * probability of segmentations with more segments go up, thus decreasing the
   * probability of deletions (and increasing the probability of insertions, which are
   * rarely modelled in the rule sets). 
   * @param INSKANTEXTGRID Switch to create an additional tier in the TextGrid output file
   * with a word segmentation labelled with the canonic phonemic transcript. 
   * @param INSORTTEXTGRID Switch to create an additional tier ORT in the TextGrid output
   * file with a word segmentation labelled with the orthographic transcript (taken from
   * the input ORT tier) 
   * @param USETRN  If set to true, the service searches the input BPF for a TRN tier. The
   * synopsis for a TRN entry is: 'TRN: (start-sample) (duration-sample) (word-link-list)
   * (label)', e.g. 'TRN: 23654 56432 0,1,2,3,4,5,6 sentence1' (the speech within the
   * recording 'sentence1' starts with sample 23654, last for 56432 samples and covers the
   * words 0-6). If only one TRN entry is found, the segmentation is restricted within a
   * time range given by this TRN tier entry. 
   * @param OUTSYMBOL Defines the encoding of phonetic symbols in output. 
   *  <ul>
   *   <li> "sampa" - (default), phonetic symbols are encoded in language specific SAM-PA
   *        (with some coding differences to official SAM-PA </li> 
   *   <li> "ipa" - the service produces UTF-8 IPA output. </li> 
   *   <li> "manner" - the service produces IPA manner of articulation for each segment;
   *        possible values are: silence, vowel, diphthong, plosive, nasal, fricative,
   *        affricate, approximant, lateral-approximant, ejective. </li> 
   *   <li> "place" - the service produces IPA place of articulation for each segment;
   *        possible values are: silence, labial, dental, alveolar, post-alveolar, palatal,
   *        velar, uvular, glottal, front, central, back. </li> </ul> 
   * @param NOINITIALFINALSILENCE Switch to suppress the automatic modeling on a leading/trailing silence interval. 
   * @param WEIGHT weights the influence of the statistical pronunciation model against
   * the acoustical scores. More precisely WEIGHT is multiplied to the pronunciation model
   * score (log likelihood) before adding the score to the acoustical score within the
   * search. Since the pronunciation model in most cases favors the canonical
   * pronunciation, increasing WEIGHT will at some point cause MAUS to choose always the
   * canonical pronunciation; lower values of WEIGHT will favor less probable paths be
   * selected according to acoustic evidence 
   * @param MODUS Operation modus of MAUS: 
   *  <ul>
   *   <li> "standard" (default) - the segmentation and labelling using the MAUS technique
   *        as described in Schiel ICPhS 1999. </li>  
   *   <li> "align" - a forced alignment is performed on the input SAM-PA string defined
   *        in the KAN tier of the BPF. </li> 
   * </ul>
   * @return The response to the request.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could
   * not be configured. 
   */
  public BASResponse MAUS(
    String LANGUAGE, InputStream SIGNAL, InputStream BPF, String OUTFORMAT, String OUTSYMBOL,
    Integer MINPAUSLEN, Integer STARTWORD, Integer ENDWORD, InputStream RULESET,
    Integer MAUSSHIFT, Double INSPROB, Boolean INSKANTEXTGRID, Boolean INSORTTEXTGRID,
    Boolean USETRN, Boolean NOINITIALFINALSILENCE, Double WEIGHT, String MODUS)
    throws IOException, ParserConfigurationException {
    if (OUTSYMBOL == null) OUTSYMBOL = "sampa";
    // "40 msec seems to be the border of perceivable silence, we set this option default to 5"
    if (MINPAUSLEN == null) MINPAUSLEN = 5; 
    HttpPost request = new HttpPost(getMAUSUrl());
    MultipartEntityBuilder builder = MultipartEntityBuilder
      .create()
      .addTextBody("LANGUAGE", languageTagger.tag(LANGUAGE))
      .addBinaryBody("SIGNAL", SIGNAL, ContentType.create("audio/wav"), "BAS.wav")
      .addBinaryBody("BPF", BPF, ContentType.create("text/plain-bas"), "BAS.par")
      .addTextBody("OUTFORMAT", OUTFORMAT)
      .addTextBody("OUTSYMBOL", OUTSYMBOL);
    if (USETRN != null) builder.addTextBody("USETRN", USETRN.toString());
    if (MINPAUSLEN != null) builder.addTextBody("MINPAUSLEN", MINPAUSLEN.toString());
    if (STARTWORD != null) builder.addTextBody("STARTWORD", STARTWORD.toString());
    if (ENDWORD != null) builder.addTextBody("ENDWORD", ENDWORD.toString());
    if (RULESET != null) builder.addBinaryBody("RULESET", RULESET, ContentType.create("text/plain"), "RULESET.txt");
    if (MAUSSHIFT != null) builder.addTextBody("MAUSSHIFT", MAUSSHIFT.toString());
    if (INSPROB != null) builder.addTextBody("INSPROB", INSPROB.toString());
    if (INSKANTEXTGRID != null) builder.addTextBody("INSKANTEXTGRID", INSKANTEXTGRID.toString());
    if (INSORTTEXTGRID != null) builder.addTextBody("INSORTTEXTGRID", INSORTTEXTGRID.toString());
    if (NOINITIALFINALSILENCE != null) builder.addTextBody("NOINITIALFINALSILENCE", NOINITIALFINALSILENCE.toString());
    if (WEIGHT != null) builder.addTextBody("WEIGHT", WEIGHT.toString());
    if (MODUS != null) builder.addTextBody("MODUS", MODUS.toString());
    HttpEntity entity = builder.build();
    request.setEntity(entity);
    HttpResponse httpResponse = httpclient.execute(request);
    HttpEntity result = httpResponse.getEntity();
    return new BASResponse(result.getContent());
  } // end of MAUS()
   
  /**
   * Invoke the Pho2Syl service to syllabify a phonemic transcription.
   * @param lng <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for
   * identifying the language. 
   * @param i Phonemic transcription of the utterance to be segmented. Format is a 
   * <a href="http://www.bas.uni-muenchen.de/forschung/Bas/BasFormatseng.html">
   *  BAS Partitur Format (BPF)</a> file with a KAN tier.
   * @param tier Name of tier in the annotation file, whose content is to be syllabified.
   * @param wsync Whether each word boundary is considered as syllable boundary.
   * @param oform Output format:
   *  <ul>
   *   <li> "bpf" - BAS Partiture format </li> 
   *   <li> "tg" - TextGrid format </li>
   *  </ul>
   * @param rate Only needed if <var>oform</var> = "tg" (TextGrid); Sample rate to convert
   * sample values from BAS partiture file to seconds in TextGrid.  
   * @return The response to the request.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could
   * not be configured. 
   */
  public BASResponse Pho2Syl(
    String lng, File i, String tier, Boolean wsync, String oform, Integer rate)
    throws IOException, ParserConfigurationException {
    return Pho2Syl(lng, new FileInputStream(i), tier, wsync, oform, rate);
  }
  
  /**
   * Invoke the Pho2Syl service to syllabify a phonemic transcription.
   * @param lng <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for
   * identifying the language. 
   * @param i Phonemic transcription of the utterance to be segmented. Format is a 
   *  <a href="http://www.bas.uni-muenchen.de/forschung/Bas/BasFormatseng.html">
   *  BAS Partitur Format (BPF)</a> file with a KAN tier.
   * @param tier Name of tier in the annotation file, whose content is to be syllabified.
   * @param wsync Whether each word boundary is considered as syllable boundary.
   * @param oform Output format:
   *  <ul>
   *   <li> "bpf" - BAS Partiture format </li> 
   *   <li> "tg" - TextGrid format </li>
   *  </ul>
   * @param rate Only needed if <var>oform</var> = "tg" (TextGrid); Sample rate to convert
   * sample values from BAS partiture file to seconds in TextGrid.  
   * @return The response to the request.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could
   * not be configured. 
   */
  public BASResponse Pho2Syl(
    String lng, InputStream i, String tier, Boolean wsync, String oform, Integer rate)
    throws IOException, ParserConfigurationException {
    HttpPost request = new HttpPost(getPho2SylUrl());
    MultipartEntityBuilder builder = MultipartEntityBuilder
      .create()
      .addTextBody("lng", languageTagger.tag(lng))
      .addBinaryBody("i", i, ContentType.create("text/plain-bas"), "BAS.par")
      .addTextBody("tier", tier)
      .addTextBody("oform", oform);
    if (wsync != null) builder.addTextBody("wsync", wsync?"yes":"no");
    if (rate != null) builder.addTextBody("rate", rate.toString());
    HttpEntity entity = builder.build();
    request.setEntity(entity);
    HttpResponse httpResponse = httpclient.execute(request);
    HttpEntity result = httpResponse.getEntity();
    return new BASResponse(result.getContent());
  } // end of Pho2Syl()

  /**
   * Convenience method to invoke the MaryTTS German Text-to-speech service with plain
   * text input, with a WAV file as output, using the default voice. 
   * @param INPUT_TEXT The text input.
   * @return The response to the request.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could
   * not be configured. 
   */
  public BASResponse TTS(String INPUT_TEXT)
    throws IOException, ParserConfigurationException {
    return TTS("TEXT", new ByteArrayInputStream(INPUT_TEXT.getBytes("UTF-8")), "AUDIO", "WAVE_FILE", null);
  }

  /**
   * Invoke the MaryTTS German Text-to-speech service.
   * @param INPUT_TYPE One of:
   *  <ul>
   *   <li> "TEXT" </li>
   *   <li> "SIMPLEPHONEMES" </li>
   *   <li> "SABLE" </li>
   *   <li> "SSML" </li>
   *   <li> "APML" </li>
   *   <li> "PHONEMES" </li>
   *   <li> "INTONATION" </li>
   *   <li> "ACOUSTPARAMS" </li>
   *   <li> "RAWMARYXML" </li>
   *   <li> "TOKENS" </li>
   *   <li> "WORDS" </li>
   *   <li> "ALLOPHONES" </li>
   *   <li> "REALISED_ACOUSTPARAMS" </li>
   *   <li> "REALISED_DURATIONS" </li>
   *   <li> "PRAAT_TEXTGRID" </li>
   *   <li> "PARTSOFSPEECH" </li>
   *  </ul>
   * @param INPUT_TEXT The text input.
   * @param OUTPUT_TYPE One of:
   *  <ul>
   *   <li> "PHONEMES" </li>
   *   <li> "INTONATION" </li>
   *   <li> "ACOUSTPARAMS" </li>
   *   <li> "RAWMARYXML" </li>
   *   <li> "TOKENS" </li>
   *   <li> "WORDS" </li>
   *   <li> "ALLOPHONES" </li>
   *   <li> "REALISED_ACOUSTPARAMS" </li>
   *   <li> "REALISED_DURATIONS" </li>
   *   <li> "PRAAT_TEXTGRID" </li>
   *   <li> "PARTSOFSPEECH" </li>
   *   <li> "AUDIO" </li>
   *   <li> "HALFPHONE_TARGETFEATURES" </li>
   *  </ul>
   * @param AUDIO If <var>OUTPUT_TYPE</var> = "AUDIO", this can be one of:
   *  <ul>
   *   <li> "WAVE_FILE" </li>
   *   <li> "AU_FILE" </li>
   *   <li> "AIFF_FILE" </li>
   *  </ul>
   * @param VOICE One of:
   *  <ul>
   *   <li> "bits4unitselautolabel" </li>
   *   <li> "bits3unitselautolabel" </li>
   *   <li> "bits3" </li>
   *   <li> "bits2unitselautolabel" </li>
   *   <li> "bits1unitselautolabel" </li>
   *   <li> "bits4unitselautolabelhmm" </li>
   *   <li> "bits3unitselautolabelhmm" </li>
   *   <li> "bits3-hsmm" </li>
   *   <li> "bits2unitselautolabelhmm" </li>
   *   <li> "bits1unitselautolabelhmm" </li>
   *  </ul>    
   * @return The response to the request.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could not be configured.
   */
  public BASResponse TTS(
    String INPUT_TYPE, String INPUT_TEXT, String OUTPUT_TYPE, String AUDIO, String VOICE)
    throws IOException, ParserConfigurationException {
    return TTS(INPUT_TYPE, new ByteArrayInputStream(INPUT_TEXT.getBytes("UTF-8")), OUTPUT_TYPE, AUDIO, VOICE);
  }

  /**
   * Invoke the MaryTTS German Text-to-speech service.
   * @param INPUT_TYPE One of:
   *  <ul>
   *   <li> "TEXT" </li>
   *   <li> "SIMPLEPHONEMES" </li>
   *   <li> "SABLE" </li>
   *   <li> "SSML" </li>
   *   <li> "APML" </li>
   *   <li> "PHONEMES" </li>
   *   <li> "INTONATION" </li>
   *   <li> "ACOUSTPARAMS" </li>
   *   <li> "RAWMARYXML" </li>
   *   <li> "TOKENS" </li>
   *   <li> "WORDS" </li>
   *   <li> "ALLOPHONES" </li>
   *   <li> "REALISED_ACOUSTPARAMS" </li>
   *   <li> "REALISED_DURATIONS" </li>
   *   <li> "PRAAT_TEXTGRID" </li>
   *   <li> "PARTSOFSPEECH" </li>
   *  </ul>
   * @param INPUT_TEXT The text input.
   * @param OUTPUT_TYPE One of:
   *  <ul>
   *   <li> "PHONEMES" </li>
   *   <li> "INTONATION" </li>
   *   <li> "ACOUSTPARAMS" </li>
   *   <li> "RAWMARYXML" </li>
   *   <li> "TOKENS" </li>
   *   <li> "WORDS" </li>
   *   <li> "ALLOPHONES" </li>
   *   <li> "REALISED_ACOUSTPARAMS" </li>
   *   <li> "REALISED_DURATIONS" </li>
   *   <li> "PRAAT_TEXTGRID" </li>
   *   <li> "PARTSOFSPEECH" </li>
   *   <li> "AUDIO" </li>
   *   <li> "HALFPHONE_TARGETFEATURES" </li>
   *  </ul>
   * @param AUDIO If <var>OUTPUT_TYPE</var> = "AUDIO", this can be one of:
   *  <ul>
   *   <li> "WAVE_FILE" </li>
   *   <li> "AU_FILE" </li>
   *   <li> "AIFF_FILE" </li>
   *  </ul>
   * @param VOICE One of:
   *  <ul>
   *   <li> "bits4unitselautolabel" </li>
   *   <li> "bits3unitselautolabel" </li>
   *   <li> "bits3" </li>
   *   <li> "bits2unitselautolabel" </li>
   *   <li> "bits1unitselautolabel" </li>
   *   <li> "bits4unitselautolabelhmm" </li>
   *   <li> "bits3unitselautolabelhmm" </li>
   *   <li> "bits3-hsmm" </li>
   *   <li> "bits2unitselautolabelhmm" </li>
   *   <li> "bits1unitselautolabelhmm" </li>
   *  </ul>    
   * @return The response to the request.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could
   * not be configured. 
   */
  public BASResponse TTS(
    String INPUT_TYPE, File INPUT_TEXT, String OUTPUT_TYPE, String AUDIO, String VOICE)
    throws IOException, ParserConfigurationException {
    return TTS(INPUT_TYPE, new FileInputStream(INPUT_TEXT), OUTPUT_TYPE, AUDIO, VOICE);
  }
  
  /**
   * Invoke the MaryTTS German Text-to-speech service.
   * @param INPUT_TYPE One of:
   *  <ul>
   *   <li> "TEXT" </li>
   *   <li> "SIMPLEPHONEMES" </li>
   *   <li> "SABLE" </li>
   *   <li> "SSML" </li>
   *   <li> "APML" </li>
   *   <li> "PHONEMES" </li>
   *   <li> "INTONATION" </li>
   *   <li> "ACOUSTPARAMS" </li>
   *   <li> "RAWMARYXML" </li>
   *   <li> "TOKENS" </li>
   *   <li> "WORDS" </li>
   *   <li> "ALLOPHONES" </li>
   *   <li> "REALISED_ACOUSTPARAMS" </li>
   *   <li> "REALISED_DURATIONS" </li>
   *   <li> "PRAAT_TEXTGRID" </li>
   *   <li> "PARTSOFSPEECH" </li>
   *  </ul>
   * @param INPUT_TEXT The text input.
   * @param OUTPUT_TYPE One of:
   *  <ul>
   *   <li> "PHONEMES" </li>
   *   <li> "INTONATION" </li>
   *   <li> "ACOUSTPARAMS" </li>
   *   <li> "RAWMARYXML" </li>
   *   <li> "TOKENS" </li>
   *   <li> "WORDS" </li>
   *   <li> "ALLOPHONES" </li>
   *   <li> "REALISED_ACOUSTPARAMS" </li>
   *   <li> "REALISED_DURATIONS" </li>
   *   <li> "PRAAT_TEXTGRID" </li>
   *   <li> "PARTSOFSPEECH" </li>
   *   <li> "AUDIO" </li>
   *   <li> "HALFPHONE_TARGETFEATURES" </li>
   *  </ul>
   * @param AUDIO If <var>OUTPUT_TYPE</var> = "AUDIO", this can be one of:
   *  <ul>
   *   <li> "WAVE_FILE" </li>
   *   <li> "AU_FILE" </li>
   *   <li> "AIFF_FILE" </li>
   *  </ul>
   * @param VOICE One of:
   *  <ul>
   *   <li> "bits4unitselautolabel" </li>
   *   <li> "bits3unitselautolabel" </li>
   *   <li> "bits3" </li>
   *   <li> "bits2unitselautolabel" </li>
   *   <li> "bits1unitselautolabel" </li>
   *   <li> "bits4unitselautolabelhmm" </li>
   *   <li> "bits3unitselautolabelhmm" </li>
   *   <li> "bits3-hsmm" </li>
   *   <li> "bits2unitselautolabelhmm" </li>
   *   <li> "bits1unitselautolabelhmm" </li>
   *  </ul>    
   * @return The response to the request.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could
   * not be configured. 
   */
  public BASResponse TTS(
    String INPUT_TYPE, InputStream INPUT_TEXT, String OUTPUT_TYPE, String AUDIO, String VOICE)
    throws IOException, ParserConfigurationException {
    if (VOICE == null) VOICE = "bits1unitselautolabel";
    HttpPost request = new HttpPost(getTTSUrl());
    MultipartEntityBuilder builder = MultipartEntityBuilder
      .create()
      .addTextBody("INPUT_TYPE", INPUT_TYPE)
      .addBinaryBody("INPUT_TEXT", INPUT_TEXT, ContentType.create("text/plain"), "BAS.txt")
      .addTextBody("OUTPUT_TYPE", OUTPUT_TYPE)
      .addTextBody("AUDIO", AUDIO)
      .addTextBody("VOICE", VOICE);
    HttpEntity entity = builder.build();
    request.setEntity(entity);
    HttpResponse httpResponse = httpclient.execute(request);
    HttpEntity result = httpResponse.getEntity();
    return new BASResponse(result.getContent());
  } // end of TTS()

  /**
   * Convenience method to invoke the TextAlign service for aligning two representations
   * of text, e.g. letters in orthographic transcript with phonemes in a phonemic
   * transcription, using no cost file, and default options for <div>displc</div> and
   * <div>dir</div>. 
   * @param i CSV text file with two semicolon-separated columns. Each row contains a
   * sequence pair to be aligned. The sequence elements must be separated by a
   * blank. Example: a word and its canonical transcription like S c h e r z;S E6 t s. 
   * @param cost Cost function for the edit operations substitution, deletion, and insertion to be used for the alignment.
   * <ul>
   *  <li> "naive" - assigns cost 1 to all operations except of null-substitution,
   *       i.e. the substitution of a symbol by itself, which receives cost 0. This 'naive' cost
   *       function should be used only if the pairs to be aligned share the same vocabulary,
   *       which is NOT the case e.g. in grapheme-phoneme alignment (grapheme 'x' is not the
   *       same as phoneme 'x'). </li> 
   *  <li> "g2p_deu", "g2p_eng" etc. are predefined cost functions for grapheme-phoneme
   * alignment for the respective language expressed as iso639-3. </li> 
   *  <li> "intrinsic" -  a cost function is trained on the input data and returned in the
   *       output zip. Costs are derived from co-occurrence probabilities, thus the bigger the
   *       input file, the more reliable the emerging cost function. </li>  
   *  <li> "import" - the user can provide his/her own cost function file, that must be a
   *       semicolon-separated 3-column csv text file. Examples: v;w;0.7 - the substitution of
   *       'v' by 'w' costs 0.7. v;_;0.8 - the delition of 'v' costs 0.8; _;w;0.9 - the
   *       insertion of 'w' costs 0.9. A typical usecase is to train a cost function on a big
   *       data set with cost='intrinsic', and to subsequently apply this cost function on
   *       smaller data sets with cost='import'. </li> 
   * </ul>
   * @return The response to the request.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could
   * not be     
   **/ 
  public BASResponse TextAlign(File i, String cost)
    throws IOException, ParserConfigurationException {
    return TextAlign(new FileInputStream(i), cost, null, null, null);
  }
  /**
   * Invoke the TextAlign service for aligning two representations of text, e.g. letters
   * in orthographic transcript with phonemes in a phonemic transcription. 
   * @param i CSV text file with two semicolon-separated columns. Each row contains a
   * sequence pair to be aligned. The sequence elements must be separated by a
   * blank. Example: a word and its canonical transcription like S c h e r z;S E6 t s. 
   * @param cost Cost function for the edit operations substitution, deletion, and
   * insertion to be used for the alignment. 
   * <ul>
   *  <li> "naive" - assigns cost 1 to all operations except of null-substitution,
   *       i.e. the substitution of a symbol by itself, which receives cost 0. This 'naive' cost
   *       function should be used only if the pairs to be aligned share the same vocabulary,
   *       which is NOT the case e.g. in grapheme-phoneme alignment (grapheme 'x' is not the
   *       same as phoneme 'x'). </li> 
   *  <li> "g2p_deu", "g2p_eng" etc. are predefined cost functions for grapheme-phoneme
   *       alignment for the respective language expressed as iso639-3. </li> 
   *  <li> "intrinsic" -  a cost function is trained on the input data and returned in the
   *       output zip. Costs are derived from co-occurrence probabilities, thus the bigger the
   *       input file, the more reliable the emerging cost function. </li>  
   *  <li> "import" - the user can provide his/her own cost function file, that must be a
   *       semicolon-separated 3-column csv text file. Examples: v;w;0.7 - the substitution of
   *       'v' by 'w' costs 0.7. v;_;0.8 - the delition of 'v' costs 0.8; _;w;0.9 - the
   *       insertion of 'w' costs 0.9. A typical usecase is to train a cost function on a big
   *       data set with cost='intrinsic', and to subsequently apply this cost function on
   *       smaller data sets with cost='import'. </li> 
   * </ul>
   * @param costfile CSV text file with three semicolon-separated columns. Each row
   * contains three columns of the form a;b;c, where c denotes the cost for substituting a
   * by b. Insertion and deletion are are marked by an underscore. 
   * @param displc whether alignment costs should be displayed in a third column in the
   * output file.  
   * @param atype Alignment type:
   *  <ul>
   *   <li> "dir" - align the second column to the first. </li>
   *   <li> "sym" symmetric alignment. </li>
   *  </ul>
   * @return The response to the request.
   * @throws IOException If an IO error occurs.
   * @throws ParserConfigurationException If the XML parser for parsing the response could
   * not be     
   **/ 
  public BASResponse TextAlign(File i, String cost, File costfile, Boolean displc, String atype)
    throws IOException, ParserConfigurationException {
    return TextAlign(new FileInputStream(i), cost, costfile==null?null:new FileInputStream(costfile), displc, atype);
  }
  /**
   * Invoke the TextAlign service for aligning two representations of text, e.g. letters
   * in orthographic transcript with phonemes in a phonemic transcription. 
   * @param i CSV text file with two semicolon-separated columns. Each row contains a
   * sequence pair to be aligned. The sequence elements must be separated by a
   * blank. Example: a word and its canonical transcription like S c h e r z;S E6 t s. 
   * @param cost Cost function for the edit operations substitution, deletion, and
   * insertion to be used for the alignment. 
   * <ul>
   *  <li> "naive" - assigns cost 1 to all operations except of null-substitution,
   *       i.e. the substitution of a symbol by itself, which receives cost 0. This 'naive' cost
   *       function should be used only if the pairs to be aligned share the same vocabulary,
   *       which is NOT the case e.g. in grapheme-phoneme alignment (grapheme 'x' is not the
   *       same as phoneme 'x'). </li> 
   *  <li> "g2p_deu", "g2p_eng" etc. are predefined cost functions for grapheme-phoneme
   *       alignment for the respective language expressed as iso639-3. </li> 
   *  <li> "intrinsic" -  a cost function is trained on the input data and returned in the
   *       output zip. Costs are derived from co-occurrence probabilities, thus the bigger the
   *       input file, the more reliable the emerging cost function. </li>  
   *  <li> "import" - the user can provide his/her own cost function file, that must be a
   *       semicolon-separated 3-column csv text file. Examples: v;w;0.7 - the substitution of
   *       'v' by 'w' costs 0.7. v;_;0.8 - the delition of 'v' costs 0.8; _;w;0.9 - the
   *       insertion of 'w' costs 0.9. A typical usecase is to train a cost function on a big
   *       data set with cost='intrinsic', and to subsequently apply this cost function on
   *       smaller data sets with cost='import'. </li> 
   * </ul>
   * @param costfile CSV text file with three semicolon-separated columns. Each row
   * contains three columns of the form a;b;c, where c denotes the cost for substituting a
   * by b. Insertion and deletion are are marked by an underscore. 
    * @param displc whether alignment costs should be displayed in a third column in the
    * output file.  
    * @param atype Alignment type:
    *  <ul>
    *   <li> "dir" - align the second column to the first. </li>
    *   <li> "sym" symmetric alignment. </li>
    *  </ul>
    * @return The response to the request.
    * @throws IOException If an IO error occurs.
    * @throws ParserConfigurationException If the XML parser for parsing the response
    * could not be     
    **/
   public BASResponse TextAlign(
     InputStream i, String cost, InputStream costfile, Boolean displc, String atype)
     throws IOException, ParserConfigurationException {
      if (displc == null) displc = Boolean.FALSE;
      if (atype == null) atype = "dir";
      HttpPost request = new HttpPost(getTextAlignUrl());
      MultipartEntityBuilder builder = MultipartEntityBuilder
	 .create()
	 .addBinaryBody("i", i, ContentType.create("text/csv"), "BAS.csv")
	 .addTextBody("cost", cost)
	 .addTextBody("displc", displc?"yes":"no")
	 .addTextBody("atype", atype);
      if (costfile != null)
        builder.addBinaryBody(
          "costfile", costfile, ContentType.create("text/csv"), "BAS.cst.csv");
      HttpEntity entity = builder.build();
      request.setEntity(entity);
      HttpResponse httpResponse = httpclient.execute(request);
      HttpEntity result = httpResponse.getEntity();
      return new BASResponse(result.getContent());
   } // end of TextAlign()

} // end of class BAS
