package com.rocketsoftware.rocketbranded;

import com.rocketsoftware.exception.ParameterException;

public class ProjectParameters {
	
	public static String INITIAL_HOST;
	public static String INITIAL_DSN;
	public static String TICKET_NUMBER;
	public static String TRANSMITTAL_URL;
	public static String BUILD_HOST;
	public static String QUALIFIER;
	public static String PTF_JCL;
	public static String OUT_JCL;
	public static String APPLY_JCL;
	public static String FTP;
	public static String outputJCL = "output/jcl";
	
	public static void readJenkinsParameters() throws Exception {
		INITIAL_HOST = System.getenv("INITIAL_HOST");
		INITIAL_DSN = System.getenv("INITIAL_DSN");
		TICKET_NUMBER = System.getenv("TICKET_NUMBER");
		TRANSMITTAL_URL = System.getenv("TRANSMITTAL_URL");
		BUILD_HOST = System.getenv("BUILD_HOST");
		QUALIFIER = System.getenv("QUALIFIER");
		PTF_JCL = System.getenv("PTF_JCL");
		OUT_JCL = System.getenv("OUT_JCL");
		APPLY_JCL = System.getenv("APPLY_JCL");
		FTP = System.getenv("FTP");
		validateParameters();
	}
	private static void validateParameters() throws Exception {
		if(INITIAL_HOST == null) {
			throw new ParameterException("INITIAL_HOST");
		}
		else if(INITIAL_DSN == null) {
			throw new ParameterException("INITIAL_DSN");
		}
		else if(TICKET_NUMBER == null) {
			throw new ParameterException("TICKET_NUMBER");
		}
		else if(TRANSMITTAL_URL == null) {
			throw new ParameterException("TRANSMITTAL_URL");
		}
		else if(BUILD_HOST == null) {
			throw new ParameterException("BUILD_HOST");
		}
		else if(QUALIFIER == null) {
			throw new ParameterException("QUALIFIER");
		}
		else if(PTF_JCL == null) {
			throw new ParameterException("PTF_JCL");
		}
		else if(OUT_JCL == null) {
			throw new ParameterException("OUT_JCL");
		}
		else if(APPLY_JCL == null) {
			throw new ParameterException("APPLY_JCL");
		}
		else if(FTP == null) {
			throw new ParameterException("FTP");
		}
	}
	public static void printJenkinsParameters() {
		System.out.println("------- Jenkins parameters ------");
		System.out.println("INITIAL_HOST: " + INITIAL_HOST);
		System.out.println("INITIAL_DSN: " + INITIAL_DSN);
		System.out.println("TICKET_NUMBER: " + TICKET_NUMBER);
		System.out.println("TRANSMITTAL_URL: " + TRANSMITTAL_URL);
		System.out.println("BUILD_HOST: " + BUILD_HOST);
		System.out.println("QUALIFIER: " + QUALIFIER);
		System.out.println("PTF_JCL: " + PTF_JCL);
		System.out.println("OUT_JCL: " + OUT_JCL);
		System.out.println("APPLY_JCL: " + APPLY_JCL);
		System.out.println("------- End ------");
		System.out.println(System.lineSeparator());
		
		
	}
	
	public static void main(String args[]) {
		System.out.println("Before calling readJenkinsParameters(): " + INITIAL_HOST );
		try {
			readJenkinsParameters();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("After calling readJenkinsParameters(): " + INITIAL_HOST );
	}
}
