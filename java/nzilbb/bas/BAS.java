//
// Copyright 2016 New Zealand Institute of Language, Brain and Behaviour, 
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
 * <p><a href="https://clarin.phonetik.uni-muenchen.de/BASWebServices/#/services">https://clarin.phonetik.uni-muenchen.de/BASWebServices/#/services</a> 
 * <p>For service discovery, links are like <a href="http://clarin.phonetik.uni-muenchen.de/BASWebServices/BAS_Webservices.cmdi.xml">http://clarin.phonetik.uni-muenchen.de/BASWebServices/BAS_Webservices.cmdi.xml</a>
 * @author Robert Fromont robert@fromont.net.nz
 */
public class BAS
{
   // Attributes:
   
   /**
    * Version of the BAS services this API is designed for.
    * @return Version of the BAS services this API is designed for.
    */
   public String getVersion() { return "2.10"; }


   /**
    * URL for the MAUSBasic service.
    * @see #getMAUSBasicUrl()
    * @see #setMAUSBasicUrl(String)
    */
   protected String MAUSBasicUrl = "https://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runMAUSBasic";
   /**
    * Getter for {@link #MAUSBasicUrl}: URL for the MAUSBasic service.
    * @return URL for the MAUSBasic service.
    */
   public String getMAUSBasicUrl() { return MAUSBasicUrl; }
   /**
    * Setter for {@link #MAUSBasicUrl}: URL for the MAUSBasic service.
    * @param newMAUSBasicUrl URL for the MAUSBasic service.
    */
   public void setMAUSBasicUrl(String newMAUSBasicUrl) { MAUSBasicUrl = newMAUSBasicUrl; }
   
   /**
    * URL for the G2P service.
    * @see #getG2PUrl()
    * @see #setG2PUrl(String)
    */
   protected String G2PUrl = "http://clarin.phonetik.uni-muenchen.de/BASWebServices/services/runG2P";
   /**
    * Getter for {@link #G2PUrl}: URL for the G2P service.
    * @return URL for the G2P service.
    */
   public String getG2PUrl() { return G2PUrl; }
   /**
    * Setter for {@link #G2PUrl}: URL for the G2P service.
    * @param newG2PUrl URL for the G2P service.
    */
   public void setG2PUrl(String newG2PUrl) { G2PUrl = newG2PUrl; }

   private CloseableHttpClient httpclient;
   private LanguageTag languageTagger = new LanguageTag();
   
   // Methods:
   
   /**
    * Default constructor.
    * @throws IOException If the ISO 639 resources could be loaded.
    */
   public BAS() throws IOException
   {
      httpclient = HttpClients.createDefault();
      languageTagger = new LanguageTag();
   } // end of constructor

   /**
    * Invokes the MAUSBasic service.
    * @param language <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for identifying the language.
    * @param signal The signal, in WAV format.
    * @param text The transcription of the text.
    * @return The result of the call.
    * @throws IOException If an IO error occurs.
    * @throws ParserConfigurationException If the XML parser for parsing the response could not be configured.
    */
   public BASResponse MAUSBasic(String language, File signal, File text)
      throws IOException, ParserConfigurationException
   {
      return MAUSBasic(language, new FileInputStream(signal), new FileInputStream(text));
   }   
   /**
    * Invokes the MAUSBasic service.
    * @param language <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for identifying the language.
    * @param signal The signal, in WAV format.
    * @param text The transcription of the text.
    * @return The result of the call.
    * @throws IOException If an IO error occurs.
    * @throws ParserConfigurationException If the XML parser for parsing the response could not be configured.
    */
   public BASResponse MAUSBasic(String language, InputStream signal, InputStream text)
      throws IOException, ParserConfigurationException
   {
      HttpPost request = new HttpPost(getMAUSBasicUrl());	       
      HttpEntity entity = MultipartEntityBuilder
	 .create()
	 .addTextBody("LANGUAGE", languageTagger.tag(language))
	 .addBinaryBody("SIGNAL", signal, ContentType.create("audio/wav"), "BAS.wav")
	 .addBinaryBody("TEXT", text, ContentType.create("text/plain"), "BAS.txt")
	 .build();
      request.setEntity(entity);
      HttpResponse httpResponse = httpclient.execute(request);
      HttpEntity result = httpResponse.getEntity();
      return new BASResponse(result.getContent());
   } // end of MAUSBasic()
   
