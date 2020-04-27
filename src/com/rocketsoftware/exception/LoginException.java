package com.rocketsoftware.exception;

public class LoginException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2885704939359662453L;
	
	public String getMessage() {
		return "Wrong credentials";
		
	}
}
