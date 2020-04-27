package com.rocketsoftware.rocketbranded;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rocketsoftware.builder.Jcl;


public class ParseJCL extends Jcl  {

	public ParseJCL() {
		
	}
	
	public void changeJCL(String path, String ticketNumber) {
		System.out.println("----- Changing JCL: " + path + " -----");
		String result = null;
		File applyJCL = new File(path);
		StringBuffer buffer = new StringBuffer();
		try {
			Scanner sc = new Scanner(applyJCL);
			while(sc.hasNext()) {
				buffer.append(sc.nextLine() + System.lineSeparator());
			}
			result = buffer.toString();	    
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		StringTokenizer tokenizer = new StringTokenizer(buffer.toString(), " "); 
		Pattern sequencePattern = Pattern.compile("^S\\d{3}$");
		Pattern aparNumberPattern = Pattern.compile("^\\S{2}\\d{5}$");
		Pattern ticketNumberPattern = Pattern.compile("^\\S{3,4}-\\d{4,5}$");//?TicketNumber
		
		String sequenceNumber = "";
	
		
		
		while(tokenizer.hasMoreTokens()) {
			String temp = tokenizer.nextToken();
			Matcher sequenceMatcher = sequencePattern.matcher(temp);
			/*
			 * In this section we search for sequence, apar#, ticket# using regular expressions
			 */
			if(sequenceMatcher.matches()) {
				//Example: token temp will look like S025
				Integer number = Integer.parseInt(temp.substring(1, temp.length())); //Take a number part from the string (25)
				sequenceNumber = temp.substring(1, temp.length());
				Integer increment = number + 1; //Increment number (26)
				String incremented = temp.replace(number.toString(), increment.toString());
				//Replace old number with incremented one S025 -> 25 to 26 -> S026
				result = result.replace(temp, incremented);
				result = result.replace("," + sequenceNumber, "," + incremented.substring(1, incremented.length()));
				//replace S025 token with S026
				System.out.println("Sequence number has been changed from " + temp + " to " + incremented);
			}
			Matcher aparNumberMatcher = aparNumberPattern.matcher(temp);
			if(aparNumberMatcher.matches()) {
				Integer number = Integer.parseInt(temp.substring(2, temp.length()));
				Integer increment = number + 1;
				String incremented = temp.replace(number.toString(), increment.toString());
				result = result.replace(temp, incremented);
				//result = result.replace(number.toString(), increment.toString());
				System.out.println("APAR number has been changed from " + temp + " to " + incremented);
			}
			Matcher ticketNumberMatcher = ticketNumberPattern.matcher(temp);
			if(ticketNumberMatcher.matches()) {
				result = result.replace(temp, ticketNumber);
				System.out.println("Ticket number has been changed from " + temp + " to " + ticketNumber);
			}
			
		}
		
		//System.out.println(System.lineSeparator() + "MODIFIED JCL: " + System.lineSeparator() + result);
		System.out.println(System.lineSeparator() + "Writing JCL to a file");
		
		clean();
		appendToFile(result);
		System.out.println("----- JCL parsing finished -----" + System.lineSeparator());
	}
	/*public void  submitMigrationJcl() {
		ActionsFtp actions = new ActionsFtp(BUILD_HOST);
		actions.submitJob();
	}*/
	public static void main(String args[]) {
		ParseJCL parseJCL = new ParseJCL();
		parseJCL.changeJCL("test/SMPBIN/MXHPTF13", "RTS-1590");
	}
		

}
