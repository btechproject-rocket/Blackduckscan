package com.rocketsoftware.smpe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rocketsoftware.ftp.JesFtp;
import com.rocketsoftware.jenkins.JenkinsParameters;
import com.rocketsoftware.log.Log;
import com.rocketsoftware.properties.Properties;


public class MCSHandler {
	
	// Key is DSN name, value is array of members name
	// e.g. RSBLD.IFPS.FPSP130.PH01862.SAMPLE -> {"FABAOP1J", "FABCOP1J", "FABAOP3J"}
	private HashMap<String, String[]> mapDSN = new HashMap<>();
	private ArrayList<String> alMissingMembersFromMcs = new ArrayList<>();
	private ArrayList<String> alMcsSuffixForFunctionXml = new ArrayList<>();
	private static Logger log = Log.log;
	
	// 
	private String smpmcsName = Properties.smpmcs; 
	private String smpmcsFile = "";
	
	public MCSHandler(JesFtp ftp, List<String> sDSN, String smpmcsLocal) {
		smpmcsFile = smpmcsLocal;
		// Save DSNs as keys and members as values to HashMap
		for(String sDsnName: sDSN) {
			sDsnName = sDsnName.replace('\'', ' ').trim();
			log.log(Level.FINE, "DSN name " + sDsnName);
			// SMPMCS has no members. it is a sequential DSN.
			if (sDsnName.endsWith(smpmcsName)) continue;
			// Remove ' from DSN names
			
			try {
				ftp.changeWorkingDirectory("'" + sDsnName + "'");
				ftp.getReplyString();
				String[] members = ftp.listNames();
				ftp.getReplyString();
				
				for (int i = 0; i < members.length; i++) {
					log.log(Level.FINE, "\t" + members[i]);
				}
				
				mapDSN.put(sDsnName, members);
				
			} catch(IOException e) {
				log.log(Level.WARNING, "Unable to read " + sDsnName);
				log.log(Level.WARNING, e.getMessage());
			}
			
		}
		if (!checkMCSAgainstMembersAndBuild()) System.exit(-12);
		
		
	}
	
	/**
	 * We need this to generate functions.xml. 
	 * Example:
	 * @param sDsnFullName = "RSBLD.IFPS.FPSP130.PH01862.DATA" 
	 * @return "DATA"
	 */
	private String getDsnLastName(String sDsnFullName) {		
		String[] parts = sDsnFullName.split("\\.");
		return parts[parts.length - 1];
	}
	