   /**
    * Invokes the G2P service for converting orthography into phonemic transcription.
    * <p>This convenience method takes a String as the text, and assumes <var>iform</var> = "txt", use {@link #G2P(String,InputStream,String,String,int,String,String,String,boolean,boolean,boolean,boolean,String)} for full set of options.
    * @param lng <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for identifying the language.
    * @param txt The text to transform as a String.
    * @param outsym Ouput phoneme symbol inventory:
    *  <ul>
    *   <li>"sampa" - language-specific SAMPA variant is the default.</li> 
    *   <li>"x-sampa" - language independent X-SAMPA and IPA can be chosen.</li> 
    *   <li>"maus-sampa" - maps the output to a language-specific phoneme subset that WEBMAUS can process.</li> 
    *   <li>"ipa" - Unicode-encoded IPA.</li> 
    *   <li>"arpabet" - supported for eng-US only</li>
    * </ul>
    * @param featset - Feature set used for grapheme-phoneme conversion. 
    *  <ul>
    *   <li>"standard" comprises a letter window centered on the grapheme to be converted.</li> 
    *   <li>"extended" set additionally includes part of speech and morphological analyses.</li>
    *  </ul>
    * @param oform Output format:
    *  <ul>
    *   <li>"bpf" indicates the BAS Partitur Format (BPF) file with a KAN tier.</li> 
    *   <li>"bpfs" differs from "bpf" only in that respect, that the phonemes are separated by blanks. In case of TextGrid input, both "bpf" and "bpfs" require the additional parameters "tgrate" and "tgitem". The content of the TextGrid tier "tgitem" is stored as a word chunk segmentation in the partiture tier TRN.</li> 
    *   <li>"txt" indicates a replacement of the input words by their transcriptions; single line output without punctuation, where phonemes are separated by blanks and words by tabulators.</li> 
    *   <li>"tab" returns the grapheme phoneme conversion result in form of a table with two columns. The first column comprises the words, the second column their blank-separated transcriptions.</li> 
    *   <li>"exttab" results in a 5-column table. The columns contain from left to right: words, transcriptions, part of speech, morpheme segmentations, and morpheme class segmentations.</li> 
    *   <li>"lex" transforms the table to a lexicon, i.e. words are unique and sorted.</li> 
    *   <li>"extlex" provides the same information as "exttab" in a unique and sorted manner. For all lex and tab outputs columns are separated by ';'.</li> 
    *   <li>"exttcf" which is currently available for German and English only additionally adds part of speech (STTS tagset), morphs, and morph classes.</li>
    *   <li>With "tg" and "exttg" TextGrid output is produced.</li>
    *  </ul>
    * @param syl whether or not word stress is to be added to the output transcription. 
    * @param stress whether or not the output transcription is to be syllabified. 
    * @return The result of this call.
    * @throws IOException If an IO error occurs.
    * @throws ParserConfigurationException If the XML parser for parsing the response could not be configured.
    */
   public BASResponse G2P(String lng, String txt, String outsym, String featset, String oform, boolean syl, boolean stress)
    throws IOException, ParserConfigurationException
   {
      return G2P(lng, new ByteArrayInputStream(txt.getBytes("UTF-8")), "txt", "", 16000, outsym, featset, oform, syl, stress, true, false, "no");
   }
   /**
    * Invokes the G2P service for converting orthography into phonemic transcription.
    * <p>This method cannot have <var>iform</var> set to "tg", use {@link #G2P(String,InputStream,String,String,int,String,String,String,boolean,boolean,boolean,boolean,String)} for full set of options.
    * @param lng <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for identifying the language.
    * @param i The text to transform.
    * @param iform The format of <var>i</var> -
    * <ul>
    *  <li>"txt" indicates connected text input, which will be tokenized before the conversion.</li> 
    *  <li>"list" indicates a sequence of unconnected words, that does not need to be tokenized. Furthermore, "list" requires a different part-of-speech tagging strategy than "txt" for the extraction of the "extended" feature set (see Parameter <var>featset</var>).</li> 
    *  <li>"tcf" indicates, that the input format is TCF containing at least a tokenization dominated by the element "tokens".</li> 
    *  <li>Input format "bpf" indicates BAS partitur file input containing an ORT tier to be transcribed.</li>
    * </ul>
    * @param outsym Ouput phoneme symbol inventory:
    *  <ul>
    *   <li>"sampa" - language-specific SAMPA variant is the default.</li> 
    *   <li>"x-sampa" - language independent X-SAMPA and IPA can be chosen.</li> 
    *   <li>"maus-sampa" - maps the output to a language-specific phoneme subset that WEBMAUS can process.</li> 
    *   <li>"ipa" - Unicode-encoded IPA.</li> 
    *   <li>"arpabet" - supported for eng-US only</li>
    * </ul>
    * @param featset - Feature set used for grapheme-phoneme conversion. 
    *  <ul>
    *   <li>"standard" comprises a letter window centered on the grapheme to be converted.</li> 
    *   <li>"extended" set additionally includes part of speech and morphological analyses.</li>
    *  </ul>
    * @param oform Output format:
    *  <ul>
    *   <li>"bpf" indicates the BAS Partitur Format (BPF) file with a KAN tier.</li> 
    *   <li>"bpfs" differs from "bpf" only in that respect, that the phonemes are separated by blanks. In case of TextGrid input, both "bpf" and "bpfs" require the additional parameters "tgrate" and "tgitem". The content of the TextGrid tier "tgitem" is stored as a word chunk segmentation in the partiture tier TRN.</li> 
    *   <li>"txt" indicates a replacement of the input words by their transcriptions; single line output without punctuation, where phonemes are separated by blanks and words by tabulators.</li> 
    *   <li>"tab" returns the grapheme phoneme conversion result in form of a table with two columns. The first column comprises the words, the second column their blank-separated transcriptions.</li> 
    *   <li>"exttab" results in a 5-column table. The columns contain from left to right: words, transcriptions, part of speech, morpheme segmentations, and morpheme class segmentations.</li> 
    *   <li>"lex" transforms the table to a lexicon, i.e. words are unique and sorted.</li> 
    *   <li>"extlex" provides the same information as "exttab" in a unique and sorted manner. For all lex and tab outputs columns are separated by ';'.</li> 
    *   <li>"exttcf" which is currently available for German and English only additionally adds part of speech (STTS tagset), morphs, and morph classes.</li>
    *   <li>With "tg" and "exttg" TextGrid output is produced.</li>
    *  </ul>
    * @param syl whether or not word stress is to be added to the output transcription. 
    * @param stress whether or not the output transcription is to be syllabified. 
    * @param nrm Detects and expands 22 non-standard word types.
    * @param com whether &lt;*&gt; strings should be treated as annotation markers. If true, then strings of this type are considered as annotation markers that are not processed but passed on to the output.
    * @param align "yes", "no", or "sym" decision whether or not the transcription is to be letter-aligned. Syllable boundaries and word stress are not part of the output of this 'sym' alignment.
    * @return The result of this call.
    * @throws IOException If an IO error occurs.
    * @throws ParserConfigurationException If the XML parser for parsing the response could not be configured.
    */
   public BASResponse G2P(String lng, File i, String iform, String outsym, String featset, String oform, boolean syl, boolean stress, boolean nrm, boolean com, String align)
    throws IOException, ParserConfigurationException
   {
      return G2P(lng, new FileInputStream(i), iform, "", 16000, outsym, featset, oform, syl, stress, nrm, com, align);
   }
   /**
    * Invokes the G2P service for converting orthography into phonemic transcription.
    * <p>This method cannot have <var>iform</var> set to "tg", use {@link #G2P(String,InputStream,String,String,int,String,String,String,boolean,boolean,boolean,boolean,String)} for full set of options.
    * @param lng <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for identifying the language.
    * @param i The text to transform.
    * @param iform The format of <var>i</var> -
    * <ul>
    *  <li>"txt" indicates connected text input, which will be tokenized before the conversion.</li> 
    *  <li>"list" indicates a sequence of unconnected words, that does not need to be tokenized. Furthermore, "list" requires a different part-of-speech tagging strategy than "txt" for the extraction of the "extended" feature set (see Parameter <var>featset</var>).</li> 
    *  <li>"tcf" indicates, that the input format is TCF containing at least a tokenization dominated by the element "tokens".</li> 
    *  <li>Input format "bpf" indicates BAS partitur file input containing an ORT tier to be transcribed.</li>
    * </ul>
    * @param outsym Ouput phoneme symbol inventory:
    *  <ul>
    *   <li>"sampa" - language-specific SAMPA variant is the default.</li> 
    *   <li>"x-sampa" - language independent X-SAMPA and IPA can be chosen.</li> 
    *   <li>"maus-sampa" - maps the output to a language-specific phoneme subset that WEBMAUS can process.</li> 
    *   <li>"ipa" - Unicode-encoded IPA.</li> 
    *   <li>"arpabet" - supported for eng-US only</li>
    * </ul>
    * @param featset - Feature set used for grapheme-phoneme conversion. 
    *  <ul>
    *   <li>"standard" comprises a letter window centered on the grapheme to be converted.</li> 
    *   <li>"extended" set additionally includes part of speech and morphological analyses.</li>
    *  </ul>
    * @param oform Output format:
    *  <ul>
    *   <li>"bpf" indicates the BAS Partitur Format (BPF) file with a KAN tier.</li> 
    *   <li>"bpfs" differs from "bpf" only in that respect, that the phonemes are separated by blanks. In case of TextGrid input, both "bpf" and "bpfs" require the additional parameters "tgrate" and "tgitem". The content of the TextGrid tier "tgitem" is stored as a word chunk segmentation in the partiture tier TRN.</li> 
    *   <li>"txt" indicates a replacement of the input words by their transcriptions; single line output without punctuation, where phonemes are separated by blanks and words by tabulators.</li> 
    *   <li>"tab" returns the grapheme phoneme conversion result in form of a table with two columns. The first column comprises the words, the second column their blank-separated transcriptions.</li> 
    *   <li>"exttab" results in a 5-column table. The columns contain from left to right: words, transcriptions, part of speech, morpheme segmentations, and morpheme class segmentations.</li> 
    *   <li>"lex" transforms the table to a lexicon, i.e. words are unique and sorted.</li> 
    *   <li>"extlex" provides the same information as "exttab" in a unique and sorted manner. For all lex and tab outputs columns are separated by ';'.</li> 
    *   <li>"exttcf" which is currently available for German and English only additionally adds part of speech (STTS tagset), morphs, and morph classes.</li>
    *   <li>With "tg" and "exttg" TextGrid output is produced.</li>
    *  </ul>
    * @param syl whether or not word stress is to be added to the output transcription. 
    * @param stress whether or not the output transcription is to be syllabified. 
    * @param nrm Detects and expands 22 non-standard word types.
    * @param com whether &lt;*&gt; strings should be treated as annotation markers. If true, then strings of this type are considered as annotation markers that are not processed but passed on to the output.
    * @param align "yes", "no", or "sym" decision whether or not the transcription is to be letter-aligned. Syllable boundaries and word stress are not part of the output of this 'sym' alignment.
    * @return The result of this call.
    * @throws IOException If an IO error occurs.
    * @throws ParserConfigurationException If the XML parser for parsing the response could not be configured.
    */
   public BASResponse G2P(String lng, InputStream i, String iform, String outsym, String featset, String oform, boolean syl, boolean stress, boolean nrm, boolean com, String align)
    throws IOException, ParserConfigurationException
   {
      return G2P(lng, i, iform, "", 16000, outsym, featset, oform, syl, stress, nrm, com, align);
   }
   /**
    * Invokes the G2P service for converting orthography into phonemic transcription.
    * @param lng <a href="https://tools.ietf.org/html/rfc5646">RFC 5646</a> tag for identifying the language.
    * @param i The text to transform.
    * @param iform The format of <var>i</var> -
    * <ul>
    *  <li>"txt" indicates connected text input, which will be tokenized before the conversion.</li> 
    *  <li>"list" indicates a sequence of unconnected words, that does not need to be tokenized. Furthermore, "list" requires a different part-of-speech tagging strategy than "txt" for the extraction of the "extended" feature set (see Parameter <var>featset</var>).</li> 
    *  <li>"tg" indicates TextGrid input. Long and short format is supported. For TextGrid input additionally the name of the item containing the words to be transcribed is to be specified by the parameter "tgname". In combination with "bpf" output format "tg" input additionally requires the specification of the sample rate by the parameter "tgrate".</li>
    *  <li>"tcf" indicates, that the input format is TCF containing at least a tokenization dominated by the element "tokens".</li> 
    *  <li>Input format "bpf" indicates BAS partitur file input containing an ORT tier to be transcribed.</li>
    * </ul>
    * @param tgitem Only needed, if <var>iform</var> is "tg". Name of the TextGrid item, that contains the words to be transcribed. In case of TextGrid output, this item is the reference for the added items. 
    * @param tgrate Only needed, if <var>iform</var> is "tg" and oform is "bpf(s)". Sample rate to convert time values from TextGrid to sample values in BAS partiture file. 
    * @param outsym Ouput phoneme symbol inventory:
    *  <ul>
    *   <li>"sampa" - language-specific SAMPA variant is the default.</li> 
    *   <li>"x-sampa" - language independent X-SAMPA and IPA can be chosen.</li> 
    *   <li>"maus-sampa" - maps the output to a language-specific phoneme subset that WEBMAUS can process.</li> 
    *   <li>"ipa" - Unicode-encoded IPA.</li> 
    *   <li>"arpabet" - supported for eng-US only</li>
    * </ul>
    * @param featset - Feature set used for grapheme-phoneme conversion. 
    *  <ul>
    *   <li>"standard" comprises a letter window centered on the grapheme to be converted.</li> 
    *   <li>"extended" set additionally includes part of speech and morphological analyses.</li>
    *  </ul>
    * @param oform Output format:
    *  <ul>
    *   <li>"bpf" indicates the BAS Partitur Format (BPF) file with a KAN tier.</li> 
    *   <li>"bpfs" differs from "bpf" only in that respect, that the phonemes are separated by blanks. In case of TextGrid input, both "bpf" and "bpfs" require the additional parameters "tgrate" and "tgitem". The content of the TextGrid tier "tgitem" is stored as a word chunk segmentation in the partiture tier TRN.</li> 
    *   <li>"txt" indicates a replacement of the input words by their transcriptions; single line output without punctuation, where phonemes are separated by blanks and words by tabulators.</li> 
    *   <li>"tab" returns the grapheme phoneme conversion result in form of a table with two columns. The first column comprises the words, the second column their blank-separated transcriptions.</li> 
    *   <li>"exttab" results in a 5-column table. The columns contain from left to right: words, transcriptions, part of speech, morpheme segmentations, and morpheme class segmentations.</li> 
    *   <li>"lex" transforms the table to a lexicon, i.e. words are unique and sorted.</li> 
    *   <li>"extlex" provides the same information as "exttab" in a unique and sorted manner. For all lex and tab outputs columns are separated by ';'.</li> 
    *   <li>"exttcf" which is currently available for German and English only additionally adds part of speech (STTS tagset), morphs, and morph classes.</li>
    *   <li>With "tg" and "exttg" TextGrid output is produced.</li>
    *  </ul>
    * @param syl whether or not word stress is to be added to the output transcription. 
    * @param stress whether or not the output transcription is to be syllabified. 
    * @param nrm Detects and expands 22 non-standard word types.
    * @param com whether &lt;*&gt; strings should be treated as annotation markers. If true, then strings of this type are considered as annotation markers that are not processed but passed on to the output.
    * @param align "yes", "no", or "sym" decision whether or not the transcription is to be letter-aligned. Syllable boundaries and word stress are not part of the output of this 'sym' alignment.
    * @return The result of this call.
    * @throws IOException If an IO error occurs.
    * @throws ParserConfigurationException If the XML parser for parsing the response could not be configured.
    */
   public BASResponse G2P(String lng, InputStream i, String iform, String tgitem, int tgrate, String outsym, String featset, String oform, boolean syl, boolean stress, boolean nrm, boolean com, String align)
    throws IOException, ParserConfigurationException
   {
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

} // end of class BAS
