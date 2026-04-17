package com.rogers.jms.util;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.DeliveryMode;

public final class JmsResources implements AutoCloseable {

    private final Connection connection;
    private final Session session;
    private final MessageProducer producer;
    private final Queue queue;

    public JmsResources(ConnectionFactory connectionFactory, Queue queue, String username, String password) throws JMSException {
        // Create an authenticated JMS connection
        this.connection = (username != null && password != null) ? connectionFactory.createConnection(username, password) 
        		: connectionFactory.createConnection();

        this.queue = queue;
        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        this.producer = session.createProducer(queue);
        this.producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        this.connection.start();
    }

    public TextMessage createTextMessage(String payload) throws JMSException {
        return session.createTextMessage(payload);
    }

    public void send(Message message, int priority) throws JMSException {
        producer.send(message, DeliveryMode.PERSISTENT, priority, Message.DEFAULT_TIME_TO_LIVE);
    }
    
    public Queue getQueue() {
        return queue;
    }

    @Override
    public void close() {
        closeQuietly(producer);
        closeQuietly(session);
        closeQuietly(connection);
    }

    private void closeQuietly(AutoCloseable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception ignored) {
                // intentionally ignored
            }
        }
    }
}