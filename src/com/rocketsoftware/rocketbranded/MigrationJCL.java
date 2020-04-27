package com.rocketsoftware.rocketbranded;

import java.util.ArrayList;
import java.util.List;

import com.rocketsoftware.builder.Jcl;
import com.rocketsoftware.filter.Filter;
import com.rocketsoftware.ftp.JesFtp;


public class MigrationJCL extends Jcl {
	
	private String INITIAL_HOST;
	private String INITIAL_DSN;
	private String QUALIFIER;
	private static final String LINE_SEPARATOR = System.lineSeparator();
	
	public MigrationJCL() {
		this.INITIAL_HOST = ProjectParameters.INITIAL_HOST;
		this.INITIAL_DSN = ProjectParameters.INITIAL_DSN;
		this.QUALIFIER = ProjectParameters.QUALIFIER;
	}
	public MigrationJCL(String test) {
		this.INITIAL_HOST = "DVLP";
		this.INITIAL_DSN = "MXH.HLD1003.HRM3268";
		this.QUALIFIER = "MXH.HLD1003";
	}
 	
	public void buildJCL() throws Exception  {
		System.out.println("----- Building JCL for dataset migration -----");
		JesFtp ftp = new JesFtp();
		ftp.connect(INITIAL_HOST);
		ftp.login();
		List<String> datasetNames = ftp.listDirectory(ftp.createMainframePathByQualifier(INITIAL_DSN));
		StringBuffer jclBuffer = new StringBuffer();
		String header = "//MCR16717 JOB ACCT#,'FTP PDS',NOTIFY=&SYSUID, " + LINE_SEPARATOR +       
						"// TIME=15,MSGLEVEL=1,REGION=0M,CLASS=A,MSGCLASS=X"   + LINE_SEPARATOR +      
						"//HOLD OUTPUT JESDS=ALL,DEFAULT=Y,OUTDISP=(HOLD,HOLD)" + LINE_SEPARATOR;
		
		String step = "//FTPSTEP  EXEC PGM=FTP,PARM='192.168.55.22 (EXIT'" + LINE_SEPARATOR + 
					  "//SYSPRINT DD   SYSOUT=*" + LINE_SEPARATOR +                             
					  "//NETRC    DD   DISP=(OLD),DSN=&SYSUID..NETRC" + LINE_SEPARATOR +                          	                        
					  "//INPUT    DD *" + LINE_SEPARATOR + LINE_SEPARATOR;  
		String input = buildInput(datasetNames);
		jclBuffer.append(header);
		jclBuffer.append(step);
		jclBuffer.append(input);
		jclBuffer.append("Close" + LINE_SEPARATOR);
		jclBuffer.append("Quit" + LINE_SEPARATOR);
		jclBuffer.append("//" + LINE_SEPARATOR);
		clean();
		appendToFile(jclBuffer.toString());
		System.out.println("----- End -----" + LINE_SEPARATOR);
	}
	
	private String buildInput(List<String> datasetNames) throws Exception {
		List<String> namesWithoutQuotes = removeQuotes(datasetNames);
		/*List<String> normalizedDsns = getNormalizedList(namesWithoutQuotes);
 		List<String> filteredNames = Filter.filterDsns(normalizedDsns, INITIAL_DSN);*/
		StringBuffer buffer = new StringBuffer();
		for(String dsn: namesWithoutQuotes) {		
			String temp;
			String initialName = dsn.replace(INITIAL_DSN + ".", ""); //MESSAGES, //S***MSGS
			String correctName = renameDsn(dsn.replace(INITIAL_DSN + ".", ""), getProjectName());//MSGS
			if(correctName != null) {
				//If there was a match in the table check if this dataset is allowed
				if(!Filter.checkDsn(correctName, INITIAL_DSN)){
					continue;
				}
			}
			else {
				//If no renaming we check initial dataset name
				if(!Filter.checkDsn(initialName, INITIAL_DSN)){
					continue;
				}
				else {
					
					correctName = initialName;
				}
			}
			
			if(correctName.equals("OUTLINK")) {
				temp = 	  "Site recfm=FBA lrecl=121 blksize=27951" + LINE_SEPARATOR + 
						  "Site pri=20 sec=10 Directory=10 TR" + LINE_SEPARATOR +     
						  "MKDIR '" + QUALIFIER + "." + correctName + "'" + LINE_SEPARATOR +
						  "cd    '" + QUALIFIER + "." + correctName + "'" + LINE_SEPARATOR +                                      
						  "LCD   '" + dsn + "'" + LINE_SEPARATOR +
						  "mput *" + LINE_SEPARATOR + LINE_SEPARATOR;
			}
			else if(correctName.equals("PKGTEXT")) {
				/*
				 * We skip PKGTEXT because we take the transmittal file straight from JIRA
				 * and put it on BUILD_HOST
				 */
				continue;
			}
			else if(correctName.equals("LOADLIB")) {
				temp =   
						  "MKDIR '" + QUALIFIER + "." + correctName + "'" + " (LIKE '"+ dsn + "'" + LINE_SEPARATOR +
						  "cd    '" + QUALIFIER + "." + correctName + "'" + LINE_SEPARATOR +                                      
						  "LCD   '" + dsn + "'" + LINE_SEPARATOR +
						  "mput *" + LINE_SEPARATOR + LINE_SEPARATOR;
			}
			
			else {
				temp =    "Site recfm=FB  lrecl=80  blksize=27951" + LINE_SEPARATOR + 
						  "Site pri=50 sec=20 Directory=10 CY" + LINE_SEPARATOR + 
						  "MKDIR '" + QUALIFIER + "." + correctName + "'" + LINE_SEPARATOR +
						  "cd    '" + QUALIFIER + "." + correctName + "'" + LINE_SEPARATOR +                                      
						  "LCD   '" + dsn + "'" + LINE_SEPARATOR +
						  "mput *" + LINE_SEPARATOR + LINE_SEPARATOR;
			}
			buffer.append(temp);
		}
		return buffer.toString();
	}
	
