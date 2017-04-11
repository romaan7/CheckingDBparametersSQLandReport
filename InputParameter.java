package com.daily_check;

import com.beust.jcommander.Parameter;
 
public class InputParameter {

	@Parameter(names = "-p", description = "password" , required = false, password = true)
	  public String password;
	
	/*@Parameter(names = "-s", description = "Server on which the file to be run" , required = true)
  public String server;
  
  @Parameter(names = "-u", description = "username", required = true)
  public String username;
  
  
  
  @Parameter(names = "-i", description = "input file name" , required = true)
  public String inputfile;
  
  @Parameter(names = "-o", description = "output file name" , required = false)
  public String outputfile;

  @Parameter(names = "-m", description = "mail host" , required = true)
  public String mailhost;

  @Parameter(names = "-to", description = "send mails to " , required = true)
  public String to;

  @Parameter(names = "-from", description = "send mails from " , required = false)
  public String from;
  
  @Parameter(names = "-cc", description = "send mails in CC " , required = false)
  public String cc;
  
  @Parameter(names = "-attachment", description = "attach the files with email " , required = false)
  public String attachment;

  @Parameter(names = "-highlights", description = "highlights input file" , required = false)
  public String highlights;*/
  
public void usage() {
	
	//System.out.println("Usages:\n"+"\t -s server\n"+"\t -u username\n"+"\t -p password\n"+"\t -i input file location\n"+"\t -o output file location\n"+"\t -m mail host\n"+"\t -to email recipent\n"+"\t -from mails from\n"+"\t -cc cc the mail to\n"+"\t -attachment attach files seperated by comas\n"+"\t -highlights file containg highlite details\n");
	System.out.println("Usage:\n"+"\t -p password");
}
  
  }
