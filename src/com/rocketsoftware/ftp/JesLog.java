package com.rocketsoftware.ftp;

import java.util.ArrayList;

public class JesLog {
	ArrayList<String> sLog = null;
	ArrayList<String> stepsInfo = null;
	String sLogName = "";
	String sExitCode = "";

	public JesLog(ArrayList<String> sJesLog, String logName, String exitCode) {
		
		sLog = sJesLog;
		sLogName = logName;
		sExitCode = exitCode;
		setStepsInfo();
	}
	


	public String getLog() {
		return sLog.toString();
	}

	public ArrayList<String> getLogAsArrayList() {
		return sLog;
	}

	
	public String getExitCode() {
		return sExitCode;
	}

	/**
	 * returns a few strings with step names and RC
	 * 
	 *  To-Do parse Log file with regular expressions.
	 * 
	 */
	private void setStepsInfo() {
		
		stepsInfo = new ArrayList<String>();
		
        int beginIndex = -1;
        
		for (String sLine: sLog) {			
			// Read until 
			if (sLine.indexOf("JES2 JOB STATISTICS") > 0) {
	    		break;
	    	}
	    	
	    	if (sLine.indexOf("RKTSW01I") > 0) {	            		
	    		/*
	    		 * Jes LOG looks like this
	    		 *  01.14.34 J0657118  RKTSW01I   JOBNAME  STEPNAME PROCSTEP STEP    RC  CPU (Total)  CPU (TCB)    CPU (SRB)     zIIP Usage
	    		 *  01.14.34 J0657118  RKTSW01I   BASFIXOT BLDMCS              1     00  00:00:00.06  00:00:00.06  00:00:00.00  00:00:00.00
	    		 *  
	    		 *  Remember column index
	    		 */
	    		if ((beginIndex == -1) && (sLine.indexOf("STEPNAME") > -1 )) {
	    			// it is time to add steps to ArrayList
	    			beginIndex = sLine.indexOf("STEPNAME");
	    		}
	    		if (beginIndex > -1 ) stepsInfo.add(sLine.substring(beginIndex, beginIndex +30).trim());
	    	}	    	
		}    
	    stepsInfo.add("RC of the Job is " + getExitCode());    
	}	
	
	public ArrayList<String> getStepsInfos() {			        
		return stepsInfo;
	}
	
	public String getLogName() {
		return sLogName;
	}
	

}
