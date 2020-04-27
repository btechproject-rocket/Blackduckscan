package com.rocketsoftware.ftp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.rocketsoftware.exception.JobFailedException;
import com.rocketsoftware.exception.LoginException;
import com.rocketsoftware.log.Log;

public class JesFtp extends FTPClient {
	private String sFtpHost = "";

	private File fFtpProperty = new File(System.getProperty("user.home"), "ftp.properties");
	
	private final int iTimeoutSec = 50;
	
	private static final Logger log = Log.log;
	
	
	public JesFtp()	{
		super();
	}
	
	public void connect(String sHost) {
		try {
			log.log(Level.INFO, "Connecting to a host: " + sHost);
			super.connect(sHost);	
			sFtpHost = sHost.trim();
			//printReply();
			// Set Passive Mode as we are behind a firewall
	        enterLocalPassiveMode();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	
	public boolean login() {
		log.log(Level.INFO, "Log into the server");
		boolean bConnected = true;
        // Log into the server
		String userName  = null;
		String password = null;
        try {
        	try {
        	//System.out.println("Reading environment variables from Jenkins");
        	log.log(Level.INFO, "Reading credentials from Jenkins");	
        	userName = System.getenv(sFtpHost+".USER");
        	password = System.getenv(sFtpHost+".PASSWORD");
        	//System.out.println("USERNAME: " + userName);
        	} catch (Exception e1) {
        		//System.out.println("Unable to get environment variable " + sFtpHost + ".USER" );
        		log.log(Level.WARNING, "Unable to get environment variable " + sFtpHost + ".USER" );
        	}
        	if (userName == null || userName.isEmpty() ) {
            	// Read login and password from $Home/ftp.properties
        		//System.out.println("Reading environment variables from ftp.properties");
        		log.log(Level.INFO, "Reading credentials from ftp.properties");
            	String[] cred = getCredentials();
            	userName = cred[1];
            	password = cred[2]; 
        	}
			super.login(userName, password);
			String replyString = this.getReplyString();
			if(replyString.trim().equals("530 PASS command failed")) {
				log.log(Level.SEVERE, "Cannot log into the system");
				throw new LoginException();
			}
			
			//printReply();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			bConnected = false;
			e.printStackTrace();
		} catch (LoginException e) {
			log.log(Level.SEVERE, e.getMessage());
			System.exit(12);
		}
        
        return bConnected; 
    }
	
	private void printReply() {
		String replyText = getReplyString();
		//System.out.println(replyText);	
		log.log(Level.INFO, "\n" + replyText);
	}
	
	public String[] getCredentials() {
		
		List<String> lHostUserPswd = null;
		boolean bIsHostFound = false;
		String[] sProps = null;
		try {
			
			if (fFtpProperty.exists()) {				
				lHostUserPswd = Files.readAllLines(fFtpProperty.toPath());
				for (String sCredentials : lHostUserPswd) {		
					// Ignore empty lines
					sCredentials = sCredentials.trim();
					if (sCredentials.isEmpty()) {							
						continue;						
					}
					
					sProps = sCredentials.split("\\s+");
					// sProps[0] is host name
					if ((sFtpHost.equalsIgnoreCase(sProps[0])) && (sProps.length > 2)) {
						
							bIsHostFound = true;	
							break;
					}
						
				}
				// Create user and password entry for the host. 
				if (!bIsHostFound) {							
					sProps = setCredentials();
				}
				
			} else {
				setCredentials();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sProps[2] = new String(Base64.decodeBase64(sProps[2]));
		return sProps;
		
		
	}
	
	public String[] setCredentials() throws IOException {
		List<String> lHostUserPswd = new ArrayList<String>();

		if (fFtpProperty.exists()) {
			lHostUserPswd = Files.readAllLines(fFtpProperty.toPath());
		} else {
			System.out.println("create file");
		}
		
		String user = "";
		String pswd = "";
		Scanner inConsole = new Scanner(System.in);
		FTPClient ftp = null;
		boolean bFtp = false;
		for (int i=0; i < 3; i++ )
		{
			System.out.println("******************************************");
			System.out.println("Set Login and Password ");
			System.out.println("******************************************");		
			System.out.println("Username: ");
			user = inConsole.nextLine().trim();
			
			System.out.println("Password: ");
			pswd = inConsole.nextLine();
			ftp = new FTPClient();
			ftp.connect(sFtpHost);
			bFtp = ftp.login(user, pswd);			
			System.out.println(ftp.getReplyString());
			if (bFtp) break;
			
		}
		
		System.out.println(ftp.getReplyCode());
		inConsole.close();

		lHostUserPswd.add(sFtpHost + " " + user + " " + Base64.encodeBase64String(pswd.getBytes()));
		Files.write(fFtpProperty.toPath(), lHostUserPswd);

		System.out.println("Credentials have been written to " + fFtpProperty.getPath());
		
		return new String[] {sFtpHost,user, pswd};
		
	}
	
	/**
	 * Example: listDir("'RSQA.HDOE110.*'")
	 */
	public ArrayList<String> listDir(String sRemoteDSN) {
		FTPFile[] dsns = null;
		ArrayList<String> dsnList = new ArrayList<String>(); 
		try {
			dsns = listDirectories(sRemoteDSN);			
			printReply();
			for (FTPFile dsn: dsns) {
				dsnList.add(dsn.getName());
			}
		} catch (Exception e) {			
			return null;
		}		

		return dsnList;
	}
	
	
	/**
	 * Example: submit("C:\\DevTools\\XMIT.JCL")
	 * @param sPathToLocalJCL
	 * @return
	 */
	
	public JesLog submit(String sPathToLocalJCL) {

		String sRemoteFilename = ""; 
		String replyText = "";
		ArrayList<String> sJesLog = null;
		String sRC = "";

        // Submit JCL
        try { 
        	
            // Tell the server to use the JES interface
    		site("filetype=jes");
            //site("JESINTERFACELEVEL=2");
            //printReply();

            FileInputStream inputStream = new FileInputStream(sPathToLocalJCL) ; 
           
            // Submits the JCL
            //System.out.println("About to submit " + sPathToLocalJCL);
            log.log(Level.INFO, "About to submit " + sPathToLocalJCL);
            storeFile (sFtpHost,inputStream) ; 
            //printReply();
            replyText = getReplyString();
           
            /* 
             * Getting log file
             * Names can be following J1234567 or JOB12345
             */
            Pattern p = Pattern.compile("\\w(\\w|\\d){2}\\d{5}");
 	        //  get a matcher object
            Matcher m = p.matcher(replyText);
          
            while(m.find()) {
        	    sRemoteFilename = replyText.substring(m.start(), m.end()).trim();
                //System.out.println("Executing Job  " + sRemoteFilename);
        	    log.log(Level.INFO, "Executing Job  " + sRemoteFilename);
               
            }
        
	        // Downloading Jes Log
            boolean done = false;
            
            site("JESJOBNAME=*");
            
            // Wait for job to complete
            for (int k=0; k < iTimeoutSec; k++ ) {
	            FTPFile[] names = listFiles(sRemoteFilename);	            
	            
	            if (names.length > 0) {
	            	//printReply();
		            for (int i = 0; i < names.length; i++) {
		                //System.out.println("file " + i + " is " + names[i].getRawListing());
		            	log.log(Level.INFO, names[i].getRawListing());
		                checkReturnCode(getReturnCode(names[i].getRawListing()));
		                String[] sResponse = names[i].getRawListing().split("\\s+");
		                sRC = sResponse[sResponse.length - 1];
		            }
		            done = true;
		            break;
	            } else {
	            	Thread.sleep(1000);
	            }
	            
            
            }
            if (!done) throw new Exception("Job has been lost");
            Thread.sleep(2000);
	        InputStream is = retrieveFileStream(sRemoteFilename);
	        //printReply();
	        
	        BufferedReader br = new BufferedReader(new InputStreamReader((is)));
	        sJesLog = new ArrayList<>(); 
	        boolean bContinue = true;
	        while (bContinue) {
	            String sLine = br.readLine();
	            if (sLine != null) {
	                //System.out.println("line ... " + sLine);
	                sJesLog.add(sLine); 
	            } else {
	                bContinue = false;
	                is.close();
	                br.close();
	            }
	        }
	        log.log(Level.INFO, "JCL executed successfully");
	        completePendingCommand();
        } 
        catch  (Exception e) { 
                //e.printStackTrace() ; 
        		log.log(Level.INFO, "Failed to read Jes Log for " + sRemoteFilename);
                return null;
        }  
        
        return new JesLog(sJesLog, sRemoteFilename, sRC);

	}
	/**
	 * Example download("'RSQA.SMPECUT.CNTL(MCRPTF75)'", "C:\\DevTools\\MCRPTF75")
	 * @param sJclDsn
	 * @param sPathToSave
	 * @return
	 * @throws IOException
	 */
	public boolean download(String sJclDsn, String sPathToSave) throws IOException {
				
		site("FILETYPE=SEQ");
		printReply();
		
		OutputStream fos = new FileOutputStream(sPathToSave.trim());
		String sJcliDsn = sJclDsn.trim();

		// Add ' sign if missed 
		if (!sJcliDsn.startsWith("'")) sJcliDsn = "'" + sJcliDsn;
		if (!sJcliDsn.endsWith("'")) sJcliDsn = sJcliDsn + "'";
		
		//Download file
		boolean bRC = retrieveFile(sJcliDsn, fos);	
		printReply();
		fos.close();
		return bRC;
	}
	
	public boolean upload(String sLocalFile, String sRemoteFile) throws IOException
	{
		site("FILETYPE=SEQ");
		printReply();
		
		if (!sRemoteFile.startsWith("'")) sRemoteFile = "'" + sRemoteFile;
		if (!sRemoteFile.endsWith("'")) sRemoteFile = sRemoteFile + "'";
		FileInputStream fis = new FileInputStream(sLocalFile);

	    // Upload file to server		
		boolean bRC = storeFile(sRemoteFile, fis);
	    printReply();	    
	    fis.close();
	    return bRC;
	}
	public List<String> listAllFiles(String dsn) {
		List<String> resultSet = new ArrayList<>();
		try {
			if(this.listNames(dsn) != null) {
				String names[] = this.listNames(dsn);
				//System.out.println("Found " + names.length);
				log.log(Level.INFO, "Found " + names.length + " files");
				for(String name: names) {
					resultSet.add(name);
				}
				/*System.out.println(name);*/
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultSet;
	}
	public List<String> listDirectory (String directory) {
		List<String> resultSet = null;	
		//System.out.println("Browsing directory -> " + directory + "\n");
		log.log(Level.INFO, "Browsing directory " + directory);
		resultSet = this.listAllFiles(directory);
		return resultSet;
	}
	public String createMainframePathByQualifier(String QUALIFIER) {
		return "'" + QUALIFIER + ".*'";
	}
	
	public void checkReturnCode(int returnCode) throws Exception {
		if(returnCode <= -1 || returnCode > 4) {
			throw new JobFailedException();
		}
		else if (returnCode == 4) {
			log.log(Level.WARNING, "RC=0004");
		}
	}
	public int getReturnCode(String replyString) {
		Pattern returnCodePattern = Pattern.compile("^RC=\\d{4}$");
		StringTokenizer tokenizer = new StringTokenizer(replyString);
		while(tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if(token.equals("(JCL error)")){
				return -1;
			}
			Matcher matcher = returnCodePattern.matcher(token);
			if(matcher.matches()) {
				Integer returnCode = Integer.parseInt(token.replace("RC=", ""));
				return returnCode;
			}
		}
		return 12;
	}
	public void deleteFromFTP(String path) throws IOException {
		this.deleteFile(path);
	}
	public static void main(String args[]) {
		try {
			Log.configureLog();
			JesFtp jesFtp = new JesFtp();
			jesFtp.connect("RS22");
			jesFtp.login();
			jesFtp.submit("test/goodJCL");
			jesFtp.logout();
		} catch (IOException e) {
			log.log(Level.SEVERE, "\n" + e.getMessage());
		}
	}
	
}
