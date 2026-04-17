package com.rogers.jms.loadtest;


import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.jms.JMSException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.rogers.jms.util.ConfigLoader;
import com.rogers.jms.util.MessageSender;

public class LoadTestVOD {
	
	private int totalMessages;
	private String testPrefix;
	private int msgPerSecond;
	private long sleepInterval;
	private int priority;
	private static final String XSL_PATH = "/xsl/SubmitOrderVOD.xsl";
	
	public LoadTestVOD(Properties props) {
	    this.totalMessages = Integer.parseInt(props.getProperty("vod.totalMessages"));
	    this.testPrefix = props.getProperty("testPrefix");
	    this.msgPerSecond = Integer.parseInt(props.getProperty("vod.msgPerSecond"));
	    this.sleepInterval = Long.parseLong(props.getProperty("vod.sleepIntervalMillis"));
	    this.priority = Integer.parseInt(props.getProperty("vod.priority"));
	}
	
	public LoadTestVOD(int totalMessages, String testPrefix, int msgPerSecond, long sleepInterval, int priority) {
		this.totalMessages = totalMessages;
		this.testPrefix = testPrefix;
		this.msgPerSecond = msgPerSecond;
		this.sleepInterval = sleepInterval;
		this.priority = priority;
	}

	public static void main(String[] args) throws IOException, TransformerException {
	    Properties props = ConfigLoader.load();
		final LoadTestVOD lt = new LoadTestVOD(props);
		
        String username = props.getProperty("username");
        String password = props.getProperty("password");
		
		final MessageSender msgSender = new MessageSender(username, password);
		msgSender.init(MessageSender.EAI_REQ_QUEUE);

		Date startDate = new Date();
		System.out.println("\nStart time : " + startDate + "\n");
		long startTime = System.nanoTime();

		lt.sendMessages(msgSender);

		long endTime = System.nanoTime();
		long elapsedTime = endTime - startTime;
		double seconds = (double)elapsedTime / 1000000000.0;

		System.out.println("\nStart time : " + startDate);
		System.out.println("\n  End time : " + new Date());		
		System.out.println("\nAll messages sent in : " + seconds + " seconds");	
	}

	private String getCreateOrder(String corrId, String eventDate, String action, String can, String macAddress, int priority, String username, String password) {
		String transformedXML = null;
		try {
			InputStream xsl = getClass().getResourceAsStream(XSL_PATH);

			TransformerFactory tFactory = TransformerFactory.newInstance();  
			Transformer transformer = tFactory.newTransformer(new StreamSource(xsl));

			transformer.setParameter("osm_ws_api_username", username);
			transformer.setParameter("osm_ws_api_password", password);

			transformer.setParameter("CORR_ID", corrId);
			transformer.setParameter("PRIORITY", priority);
			transformer.setParameter("EVENT_TIME", eventDate);
			transformer.setParameter("ACTION", action);
			transformer.setParameter("CAN", can);
			transformer.setParameter("MACADDR", macAddress);
			transformer.setParameter("PRIORITY", priority);

			StringReader reader = new StringReader("<OSMORDER></OSMORDER>");
			StringWriter writer = new StringWriter();

			transformer.transform(new StreamSource(reader), new StreamResult(writer));
			transformedXML = writer.toString();

		} catch (TransformerException e) {
			e.printStackTrace();
		}
//		System.out.println(transformedXML);
		return transformedXML;
	}

	
	public void sendMessages(MessageSender msgSender) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS");
		try {
			for(int index = 1; index <= totalMessages; index++) {
				String corrId = "VOD-" + testPrefix + String.format("%06d", index);
				String customer = "C" + index;
				String macAddress = "DEVICE" + index;
				String eventDate = sdf.format(new Date());
				String action = "ADDSTB";	//Use this for DTVOrder (VOD)
				String message = getCreateOrder(corrId, eventDate, action, customer, macAddress, priority, msgSender.getUsername(), msgSender.getPassword());
				System.out.println("[VOD] Sending order" + index);
				msgSender.send(message, priority, corrId);
				if(index % msgPerSecond == 0 && index < totalMessages) {
					try {
						System.out.println("Sleeping " + (sleepInterval/1000) + " sec");
						Thread.sleep(sleepInterval);
					} catch (InterruptedException e) {
						System.err.println(e.getMessage());
					}
				}
			}

		} 
		catch (JMSException e) {
			e.printStackTrace();
		}

	}
	

}
