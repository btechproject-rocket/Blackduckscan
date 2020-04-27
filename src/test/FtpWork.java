package test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rocketsoftware.ftp.JesFtp;
import com.rocketsoftware.log.Log;

public class FtpWork {
	private static String TEST_HOST;
	
	@BeforeClass
	public static void setUpParms() {
		TEST_HOST = "RS22";
		Log.configureLog();
	}
	
	@Test
	public void testListDirectory() {
		JesFtp ftp = new JesFtp();
		ftp.connect(TEST_HOST);
		ftp.login();
		List<String> datasetNames = ftp.listDirectory(ftp.createMainframePathByQualifier("TS5071.TST.V123.PH12345"));
		assertEquals(datasetNames.size(), 6);
		try {
			ftp.logout();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Test(expected = Exception.class)
	public void testSubmitJobNegative() {
		JesFtp ftp = new JesFtp();
		ftp.connect(TEST_HOST);
		ftp.login();
		ftp.submit("test/errorJCL");
		
	}
	@Test
	public void testSubmitJobPositive() {
		JesFtp ftp = new JesFtp();
		ftp.connect(TEST_HOST);
		ftp.login();
		ftp.submit("test/goodJCL");
	}
	@Test(expected = Exception.class)
	public void testCheckReturnCodeNegative1() throws Exception {
		JesFtp ftp = new JesFtp();
		ftp.checkReturnCode(8);
	}
	@Test(expected = Exception.class)
	public void testCheckReturnCodeNegative2() throws Exception {
		JesFtp ftp = new JesFtp();
		ftp.checkReturnCode(-1);
	}
	@Test
	public void testCheckReturnCodePositive() throws Exception {
		JesFtp ftp = new JesFtp();
		ftp.checkReturnCode(0);
	}

}
