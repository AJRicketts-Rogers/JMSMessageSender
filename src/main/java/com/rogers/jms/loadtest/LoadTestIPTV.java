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

public class LoadTestIPTV {
	
	private int totalMessages;
	private String testPrefix;
	private int msgPerSecond;
	private long sleepInterval;
	private int priority;
	private static final String XSL_PATH = "/xsl/SubmitOrderIPTV.xsl";
	
	public LoadTestIPTV(Properties props) {
	    this.totalMessages = Integer.parseInt(props.getProperty("iptv.totalMessages"));
	    this.testPrefix = props.getProperty("testPrefix");
	    this.msgPerSecond = Integer.parseInt(props.getProperty("iptv.msgPerSecond"));
	    this.sleepInterval = Long.parseLong(props.getProperty("iptv.sleepIntervalMillis"));
	    this.priority = Integer.parseInt(props.getProperty("iptv.priority"));
	}
	
	public LoadTestIPTV(int totalMessages, String testPrefix, int msgPerSecond, long sleepInterval, int priority) {
		this.totalMessages = totalMessages;
		this.testPrefix = testPrefix;
		this.msgPerSecond = msgPerSecond;
		this.sleepInterval = sleepInterval;
		this.priority = priority;
	}	

	public static void main(String[] args) throws IOException, TransformerException {
	    Properties props = ConfigLoader.load();
		final LoadTestIPTV sendIptv = new LoadTestIPTV(props);
		
        String username = props.getProperty("username");
        String password = props.getProperty("password");
		
		final MessageSender msgSender = new MessageSender(username, password);
		msgSender.init(MessageSender.ASMS_REQ_QUEUE);

		Date startDate = new Date();
		System.out.println("\nStart time : " + startDate + "\n");
		long startTime = System.nanoTime();

		sendIptv.sendMessages(msgSender);

		long endTime = System.nanoTime();
		long elapsedTime = endTime - startTime;
		double seconds = (double)elapsedTime / 1000000000.0;

		System.out.println("\nStart time : " + startDate);
		System.out.println("\n  End time : " + new Date());		
		System.out.println("\nAll messages sent in : " + seconds + " seconds");	
	}

	private String getCreateOrder(String productOrderKey, String cbp, String sam, String serviceKey, String w3id, int priority, String username, String password) {
		String transformedXML = null;
		try {
			InputStream xsl = getClass().getResourceAsStream(XSL_PATH);

			TransformerFactory tFactory = TransformerFactory.newInstance();  
			Transformer transformer = tFactory.newTransformer(new StreamSource(xsl));

			transformer.setParameter("osm_ws_api_username", username);
			transformer.setParameter("osm_ws_api_password", password);					

			transformer.setParameter("JR_PRODUCT_ORDER_KEY", productOrderKey);
			transformer.setParameter("CBP", cbp);
			transformer.setParameter("SAM", sam);
			transformer.setParameter("SERVICE_KEY", serviceKey);
			transformer.setParameter("W3ID", w3id);
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
		SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmssSS");
		try {
			for(int index = 1; index <= totalMessages; index++) {
				String eventDate = sdf.format(new Date());
				
				String productOrderKey = testPrefix + String.format("%06d", index);
				String cbp = eventDate;
				int random = (int)(Math.random() * 25) + 75;
				String sam = random + eventDate;
				String serviceKey = productOrderKey;
				String w3id = productOrderKey;
				
				String message = getCreateOrder(productOrderKey, cbp, sam, serviceKey, w3id, priority, msgSender.getUsername(), msgSender.getPassword());
				System.out.println("[IPTV] Sending order " + index);
				msgSender.send(message, priority, cbp);
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
