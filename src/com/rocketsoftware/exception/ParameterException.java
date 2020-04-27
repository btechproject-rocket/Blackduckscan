package com.rocketsoftware.exception;

public class ParameterException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String parameterName;
	
	public ParameterException(String name) {
		parameterName = name;
	}
	public String getMessage() {
		return "Parameter '" + parameterName + "' was not specified";
		
	}
}
