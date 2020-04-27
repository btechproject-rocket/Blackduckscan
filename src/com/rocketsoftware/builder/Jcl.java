package com.rocketsoftware.builder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rocketsoftware.log.Log;
import com.rocketsoftware.properties.Properties;


public class Jcl {
	
	protected static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String filePath = Properties.outputDirectoryForJcls + File.separator + "jclTemp";
    private static final Logger log = Log.log;
 
    public void appendToFile(String statement) {
        try {
            // Save XML to the file.
        	Files.createDirectories(Paths.get(Properties.outputDirectoryForJcls));
        	if(Files.notExists(Paths.get(filePath))) {
        		Files.createFile(Paths.get(filePath));
        	}
            Files.write(Paths.get(filePath), statement.getBytes(), (OpenOption)StandardOpenOption.APPEND);
          }
          catch (IOException e) {
            e.printStackTrace();
          }
    }
    /**
     * Cleans an output file
     */
    public void clean() {
    	try (FileWriter fw = new FileWriter(filePath)) {

        } catch (IOException e) {

        }
    }
    public void clean(String path) {
    	try (FileWriter fw = new FileWriter(path)) {

        } catch (IOException e) {

        }
    }
    public void printJcl() {
    	try{
    		File jcl = new File(filePath);
        	Scanner sc = new Scanner(jcl);
        	while(sc.hasNext()) {
        		System.out.println(sc.nextLine());
        	}
        	sc.close();
        	System.out.println(LINE_SEPARATOR);
    	}
    	catch(IOException e) {
    		e.printStackTrace();
    	}
    	
    }
    public void saveJclTo(String path) {
    	log.log(Level.INFO, "Saving JCL to " + path);
    	clean(path);
    	StringBuffer buffer = new StringBuffer();
    	try {
    		File file1 = new File(filePath);
        	Scanner sc = new Scanner(file1);
        	while(sc.hasNext()) {
        		buffer.append(sc.nextLine() + System.lineSeparator());
        	}
    	}
    	catch(IOException e) {
    		
    	}   	
    	File file = new File(path);
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(buffer.toString());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public String getJclAsString(String path) {
    	StringBuffer buffer = new StringBuffer();
    	try {
    		File jcl = new File(path);
    		Scanner sc = new Scanner(jcl);
    		while(sc.hasNext()) {
    			buffer.append(sc.nextLine());
    		}
    	}
    	catch(IOException e) {
    		e.printStackTrace();
    	}
    	return buffer.toString();
    }
   
}
