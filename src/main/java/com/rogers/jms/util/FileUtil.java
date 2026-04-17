/**
 * 
 */
package com.rogers.jms.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * @author Sreenivasa Reddy Mulinti
 *
 */
public class FileUtil {
	
	/**
	 * @param fileURI - The location of the file to read.
	 * @return the XML as string
	 * @throws IOException 
	 */
	public static String readFileAsString(String fileURI)
	{
		String fileContent= "";
        BufferedReader reader = null;
        try {
        	if(!new File(fileURI ).exists()){ 
            	String URI = FileUtil.class.getClassLoader().getResource(".").getPath().replaceAll("%20", " ");
            	fileURI = URI + fileURI;
        	}
			reader = new BufferedReader(new FileReader(fileURI));
		    String line = reader.readLine();
		    while (line != null) {
		    	fileContent += line + "\n";
		        line = reader.readLine();
		    }
        }
        catch(IOException e)
        {
        	e.printStackTrace();
        }
        if(reader != null)
        {
			try {
				reader.close();
			} catch (IOException e) {				
			}
        }
        return fileContent;
	}
	
	/**
	 * @param fileURI - The location of the file to read.
	 * @return the XML as string
	 */
	public static Document readFileAsDocument(String fileURI)
	{
		Document doc = null;
		if(fileURI != null)
		{
			if(!new File(fileURI ).exists()){ 
            	String URI = FileUtil.class.getClassLoader().getResource(".").getPath().replaceAll("%20", " ");
            	fileURI = URI + fileURI;
        	}
			DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder;
	        try
	        {
	        	builder = fac.newDocumentBuilder();
	        	doc = builder.parse(fileURI);
	        	doc.getDocumentElement().normalize();
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
		}
		return doc;
	}

	/**
	 * @param fileURI - The location of the file to read.
	 * @return the XML as string
	 * @throws FileNotFoundException 
	 */
	public static InputStream readFileAsInputStream(String fileURI)
	{
		if(!new File(fileURI ).exists()){ 
        	String URI = FileUtil.class.getClassLoader().getResource(".").getPath().replaceAll("%20", " ");
        	fileURI = URI + fileURI;
    	}
		File file = new File(fileURI);
        FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return fis;
	}
	

	
}
