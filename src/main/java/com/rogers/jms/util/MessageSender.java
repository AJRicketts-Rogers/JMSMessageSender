package com.rogers.jms.util;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageSender
{
	// Defines the JNDI context factory.
	public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";

	// Defines the JMS connection factory for the OSM queue.
	public final static String OSM_JMS_FACTORY = "oracle/communications/ordermanagement/osm/ExternalClientConnectionFactory";

	// Defines the OSM web service queue.
	public final static String OSM_QUEUE = "oracle/communications/ordermanagement/WebServiceQueue";
	public final static String EAI_REQ_QUEUE = "jms/EAI.OSM.REQ.Q";
	public final static String OMS_REQ_QUEUE = "jms/OMS.NGPP.REQ.Q";
	public final static String ASMS_REQ_QUEUE = "jms/ASMS.OSM.REQ.Q";
	
    private static final Logger logger = LoggerFactory.getLogger(Generation.class);

	private QueueConnectionFactory qconFactory;
	private QueueConnection qcon;
	private QueueSession qsession;
	private QueueSender qsender;
	private Queue queue;
	private TextMessage msg;
	
	private String username;
	private String password;
	private JmsResources jmsResources;
	
	
	public MessageSender(String username, String password) {
		this.username = username;
		this.password = password;
		this.jmsResources = null;
	}
	
	public JmsResources getJmsResources() {
		return this.jmsResources;
	}

	public void init(String queueName) {
		this.jmsResources = WebLogicJmsXmlSender.initJmsResources(queueName);
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
	
	public void init(Context ctx, String queueName) throws NamingException, JMSException {
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

	/**
	 * Sends a message to a JMS queue.
	 *
	 * @param message  message to be sent
	 * @exception JMSException if JMS fails to send message due to internal error
	 */
	public void send(String message, int priority, String corrId) throws JMSException {
	   	if (this.jmsResources == null) {
    		logger.error("jmsResources returned as null, check log output");
    		System.exit(1);
    	}
		
    	WebLogicJmsXmlSender.sendOneMessage(jmsResources, message, priority, corrId);
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
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
}
