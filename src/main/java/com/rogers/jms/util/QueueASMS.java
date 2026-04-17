package com.rogers.jms.util;

import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class QueueASMS
{
	// Defines the JNDI context factory.
	public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";

	// Defines the JMS connection factory for the OSM queue.
	public final static String OSM_JMS_FACTORY = "rogers/osm/jms/ConnectionFactory";

	// Defines the OSM web service queue.
	public final static String OSM_QUEUE = "jms/ASMS.OSM.REQ.Q";

	private QueueConnectionFactory qconFactory;
	private QueueConnection qcon;
	private QueueSession qsession;
	private QueueSender qsender;
	private Queue queue;
	private TextMessage msg;

	/**
	 * Creates all the necessary objects for sending
	 * messages to a JMS queue.
	 *
	 * @param ctx JNDI initial context
	 * @param queueName name of queue
	 * @exception NamingException if operation cannot be performed
	 * @exception JMSException if JMS fails to initialize due to internal error
	 */
	public void init(Context ctx, String queueName)
			throws NamingException, JMSException {
		qconFactory = (QueueConnectionFactory) ctx.lookup(OSM_JMS_FACTORY);
		qcon = qconFactory.createQueueConnection();
		qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		queue = (Queue) ctx.lookup(queueName);
		qsender = qsession.createSender(queue);
		msg = qsession.createTextMessage();
		qcon.start();
	}
	
	public void connectionStart() throws JMSException {
		qcon.start();
	}
	
	public void connectionStop() throws JMSException {
		qcon.stop();
	}
	
	public void init() {
		Context ctx = getInitialContext();
		try {
			init(ctx, OSM_QUEUE);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}	

	/**
	 * Sends a message to a JMS queue.
	 *
	 * @param message  message to be sent
	 * @exception JMSException if JMS fails to send message due to internal error
	 */
	public void send(String message, int priority) throws JMSException {
		msg.setText(message);
//		msg.setStringProperty("_wls_mimehdrContent_Type", "text/xml; charset=UTF-8");
//		msg.setStringProperty("URI", "/OrderManagement/wsapi");
		qsender.setPriority(priority);
		
		qsender.send(msg);
	}

	/**
	 * Closes JMS objects.
	 * @exception JMSException if JMS fails to close objects due to internal error
	 */
	public void close() throws JMSException {
		qsender.close();
		qsession.close();
		qcon.close();
	}

	private static InitialContext getInitialContext() {
		InitialContext ctx = null;
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
	
		
//		//DEV1
//		env.put(Context.PROVIDER_URL, "t3://osmdev1-z1.osn.wireless.rogers.com:7001,osmdev1-z2.osn.wireless.rogers.com:7001");
//		env.put(Context.SECURITY_PRINCIPAL, "si_testing");
//		env.put(Context.SECURITY_CREDENTIALS, "si_testing_123");		

		
//		//DEV2
//		env.put(Context.PROVIDER_URL, "t3://osmdev2-z1.osn.wireless.rogers.com:7001,osmdev2-z2.osn.wireless.rogers.com:7001");
//		env.put(Context.SECURITY_PRINCIPAL, "capgemini");
//		env.put(Context.SECURITY_CREDENTIALS, "capgemini1");		
		
//		//DEV3
//		env.put(Context.PROVIDER_URL, "t3://osmdev3-z1.osn.wireless.rogers.com:7001,osmdev3-z2.osn.wireless.rogers.com:7001");
//		env.put(Context.SECURITY_PRINCIPAL, "admin");
//		env.put(Context.SECURITY_CREDENTIALS, "capgemini1");

		
//		//DEV4
//		env.put(Context.PROVIDER_URL, "t3://osmdev4-z1.osn.wireless.rogers.com:7001,osmdev4-z2.osn.wireless.rogers.com:7001");
//		env.put(Context.SECURITY_PRINCIPAL, "si_testing");
//		env.put(Context.SECURITY_CREDENTIALS, "si_testing_123");
		
		
//		//DEV5
//		env.put(Context.PROVIDER_URL, "t3://osmdev5-z1.osn.wireless.rogers.com:7001,osmdev5-z2.osn.wireless.rogers.com:7001");
//		env.put(Context.SECURITY_PRINCIPAL, "it_osmops");
//		env.put(Context.SECURITY_CREDENTIALS, "it_osmops_123");		
		
		
//		//QA1
//		env.put(Context.PROVIDER_URL, "t3://osmqa1-1.osn.wireless.rogers.com:7001,osmqa1-2.osn.wireless.rogers.com:7001");
//		env.put(Context.SECURITY_PRINCIPAL, "si_testing");
//		env.put(Context.SECURITY_CREDENTIALS, "si_testing_123");		
		
		
//		//QA4
//		env.put(Context.PROVIDER_URL, "t3://osmqa4-1.osn.wireless.rogers.com:7001,osmqa4-2.osn.wireless.rogers.com:7001");
//		env.put(Context.SECURITY_PRINCIPAL, "si_testing");
//		env.put(Context.SECURITY_CREDENTIALS, "si_testing_123");
		
		//PET
		env.put(Context.PROVIDER_URL, "t3://osmpet1.osn.wireless.rogers.com:7001,osmpet2.osn.wireless.rogers.com:7001,osmpet3.osn.wireless.rogers.com:7001,osmpet4.osn.wireless.rogers.com:7001");
		env.put(Context.SECURITY_PRINCIPAL, "si_testing");
		env.put(Context.SECURITY_CREDENTIALS, "si_testing_123");		
		
		

		try {
			ctx = new InitialContext(env);
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return ctx;
	}
	
}