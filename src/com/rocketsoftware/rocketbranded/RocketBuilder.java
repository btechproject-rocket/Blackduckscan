package com.rocketsoftware.rocketbranded;


import java.net.URL;

import com.rocketsoftware.ftp.JesFtp;
import com.rocketsoftware.ftp.JesLog;

public class RocketBuilder {
	
	

	public static void main(String[] args) {
		
		URL transmittalURL;
		try {
			/*1. Reading environment variables from Jenkins 
			 *    
			 */			
			ProjectParameters.readJenkinsParameters();	
			ProjectParameters.printJenkinsParameters();
			/*
			 * 2. Saves transmittal directly to BUILD_HOST MCR.HLD0770.PKGTEXT
			 */
			transmittalURL = new URL(ProjectParameters.TRANSMITTAL_URL);
			Transmittal.saveTransmittalToMVS(transmittalURL, ProjectParameters.BUILD_HOST,"'" +  ProjectParameters.QUALIFIER + ".PKGTEXT'");
			Transmittal.printTransmittal(transmittalURL);
			/*
			 * 3. Building a JCL to send files from 
			 */
			MigrationJCL jcl = new MigrationJCL();
			jcl.buildJCL();
			JesFtp ftp = new JesFtp();
			ftp.connect(ProjectParameters.INITIAL_HOST);
			ftp.login();
			ftp.submit(ProjectParameters.outputJCL);
			ftp.logout();
			/*
			 * 4. Parsing JCL from Git that generates PTF
			 * 	  Replacing sequence number, apar number, ticket number
			 */
			ftp.connect(ProjectParameters.BUILD_HOST);
			ftp.login();
			ParseJCL parser = new ParseJCL();
			parser.changeJCL(ProjectParameters.PTF_JCL, ProjectParameters.TICKET_NUMBER);
			JesLog newLog = ftp.submit(ProjectParameters.outputJCL);
			
			for(String line: newLog.getStepsInfos()) {
				System.out.println(line);
			}
			/*
			 * 5. Parsing JCL from Git that applies PTF to SMPE environment on BUILD_HOST
			 * 	  Replacing sequence number, apar number
			 */
			parser.changeJCL(ProjectParameters.APPLY_JCL, ProjectParameters.TICKET_NUMBER);
			ftp.submit(ProjectParameters.outputJCL);
			/*
			 * 6. Parsing JCL from Git that sends PTF to a ticket and FTP server
			 */
			parser.changeJCL(System.getenv("OUT_JCL"), ProjectParameters.TICKET_NUMBER);
			ftp.submit(ProjectParameters.outputJCL);
			
			ftp.logout();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
	}

}
