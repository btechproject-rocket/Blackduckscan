package test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rocketsoftware.builder.Jcl;
import com.rocketsoftware.filter.Filter;
import com.rocketsoftware.ftp.JesFtp;
import com.rocketsoftware.log.Log;
import com.rocketsoftware.properties.Properties;
import com.rocketsoftware.transfer.DatasetTransfer;

public class FtpTransfer {
	private static String INITIAL_HOST;
	private static String INITIAL_QUALIFIER;
	private static String APAR_NUMBER;
	private static String FMID;
	
	@BeforeClass
	public static void setUpClass() {
	    INITIAL_HOST = "RS22";
	    INITIAL_QUALIFIER = "TS5071.TST.V123.PH12345";
	    APAR_NUMBER = "PH12345";
	    FMID = "KBRANA";
	    Log.configureLog();
	}

	@Test
	public void testSendAparDatasetsToFTP() {
		try {
			JesFtp ftp = new JesFtp();
			
			ftp.connect(INITIAL_HOST);
			ftp.login();
			
			DatasetTransfer datasetTransfer = new DatasetTransfer();			
			datasetTransfer.sendAparDatasetsToFTP(ftp, INITIAL_HOST, INITIAL_QUALIFIER, APAR_NUMBER, FMID);
			datasetTransfer.saveJclRecieveDatasetsOnWinbox(datasetTransfer.getListOfDsnsOnFtp(), "ROCKET", "PH12345", "KBRANA");
			
			
			ftp.connect(Properties.ftpServer);
			ftp.login();
			List<String> expectedDatasets = datasetTransfer.getListOfDatasetToTransfer();

			List<String> resultDatasets = ftp.listDirectory(Properties.ftpDirectory);
			boolean isPresent = Filter.lookForName(resultDatasets, expectedDatasets.get(0).replace(INITIAL_QUALIFIER, FMID + "." + APAR_NUMBER));		
			assertTrue(isPresent);
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	@AfterClass
	public static void cleanup() {
		/*JesFtp ftp = new JesFtp();
		ftp.connect(Properties.ftpServer);
		ftp.login();
		try {
			ftp.deleteFromFTP("/ftpsite/IBM/SysTools/Packaging/KBRANA.PH12345.CLIST");
			ftp.deleteFromFTP("/ftpsite/IBM/SysTools/Packaging/KBRANA.PH12345.LOADLIB");
			ftp.deleteFromFTP("/ftpsite/IBM/SysTools/Packaging/KBRANA.PH12345.REXX");
			ftp.logout();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}

}
