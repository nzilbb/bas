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
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Vector;
import org.w3c.dom.*;
import javax.xml.*;
import javax.xml.xpath.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.parsers.*;
import javax.xml.validation.*;
import org.xml.sax.*;
import org.w3c.dom.*;

/**
 * Response from BAS request.
 * <p>Assuming you've called a BAS method, something like <br> <code>BASResponse response = new BAS().MAUSBasic(language, signal, text);</code>
 * <ul>
 *  <li><code>response.getSuccess()</code> is <tt>true</tt> if the request succeeded</li>
 *  <li><code>response.getDownloadLink()</code> returns the URL of the results file</li>
 *  <li><code>response.saveDownload()</code> downloads the results file and returns it</li>
 * </ul>
 * @author Robert Fromont robert.fromont@canterbury.ac.nz
 */
public class BASResponse
{
   // Attributes:

   // XML stuff
   protected DocumentBuilderFactory factory;
   protected DocumentBuilder builder;
   protected Document document;
   protected XPathFactory xpathFactory;
   protected XPath xpath;
   
   /**
    * true if successful, false otherwise.
    * @see #getSuccess()
    * @see #setSuccess(boolean)
    */
   protected boolean success;
   /**
    * Getter for {@link #success}: true if successful, false otherwise.
    * @return true if successful, false otherwise.
    */
   public boolean getSuccess() { return success; }
   /**
    * Setter for {@link #success}: true if successful, false otherwise.
    * @param newSuccess true if successful, false otherwise.
    */
   public void setSuccess(boolean newSuccess) { success = newSuccess; }

   /**
    * URL for downloading result.
    * @see #getDownloadLink()
    * @see #setDownloadLink(String)
    */
   protected URL downloadLink;
   /**
    * Getter for {@link #downloadLink}: URL for downloading result.
    * @return URL for downloading result.
    */
   public URL getDownloadLink() { return downloadLink; }
   /**
    * Setter for {@link #downloadLink}: URL for downloading result.
    * @param newDownloadLink URL for downloading result.
    */
   public void setDownloadLink(URL newDownloadLink) { downloadLink = newDownloadLink; }
   /**
    * Setter for {@link #downloadLink}: URL for downloading result.
    * @param newDownloadLink URL for downloading result.
    * @throws MalformedURLException If the string URL is invalid.
    */
   public void setDownloadLink(String newDownloadLink)
      throws MalformedURLException
   { 
      if (newDownloadLink == null || newDownloadLink.length() == 0)
      {
	 downloadLink = null;
      }
      else
      {
	 downloadLink = new URL(newDownloadLink); 
      }
   }

   /**
    * Output message.
    * @see #getOutput()
    * @see #setOutput(String)
    */
   protected String output;
   /**
    * Getter for {@link #output}: Output message.
    * @return Output message.
    */
   public String getOutput() { return output; }
   /**
    * Setter for {@link #output}: Output message.
    * @param newOutput Output message.
    */
   public void setOutput(String newOutput) { output = newOutput; }

   /**
    * Warning messages.
    * @see #getWarnings()
    * @see #setWarnings(String)
    */
   protected String warnings = "";
   /**
    * Getter for {@link #warnings}: Warning messages.
    * @return Warning messages.
    */
   public String getWarnings() { return warnings; }
   /**
    * Setter for {@link #warnings}: Warning messages.
    * @param newWarnings Warning messages.
    */
   public void setWarnings(String newWarnings) { warnings = newWarnings; }

   /**
    * Original XML.
    * @see #getXml()
    */
   protected String xml;
   /**
    * Getter for {@link #xml}: Original XML.
    * @return Original XML.
    */
   public String getXml() { return xml; }
   
   // Methods:
   
