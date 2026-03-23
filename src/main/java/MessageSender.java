//package com.test.jms;


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

public class MessageSender
{
	// Defines the JNDI context factory.
	public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";

	// Defines the JMS connection factory for the OSM queue.
	public final static String OSM_JMS_FACTORY = "oracle/communications/ordermanagement/osm/ExternalClientConnectionFactory";

	// Defines the OSM web service queue.
	public final static String OSM_QUEUE = "oracle/communications/ordermanagement/WebServiceQueue";
	public final static String EAI_REQ_QUEUE = "jms/EAI.OSM.REQ.Q";
//	public final static String EAI_REQ_QUEUE = "oracle/communications/ordermanagement/WebServiceQueue";
	public final static String ASMS_REQ_QUEUE = "jms/ASMS.OSM.REQ.Q";
	

	private QueueConnectionFactory qconFactory;
	private QueueConnection qcon;
	private QueueSession qsession;
	private QueueSender qsender;
	private Queue queue;
	private TextMessage msg;
	
	private String username;
	private String password;
	private String providerURL;
	
	
	public MessageSender(String username, String password, String providerURL) {
		this.username = username;
		this.password = password;
		this.providerURL = providerURL;
	}

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
	
	public void init(String queueName) {
		Context ctx = getInitialContext();
		try {
			init(ctx, queueName);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void init() {
		init(OSM_QUEUE);
	}

	/**
	 * Sends a message to a JMS queue.
	 *
	 * @param message  message to be sent
	 * @exception JMSException if JMS fails to send message due to internal error
	 */
	public void send(String message, int priority) throws JMSException {
		msg.setText(message);
		msg.setStringProperty("_wls_mimehdrContent_Type", "text/xml; charset=UTF-8");
		msg.setStringProperty("URI", "/OrderManagement/wsapi");
		
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

	private InitialContext getInitialContext() {
		InitialContext ctx = null;
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
		env.put(Context.PROVIDER_URL, providerURL);
		env.put(Context.SECURITY_PRINCIPAL, username);
		env.put(Context.SECURITY_CREDENTIALS, password);			
		
		try {
			ctx = new InitialContext(env);
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return ctx;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
}
