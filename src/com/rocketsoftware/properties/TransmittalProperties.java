package com.rocketsoftware.properties;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TransmittalProperties {
	
	public static List<String> ptfTransmittalFields = new ArrayList<>();
	public static List<String> obligatoryList = new ArrayList<>();
	public static final String fieldsFilePath = "testFolder/availableFields.txt";
	
	public static void initializeTransmittalFields() {
		Scanner scanner;
		try {
			scanner = new Scanner(new File(fieldsFilePath));
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if(line.contains("*")) {
					ptfTransmittalFields.add(line.replace("*", "").trim());
					obligatoryList.add(line.replace("*", "").trim());
				}
				else {
					ptfTransmittalFields.add(line.trim());
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
	}
	public static void main(String args[]) {
		initializeTransmittalFields();
	}
}
