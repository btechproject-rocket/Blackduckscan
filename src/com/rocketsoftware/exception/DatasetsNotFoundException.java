package com.rocketsoftware.exception;

/**
 * This class is the Exception that happens when no datasets found for the JCL
 * @author kbranavitski
 *
 */
public class DatasetsNotFoundException extends Exception {
	
	private String programMessage;
	
	private static final long serialVersionUID = 1L;
	
	public DatasetsNotFoundException(String datasetName) {
		this.programMessage = datasetName;
	}
	
	public String getMessage() {
		return "Datasets have been not found: " + programMessage + "\n";
	}

}
