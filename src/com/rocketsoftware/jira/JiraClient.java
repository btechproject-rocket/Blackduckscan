package com.rocketsoftware.jira;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;


public class JiraClient {
	
	public JiraClient() {
		// TODO Auto-generated constructor stub
	}
	
	public HttpURLConnection getHttpURLConnection(URL url) {
		HttpURLConnection connection = null;
		try {
		    connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("GET");
		    connection.setDoOutput(true);
		    connection.setRequestProperty("Authorization", "Basic " + getBase64EncodedCredentioals());
		    System.out.println("Reading from " + url);
		} catch(Exception e) {
			e.printStackTrace();
		}
	    return connection;
	}
	
	
	
	private String getBase64EncodedCredentioals() throws UnsupportedEncodingException {
		
		String userName = "packagingscript";//System.getenv("JIRA_SA.USER");
		String passwd = "rd172ff";//System.getenv("JIRA_SA.PASSWORD");	
		
		String toEncode = userName + ":" +passwd;
		return Base64.encodeBase64String(toEncode.getBytes("UTF8"));
	}
	
	public static void main(String[] args) {
		
		String userName = System.getenv("JIRA_SA.USER");
		String passwd = System.getenv("JIRA_SA.PASSWORD");	
		String toEncode = userName + ":" + passwd;
		
		try {
			System.out.println(Base64.encodeBase64String(toEncode.getBytes("UTF8")));
			System.exit(0);
			JiraClient jiraClient = new JiraClient();
			URL url = new URL ("https://jira.rocketsoftware.com/secure/attachment/1091059/Transmittal.txt");			
			HttpURLConnection connection = jiraClient.getHttpURLConnection(url);
			InputStream content = (InputStream)connection.getInputStream();
			BufferedReader in = new BufferedReader (new InputStreamReader (content));
	        String line;
	        while ((line = in.readLine()) != null) {
	            System.out.println(line);
	        }
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		
	}

}
