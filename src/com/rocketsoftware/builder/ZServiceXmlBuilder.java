package com.rocketsoftware.builder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.rocketsoftware.jenkins.JenkinsParameters;
import com.rocketsoftware.properties.Properties;

public class ZServiceXmlBuilder
{
  public ZServiceXmlBuilder() {}
  
  public static Path saveBuildXML(Path path, String sysmodType)
  {
    ArrayList<String> buildXML = new ArrayList<>();
    buildXML.add("<?xml version=\"1.0\"?>");
    buildXML.add("<project name=\"com.ibm.rocket."+ JenkinsParameters.FMID +"\" basedir=\".\" default=\"zservice\" xmlns:host=\"antlib:com.ibm.hostservices.ant\" xmlns:hostzos=\"antlib:com.ibm.hostservices.zos.ant\" xmlns:contrib=\"antlib:net.sf.antcontrib\" xmlns:zservice=\"antlib:com.ibm.zservice.ant\">");
    buildXML.add("                                                                                                                                                                                              ");
    buildXML.add("\t<description>Sample build.xml</description>                                                                                                                                                 ");
    buildXML.add("                                                                                                                                                                                              ");
    buildXML.add("\t<!-- ********************************************************************************************* -->                                                                                      ");
    buildXML.add("\t<!-- Existing properties from the ROCKET FMID build -->                                                                                                                                     ");
    buildXML.add("\t<!-- ********************************************************************************************* -->                                                                                      ");
    buildXML.add("                                                                                                                                                                                              ");
    buildXML.add("\t<!-- Define the fmids used for this product -->                                                                                                                                             ");
    buildXML.add("\t<property environment=\"env\"/>                                                                                                                                                             ");
    buildXML.add("\t<property name=\"fmid\" value=\""+ JenkinsParameters.FMID +"\" />                                                                                                                                            ");
    buildXML.add("                                                                                                                                                                                              ");
    buildXML.add("                                                                                                                                                                                              ");
    buildXML.add("\t<!--H22O310 COMPID for Anaconda -->                                                                                                                                                         ");
    buildXML.add("\t<property name=\"comp.id\" value=\""+ JenkinsParameters.COMP_ID + "\" />                                                                                                                                      ");
    buildXML.add("\t<property name=\"srel\" value=\""+ JenkinsParameters.SREL + "\" />                                                                                                                                            ");
    buildXML.add("                                                                                                                                                                                              ");
    buildXML.add("\t<!-- ********************************************************************************************* -->                                                                                      ");
    buildXML.add("\t<!-- Additional properties required for zService -->                                                                                                                                        ");
    buildXML.add("\t<!-- ********************************************************************************************* -->                                                                                      ");
    buildXML.add("                                                                                                                                                                                              ");
    buildXML.add("\t<property name=\"type\" value=\"RAW\" />                                                                                                                                                    ");
    buildXML.add("                                                                                                                                                                                              ");
    buildXML.add("\t<!-- The name of the build -->                                                                                                                                                              ");
    buildXML.add("\t<property name=\"build\" value=\""+ JenkinsParameters.APAR_NUMBER + "\" />                                                                                                                                     ");
    buildXML.add("                                                                                                                                                                                              ");
    buildXML.add("\t<!-- The name of the level e.g. APAR name. Defaults to build name -->                                                                                                                       ");
    buildXML.add("\t<property name=\"level\" value=\"${build}\" />                                                                                                                                              ");
    buildXML.add("\t                                                                                                                                                                                            ");
    buildXML.add("\t<!-- The name of the release -->                                                                                                                                                            ");
    buildXML.add("\t<property name=\"release\" value=\""+ JenkinsParameters.RELEASE_NAME+ "\" />                                                                                                                                  ");
    buildXML.add("                                                                                                                                                                                              ");
    buildXML.add("                                                                                                                                                                                              ");
    buildXML.add("\t<!-- The location of the liberty server that is used to service FMID -->                                                                                                                    ");
    buildXML.add("\t<property name=\"zservice.uri\" value=\""+ JenkinsParameters.Z_SERVER + "\" />                                                                                                                                 ");
    buildXML.add("                                                                                                                                                                                              ");
    buildXML.add("\t<!-- The location of the liberty server running on a z/OS host that is used to package FMID  -->                                                                                            ");
    buildXML.add("\t<property name=\"zbuild.uri\" value=\"https://winmvs4e.hursley.ibm.com:8581\" />                                                                                                            ");
    buildXML.add("\t<property name=\"zpackage.uri\" value=\"https://winmvs4e.hursley.ibm.com:8581\" />                                                                                                          ");
    buildXML.add("                                                                                                                                                                                              ");
    buildXML.add("\t<!-- Authentication credentials to use when communicating with security  enabled servers -->                                                                                                ");
    buildXML.add("\t<host:auth user=\"${env.w3iduser}\" password=\"${env.w3idpassword}\" />                                                                                                                     ");
    buildXML.add("\t<host:trustall />                                                                                                                                                                           ");
    buildXML.add("                                                                                                                                                                                              ");
    buildXML.add("\t<target name=\"zservice\" description=\"Service Level define\">\t\t                                                                                                                        ");
    // Execute JCL on WINBOX
    buildXML.add("		<hostzos:job uri=\"${zpackage.uri}\" jcl=\"${basedir}/"+ Properties.jclToReceiveOnWinbox +"\">");
    buildXML.add("	</hostzos:job>");    
    // Define and build APAR
    buildXML.add("\t\t<ant antfile=\""+Properties.serviceXML+"\" target=\"define\" inheritrefs=\"true\" />                                                                               ");
    // Distributing Sysmod
    buildXML.add("		<echo>Distributing Sysmod</echo>                                                                                ");
    buildXML.add("		<zservice:distribute uri=\""+ JenkinsParameters.Z_SERVER + "\" release=\""+ JenkinsParameters.RELEASE_NAME+ "\">                                ");
    buildXML.add("			<zservice:sysmods uri=\""+ JenkinsParameters.Z_SERVER + "\" name=\""+ JenkinsParameters.RELEASE_NAME+ "/"+ JenkinsParameters.APAR_NUMBER +"/"+sysmodType.toUpperCase()+"\" includes=\"*."+sysmodType.toUpperCase()+"\"/>  ");
    buildXML.add("			<site name=\"winmvs4e\" type=\"FTP_ZOS\">                                                                         ");
    buildXML.add("				<property name=\"host\" value=\"winmvs4e.hursley.ibm.com\"/>                                                  ");
    buildXML.add("				<property name=\"user\" value=\""+ JenkinsParameters.WINBOX_USER + "\"/>                                                                     ");
    buildXML.add("				<property name=\"password\" value=\""+ JenkinsParameters.WINBOX_PASSWORD + "\"/>                                                              ");
    buildXML.add("				<property name=\"destination\" value=\"ROCKET."+ JenkinsParameters.FMID +"\"/>                                                     ");
    buildXML.add("			</site>                                                                                                           ");
    buildXML.add("		</zservice:distribute>                                                                                                ");
    // Getting SYSMOD ID
    buildXML.add("		<!-- Getting SYSMOD_ID from zService-->                                                                                                                                           ");
    buildXML.add("		<contrib:for param=\"release\">                                                                                                                                                   ");
    buildXML.add("				    <mappedresources>                                                                                                                                                     ");
    buildXML.add("				        <zservice:sysmods uri=\"${zservice.uri}\"  name=\"${release}/${level}/"+sysmodType.toUpperCase()+"\" includes=\"*."+sysmodType.toUpperCase()+"\"/>                ");
    buildXML.add("				        <globmapper from=\"*."+sysmodType.toUpperCase()+"\" to=\"*\" />                                                                                                   ");
    buildXML.add("				    </mappedresources>                                                                                                                                                    ");
    buildXML.add("				    <sequential>                                                                                                                                                          ");
    buildXML.add("				        <echo>Building send JCL for @{release}</echo>                                                                                                                     ");
    buildXML.add("						<replace file=\"" + Properties.outputDirectoryForJcls + File.separator + Properties.jclToSendFromWinbox + "\" token=\"@P@R_NUMBER\" value=\"@{release}\"/>       ");
    buildXML.add("				    	<echo>Building RECEIVE JCL for @{release}</echo>                                                                                                                  ");
    buildXML.add("				    	<replace file=\""+ Properties.outputDirectoryForJcls + File.separator + Properties.jclToReceiveOnArtifactoryHost +"\" token=\"@P@R_NUMBER\" value=\"@{release}\"/>");
    buildXML.add("				    </sequential>                                                                                                                                                         ");
    buildXML.add("		</contrib:for>                                                                                                                                                                    ");
    buildXML.add("		                                                                                                                                                                                  ");
    buildXML.add("		<hostzos:job uri=\"${zpackage.uri}\" jcl=\"" + Properties.outputDirectoryForJcls + File.separator + Properties.jclToSendFromWinbox + "\">                                        ");
    buildXML.add("		</hostzos:job>                                                                                                                                                                    ");
    buildXML.add("		<java classname=\"com.rocketsoftware.main.ReceiveSysmodOnArtifactoryHost\">                     ");
    buildXML.add("         <sysproperty key=\"HOST\" value=\""+ JenkinsParameters.ARTIFACTORY_HOST +"\"/>");
    buildXML.add("		  <classpath>                                                                                 ");
    buildXML.add("			<pathelement location=\"${env.WORKSPACE}\\pkg\\PackagingAutomation\\PrepareAparBuild.jar\"/>   ");
    buildXML.add("			<pathelement location=\"${env.WORKSPACE}\\pkg\\PackagingAutomation\\ant\\ant-extension-zService\\commons-net-3.6.jar\"/>   ");
    buildXML.add("			<pathelement location=\"${env.WORKSPACE}\\pkg\\PackagingAutomation\\lib\\commons-codec-1.9.jar\"/>   ");
    buildXML.add("		  </classpath>                                                                                ");
    buildXML.add("		</java>                                                                                       ");
    buildXML.add("\t</target>                                                                                                                                                                                   ");
    buildXML.add("\t                                                                                                                                                                                            ");
    buildXML.add("</project>                                                                                                                                                                                    ");
    

    try
    {
      // Save XML to the file. 
      Files.write(path, buildXML );
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return path;
  }
  
  public static Path saveServiceXML(Path path, String sysmodType) { 
	  List<String> serviceXML = new ArrayList<>();
    serviceXML.add("<?xml version=\"1.0\"?>");
    serviceXML.add("<project name=\"com.ibm.rocket."+ JenkinsParameters.FMID +"\" basedir=\"..\" default=\"define\" xmlns:contrib=\"antlib:net.sf.antcontrib\" xmlns:rsel=\"antlib:org.apache.tools.ant.types.resources.selectors\" xmlns:antz=\"antlib:com.ibm.tools.antz\" xmlns:host=\"antlib:com.ibm.hostservices.ant\" xmlns:hostzos=\"antlib:com.ibm.hostservices.zos.ant\" xmlns:zservice=\"antlib:com.ibm.zservice.ant\">");
    serviceXML.add("                                                                                                                                                                                                                                                                                                                                                                                                    ");
    serviceXML.add("\t<import file=\""+Properties.functionXML+"\" />                                                                                                                                                                                                                                                                                                                                                               ");
    serviceXML.add("                                                                                                                                                                                                                                                                                                                                                                                                    ");
    serviceXML.add("\t<!-- Set aparsfixed property based on whether the level has been named as an internal level (blank name means internal) -->                                                                                                                                                                                                                                                                     ");
    serviceXML.add("\t<condition property=\"aparsfixed\" value=\"\" else=\"${level}\">                                                                                                                                                                                                                                                                                                                                ");
    serviceXML.add("\t\t<matches string=\"${build}\" pattern=\"^IN.*\" />                                                                                                                                                                                                                                                                                                                                           ");
    serviceXML.add("\t</condition>                                                                                                                                                                                                                                                                                                                                                                                    ");
    serviceXML.add("                                                                                                                                                                                                                                                                                                                                                                                                    ");
    serviceXML.add("\t<!-- Define a level to zService                                                                                                                                                                                                                                                                                                                                                                 ");
    serviceXML.add("\t\tusermod is used to test purpose. We will replace it with APAR or PTF when ready.                                                                                                                                                                                                                                                                                                            ");
    serviceXML.add("\t -->                                                                                                                                                                                                                                                                                                                                                                                            ");
    serviceXML.add("\t<target name=\"define\">                                                                                                                                                                                                                                                                                                                                                                        ");
    serviceXML.add("\t\t<echo level=\"info\">Defining the zservice level ${level} using ${zservice.uri}</echo>                                                                                                                                                                                                                                                                                                      ");
    serviceXML.add("\t\t <zservice:"+sysmodType.toLowerCase()+" uri=\"${zservice.uri}\" name=\"${release}/${level}\">                                                                                                                                                                                                                                                                                                                    ");
    serviceXML.add("\t\t \t<function refid=\"base\" aparsfixed=\"${aparsfixed}\" />                                                                                                                                                                                                                                                                                                                                ");
    serviceXML.add("\t\t </zservice:"+sysmodType.toLowerCase()+">                                                                                                                                                                                                                                                                                                                                                                        ");
    serviceXML.add("\t</target>                                                                                                                                                                                                                                                                                                                                                                                       ");
    serviceXML.add("                                                                                                                                                                                                                                                                                                                                                                                                    ");
    serviceXML.add("</project>                                                                                                                                                                                                                                                                                                                                                                                          ");

    try {
      // Save XML to the file. 
      Files.write(path, serviceXML);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    
    return path;
  }
  
  /**
   *  Create and save .xml file to submit RECVWIN job through zService Ant API
   * @param pathToXml to save. Usually it is Jenkins workspace/output/build_number
   * @return
   */
  public static Path saveZServiceSubmitJobXML(Path pathToXml, String recieveJcl) {
	  ArrayList<String> subJobXML = new ArrayList<>();
	  try {
		  subJobXML.add("<?xml version=\"1.0\"?>                                                                                                              ");
		  subJobXML.add("<project name=\"subJob\" basedir=\"..\" default=\"subjob\" xmlns:host=\"antlib:com.ibm.hostservices.ant\"                            ");
		  subJobXML.add("	xmlns:hostzos=\"antlib:com.ibm.hostservices.zos.ant\" >                                                                          ");
		  subJobXML.add("   <property environment=\"env\"/>                                                                                                                                  ");
		  subJobXML.add("	<property name=\"zpackage.uri\" value=\"https://winmvs4e.hursley.ibm.com:8581\" />                                               ");
		  subJobXML.add("	<!-- Authentication credentials to use when communicating with security  enabled servers -->                                     ");
		  subJobXML.add("	<host:auth user=\"${env.w3iduser}\" password=\"${env.w3idpassword}\" />                                                          ");
		  subJobXML.add("	<host:trustall />                                                                                                                ");
		  subJobXML.add("                                                                                                                                     ");
		  subJobXML.add("	<target name=\"subjob\" description=\"Service Level define\" >                                                                   ");
		  subJobXML.add("		<hostzos:job uri=\"${zpackage.uri}\" jcl=\""+ recieveJcl +"\"> ");
		  subJobXML.add("		  </hostzos:job>                                                                                                             ");
		  subJobXML.add("		                                                                                                                             ");
		  subJobXML.add("	</target>                                                                                                                        ");
		  subJobXML.add("</project>");
		  
		  Files.write(pathToXml, subJobXML);
	  }
	  catch(Exception e) {
		  e.printStackTrace();
	  }
	  return pathToXml;
  }
}