	// Check for missing members
	private boolean checkMCSAgainstMembersAndBuild() {	
		
		List<String> lMCSLines = getSMPMCSAsArrayList();		
		String sDsnLastName = "";
		// Iterate over all DSNs
		for(Map.Entry<String, String[]> entry: mapDSN.entrySet()) {			
			log.log(Level.INFO,"Dsn is " + entry.getKey());
			sDsnLastName = getDsnLastName(entry.getKey());
			// init
			boolean bProcessingMember = false;
			boolean bMemberFoundInMCS = false;
			String sRestOfMcs = "";
			String sMCSmod = "";

			// Iterate over members in the DSN
			for(String member: entry.getValue()) {
				log.log(Level.INFO,"\t - " + member);				
				bMemberFoundInMCS = false;
				// JCLIN
				if (sDsnLastName.equalsIgnoreCase("JCLIN")) {
					saveMcsSuffix(getMcsSuffix(sDsnLastName, member, "++JCLIN CALLLIBS", " "));
					break;
				}

				for (String sMcsLine: lMCSLines) {
					
					// Process next line of MCS
					// We need this block if ++statemet has several lines
					if ( bProcessingMember ) {						
						// We didn't find a dot character at the end of ++statement
						// check whether it new ++statement or not
						if (sMcsLine.indexOf("++") < 0) {							
							sRestOfMcs = sRestOfMcs + sMcsLine;
							if ((sRestOfMcs.indexOf(".") >= 0)) {
								// read next line and append
								saveMcsSuffix(getMcsSuffix(sDsnLastName, member, sMCSmod, sRestOfMcs));
								bProcessingMember = false;
								
								log.log(Level.FINE, "Save and jump to next ++stetment");
								continue;
								
							} else {
							  // There are no dot char or ++statement
							  // read next line
								continue;
							}
													
						} else {
						    // We have found ++statement
							log.log(Level.FINE, "We have found ++statemt");
							// Save existing MCS
							saveMcsSuffix(getMcsSuffix(sDsnLastName, member, sMCSmod, sRestOfMcs));
							// move to build next MCS
							bProcessingMember = false;
						}
					}
						// Main
					if ((sMcsLine.indexOf("++") >=0 ) && (getMemberNameFromMcsLine(sMcsLine).equalsIgnoreCase(member))  && (!bProcessingMember)) {	
							// MCS can contain several lines. We have to read all of them
							bProcessingMember = true;
							
							bMemberFoundInMCS = true;
							
							log.log(Level.FINE, "Member " + member);
							log.log(Level.FINE, "Raw MCS " + sMcsLine);
							int memStarts = sMcsLine.indexOf("(");
							int memEnds  = sMcsLine.indexOf(")");						
							
							// Getting MCS pieces
							// ++statement
							sMCSmod = sMcsLine.substring(0, memStarts).trim();
							// member name
							String sMemberName = getMemberNameFromMcsLine(sMcsLine);
							// SYSLIB, DISTLIB and so on
							sRestOfMcs = sMcsLine.substring(memEnds + 1);
							log.log(Level.FINE, getMcsSuffix(sDsnLastName, sMemberName, sMCSmod, sRestOfMcs));
							if (sRestOfMcs.indexOf(".") < 0) {
								// read next line and append
								bProcessingMember = true;						
								log.log(Level.FINE, "No dot. Processing next line" + sRestOfMcs);
								
							} else {
								bProcessingMember = false;
								saveMcsSuffix(getMcsSuffix(sDsnLastName, sMemberName, sMCSmod, sRestOfMcs));
							}
						}
					}
					if (!bMemberFoundInMCS) alMissingMembersFromMcs.add(member);
				}
		}
		
		try {
			
			log.log(Level.INFO,"************************************************");
			log.log(Level.INFO,"*   Result is following                        *");
			log.log(Level.INFO,"************************************************");
			
			if (!alMissingMembersFromMcs.isEmpty()) {
				log.log(Level.SEVERE,"Following members are missing");
				for(String sMissingMember: alMissingMembersFromMcs) {
					log.log(Level.SEVERE,sMissingMember);
				}
				return false;
			} else {
			
				for(String sLine: alMcsSuffixForFunctionXml) {
					log.log(Level.FINE, sLine);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	private String getMemberNameFromMcsLine(String sMcsLine) {
		int memStarts = sMcsLine.indexOf("(");
		int memEnds  = sMcsLine.indexOf(")");						
		
		// member name
		String memberName = "";
		try {
			memberName = sMcsLine.substring(memStarts + 1, memEnds).trim();
		} catch (Exception e) {
			log.log(Level.FINE, "It is not ++statement");
		}
		return memberName;
	}
	
	private boolean saveMcsSuffix(String sSuffix) {
		
		return alMcsSuffixForFunctionXml.add(sSuffix);
	}
		
    private String getMcsSuffix(String sDsnLastName, String sMemberName, String sMCSmod, String sRestOfMcs) {
    	// Remove RELFILE(n) and . sign from MCS as .xml doesn't require it.
		sRestOfMcs = sRestOfMcs.replaceAll("RELFILE\\(\\d+\\)", "").replaceAll("\\.", "");
		log.log(Level.FINE, sMCSmod + "\n" + sMemberName + "\n" + sRestOfMcs);
		
    
    	return sDsnLastName + "\" includes=\"" + sMemberName + "\" mcs=\"" + sMCSmod + " " + sRestOfMcs + "\"/>";
    }
		
	private List<String> getSMPMCSAsArrayList() {
		List<String> aListMCS = null;
		try {
			aListMCS = Files.readAllLines(Paths.get(smpmcsFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return aListMCS;
	}
	
	private ArrayList<String> getFunctionsXMLAsArrayList() {
		ArrayList<String> funcList = new ArrayList<>();
		funcList.add("<?xml version=\"1.0\"?>                                                                                                                                  ");
		funcList.add("<project name=\"functions\" basedir=\"..\"                                                                                                                 ");
		funcList.add("    xmlns:zpackage=\"antlib:com.ibm.zpackage.ant\"                                                                                                       ");
		funcList.add("    xmlns:hostzos=\"antlib:com.ibm.hostservices.zos.ant\">                                                                                               ");
		funcList.add("    <zpackage:function id=\"base\" name=\"" + JenkinsParameters.COMP_ID + "-" + JenkinsParameters.FMID + "\" srel=\"" + JenkinsParameters.SREL + "\">                                                                             ");
		
			for(String suffixLine: alMcsSuffixForFunctionXml) {
				funcList.add("		<zospartversionprovider uri=\"${zpackage.uri}\" hlq=\"" + JenkinsParameters.WINBOX_QUALIFIER + "." +suffixLine);
			  
			}	
		
		funcList.add("    </zpackage:function>  ");
		funcList.add("</project>                ");  
		return funcList;
	}
	
	public File saveFunctionsXML(Path pathToSaveFunctionsXML) {	
		
		try {
			
			Files.write(pathToSaveFunctionsXML, getFunctionsXMLAsArrayList());
			log.log(Level.INFO, "File has been saved to " + pathToSaveFunctionsXML.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pathToSaveFunctionsXML.toFile();
	}
	
	public void printDsnAndMembes() {
		System.out.println("******************************");
		System.out.println("Printing members");
		System.out.println("******************************");
		
		for(Map.Entry<String, String[]> entry: mapDSN.entrySet()) {
			System.out.println(entry.getKey());
			for(String member: entry.getValue()) {
				System.out.println(member);
			}
		}
	}
	public static void main(String[] args) throws IOException {
		
		Log.configureLog();
		String host = "rsi1";
		
		JesFtp ftp = new JesFtp();
		ftp.connect(host);
		ftp.login();
		String sDSN = "RSBLD.IFPS.FPSP130.PH01862";
		ArrayList<String> dirs = ftp.listDir("'" + sDSN + ".*'");
		String sSMPMCS = "C:\\DevTools\\HAHQ130.MNT.SMPMCS";
		ftp.download("'PDSAMO.HAHQ130.MNT.SMPMCS'", sSMPMCS);
		
		MCSHandler mcsHandler = new MCSHandler(ftp, dirs, sSMPMCS);
		mcsHandler.saveFunctionsXML(Paths.get("C:\\DevTools\\", "functions.xml"));
		
		
	}
}
