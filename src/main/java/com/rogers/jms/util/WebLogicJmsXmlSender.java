//package Success;
package com.rogers.jms.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rogers.jms.loadtest.LoadTest;
import com.rogers.jms.loadtest.LoadTestDTV;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

public class WebLogicJmsXmlSender {

    // ======REQUIRED PLACEHOLDERS (REPLACE THESE) ======
	static Properties props = ConfigLoader.load();	
	
	// WebLogic JNDI connection settings
//  private static final String WL_PROVIDER_URL = "t3://osmdev1-z1.ngpp.mgmt.vf.rogers.com:7001,osmdev1-z2.ngpp.mgmt.vf.rogers.com:7001"; // e.g., t3://wls-host.example.com:7001 (use t3s:// for SSL)
//	private static final String WL_PROVIDER_URL = "t3://osmqa1-1.ngpp.mgmt.vf.rogers.com:7001,osmqa1-2.ngpp.mgmt.vf.rogers.com:7001"; // e.g., t3://wls-host.example.com:7001 (use t3s:// for SSL)
//	private static final String WL_PROVIDER_URL = "t3://vlrenbptosmwl01.rci.rogers.ca:7001,vlrenbptosmwl01.rci.rogers.ca:7003,vlrenbptosmwl02.rci.rogers.ca:7001,vlrenbptosmwl02.rci.rogers.ca:7003,vlrenbptosmwl03.rci.rogers.ca:7001,vlrenbptosmwl03.rci.rogers.ca:7003,vlrenbptosmwl04.rci.rogers.ca:7001,vlrenbptosmwl04.rci.rogers.ca:7003,vlrenbptosmwl05.rci.rogers.ca:7001,vlrenbptosmwl05.rci.rogers.ca:7003"; 
	private static final String WL_PROVIDER_URL = props.getProperty("providerURL");
    
    private static final String WL_INITIAL_CONTEXT_FACTORY = "weblogic.jndi.WLInitialContextFactory";
    private static final String WL_USERNAME = props.getProperty("username");
    private static final String WL_PASSWORD = props.getProperty("password");

   // JNDI names
    private static final String CF_JNDI_NAME = "mslv/oms/oms1/internal/jms/ConnectionFactory";     // e.g.,jms/MyCustomConnectionFactory
    public static final String QUEUE_JNDI_NAME = "jms/OMS.NGPP.REQ.Q";
    private static final String XML_MESSAGE_FILE_PATH = "/xml/TT_order.xml";

    // Optional: SSL Trust Store if using t3s (leave empty if not needed)
    private static final String SSL_TRUSTSTORE_PATH = "";     // e.g.,/path/to/truststore.jks
    private static final String SSL_TRUSTSTORE_PASSWORD = ""; // e.g., changeit
    
    // Concurrency settings/tuning
    private static final int THREAD_COUNT = 5;          // concurrent JMS senders
    private static final int TOTAL_MESSAGES = 1;    // total messages to send
    private static final int BATCH_SIZE = 1;            // orders generated per batch
    private static final int MAX_MSGS_PER_SECOND = 2;   // hard throttle

    private static final Logger logger = LoggerFactory.getLogger(LoadTest.class);
    
