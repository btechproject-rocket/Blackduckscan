package com.rocketsoftware.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JclAparWinbox extends Jcl{
	private static final String LINE_SEPARATOR = System.lineSeparator();
	
	public void createJclForReceivingAparDatasetsOnWinbox(List<String> datasetNamesForReceiveOnFTP, String WINBOX_QUALIFIER, String APAR_NUMBER, String FMID) {
		List<String> datasetNamesForReceive = removeQualifierFromName(datasetNamesForReceiveOnFTP, FMID + "." + APAR_NUMBER);
		clean();
		String header = "//" + "SAVEAPAR" + " JOB ,CLASS=A,MSGCLASS=X,NOTIFY=&SYSUID" + LINE_SEPARATOR;
		appendToFile(header);
		appendToFile(generateDeleteStatement(WINBOX_QUALIFIER));
		appendToFile(generateAllocateStatement(datasetNamesForReceive, WINBOX_QUALIFIER));
		appendToFile(generateGetStatement(datasetNamesForReceive, WINBOX_QUALIFIER, APAR_NUMBER, FMID));
		appendToFile(generateReceiveStatement(datasetNamesForReceive, WINBOX_QUALIFIER));
		appendToFile(generateXmitCleanupStatement(WINBOX_QUALIFIER));
		//printJcl();

	}
	public void createJclForSendningAparFromWinbox(String FMID) {
		clean();
		String header = "//" + "SENDAPAR" + " JOB ,CLASS=A,MSGCLASS=X,NOTIFY=&SYSUID" + LINE_SEPARATOR;
		appendToFile(header);
		appendToFile(generateXmitStatement(FMID));
		appendToFile(generatePutStatement(FMID));
		//printJcl();
		
	}
	public String generateXmitStatement(String FMID) {
		StringBuffer buffer = new StringBuffer();
		String header = "//STEP1    EXEC PGM=IKJEFT01" + LINE_SEPARATOR +
				 		"//SYSPRINT  DD SYSOUT=*" + LINE_SEPARATOR +
				 		"//SYSTSPRT  DD SYSOUT=*" + LINE_SEPARATOR + 
				 		"//SYSTSIN   DD * " + LINE_SEPARATOR;
		buffer.append(header);  
	    String statement = " XMIT " + "WINMVS4E" + ".&SYSUID DATASET('" + "ROCKET" + "." + FMID + "." + "@P@R_NUMBER" + "') + " + LINE_SEPARATOR +
		" OUTDATASET('" + "ROCKET" + "." + FMID + "." + "@P@R_NUMBER" + ".XMIT')" + LINE_SEPARATOR;
		buffer.append(statement);
		buffer.append("/*" + LINE_SEPARATOR);
		return buffer.toString();
	}
	public String generatePutStatement(String FMID) {
		StringBuffer buffer = new StringBuffer();
		String header = "//STEP2  EXEC PGM=FTP" + LINE_SEPARATOR +
				 		"//OUTPUT DD SYSOUT=*" + LINE_SEPARATOR +
				 		"//NETRC  DD DISP=SHR,DSN=ROCKET.NETRC " + LINE_SEPARATOR + 
				 		"//INPUT  DD *" + LINE_SEPARATOR +
				 		" ftp.rocketsoftware.com" + LINE_SEPARATOR +
						" cd /ftpsite/IBM/SysTools/Packaging" + LINE_SEPARATOR +
						" binary " + LINE_SEPARATOR;
		buffer.append(header);  
		String statement = " PUT '" + "ROCKET" + "." + FMID + "." + "@P@R_NUMBER" + ".XMIT'" + "   " + FMID + "." + "@P@R_NUMBER" + LINE_SEPARATOR;
		buffer.append(statement);
		buffer.append(" QUIT" + LINE_SEPARATOR);
		buffer.append("/*" + LINE_SEPARATOR);
		return buffer.toString();
	}
	public String generateXmitCleanupStatement(String WINBOX_QUALIFIER) {
		StringBuffer buffer = new StringBuffer();
		String header = "//CLNUP EXEC PGM=IDCAMS" + LINE_SEPARATOR + 
				"//SYSPRINT DD SYSOUT=* " + LINE_SEPARATOR +
				"//SYSIN    DD *" + LINE_SEPARATOR;
		buffer.append(header);
		String delete = " DELETE " + WINBOX_QUALIFIER + ".*.XMIT" + LINE_SEPARATOR;
		buffer.append(delete);
		buffer.append(" SET MAXCC=0" + LINE_SEPARATOR);
		buffer.append("/*" + LINE_SEPARATOR);
		return buffer.toString();
	}
	public String generateDeleteStatement(String WINBOX_QUALIFIER) {
		StringBuffer buffer = new StringBuffer();
		String header = "//STEP1 EXEC PGM=IDCAMS" + LINE_SEPARATOR + 
						"//SYSPRINT DD SYSOUT=* " + LINE_SEPARATOR +
						"//SYSIN    DD *" + LINE_SEPARATOR;
		buffer.append(header);
		String delete = " DELETE " + WINBOX_QUALIFIER + ".*" + LINE_SEPARATOR +
						" DELETE " + WINBOX_QUALIFIER + ".*.XMIT" + LINE_SEPARATOR;
		buffer.append(delete);
		buffer.append(" SET MAXCC=0" + LINE_SEPARATOR);
		buffer.append("/*" + LINE_SEPARATOR);
		return buffer.toString();
	}
	public String generateAllocateStatement(List<String> datasetNamesForReceive , String WINBOX_QUALIFIER) {
		StringBuffer buffer = new StringBuffer();
		String header = "//IEFBR14  EXEC PGM=IEFBR14" + LINE_SEPARATOR;
		buffer.append(header);
		for(String dsn: datasetNamesForReceive) {
			String allocate = "//SYSALLOC DD  DSN=" + WINBOX_QUALIFIER + "." + dsn + ".XMIT," + LINE_SEPARATOR + 
					  		"//      DISP=(NEW,CATLG,CATLG)," + LINE_SEPARATOR + 
					  		"//      RECFM=FB,LRECL=80,BLKSIZE=3120, " + LINE_SEPARATOR + 
					  		"//      DSORG=PS," + LINE_SEPARATOR + 
					  		"//      SPACE=(TRK,(4000,95),RLSE) " + LINE_SEPARATOR;
			buffer.append(allocate);
		}
		return buffer.toString();
	}
	public String generateGetStatement(List<String> datasetNamesForReceive, String WINBOX_QUALIFIER, String APAR_NUMBER, String FMID) {
		StringBuffer buffer = new StringBuffer();
		String header = "//FTP    EXEC PGM=FTP" + LINE_SEPARATOR + 
						"//NETRC  DD DISP=SHR,DSN=ROCKET.NETRC " + LINE_SEPARATOR + 
						"//OUTPUT DD SYSOUT=*" + LINE_SEPARATOR + 
						"//INPUT  DD *" + LINE_SEPARATOR + 
						" ftp.rocketsoftware.com  " + LINE_SEPARATOR + 
						" cd /ftpsite/IBM/SysTools/Packaging" + LINE_SEPARATOR + 
						" binary" + LINE_SEPARATOR;   
		buffer.append(header);
		for(String dsn: datasetNamesForReceive) {
			String get = " GET " + FMID + "." + APAR_NUMBER + "." + dsn + " '" + WINBOX_QUALIFIER + "." + dsn + ".XMIT' (REP" + LINE_SEPARATOR;
			buffer.append(get);
		}
		buffer.append(" quit" + LINE_SEPARATOR);
		return buffer.toString();
	}
	public String generateReceiveStatement(List<String> datasetNamesForReceive, String WINBOX_QUALIFIER) {
		StringBuffer buffer = new StringBuffer();
		String header = "//RECV     EXEC PGM=IKJEFT01,REGION=4M" + LINE_SEPARATOR +
						"//SYSPRINT DD SYSOUT=*" + LINE_SEPARATOR +
						"//SYSTSPRT DD SYSOUT=* " + LINE_SEPARATOR +
						"//SYSTSIN  DD *" + LINE_SEPARATOR;
		buffer.append(header);	
		for(String dsn: datasetNamesForReceive) {
			String receive = " RECEIVE INDATASET('" + WINBOX_QUALIFIER + "." + dsn + ".XMIT')"  + LINE_SEPARATOR +
					 		 " DATASET('"  + WINBOX_QUALIFIER + "." + dsn + "')"  + LINE_SEPARATOR;
			buffer.append(receive);
		}
		buffer.append("/*" + LINE_SEPARATOR);
		return buffer.toString();
	}
	public List<String> removeQualifierFromName(List<String> namesWithQualifier, String QUALIFIER) {
		List<String> namesWithoutQualifier = new ArrayList<>();
		for(String dsn: namesWithQualifier) {
			namesWithoutQualifier.add(dsn.replace(QUALIFIER + ".", ""));
		}
		return namesWithoutQualifier;
	}
	public static void main(String args[]) {
		System.out.println(File.separator);
	}
	
}
