package com.rocketsoftware.main;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.rocketsoftware.builder.Jcl;
import com.rocketsoftware.builder.JclApar;
import com.rocketsoftware.builder.ZServiceXmlBuilder;
import com.rocketsoftware.ftp.JesFtp;
import com.rocketsoftware.jenkins.JenkinsParameters;
import com.rocketsoftware.log.Log;
import com.rocketsoftware.properties.Properties;
import com.rocketsoftware.smpe.MCSHandler;
import com.rocketsoftware.transfer.DatasetTransfer;

public class BuildApar {
	
	
	public static void main(String[] args) {
		// Move datasets to Rocket FTP
		// create .xml files
		// create JCLs
		try {	
			
			Log.configureLog();
			JenkinsParameters.readJenkinsParameters();
			
			JesFtp ftp = new JesFtp();
			ftp.connect(JenkinsParameters.INITIAL_HOST);
			ftp.login();
			
			Files.createDirectories(Paths.get(Properties.outputDirectoryForJcls));
			
			DatasetTransfer transfer = new DatasetTransfer();
			// Sending datasets from INITIAL_HOST to Rocket Ftp 
			transfer.sendAparDatasetsToFTP(ftp, JenkinsParameters.INITIAL_HOST, JenkinsParameters.INITIAL_QUALIFIER, JenkinsParameters.APAR_NUMBER, JenkinsParameters.FMID);
			
			
			// Save SMPMCS
			String sSMPMCS = Properties.outputDirectoryForJcls + File.separator + Properties.smpmcs;
			//ftp.download(JenkinsParameters.INITIAL_QUALIFIER + "." + Properties.smpmcs, sSMPMCS);
					
			// Generate XMLs for zService
			//  - fuction.xml
			//  - build.xml
			//  - service.xml 
			//MCSHandler mcsHandler = new MCSHandler(ftp, transfer.getListOfDatasetToTransfer(), sSMPMCS);
			//mcsHandler.saveFunctionsXML(Paths.get(Properties.outputDirectoryForJcls, Properties.functionXML));
			ZServiceXmlBuilder.saveZServiceSubmitJobXML(Paths.get(Properties.outputDirectoryForJcls, "subJob.xml"), new String(Properties.outputDirectoryForJcls + File.separator + Properties.jclToReceiveOnWinbox));
			ZServiceXmlBuilder.saveBuildXML(Paths.get(Properties.outputDirectoryForJcls, Properties.buildXML), "usermod");
			ZServiceXmlBuilder.saveServiceXML(Paths.get(Properties.outputDirectoryForJcls, Properties.serviceXML), "usermod");
			
			// Refactor this
			transfer.saveJclRecieveDatasetsOnWinbox(JclApar.datasetsForAparOnFTP, JenkinsParameters.WINBOX_QUALIFIER, JenkinsParameters.APAR_NUMBER, JenkinsParameters.FMID);
			
			Jcl jcl = new Jcl();
			jcl.saveJclTo(Properties.outputDirectoryForJcls + File.separator + Properties.jclToReceiveOnWinbox);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(24);
		}

	}

}
