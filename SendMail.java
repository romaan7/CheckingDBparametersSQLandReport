package com.daily_check;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Multipart;
import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage; 
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.daily_check.ParameterPojo;

public class SendMail {
	StringBuilder title,tableContainer,highlightsContainer;
	String subject,attachments;
	String[] to,cc;
	//Calendar calobj = Calendar.getInstance();
	
	
	protected String getTime()  {
		Date dateobj = new Date();
		SimpleDateFormat timeStamp = new SimpleDateFormat("\n[dd/MM/yyyy HH:mm:ss:SS] ");	
		return timeStamp.format(dateobj);
	}

	protected Connection getDBConnection(ParameterPojo p) throws ClassNotFoundException, SQLException {
		
		System.out.println(getTime()+"Connecting to DB Server: "+p.getServer());
		String connurl=("jdbc:oracle:thin:@"+p.getServer());
		
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection con = DriverManager.getConnection(connurl,p.getUsername(),p.getPassword());
		System.out.println(getTime()+"Connected to DB server.\n");
		
		return con;
	}
	
	protected void closeDBConnection(Connection con) throws SQLException {
		if(con!=null)
			con.close();
		System.out.println(getTime()+"Disconnected from DB server.\n");
	}
	
	private String executeCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader =
	                        new BufferedReader(new InputStreamReader(p.getInputStream()));