	/**
	 * This method renames datasets when sending them from one system to another
	 * <p>
	 * @param initialDsn - the name of library on INITIAL_HOST
	 * @param projectName - name of the project Ex: MCR
	 * @return
	 */
	private String getProjectName() {
		return INITIAL_DSN.substring(0,3);
	}
	private List<String> getNormalizedList(List<String> allDsns) {
		//Map<String,String> normalizedMap = new HashMap<>();
		List<String> normalizedDsns = new ArrayList<>();
		for(String dsn: allDsns) {
			String notNoramlizedName = dsn.replace(INITIAL_DSN + ".", ""); //EXAMPLE: MESSAGES, SMXHMSGS 
			String normalizedName = renameDsn(dsn.replace(INITIAL_DSN + ".", ""), getProjectName()); //EXAMPLE: MSGS
			if(normalizedName != null) {			
				normalizedDsns.add(dsn.replace(notNoramlizedName, normalizedName));
			}

		}
		return normalizedDsns;
	}
	private String renameDsn(String initialDsn, String projectName) {
		
		String[] objlibArray = {"OBJLIB", "OBJ", "S" + projectName + "OBJ"};
		String[] loadArray = {"LOADLIB","LOAD","S" + projectName + "LOAD"};
		String[] outlinkArray = {"OUTLINK","LMAP"};
		String[] messagesArray = {"MSGS","MESSAGES","S" + projectName + "MSGS"};
		String[] cntlArray = {"CNTL", "S" + projectName + "CNTL"};
		String[] ispplib = {"ISPPLIB", "S" + projectName + "PENU"};
		String[] ispmlib = {"ISPMLIB", "S" + projectName + "MENU"};
		String[] isptlib = {"ISPTLIB", "S" + projectName + "TENU"};
		String[] parmlib = {"PARMLIB", "S" + projectName + "PARM"};
		String[] samplib = {"SAMPLIB", "S" + projectName + "SAMP"};
		Object[] arrays = {objlibArray,loadArray,outlinkArray,messagesArray,cntlArray,ispplib,ispmlib,isptlib,parmlib,samplib};
		for(Object arr: arrays) {
			String[] strArr = (String[])arr;
			for(String dsnInTable: strArr) {
				if(dsnInTable.equals(initialDsn)) {
					if(!initialDsn.equals(strArr[0])) {
						System.out.println("DSN was changed from " + initialDsn + " to " + strArr[0] + System.lineSeparator());
					}
					else {
						System.out.println("DSN was left as it is: " + initialDsn + System.lineSeparator());
					}
					return strArr[0];
				}
				
			}
		}
		//In case there is no match in the table we leave it as it is 
		System.out.println("Nothing has been done to "  + initialDsn + "\nREASON: no match in the table" + System.lineSeparator());
		return null;
	}
	
	private List<String> removeQuotes(List<String> names) {
		List<String> namesWithoutQuotes = new ArrayList<>();
		for(String dsn: names) {
			StringBuffer buffer = new StringBuffer(dsn);
			buffer.deleteCharAt(0);
			buffer.deleteCharAt(buffer.length() - 1);
			dsn = buffer.toString();
			namesWithoutQuotes.add(dsn);
		}
		return namesWithoutQuotes;
	}
	public static void main(String args[]) {
		MigrationJCL jcl = new MigrationJCL("test");
		try {
			jcl.buildJCL();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//renameDsn("xc","MCR");
		
	}
	public void  submitMigrationJcl() {
		
	}

}
