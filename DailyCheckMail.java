package com.daily_check;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Connection;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.daily_check.ParameterPojo;

public class DailyCheckMail extends SendMail{  

	public static void main(String [] args) throws IOException, ParserConfigurationException, SQLException, SAXException, ClassNotFoundException, ArrayIndexOutOfBoundsException, ParameterException{
	 ParameterPojo p = new ParameterPojo();
	 SendMail s = new SendMail();
	 String pass=;
	 try{
		 InputParameter params = new InputParameter();
		 new JCommander(params,args);
		 pass=params.password;
		    if (pass==null){p.loadProperties();}
		    else {p.loadProperties(pass);}
	 Connection con = s.getDBConnection(p);
	 s.composeMailBody(p,con);
	 s.closeDBConnection(con);
	 s.sendMail(p);
	 }
	 catch(Exception e) {
		 e.printStackTrace();
	 }
 }
	
}  