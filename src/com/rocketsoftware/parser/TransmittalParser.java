package com.rocketsoftware.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.rocketsoftware.log.Log;
import com.rocketsoftware.properties.TransmittalProperties;

public class TransmittalParser {
	public static Map<String,String> transmittalProperties = new HashMap<>();
	private static final Logger log = Log.log;
	
	public static void parseTransmittal(String transmittal) {
		log.log(Level.INFO, "Reading a transmittal file");
		Scanner transmittalScanner = new Scanner(transmittal);
		String key = "";
		String value = "";
		try{
			while(transmittalScanner.hasNextLine()) {
				String currentLine = transmittalScanner.nextLine().trim();
				if(checkIfLineContainsField(currentLine)) {
					if(currentLine.charAt(0) == '*') {
						key = currentLine.substring(1, currentLine.indexOf(":")).trim();
					}
					else {
						key = currentLine.substring(0, currentLine.indexOf(":")).trim();
					}
					value = currentLine.substring(currentLine.indexOf(":") + 1, currentLine.length()).trim();
					if(value.trim().length() == 0) {
						StringBuffer multilineBuffer = new StringBuffer();
						int requiredLength = 0;
						boolean isRequiredLengthField = false;
						while(transmittalScanner.hasNextLine()) {
							currentLine = transmittalScanner.nextLine().trim();
							if(currentLine.contains("$=====")) {
								break;
							}
							else if(currentLine.contains("|...+")) {
								isRequiredLengthField = true;
								requiredLength = currentLine.trim().length() - 1;
								continue;
							}
						    if(isRequiredLengthField & currentLine.length() > requiredLength) {
						    	log.log(Level.SEVERE, "Line exceeds the required length");
						    	log.log(Level.SEVERE, "Line: " + currentLine);
						    	throw new Exception();
							}
							multilineBuffer.append(currentLine + System.lineSeparator());
						}
						value = multilineBuffer.toString().trim();
						if(value.length() == 0 & checkIfFieldIsObligatiry(key)) {
							log.log(Level.SEVERE, "Obligatory field is empty");
							log.log(Level.SEVERE, "Field: " + key);
							throw new Exception();
						}
						
					}
				}
				transmittalProperties.put(key, value);
			}
		}
		catch(Exception e) {
			System.exit(12);
		}
		transmittalScanner.close();
		log.log(Level.INFO, "Transmittal validation has been successfully passed");
		log.log(Level.INFO, "Transmittal parsing has been finished");
	}
	public static boolean checkIfLineContainsField(String token) {
		for(String field: TransmittalProperties.ptfTransmittalFields) {
			if(token.contains(field + ":")) {
				return true;
			}
		}
		return false;
	}
	public static boolean checkIfFieldIsObligatiry(String field) {
		for(String obligatoryField: TransmittalProperties.obligatoryList) {
			if(field.equals(obligatoryField)) {
				return true;
			}
		}
		return false;
	}
	public static void main(String args[]) {
		Log.configureLog();
		TransmittalProperties.initializeTransmittalFields();
		StringBuffer transmittalBuffer = new StringBuffer();
		try {
			Scanner scanner = new Scanner(new File("testFolder/transmittal"));
			while(scanner.hasNextLine()) {
				transmittalBuffer.append(scanner.nextLine());
				transmittalBuffer.append(System.lineSeparator());
			}
			scanner.close();
			parseTransmittal(transmittalBuffer.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(transmittalProperties.get("APAR_NUMBER"));
		System.out.println(transmittalProperties.get("TOOL_NAME"));
		System.out.println(transmittalProperties.get("PROBLEM_SUMMARY"));
	}
}
