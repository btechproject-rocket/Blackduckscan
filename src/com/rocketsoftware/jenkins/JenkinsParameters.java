package com.rocketsoftware.jenkins;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.rocketsoftware.log.Log;

public class JenkinsParameters {
	
	public static String INITIAL_HOST;
	public static String INITIAL_QUALIFIER;
	public static String JIRA_TICKET;
	public static String TRANSMITTAL_URL;
	public static String BUILD_TYPE;
	public static String APAR_NUMBER;
	public static String PTF_NUMBER;
	public static String ARTIFACTORY_HOST;
	public static String ARTIFACTORY_QUALIFIER;
	public static String FMID;
	public static String WINBOX_QUALIFIER;
	public static String PATH_TO_WINBOX_JCL;
	public static String SREL;
	public static String COMP_ID;
	public static String RELEASE_NAME;
	public static String Z_SERVER;
	public static String WINBOX_USER;
	public static String WINBOX_PASSWORD;
	private static Logger log = Log.log;
	
	public static void readJenkinsParameters() throws Exception {
		INITIAL_HOST = System.getenv("INITIAL_HOST");
		INITIAL_QUALIFIER = System.getenv("INITIAL_QUALIFIER");
		JIRA_TICKET = System.getenv("JIRA_TICKET");
		TRANSMITTAL_URL = System.getenv("TRANSMITTAL_URL");
		BUILD_TYPE = System.getenv("BUILD_TYPE");
		APAR_NUMBER = System.getenv("APAR_NUMBER");
		PTF_NUMBER = System.getenv("PTF_NUMBER");
		ARTIFACTORY_HOST = System.getenv("ARTIFACTORY_HOST");
		ARTIFACTORY_QUALIFIER = System.getenv("ARTIFACTORY_QUALIFIER");
		FMID = System.getenv("FMID");
		SREL = System.getenv("SREL");
		COMP_ID = System.getenv("COMP_ID");
		RELEASE_NAME = System.getenv("RELEASE_NAME");
		Z_SERVER = System.getenv("Z_SERVER");
		WINBOX_QUALIFIER = System.getenv("WINBOX_QUALIFIER");
		PATH_TO_WINBOX_JCL = System.getenv("PATH_TO_WINBOX_JCL");
		WINBOX_USER = System.getenv("WINBOX_USER");
		WINBOX_PASSWORD = System.getenv("WINBOX_PASSWORD");
		validateParameters();
		printJenkinsParameters();
	}
	private static void validateParameters() throws Exception {
		log.log(Level.INFO, "Validating Jenkins parameters");
	}
	public static void printJenkinsParameters() {
		log.log(Level.INFO, "Printing Jenkins parameters: ");
		log.log(Level.INFO, getParametersAsString());
	}
	
	public static String getParametersAsString() {
		return "\n" + "                                   " + "INITIAL_HOST: " + INITIAL_HOST + "\n" +
				"                                   " + "INITIAL_QUALIFIER: " + INITIAL_QUALIFIER + "\n" + 
				"                                   " + "JIRA_TICKET: " + JIRA_TICKET + "\n" + 
				"                                   " + "TRANSMITTAL_URL: " + TRANSMITTAL_URL + "\n" + 
				"                                   " + "BUILD_TYPE: " + BUILD_TYPE + "\n" + 
				"                                   " + "APAR_NUMBER: " + APAR_NUMBER + "\n" + 
				"                                   " + "PTF_NUMBER: " + PTF_NUMBER + "\n" + 
				"                                   " + "ARTIFACTORY_HOST: " + ARTIFACTORY_HOST + "\n" + 
				"                                   " + "ARTIFACTORY_QUALIFIER: " + ARTIFACTORY_QUALIFIER+ "\n" + 
				"                                   " + "FMID: " + FMID + "\n" + 
				"                                   " + "WINBOX_QUALIFIER: " + WINBOX_QUALIFIER + "\n" + 
				"                                   " + "PATH_TO_WINBOX_JCL: " + PATH_TO_WINBOX_JCL + "\n";
	}
	public static void main(String args[]) {
		
	}
}