   /**
    * Constructor. 
    * @param stream The stream containing the XML of the response.
    * @throws ParserConfigurationException If the XML parser can't be configured.
    * @throws IOException If there's an IO error.
    */
   public BASResponse(InputStream stream)
      throws ParserConfigurationException, IOException
   {
      factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);	    
      builder = factory.newDocumentBuilder();
      xpathFactory = XPathFactory.newInstance();
      xpath = xpathFactory.newXPath();
      loadXml(stream);
   } // end of constructor

   /**
    * Convenience function for downloading the result, if any.
    * @return A temporary file (which the caller has the responsibility to delete), or null if content couldn't be downloaded.
    * @throws IOException If an IO error occurs.
    */
   public File saveDownload()
    throws IOException
   {
      if (getDownloadLink() == null) return null;
      return saveDownload(File.createTempFile("BAS", getDownloadLink().getFile()));
   } // end of saveDownload()
   
   /**
    * Loads XML response from an InputStream
    * @param stream The stream containing the XML of the response.
    * @throws IOException If there's an IO error.
    */
   protected void loadXml(InputStream stream)
      throws IOException
   {
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      xml = "";
      String line = reader.readLine();
      while (line != null)
      {
	 xml += line + "\n";
	 line = reader.readLine();
      }
      stream.close();
      
      // interpret the XML
      try
      {
	 builder.setEntityResolver(new EntityResolver()
	    {
	       public InputSource resolveEntity(String publicId, String systemId)
		  throws SAXException, IOException
	       {
		  // Get DTDs from jar file, to prevent not found errors
		  String sName = systemId.substring(systemId.lastIndexOf('/'));
		  URL url = getClass().getResource(sName);
		  if (url != null) return new InputSource(url.openStream());
		  else return null;
	       }
	    });
	 document = builder.parse(new InputSource(new StringReader(xml)));
	 setSuccess(Boolean.parseBoolean(getXpathString("/WebServiceResponseLink/success")));
	 setOutput(getXpathString("/WebServiceResponseLink/output"));
	 setWarnings(getXpathString("/WebServiceResponseLink/warnings"));
	 setDownloadLink(getXpathString("/WebServiceResponseLink/downloadLink"));
      }
      catch(Exception x)
      {
	 warnings += "Error parsing response: " + x;
	 System.err.println("loadXml: " + x);
	 System.err.println(xml);
      }
      
   } // end of loadXml()
   
   /**
    * Convenience function for downloading the result, if any.
    * @param file The file to save the download to.
    * @return The given file, or null if content couldn't be downloaded.
    * @throws IOException If an IO error occurs.
    */
   public File saveDownload(File file)
    throws IOException
   {
      if (getDownloadLink() == null) return null;
      FileOutputStream output = new FileOutputStream(file);
      InputStream input = getDownloadLink().openConnection().getInputStream();
      long totalBytes = 0;
      
      byte[] buffer = new byte[1024];
      int bytesRead = input.read(buffer);
      while (bytesRead >= 0)
      {
	 totalBytes += bytesRead;
	 output.write(buffer, 0, bytesRead);
	 bytesRead = input.read(buffer);
      } // next chunk	
      output.flush();
      output.close();
      input.close();
      return file;
   } // end of saveDownload()

   /**
    * Returns a single string result for the given xpath string
    * @param sXpath The XPath expression
    * @return Result matching the Xpath, or ""
    * @throws XPathExpressionException If <var>sXpath</var> is invalid.
    */
   protected String getXpathString(String sXpath)
      throws XPathExpressionException
   {
      if (document == null) return "";
      return getXpathString(sXpath, document.getDocumentElement());
   } // end of getXpathString()
   
   /**
    * Returns a single string result for the given xpath string
    * @param sXpath The XPath expression
    * @param node The root node to use.
    * @return Result matching the Xpath, or ""
    * @throws XPathExpressionException If <var>sXpath</var> is invalid.
    */
   protected String getXpathString(String sXpath, Node node)
      throws XPathExpressionException
   {
      String s = xpath.evaluate(sXpath, node);
      return tidyText(s);
   } // end of getXpathString()
        
   /**
    * Tidies up the given string - e.g. replaces series' of whitespaces characters with a single space.
    * @param s The original text.
    * @return A tidy version of the given string.
    */
   protected String tidyText(String s)
   {
      if (s == null) return "";
      return s
	 .replaceAll(" +", " ")
	 .replaceAll("\n+", "\n")
	 ;
   } // end of tidyText()
   
} // end of class BASResponse
