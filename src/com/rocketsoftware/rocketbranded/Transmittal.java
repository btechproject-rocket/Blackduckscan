
package com.rocketsoftware.rocketbranded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.rocketsoftware.ftp.JesFtp;
import com.rocketsoftware.jira.JiraClient;


public class Transmittal {
	
	
	/**
	 * Example of use:
	 *     URL url = new URL ("https://jira.rocketsoftware.com/secure/attachment/1091059/Transmittal.txt");
	 *     Transmittal.saveTransmittalToMVS(url, "rs22", "'PDSAMO.PKGTEXT'");
	 *
	 *	
	 * @param sURL
	 * @param sTargetHost
	 * @param sTargetDSN
	 * @return 
	 */	
	public static boolean saveTransmittalToMVS(URL url, String sTargetHost, String sTargetDSN) 
	{
		boolean result = false;
		try
	      {                 
				JiraClient jiraClient = new JiraClient();
				HttpURLConnection connection = jiraClient.getHttpURLConnection(url);
			    InputStream local = (InputStream)connection.getInputStream();
			    JesFtp ftp = new JesFtp();
			    ftp.connect(sTargetHost);
			    ftp.login();
			    result = ftp.storeFile(sTargetDSN, local);
			    if (result) System.out.println("Uploaded transmittal to " + sTargetDSN);
	      } 
	      catch(Exception e)
	      {
	    	  e.printStackTrace();
	    	  e.getLocalizedMessage();
	      }
		return result;
	}

	/**
	 * 
	 * Example of use:
	 *     URL url = new URL ("https://jira.rocketsoftware.com/secure/attachment/1091059/Transmittal.txt");
	 *     InputStream inStream = getTransmittalAsInputStream(url);
	 *     JesFTP jesFTP = new FTP();
	 *     jesFTP.connect(host);
	 *     jesFTP.login(); 
	 *     jesFTP.stroreFile("'DSN'", inStream);
	 * 	
	 * @param url
	 * @return
	 */
	public static InputStream getTransmittalAsInputStream(URL url) {
		InputStream inputStream = null;
		try {
			JiraClient jiraClient = new JiraClient();
			HttpURLConnection connection = jiraClient.getHttpURLConnection(url);
		    inputStream = (InputStream)connection.getInputStream();		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inputStream;
	}
	public static void printTransmittal(URL url) {
		System.out.println("----- Transmittal -----");
		//TODO
		
		try{
			BufferedReader in = new BufferedReader (new InputStreamReader(getTransmittalAsInputStream(url)));
	        String line;
	            while ((line = in.readLine()) != null) {
	                System.out.println(line);
	            }		}
		catch(IOException e) {
			
		}
		System.out.println("----- End of transmittal file ------");
		
	}
	
	public static void main(String[] args) {
		// 
		 try {
			 
	         URL url = new URL ("https://jira.rocketsoftware.com/secure/attachment/1091059/Transmittal.txt");
	         Transmittal.saveTransmittalToMVS(url, "RS22", "'TS5071.HLD0770.PKGTEXT'");
	         printTransmittal(url);
	         
			 /*BufferedReader in = new BufferedReader (new InputStreamReader (Transmittal.getTransmittalAsInputStream(url)));
			           String line;
			            while ((line = in.readLine()) != null) {
			                System.out.println(line);
			            }*/
	      } 
	      catch(Exception e){
	    	  
	    	  e.printStackTrace();
	    	  e.getLocalizedMessage();
	      }
	}

}

