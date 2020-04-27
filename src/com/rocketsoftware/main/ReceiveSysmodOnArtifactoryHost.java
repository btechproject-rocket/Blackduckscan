package com.rocketsoftware.main;

import java.io.File;

import com.rocketsoftware.ftp.JesFtp;
import com.rocketsoftware.properties.Properties;

public class ReceiveSysmodOnArtifactoryHost {

	public static void main(String[] args) {
		JesFtp ftp = new JesFtp();
		String sHost = System.getProperty("HOST").trim();
		if (sHost.equalsIgnoreCase("None")) System.exit(0);
		ftp.connect(sHost);
		ftp.login();
		
		ftp.submit(Properties.outputDirectoryForJcls + File.separator + Properties.jclToReceiveOnArtifactoryHost);
		

	}

}