    public static void main(String[] args) {
    	
    	JmsResources jmsResources = initJmsResources("jms/OMS.NGPP.REQ.Q");
    	if (jmsResources == null) {
    		logger.error("jmsResources returned as null, check log output");
    		System.exit(3);
    	}
    	SendSOMRMessages(jmsResources, TOTAL_MESSAGES, BATCH_SIZE, THREAD_COUNT, MAX_MSGS_PER_SECOND);
        logger.info("Program completed.");
        
		final LoadTestDTV lt = new LoadTestDTV(props);
		
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
    
    private static Hashtable<String, String> buildJNDIEnv() {
        Hashtable<String, String> env = new Hashtable<>();
        
      // Basic placeholder sanity checks (fail fast if obvious placeholders not replaced)
      if(isPlaceholderOrBlank(WL_PROVIDER_URL) || isPlaceholderOrBlank(WL_USERNAME) || isPlaceholderOrBlank(WL_PASSWORD)) {
    	  System.err.println("[ERROR] One or more required placeholders were not replaced. Please set WL_PROVIDER_URL, WL_USERNAME, WL_PASSWORD.");
    	  System.exit(2);
      }
        env.put(Context.INITIAL_CONTEXT_FACTORY, WL_INITIAL_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, WL_PROVIDER_URL);
        env.put(Context.SECURITY_PRINCIPAL, WL_USERNAME);
        env.put(Context.SECURITY_CREDENTIALS, WL_PASSWORD);

        // Optional: support t3s (SSL)
        if (WL_PROVIDER_URL != null && WL_PROVIDER_URL.toLowerCase().startsWith("t3s://")) {

            if (SSL_TRUSTSTORE_PATH != null && !SSL_TRUSTSTORE_PATH.isBlank()) {
                System.setProperty("javax.net.ssl.trustStore", SSL_TRUSTSTORE_PATH);

                if (SSL_TRUSTSTORE_PASSWORD != null && !SSL_TRUSTSTORE_PASSWORD.isBlank()) {
                    System.setProperty("javax.net.ssl.trustStorePassword", SSL_TRUSTSTORE_PASSWORD);
                }
            }
        }

        return env;
    }
  
    public static JmsResources initJmsResources(String queueName) {

    	try {
    		logger.info("Connecting to {}", WL_PROVIDER_URL);
	        InitialContext ctx = new InitialContext(buildJNDIEnv());
	
	        logger.info("Looking up ConnectionFactory: " + CF_JNDI_NAME);
	        ConnectionFactory cf = (ConnectionFactory) ctx.lookup(CF_JNDI_NAME);
	        logger.info("ConnectionFactory lookup successful.");
	
	        logger.info("Looking up Queue: " + queueName);
	        Queue queue = (Queue) ctx.lookup(queueName);
	        logger.info("Queue lookup successful.\n");

	        return new JmsResources(cf, queue, WL_USERNAME, WL_PASSWORD);
	        
    	} catch (NamingException e) {
    		logger.error("[JNDI ERROR] JNDI name not found: {}", e.getMessage());
    		e.printStackTrace(System.err);
    		System.exit(1);
    	} catch (JMSException e) {
        	logger.error("[JMS ERROR] Runtime error while interacting with JMS: {}", e.getMessage());
        	e.printStackTrace(System.err);
        	System.exit(2);
    	}
		return null;
    }
    
    public static void SendSOMRMessages(JmsResources jmsResources, int total_messages, int batch_size, int thread_count, int max_msgs_per_second) {
    	final Semaphore rate_limiter = new Semaphore(max_msgs_per_second);
    	try {
            
            ExecutorService senderPool = Executors.newFixedThreadPool(THREAD_COUNT);

            ScheduledExecutorService rateLimiterScheduler = startRateLimiterRefill(rate_limiter);

            logger.info("Starting SOMR Message Send:");
            logger.info("       Total messages: {}", total_messages);
            logger.info("       Batch size:     {}", batch_size);
            logger.info("       Threads:        {}", thread_count);
            logger.info("       Max msg/sec:    {}\n", max_msgs_per_second);

            int remaining = total_messages;
            List<String> referenceNumbers = new ArrayList<String>();

            while (remaining > 0) {
                int currentBatchSize = Math.min(batch_size, remaining);
                List<String> batch = null;

                // Batch generation
                List<List<String>> batchAndRefs = generateOrderBatch(currentBatchSize);
                if (!batchAndRefs.isEmpty()) {
                	batch = batchAndRefs.get(0);
                	referenceNumbers.addAll(batchAndRefs.get(1));
                }
                else {
                	logger.error("BATCH AND REFERENCE NUMBERS WERE EMPTY, THROW ERROR");
                }

                for (String xml : batch) {
                    senderPool.submit(() -> sendOneMessage(jmsResources, xml, 5, null));
                }
                remaining -= currentBatchSize;
                if (remaining == 0) {
                	logger.debug("\n{} Orders left to generate!\n", remaining);                	
                }
                else {                	
                	logger.debug("\nGenerating a new batch of orders,  {} orders left\n", remaining);
                }
            }

            // Shutdown cleanly
            senderPool.shutdown();
            senderPool.awaitTermination(30, TimeUnit.MINUTES);

            rateLimiterScheduler.shutdown();

            logger.info("Reference Numbers = {}", referenceNumbers);
            logger.info("All messages sent" +  System.lineSeparator());

        }  catch (IOException io) {
        	logger.error("[IO ERROR] FilePath error, given filepath -> {}, error: {}", XML_MESSAGE_FILE_PATH, io.getMessage());
        	io.printStackTrace(System.err);
        	System.exit(15);
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
        	logger.error("[ERROR] Unexpected exception: {}", e.getMessage());
            e.printStackTrace(System.err);
            System.exit(16);
        }
    }
    
    public static void sendOneMessage(JmsResources jmsResources, String xmlPayload, int priority, String corrId) {

        try {
            TextMessage msg = jmsResources.createTextMessage(xmlPayload);

            // Preserve legacy correlation behavior, corrId will be null for somr orders
            if (corrId != null) {
            	msg.setJMSCorrelationID(corrId);            	
            	msg.setStringProperty("_wls_mimehdrContent_Type", "text/xml; charset=UTF-8");
            	msg.setStringProperty("URI", "/OrderManagement/wsapi");
            }

            jmsResources.send(msg, priority);

            try {
                logger.info("[SUCCESS] Sent | Thread={} | MsgID={}", Thread.currentThread().getName(), msg.getJMSMessageID());
            } catch (JMSException ignore) {
                logger.info("[SUCCESS] Message sent (JMSMessageID unavailable)");
            }

        } catch (JMSException e) {
            logger.error("[ERROR] Failed to send message: {}", e.getMessage(), e);
        }
	}
    
    private static List<List<String>> generateOrderBatch(int batchSize) throws IOException {
        List<String> batch = new ArrayList<>(batchSize);
        List<String> referenceNumbers = new ArrayList<String>();
        for (int i = 0; i < batchSize; i++) {
        	Map<String, String> order = Generation.generateOrder(XML_MESSAGE_FILE_PATH);
        	
        	if (!order.isEmpty()) {
        		Map.Entry<String, String> entry = order.entrySet().iterator().next();
        		String key = entry.getKey();
        		String value = entry.getValue();
        		
        		referenceNumbers.add(key);
        		batch.add(value);        		
        	}
        	else {
        		logger.error("ORDER IS EMPTY, ADD SOME ERROR LOGGING / HANDLING HERE");
        	}
        }
        
        List<List<String>> ret = new ArrayList<>();
        ret.add(batch);
        ret.add(referenceNumbers);
        
        return ret;
    }
    
    private static ScheduledExecutorService startRateLimiterRefill(Semaphore rate_limiter) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            int missingPermits = MAX_MSGS_PER_SECOND - rate_limiter.availablePermits();
            
            if (missingPermits > 0) {
            	rate_limiter.release(missingPermits);
            }
        }, 0, 1, TimeUnit.SECONDS); // Run refill every 1 second, missing permits will be restored.

        return scheduler;
    }
    

    public static String loadXml(String path) {
        try (InputStream is = openStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException(
                    "XML not found for path: " + path
                );
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load XML from " + path, e);
        }
    }

    private static InputStream openStream(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            String resource = path.substring("classpath:".length());
            if (!resource.startsWith("/")) {
                resource = "/" + resource;
            }
            return WebLogicJmsXmlSender.class.getResourceAsStream(resource);
        } else {
            return Files.newInputStream(Paths.get(path));
        }
    }


    private static boolean containsPlaceholder(String val) {
        return val != null && val.contains("REPLACE_ME");
    }

    private static boolean isPlaceholderOrBlank(String val) {
        return isBlank(val) || containsPlaceholder(val);
    }

    private static boolean isBlank(String val) {
        return val == null || val.trim().isEmpty();
    }
}
