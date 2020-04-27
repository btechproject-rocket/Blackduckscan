package com.rocketsoftware.exception;

public class JobFailedException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String getMessage() {
		return "Job returned code higher than 04 or JCL error";
	}
}
