package com.rocketsoftware.properties;

import java.io.File;

public class Properties {
	public static String ftpServer = "ftp.rocketsoftware.com";
	public static String ftpDirectory = "/ftpsite/IBM/SysTools/Packaging";
	public static String outputDirectoryForJcls =  System.getenv("WORKSPACE") + File.separator+ "output" + File.separator + System.getenv("BUILD_NUMBER");
	public static String jclSendToFtp = "FIXOUT";
	public static String jclToReceiveOnWinbox = "RECVWIN";
	public static String jclToReceiveOnArtifactoryHost = "RECVARTF";
	public static String jclToSendFromWinbox = "SENDWIN";
	public static String smpmcs = "SMPMCS";
	public static String functionXML = "functions.xml";
	public static String buildXML = "build.xml";
	public static String serviceXML = "service.xml";
	public static String sysmodAPAR = "apar";
}