	                    String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}
	
	protected String getHtmlProperties(String sec,String headercolor,String font,String font_size){
		
		String header="";
		if (sec.equals("tableHeader"))
		{
		header="<html><head>" +
		"<style type='text/css'>" +
		"table {border-collapse: collapse;width:100%;border: 1px solid #000000;}" +
		"td {border: 1px solid #000000;font-family: '"+font +"';background:#ffffff; font-size:"+font_size+"; text-align:center;}"+
		"th {border: 1px solid #000000;padding: 0; background:" +headercolor+
		";font-family: '" +font+
		"';font-size:"+font_size+";}"+
		"p{text-align:center;}"+
		"</style></head>"+"" +
		"<body><table>"+
		"<tr style='border:solid #000000;background:#000000;font-color:#ffffff;font-family:"+font+" ;'>"+
		"<th><p>Entity</p></th>"+
	    "<th><p>Check</p></th>"+
	    "<th><p>Description</p></th>"+
	    "<th><p>Type/Status</p></th>"+
	    "<th><p>Value</p></th>"+"</tr>";
		}
		else if(sec.equals("highlitesHeader")){
			
			header="<html><head><html><head><style type='text/css'>"+
			"body {font-family:"+font+"; font-size:"+font_size+";}" +
			"font {font-family:"+font+"; font-size:14.5px;}" +
			"</style></head><body>";
		}
		
		return header;

	}
	
	protected void composeMailBody(ParameterPojo p,Connection con)throws ParserConfigurationException, SQLException, FileNotFoundException, IOException, SAXException{
		
		try{
			tableContainer = new StringBuilder();
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			
	    	Document doc = docBuilder.parse(new FileInputStream(p.getInputFile()));
	    	System.out.println(getTime()+"Scanning XML file");
			// normalize text representation
			doc.getDocumentElement().normalize();
			System.out.println("Root element of the xml is "
					+ doc.getDocumentElement().getNodeName());
			
			//Set Subject and Title.
			subject = new String(doc.getDocumentElement().getAttribute("subject"));
			Date d = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("MMMMM d, yyyy");
			String date = sdf.format(d);
			subject = subject+date;
			title = new StringBuilder();
			title.append(doc.getDocumentElement().getAttribute("title"));

			String headercolor=doc.getDocumentElement().getAttribute("headercolor");
			String font=doc.getDocumentElement().getAttribute("font");
			String font_size=doc.getDocumentElement().getAttribute("font-size");
			String signoff=doc.getDocumentElement().getAttribute("signoff");
			
			NodeList listOfQueries = doc.getElementsByTagName("row");
			int totalQueries = listOfQueries.getLength();
			System.out.println("Total no of queries : " + totalQueries);
			
			
			NodeList listOfHighlites = doc.getElementsByTagName("highlites");
			int totalHighlites = listOfHighlites.getLength();
			System.out.println("Total no of highlites : " + totalHighlites);
			
			//try merge highlites start
			
			
			highlightsContainer = new StringBuilder();
			highlightsContainer.append(getHtmlProperties("highlitesHeader",headercolor,font,font_size));
			highlightsContainer.append("<font><b><u>"+subject+"</u></b></font><br><br>");
			
			System.out.println(getTime()+"Starting Generation of highlites section. \n");
			
			for (int highlitesCount = 0; highlitesCount < totalHighlites; highlitesCount++) {
				Node xmlNode = listOfHighlites.item(highlitesCount);
				if (xmlNode.getNodeType() == Node.ELEMENT_NODE) {

					Element xmlElement = (Element) xmlNode;
					System.out.println(getTime()+"Executing Query");
					//
					NodeList indexNodeList = xmlElement.getElementsByTagName("index");
				    Element indexElement = (Element) indexNodeList.item(0);
				   
				    
					NodeList textindexnumberNodeList = indexElement.getChildNodes();
					String index = new String(((Node) textindexnumberNodeList.item(0)).getNodeValue().trim());
					System.out.println("Index: " + index);
					
					//
					NodeList statmentsNodeList = xmlElement.getElementsByTagName("statments");
					Element statmentsElement = (Element) statmentsNodeList.item(0);
					
					NodeList textstatmentsNodeList = statmentsElement.getChildNodes();
					//StringBuilder statment = new StringBuilder(((Node) textstatmentsNodeList.item(0)).getNodeValue().trim());
					String statment = ((Node) textstatmentsNodeList.item(0)).getNodeValue().trim();
					System.out.println("Statment : " +statment);
					
					//
					NodeList queryList = xmlElement.getElementsByTagName("query");
					Element queryElement = (Element) queryList.item(0);
					
					NodeList textQueryList = queryElement.getChildNodes();
					String query = ((Node) textQueryList.item(0)).getNodeValue().trim();
					//System.out.println("Query: " +query);
					//
					NodeList checkIncidentList = xmlElement.getElementsByTagName("checkIncident");
					Element checkIncidentElement = (Element) checkIncidentList.item(0);
					
					NodeList textcheckIncidentList = checkIncidentElement.getChildNodes();
					String checkIncident = ((Node) textcheckIncidentList.item(0)).getNodeValue().trim();
					System.out.println("checkIncident: " +checkIncident);
					
					ArrayList<String> listOfResultsetValues = new ArrayList<String>();
					
					String MUM_Invocation_command="se_send_alert -severity WARNING -group \"MWMPROD - WARNING TEST AUTOMATED ALERT: DATE=$DATE\" -slot 1 \"TEST AUTOMATED ALERT -MWMPROD\"";
					
					if(checkIncident.contains("Y"))
					{
						
						try{
						Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
						ResultSet rs = stmt.executeQuery(query);//EXECUTE QUERIES FROM query TAG IN XML
						ResultSetMetaData rsmd = rs.getMetaData();//GET METADATA FOR RESULTSET
						int colCount=rsmd.getColumnCount();
							while (rs.next()) {
								for (int k=1;k<=colCount;k++){//FOR EVERY ROW
									String resultValue=rs.getString(k);
									listOfResultsetValues.add(resultValue);//Add the result in the array
								}				
							}
							System.out.println("Values for statments : "+listOfResultsetValues.toString());
							
							//Below code invokes the command for raising an ITSC incident.
							if(!listOfResultsetValues.isEmpty()||listOfResultsetValues.size()!=0){
								String ITSCqueueName=checkIncidentElement.getAttribute("ITSCqueueName");
								//MUM_Invocation_command="se_send_alert -severity WARNING -group \" "+ ITSCqueueName +" - WARNING TEST AUTOMATED ALERT: DATE=$DATE\" -slot 1 \"" +statment+ "\"";
								MUM_Invocation_command="java -version";
								executeCommand(MUM_Invocation_command);
								System.out.println("Incident raised with Command: "+MUM_Invocation_command);
								
								for (int i=0;i<=listOfResultsetValues.size()-1;i++){
									statment=statment.toString().replaceFirst("(?:%)+", listOfResultsetValues.toString());
								}
								System.out.println("Updated statments : "+statment);
								highlightsContainer.append(statment);
							}
							
						}catch (SQLException e ) {System.out.println(e);} 	
					}
					else 
					{
					try {
					Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
					ResultSet rs = stmt.executeQuery(query);//EXECUTE QUERIES FROM query TAG IN XML
					ResultSetMetaData rsmd = rs.getMetaData();//GET METADATA FOR RESULTSET
					int colCount=rsmd.getColumnCount();
						while (rs.next()) {
							for (int k=1;k<=colCount;k++){//FOR EVERY ROW
								String resultValue=rs.getString(k);
								listOfResultsetValues.add(resultValue);//Add the result in the array
							}												
						}
						System.out.println("Values for statments : "+listOfResultsetValues.toString());
						} catch (SQLException e ) {System.out.println(e);} 
						
					//Replace all % in the statment with the values of listOfResultsetValues array one by one
						for (int i=0;i<=listOfResultsetValues.size()-1;i++){
							statment=statment.toString().replaceFirst("(?:%)+", listOfResultsetValues.get(i));
						}
						System.out.println("Updated statments : "+statment);
						highlightsContainer.append(statment);
						
					}
				}
			}
			highlightsContainer.append("<font><u><b>"+title+"</b></u></font><br>");
			highlightsContainer.append("</body></html>");
			
			System.out.println(getTime()+"Highlites Section Generated Successfully.\n");
			
			tableContainer.append(getHtmlProperties("tableHeader",headercolor,font,font_size));
			
			System.out.println(getTime()+"Starting Generation of Checks Table section. \n");
			
			for (int s = 0; s <totalQueries; s++) {
				
				Node xmlNode = listOfQueries.item(s);
				
				if (xmlNode.getNodeType() == Node.ELEMENT_NODE) {

					Element xmlElement = (Element) xmlNode;

					System.out.println(getTime()+"Executing Query");
					//
					NodeList serialnumberNodeList = xmlElement.getElementsByTagName("serialNumber");
				    Element serialnumberElement = (Element) serialnumberNodeList.item(0);
				   
					NodeList textserialnumberNodeList = serialnumberElement.getChildNodes();
					String serialnumber = new String(((Node) textserialnumberNodeList.item(0)).getNodeValue().trim());
					System.out.println("SERIAL NUMBER : " + serialnumber);
					
					//
					NodeList entityNodeList = xmlElement.getElementsByTagName("entity");
					Element entityElement = (Element) entityNodeList.item(0);
					
					NodeList textEntityNodeList = entityElement.getChildNodes();
					String entity = new String(((Node) textEntityNodeList.item(0)).getNodeValue().trim());
					System.out.println("ENTITY : " +entity);
					
					//
					NodeList checkList = xmlElement.getElementsByTagName("check");
					Element checkElement = (Element) checkList.item(0);
					
					NodeList textCheckList = checkElement.getChildNodes();
					String check = ((Node) textCheckList.item(0)).getNodeValue().trim();
					System.out.println("CHECK : " + check);
					//
					NodeList descriptionList = xmlElement.getElementsByTagName("description");
					Element descriptionElement = (Element) descriptionList.item(0);
					
					NodeList textdescriptionList = descriptionElement.getChildNodes();
					String description = ((Node) textdescriptionList.item(0)).getNodeValue().trim();
					System.out.println("DESCRIPTION : " + description);

					//
					NodeList queryList = xmlElement.getElementsByTagName("query");
					Element queryElement = (Element) queryList.item(0);
					
					NodeList textQueryList = queryElement.getChildNodes();
					String query = ((Node) textQueryList.item(0)).getNodeValue().trim();
					//System.out.println("QUERY : " +query)
					
					//----------------------start executing the SQL statments from XML 
					
					try {
						Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
						ResultSet rs = stmt.executeQuery(query);//EXECUTE QUERIES FROM query TAG IN XML
						ResultSetMetaData rsmd = rs.getMetaData();//GET METADATA FOR RESULTSET
						int columnCount =rsmd.getColumnCount();//GET COLUMN CONT( NUMBER OF COLUMNS RETURNED BY QUERY
						int rowCount = 0;
						//int firstrow=1;//SET VERIABLE FOR FORMATING THE HTML TABLE ACCORDING TO THE TABLE ROWS
						rs.last();
						rowCount = rs.getRow();
						rs.beforeFirst();

						tableContainer.append("<tr><td rowspan="+rowCount+">"+entity+"</td><td rowspan="+rowCount+">"+check+"</td><td rowspan="+rowCount+">"+description+"</td>");
	    
						if(!rs.next()){ //IF NO RECORDS ARE RETURNED. THIS MOVES CURSOR TO NEXT ROW
							System.out.println("No data to display");
							tableContainer.append("<td> - </td><td> - </td></tr>");

	// COMMENTING THE CODE FOR FORMATING ROWS(this was implemented before implementing the row merging
	/*						if(firstrow==1){
							htmlContainer.append("<td> - </td><td> - </td></tr>");
							firstrow=0;
							}
							else{
							htmlContainer.append("<td></td>"+"<td></td>"+"<td></td>"+"<td> - </td><td> - </td></tr>");
							}
	*/
						}
						else{
					for (int j=1;j<=columnCount;j++){//LOOP FOR GETTING ALL THE COLUMNS RETURNED BY THE QUERY
							rs.previous();//MOVE THE CURSOR TO PREVIOUS ROW
							while (rs.next()) {
								
								for (int k=0;k<columnCount;k++){//FOR EVERY ROW
									String column=rs.getString(rsmd.getColumnName(j+k));
									tableContainer.append("<td>"+column+"</td>");
									
									System.out.println("OUTPUT : "+column);
								}
								tableContainer.append("</tr>");
							}
							}
						}
					}catch (SQLException e ) {System.out.println(e);} 
					//------------------ End the sql execution.
				}
		}
			tableContainer.append("</table></body></html>"+"<br>"+signoff);
			System.out.println(getTime()+"Table generation completed.\n");
		
	}catch (FileNotFoundException e){
		 System.out.println(getTime()+"Input File Not found!");}	
			
	}//End generateHTMLtable method

	protected void sendMail(ParameterPojo p) throws IOException {
		try {
			
			  to = p.getTo().split(",");
			  InternetAddress[] address_to = new InternetAddress[to.length];
			  for(int i = 0; i < to.length; i++)
					address_to[i] = new InternetAddress(to[i]);  
			  
			  cc = p.getCc().split(",");
			  InternetAddress[] address_cc = new InternetAddress[cc.length];
			  for(int i = 0; i < cc.length; i++)
					address_cc[i] = new InternetAddress(cc[i]);
		     
			  //Get the session object  
		      Properties properties = System.getProperties();  
		      properties.setProperty("mail.smtp.host",p.getHost());  
		      Session session = Session.getDefaultInstance(properties);
		      
		     //compose the message  
		         MimeMessage message = new MimeMessage(session);  
		         message.setFrom(new InternetAddress(p.getFrom()));  
		         message.addRecipients(Message.RecipientType.TO, address_to);
		         message.addRecipients(Message.RecipientType.CC, address_cc);
		         message.setSubject(subject);
		         message.setText(title.toString());
		         Multipart multipart = new MimeMultipart();
		         BodyPart htmlBodyPart = new MimeBodyPart();
		         htmlBodyPart.setContent(highlightsContainer+"<br>"+tableContainer, "text/html"); 
		         multipart.addBodyPart(htmlBodyPart); 
		         
		         File[] files = new File(p.getAttachments()).listFiles();

		         
		         
		         for (File fileName : files) {
		        	    MimeBodyPart messageBodyPart = new MimeBodyPart();
		        	 // Attach the files
		        	    DataSource source = new FileDataSource(fileName);
		        	    messageBodyPart.setDataHandler(new DataHandler(source));
		        	    messageBodyPart.setFileName(source.getName());
		        	  //add the attachments part to multipart message body
		        	    multipart.addBodyPart(messageBodyPart);
		        	    
		        	    System.out.println(getTime()+"Attaching File: "+fileName); 
		        	}
		         
		         
		         message.setContent(multipart);

		         // Send message  
		         Transport.send(message);  
		         System.out.println(getTime()+"Message Sent Successfully.");  
		  
		      }catch (MessagingException mex) {
		    	  mex.printStackTrace();
		    	  }  
		     
	}

}
