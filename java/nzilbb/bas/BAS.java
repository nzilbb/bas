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
   
   // Methods:
   
   /**
    * Default constructor.
    */
   public BAS()
   {
      httpclient = HttpClients.createDefault();
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
	 .addTextBody("LANGUAGE", language)
	 .addBinaryBody("SIGNAL", signal, ContentType.create("audio/wav"), "BAS.wav")
	 .addBinaryBody("TEXT", text, ContentType.create("text/plain"), "BAS.txt")
	 .build();
      request.setEntity(entity);
      HttpResponse httpResponse = httpclient.execute(request);
      HttpEntity result = httpResponse.getEntity();
      return new BASResponse(result.getContent());
   } // end of MAUSBasic()

} // end of class BAS
