package com.rocketsoftware.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.rocketsoftware.properties.Properties;

public class JclApar extends Jcl  {
	
	private static final String LINE_SEPARATOR = System.lineSeparator();
	public static List<String> datasetsForAparOnFTP = new ArrayList<>();
	
	public JclApar() {
		
	}
	/**
	 * @param datasetNames - datasets that we will send to FTP server
	 * @param INITIAL_HOST - system where datasets for APAR are present
	 * @param INITIAL_QUALIFIER - directory where datasets for APAR are present
	 * @param APAR_NUMBER - number of APAR
	 */
	public void createJclForSendingDatasetsToFTP(List<String> datasetNames, String INITIAL_HOST, String INITIAL_QUALIFIER, String APAR_NUMBER, String FMID) {
		clean();//cleaning the output file
		String header = "//" + "SENDAPAR" + " JOB ,CLASS=A,MSGCLASS=X,NOTIFY=&SYSUID" + LINE_SEPARATOR;//creating header of a JCL
		appendToFile(header);
		appendToFile(generateXmitStatement(datasetNames, INITIAL_HOST));
		appendToFile(generatePutStatement(datasetNames, APAR_NUMBER, INITIAL_QUALIFIER, FMID));
		//printJcl();		
	}
	public void createJclForReceivingDatasetsFromFTP(String ARTIFACTORY_QUALIFIER, String FMID) {
		clean();
		String header = "//" + "SAVEAPAR" + " JOB ,CLASS=A,MSGCLASS=X,NOTIFY=&SYSUID" + LINE_SEPARATOR;
		appendToFile(header);
		appendToFile(generateDeleteStatement(ARTIFACTORY_QUALIFIER));
		appendToFile(generateAllocateStatement(ARTIFACTORY_QUALIFIER));
		appendToFile(generateGetStatement(ARTIFACTORY_QUALIFIER, FMID)); 
		appendToFile(generateReceiveStatement(ARTIFACTORY_QUALIFIER));
		//printJcl();
	}
	/**
	 * 
	 * @param names
	 * @param INITIAL_HOST
	 * @return
	 */
	private String generateXmitStatement(List<String> names, String INITIAL_HOST) {
		StringBuffer buffer = new StringBuffer();
		String header = "//STEP1    EXEC PGM=IKJEFT01" + LINE_SEPARATOR +
				 		"//SYSPRINT  DD SYSOUT=*" + LINE_SEPARATOR +
				 		"//SYSTSPRT  DD SYSOUT=*" + LINE_SEPARATOR + 
				 		"//SYSTSIN   DD * " + LINE_SEPARATOR;
		buffer.append(header);  
		for(String dsn: names) {
			String statement = " XMIT " + INITIAL_HOST + ".&SYSUID DATASET('" + dsn + "') + " + LINE_SEPARATOR +
							   " OUTDATASET('" + dsn + ".XMIT')" + LINE_SEPARATOR;
			buffer.append(statement);
		}
		buffer.append("/*" + LINE_SEPARATOR);
		return buffer.toString();
	}
	/**
	 * 
	 * @param names
	 * @param APAR_NUMBER
	 * @param INITIAL_QUALIFIER
	 * @return
	 */
	private String generatePutStatement(List<String> names, String APAR_NUMBER, String INITIAL_QUALIFIER, String FMID) {
		StringBuffer buffer = new StringBuffer();
		String header = "//STEP2  EXEC PGM=FTP" + LINE_SEPARATOR +
				 		"//OUTPUT DD SYSOUT=*" + LINE_SEPARATOR +
				 		"//NETRC  DD DISP=SHR,DSN=&SYSUID..NETRC " + LINE_SEPARATOR + 
				 		"//INPUT  DD *" + LINE_SEPARATOR +
				 		" ftp.rocketsoftware.com" + LINE_SEPARATOR +
						" cd /ftpsite/IBM/SysTools/Packaging" + LINE_SEPARATOR +
						" binary " + LINE_SEPARATOR;
		buffer.append(header);  
		for(String dsn: names) {
			String statement = " PUT '" + dsn + ".XMIT'" + "   " + FMID + "." + APAR_NUMBER + dsn.replace(INITIAL_QUALIFIER, "") + LINE_SEPARATOR;
			buffer.append(statement);
			datasetsForAparOnFTP.add(FMID + "." + APAR_NUMBER + dsn.replace(INITIAL_QUALIFIER, ""));
		}
		buffer.append(" QUIT" + LINE_SEPARATOR);
		buffer.append("/*" + LINE_SEPARATOR);
		return buffer.toString();
	}
	/**
	 * 
	 * @param ARTIFACTORY_QUALIFIER
	 * @param APAR_NUMBER
	 * @return
	 */
	private String generateDeleteStatement(String ARTIFACTORY_QUALIFIER) {
		StringBuffer buffer = new StringBuffer();
		String header = "//STEP1 EXEC PGM=IDCAMS" + LINE_SEPARATOR + 
						"//SYSPRINT DD SYSOUT=* " + LINE_SEPARATOR +
						"//SYSIN    DD *" + LINE_SEPARATOR;
		buffer.append(header);
		String delete = " DELETE " + ARTIFACTORY_QUALIFIER + "." + "@P@R_NUMBER" + ".APAR" + LINE_SEPARATOR +
						" DELETE " + ARTIFACTORY_QUALIFIER + "." + "@P@R_NUMBER" + ".APAR.XMIT" + LINE_SEPARATOR;
		buffer.append(delete);
		buffer.append(" SET MAXCC=0" + LINE_SEPARATOR);
		buffer.append("/*" + LINE_SEPARATOR);
        
		return buffer.toString();
	}
	/**
	 * 
	 * @param names
	 * @param APAR_NUMBER
	 * @param ARTIFACTORY_QUALIFIER
	 * @return
	 */
	private String generateGetStatement(String ARTIFACTORY_QUALIFIER, String FMID) {
		StringBuffer buffer = new StringBuffer();
		String header = "//FTP    EXEC PGM=FTP" + LINE_SEPARATOR + 
						"//NETRC  DD DISP=SHR,DSN=&SYSUID..NETRC " + LINE_SEPARATOR + 
						"//OUTPUT DD SYSOUT=*" + LINE_SEPARATOR + 
						"//INPUT  DD *" + LINE_SEPARATOR + 
						" ftp.rocketsoftware.com  " + LINE_SEPARATOR + 
						" cd /ftpsite/IBM/SysTools/Packaging" + LINE_SEPARATOR + 
						" binary" + LINE_SEPARATOR;   
		buffer.append(header);
		String get = " GET " + FMID + "." + "@P@R_NUMBER" + " '" + ARTIFACTORY_QUALIFIER + "." + "@P@R_NUMBER" + ".APAR.XMIT'" + " (REP    " + LINE_SEPARATOR;
		buffer.append(get);
		buffer.append("quit" + LINE_SEPARATOR);
		return buffer.toString();
	}
	/**
	 * 
	 * @param ARTIFACTORY_QUALIFIER
	 * @param APAR_NUMBER
	 * @return
	 */
	private String generateAllocateStatement(String ARTIFACTORY_QUALIFIER) {
		StringBuffer buffer = new StringBuffer();
		String header = "//IEFBR14  EXEC PGM=IEFBR14" + LINE_SEPARATOR;
		buffer.append(header);
		String allocate = "//SYSALLOC DD  DSN=" + ARTIFACTORY_QUALIFIER + "." + "@P@R_NUMBER" + ".APAR.XMIT," + LINE_SEPARATOR + 
						  "//      DISP=(NEW,CATLG,CATLG)," + LINE_SEPARATOR + 
						  "//      RECFM=FB,LRECL=80,BLKSIZE=3120, " + LINE_SEPARATOR + 
						  "//      DSORG=PS," + LINE_SEPARATOR + 
						  "//      SPACE=(TRK,(4000,95),RLSE) " + LINE_SEPARATOR;
		buffer.append(allocate);          
		return buffer.toString();
	}
	
	private String generateReceiveStatement(String ARTIFACTORY_QUALIFIER) {
		StringBuffer buffer = new StringBuffer();
		String header = "//RECV     EXEC PGM=IKJEFT01,REGION=4M" + LINE_SEPARATOR +
						"//SYSPRINT DD SYSOUT=*" + LINE_SEPARATOR +
						"//SYSTSPRT DD SYSOUT=* " + LINE_SEPARATOR +
						"//SYSTSIN  DD *" + LINE_SEPARATOR;
		buffer.append(header);
		String receive = " RECEIVE INDATASET('" + ARTIFACTORY_QUALIFIER + "." + "@P@R_NUMBER" + ".APAR.XMIT')"  + LINE_SEPARATOR +
						 " DATASET('"  + ARTIFACTORY_QUALIFIER + "." + "@P@R_NUMBER" + ".APAR')"  + LINE_SEPARATOR;
		buffer.append(receive);
		buffer.append("/*" + LINE_SEPARATOR);
		                                         
		                                                
		return buffer.toString();
	}
	
}
	