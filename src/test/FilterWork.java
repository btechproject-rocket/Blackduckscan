package test;

import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rocketsoftware.exception.DatasetsNotFoundException;
import com.rocketsoftware.filter.Filter;
import com.rocketsoftware.ftp.JesFtp;
import com.rocketsoftware.log.Log;

public class FilterWork {
	private static String goodDirectory;
	private static String badDirectory;
	private static String host;
	@BeforeClass
	public static void setUpParms() {
		goodDirectory = "RSBLD.DOE.PKG0110.PH05068";
		badDirectory = "RSQWD.12345";
		host = "RS22";
		Log.configureLog();
	}
	@Test
	public void testAparFilterPositive() throws DatasetsNotFoundException {
		JesFtp ftp = new JesFtp();
		ftp.connect(host);
		ftp.login();
		List<String> dsns = ftp.listDirectory(ftp.createMainframePathByQualifier(goodDirectory));
		List<String> filtered = Filter.filterAparDatasets(dsns, false);
		for(String dsn: filtered) {
			assertFalse(dsn.contains("XMIT"));
		}
	}
	@Test(expected = Exception.class)
	public void testAparFilterNegative() throws DatasetsNotFoundException {
		Log.configureLog();
		JesFtp ftp = new JesFtp();
		ftp.connect(host);
		ftp.login();
		List<String> dsns = ftp.listDirectory(ftp.createMainframePathByQualifier(badDirectory));
		Filter.filterAparDatasets(dsns, false);
	}
}
