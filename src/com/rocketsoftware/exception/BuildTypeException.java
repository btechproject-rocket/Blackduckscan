package com.rocketsoftware.exception;
/**
 * This class is the Exception that happens when there are some problems with the build type in configs.xml
 * @author kbranavitski
 *
 */
public class BuildTypeException extends Exception {
	
 static final long serialVersionUID = 6670527039252161689L;

	public String getMessage() {
		return "Probably you have entered ivalid build type + \n" + 
				"Valid are: APAR/PTF/FULL + \n";
		
	}
}
