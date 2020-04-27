package com.rocketsoftware.transfer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rocketsoftware.builder.Jcl;
import com.rocketsoftware.builder.JclApar;
import com.rocketsoftware.builder.JclAparWinbox;
import com.rocketsoftware.exception.DatasetsNotFoundException;
import com.rocketsoftware.filter.Filter;
import com.rocketsoftware.ftp.JesFtp;
import com.rocketsoftware.jenkins.JenkinsParameters;
import com.rocketsoftware.log.Log;
import com.rocketsoftware.properties.Properties;
import com.rocketsoftware.smpe.MCSHandler;


public class DatasetTransfer {
	private static HashMap<String, String[]> hasmapOfDSNToTransfer = new HashMap<>();
	private List<String> listOfDSNToTransfer;
	private List<String> dsnsOnFtp;
	private static Logger log = Log.log;
	
	/**
	 * Sends datasets directly from mainframe host to Rocket FTP.
	 * It submits JCL on mainframe.
	 *   
	 * @param ftp
	 * @param INITIAL_HOST
	 * @param INITIAL_QUALIFIER
	 * @param APAR_NUMBER
	 * @param FMID
	 * @throws Exception
	 */
	public void sendAparDatasetsToFTP(JesFtp ftp, String INITIAL_HOST, String INITIAL_QUALIFIER, String APAR_NUMBER, String FMID) throws Exception {
		log.log(Level.INFO, "Generating JCL for sending APAR datasets to FTP server");

		listOfDSNToTransfer = Filter.filterAparDatasets(ftp.listDirectory(ftp.createMainframePathByQualifier(INITIAL_QUALIFIER)), false);
		// Set HashMap is used by MCSHandler class.
		setHashMapOfDSNtoTransfer(ftp);
		JclApar jclApar = new JclApar();
		// Create and save to jclTemp
		jclApar.createJclForSendingDatasetsToFTP(listOfDSNToTransfer, INITIAL_HOST, INITIAL_QUALIFIER, APAR_NUMBER, FMID);
		// Save the JCL permanently as JCLFIXOT
		dsnsOnFtp = JclApar.datasetsForAparOnFTP;
		String jcl = Properties.outputDirectoryForJcls + File.separator + Properties.jclSendToFtp; //
		jclApar.saveJclTo(jcl);
		log.log(Level.INFO, "JCL was successfully generated at " + jcl);
		ftp.submit(jcl);
	}
	
	public String saveJclForReceivingAparOnArtifactoryHost(String ARTIFACTORY_HOST, String ARTIFACTORY_QUALIFIER, String FMID) throws DatasetsNotFoundException {
		log.log(Level.INFO, "Generating JCL for receiving APAR on artifactory host");
		JclApar jclApar = new JclApar();
		jclApar.createJclForReceivingDatasetsFromFTP(ARTIFACTORY_QUALIFIER, FMID);
		String jcl = Properties.outputDirectoryForJcls + File.separator + Properties.jclToReceiveOnArtifactoryHost;
		jclApar.saveJclTo(jcl);
		log.log(Level.INFO, "JCL was successfully generated at " + jcl);
		return jcl;
	}
	
	public void saveJclRecieveDatasetsOnWinbox(List<String> datasetNamesForReceiveOnFTP, String WINBOX_QUALIFIER, String APAR_NUMBER, String FMID) {
		log.log(Level.INFO, "Generating JCL for receiving datasets on WINBOX");
		JclAparWinbox winJcl = new JclAparWinbox();
		winJcl.createJclForReceivingAparDatasetsOnWinbox(datasetNamesForReceiveOnFTP, WINBOX_QUALIFIER, APAR_NUMBER, FMID);
		String jcl = Properties.outputDirectoryForJcls + File.separator + Properties.jclToReceiveOnWinbox;
		winJcl.saveJclTo(jcl);
		log.log(Level.INFO, "JCL was successfully generated at " + jcl);
	}
	public void saveJclForSendingAparFromWinbox(String FMID) {
		log.log(Level.INFO, "Generating JCL for sending APAR from WINBOX");
		JclAparWinbox winJcl = new JclAparWinbox();
		winJcl.createJclForSendningAparFromWinbox(FMID);
		String jcl = Properties.outputDirectoryForJcls + File.separator + Properties.jclToSendFromWinbox;
		winJcl.saveJclTo(jcl);
		log.log(Level.INFO, "JCL was successfully generated at " + jcl);
	}
	
