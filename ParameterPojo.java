package com.daily_check;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


public class ParameterPojo {
	FileInputStream inputstream;
	String to,from,cc,host,server,inputfile,username,password,attachments;
	
	private void setTo(String to) {
		this.to = to;
	}
	
	private void setFrom(String from) {
		this.from = from;
	}
	
	private void setCc(String cc) {
		this.cc = cc;
	}
	
	private void setHost(String host) {
		this.host = host;
	}
	
	private void setServer(String server) {
		this.server = server;
	}
	
	private void setUsername(String username) {
		this.username = username;
	}
	
	private void setPassword(String password) {
		this.password = password;
	}
	
	private void setInputFile(String inputfile) {
		this.inputfile = inputfile;
	}
	
	protected void setAttachments(String attachments) {
		this.attachments = attachments;
	}
	
	protected String getTo() {
		return (this.to);
	}

	protected String getFrom() {
		return (this.from);
	}
	
	protected String getCc() {
		return (this.cc);
	}

	protected String getHost() {
		return (this.host);
	}
	
	protected String getServer() {
		return (this.server);
	}
	
	protected String getUsername() {
		return (this.username);
	}
	
	protected String getPassword() {
		return (this.password);
	}
	
	protected String getInputFile() {
		return (this.inputfile);
	}
	
	protected String getAttachments() {
		return (this.attachments);
	}
	
	
	protected void loadProperties(String pass) throws IOException
	{
		try {

		Properties prop = new Properties();
		String propFileName = "config.properties";
		
		
		inputstream = new FileInputStream(propFileName);
		prop.load(inputstream);
		
		if(inputstream == null)
			throw new FileNotFoundException("Property file: "+propFileName+" not found!");
			
		
		Date date = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("E");
		
		setTo(prop.getProperty("to"));
		setFrom(prop.getProperty("from"));
		setCc(prop.getProperty("cc"));
		setHost(prop.getProperty("host"));
		setServer(prop.getProperty("server"));
		if(ft.format(date).equals("Mon"))
			setInputFile(prop.getProperty("inputfile1"));
		else
			setInputFile(prop.getProperty("inputfile2"));
		setUsername(prop.getProperty("username"));
		setAttachments(prop.getProperty("attachments"));
		if(pass.isEmpty())
			setPassword(prop.getProperty("password"));
		else
			setPassword(pass);
	}catch(IOException e) {
		e.printStackTrace();
	}
	finally {
		inputstream.close();
	}
	}
	
}

