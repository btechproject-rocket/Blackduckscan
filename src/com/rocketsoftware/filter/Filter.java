package com.rocketsoftware.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rocketsoftware.exception.DatasetsNotFoundException;
import com.rocketsoftware.log.Log;
import com.rocketsoftware.properties.Properties;

/**
 * Super class that contains methods that extended classes should override.
 * @author kbranavitski
 *
 */
public class Filter {
	private static final Logger log = Log.log;
	public Filter() {
		
	}
	
	protected List<String> deleteFilter(List<String> names) {
		return null;
		
	}
	protected List<String> ftpFilter(List<String> names) throws DatasetsNotFoundException {
		return null;
	}
	protected List<String> filter(List<String> names) throws DatasetsNotFoundException {
		return null;
	}
	/**
	 * This method takes a list of file names and removes quotes
	 * @param names
	 * @return namesWitoutQuotes
	 */
	public static List<String> removeQuotes(List<String> names) {
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
	/**
	 * This method takes a list of file names and removes path from it.
	 * @param names
	 * @param ftpDirectory
	 * @return namesWithoutPath
	 */
	protected static List<String> removePath(List<String> names, String ftpDirectory) {
		List<String> namesWithoutPath = new ArrayList<>();
		for(String name: names) {
			String nameWithoutPath = name.replace(ftpDirectory, "");
			nameWithoutPath = nameWithoutPath.replace("/","");
			namesWithoutPath.add(nameWithoutPath);
		}
		return namesWithoutPath;
	}
	/**
	 * 
	 * @param unfilteredNames - list of names found in directory
	 * @param filterMode - to take all datasets or filter them
	 * @return
	 * @throws DatasetsNotFoundException
	 */
	public static List<String> filterAparDatasets(List<String> unfilteredNames, boolean filterMode) throws DatasetsNotFoundException {
		StringBuffer buffer = new StringBuffer();
		log.log(Level.INFO, "Filtering datasets for APAR");
		log.log(Level.INFO, "Included datasets: ");
		List<String> namesWithoutQuotes = removeQuotes(unfilteredNames);
		List<String> filteredNames = new ArrayList<>();
		if(filterMode) {
			
		}
		else {
			for(String dsn: namesWithoutQuotes) {
				if(dsn.contains(".XMIT")) {
					continue;
				}
				else {
					filteredNames.add(dsn);
					buffer.append("                                   " + dsn + "\n");
				}
			}
		}
		log.log(Level.INFO, "\n" + buffer.toString());
		if(filteredNames.size() == 0) {
			log.log(Level.SEVERE, "No datasets were added to a filtered list");
			throw new DatasetsNotFoundException("Datasets were not found for APAR");
		}
		return filteredNames;
	}
	public static boolean lookForName(List<String> names, String name) {
		System.out.println("Looking for: " + name);
		List<String> namesWithoutPath = removePath(names, Properties.ftpDirectory);
		for(String n: namesWithoutPath) {
			if(n.trim().equals(name.trim())) {
				return true;
			}
		}
		return false;
	}
	public static List<String> filterDsns(List<String> names, String INITIAL_DSN) throws DatasetsNotFoundException {
		String[] excludedList = {"DBRMLIB","EXEC","EXP","ISPMLIB","ISPPLIB","ISPTLIB","JCL","MSGS","OBJ",
                "OBJLIB","PARMLIB","PKGTEXT","REXXSAMP","SKELLIB","ISPSLIB","CNTL","CLIST",
                "SAMPLIB","LOADLIB","LMAP", "OUTLINK", "TCZDENU","JCLIN"};
		List<String> filteredNames = new ArrayList<String>();
		for(String dsn: names) {
			//System.out.println("Working on " + dsn.replace(INITIAL_DSN + ".", ""));
			for(String excludedDsn: excludedList) {
				if(dsn.replace(INITIAL_DSN + ".", "").equals(excludedDsn)) {
					System.out.println(dsn + " was included in PTF");
					filteredNames.add(dsn);
				}
			}
		}
		
		
		if(filteredNames.size() == 0) {
			throw new DatasetsNotFoundException(INITIAL_DSN);
		}
		return filteredNames;
	}
	public static boolean checkDsn(String dsn, String INITIAL_DSN) {
		String[] includedList = {"DBRMLIB","EXEC","EXP","ISPMLIB","ISPPLIB","ISPTLIB","JCL","MSGS","OBJ",
                "OBJLIB","PARMLIB","PKGTEXT","REXXSAMP","SKELLIB","ISPSLIB","CNTL","CLIST",
                "SAMPLIB","LOADLIB","LMAP", "OUTLINK", "TCZDENU","JCLIN"};
		for(String excludedDsn: includedList) {
			if(dsn.replace(INITIAL_DSN + ".", "").equals(excludedDsn)) {
				System.out.println(dsn + " was included in PTF");
				return true;
			}
		}
		return false;
	}

}