	/**
	 * It returns HasMap of DSNs that will be send to Rocket FTP
	 * It looks like this
	 *   RSBLD.IADM.ATY110.PH07851.OBJLIB {MEM1, MEM2, MEM3}
	 *   RSBLD.IADM.ATY110.PH07851.SAMPLIB ...
	 *   RSBLD.IADM.ATY110.PH07851.DATA ...
	 * 
	 * @return 
	 */
	
	public HashMap<String, String[]> getHashMapOfDSNtoTransfer() {
		return hasmapOfDSNToTransfer;
	}
	
	/**
	 *  * It looks like this
	 *   RSBLD.IADM.ATY110.PH07851.OBJLIB 
	 *   RSBLD.IADM.ATY110.PH07851.SAMPLIB 
	 *   RSBLD.IADM.ATY110.PH07851.DATA
	 * @return
	 */
	public List<String> getListOfDatasetToTransfer() {
		return listOfDSNToTransfer;
	}
	
	private void setHashMapOfDSNtoTransfer(JesFtp ftp)
	{
		
		for(String sDsnName: listOfDSNToTransfer) {
			sDsnName = sDsnName.replace('\'', ' ').trim();
			log.log(Level.INFO, "Listing dataset members: " + sDsnName);
			// SMPMCS has no members. it is a sequential DSN.
			if (sDsnName.endsWith(Properties.smpmcs)) continue;
			// Remove ' from DSN names
			
			try {
				ftp.changeWorkingDirectory("'" + sDsnName + "'");
				ftp.getReplyString();
				String[] members = ftp.listNames();
				ftp.getReplyString();
				for (int i = 0; i < members.length; i++) {
					System.out.println("\t\t\t" + members[i]);
				}
				hasmapOfDSNToTransfer.put(sDsnName, members);
			} catch(IOException e) {
				System.out.println("Unable to read " + sDsnName);
				log.log(Level.SEVERE, "Unable to read " + sDsnName);
				log.log(Level.SEVERE, e.getMessage());
				System.exit(-12);
			}
		}
		
	}
	public List<String> getListOfDsnsOnFtp() {
		return dsnsOnFtp;
	}
	
	
	public void sendAparFromWinbox() {
		//TODO
	}
	public static void main(String args[]) {
		try {	
			//configuring log		
			Log.configureLog();
			//reading Jenkins parms
			JenkinsParameters.readJenkinsParameters();
			//establishing connection with mainframe
			JesFtp ftp = new JesFtp();
			ftp.connect(JenkinsParameters.INITIAL_HOST);
			ftp.login();
			
			
			DatasetTransfer transfer = new DatasetTransfer();
			transfer.sendAparDatasetsToFTP(ftp, JenkinsParameters.INITIAL_HOST, JenkinsParameters.INITIAL_QUALIFIER, JenkinsParameters.APAR_NUMBER, JenkinsParameters.FMID);
			transfer.saveJclRecieveDatasetsOnWinbox(JclApar.datasetsForAparOnFTP, JenkinsParameters.WINBOX_QUALIFIER, JenkinsParameters.APAR_NUMBER, JenkinsParameters.FMID);
			transfer.saveJclForSendingAparFromWinbox(JenkinsParameters.FMID);
			transfer.saveJclForReceivingAparOnArtifactoryHost(JenkinsParameters.ARTIFACTORY_HOST, JenkinsParameters.ARTIFACTORY_QUALIFIER, JenkinsParameters.FMID);
			// Save SMPMCS
			//String sSMPMCS = Properties.outputDirectoryForJcls + File.separator + Properties.smpmcs;
			//ftp.download(JenkinsParameters.INITIAL_QUALIFIER + "." + Properties.smpmcs, sSMPMCS);
					
			// Generate fuction.xml
			// build.xml
			// service.xml 
			//MCSHandler mcsHandler = new MCSHandler(ftp, transfer.getListOfDatasetToTransfer(), sSMPMCS);
			//mcsHandler.saveFunctionsXML(Paths.get(Properties.outputDirectoryForJcls, "functions.xml"));
			
			
			//  
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(24);
		}
	}
}
