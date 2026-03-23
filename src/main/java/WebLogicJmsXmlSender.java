//package Success;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.NameNotFoundException;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.DeliveryMode;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class WebLogicJmsXmlSender {

    // ======REQUIRED PLACEHOLDERS (REPLACE THESE) ======
    // WebLogic JNDI connection settings
//    private static final String WL_PROVIDER_URL = "t3://vlrenbptosmwl01.rci.rogers.ca:7001"; // e.g., t3://wls-host.example.com:7001 (use t3s:// for SSL)
    private static final String WL_PROVIDER_URL = "t3://osmqa1-1.ngpp.mgmt.vf.rogers.com:7001,osmqa1-2.ngpp.mgmt.vf.rogers.com:7001"; // e.g., t3://wls-host.example.com:7001 (use t3s:// for SSL)
//    private static final String WL_PROVIDER_URL = "t3://osmdev1-z1.ngpp.mgmt.vf.rogers.com:7001,osmdev1-z2.ngpp.mgmt.vf.rogers.com:7001"; // e.g., t3://wls-host.example.com:7001 (use t3s:// for SSL)
    private static final String WL_INITIAL_CONTEXT_FACTORY = "weblogic.jndi.WLInitialContextFactory";
    private static final String WL_USERNAME = "admin";
    private static final String WL_PASSWORD = "welcome1";

   // JNDI names
    private static final String CF_JNDI_NAME = "mslv/oms/oms1/internal/jms/ConnectionFactory";     // e.g.,jms/MyCustomConnectionFactory
    private static final String QUEUE_JNDI_NAME = "jms/OMS.NGPP.REQ.Q";
//    private static final String QUEUE_JNDI_NAME = "jms/OSM.SOMTOCOM.ERR.Q"; // e.g.,jms/MyTargetQueue

    // JMS Connection credentials (if your CF requires them). If not set, code will tryWL_USERNAME/WL_PASSWORD.
    private static final String JMS_USERNAME = "admin";
    private static final String JMS_PASSWORD = "welcome1";

    private static final String XML_MESSAGE_FILE_PATH =""; // e.g., /tmp/message.xml

    // Optional: SSL truststore if using t3s(leave empty if not needed)
    private static final String SSL_TRUSTSTORE_PATH = "";     // e.g.,/path/to/truststore.jks
    private static final String SSL_TRUSTSTORE_PASSWORD = ""; // e.g., changeit

    // Optionalproducer settings
    private static final boolean USE_PERSISTENT_DELIVERY = true;
    private static final long TIME_TO_LIVE_MS = 0L; // 0 = never expire
    private static final int PRIORITY = javax.jms.Message.DEFAULT_PRIORITY; //4 default
    
    // Concurrency settings/tuning
    private static final int THREAD_COUNT = 5;          // concurrent JMS senders
    private static final int TOTAL_MESSAGES = 5;    // total messages to send

    // Batch generation
    private static final int BATCH_SIZE = 25;            // orders generated per batch

    // Rate limiting
    private static final int MAX_MSGS_PER_SECOND = 50;   // hard throttle
    
    private static final Semaphore RATE_LIMITER = new Semaphore(MAX_MSGS_PER_SECOND);

    public static void main(String[] args) {
        System.out.println("[INFO] Starting WebLogic JMS XMLsender...");

        // Basic placeholder sanity checks (fail fast if obvious placeholders not replaced)
        if(containsPlaceholder(WL_PROVIDER_URL) || containsPlaceholder(WL_USERNAME) ||
           containsPlaceholder(WL_PASSWORD) || containsPlaceholder(CF_JNDI_NAME) ||
           containsPlaceholder(QUEUE_JNDI_NAME)) {
            System.err.println("[ERROR] One or more required placeholders werenot replaced. Please set WL_PROVIDER_URL, WL_USERNAME, WL_PASSWORD, CF_JNDI_NAME, and QUEUE_JNDI_NAME.");
           System.exit(2);
        }

        // If using SSL (t3s), set truststore if provided
        if(WL_PROVIDER_URL.trim().toLowerCase().startsWith("t3s://")) {
            if (!isBlank(SSL_TRUSTSTORE_PATH)) {
               System.setProperty("javax.net.ssl.trustStore", SSL_TRUSTSTORE_PATH);
                if(!isBlank(SSL_TRUSTSTORE_PASSWORD)) {
                    System.setProperty("javax.net.ssl.trustStorePassword",SSL_TRUSTSTORE_PASSWORD);
                }
                System.out.println("[INFO] Using provided SSL truststore fort3s.");
            } else {
                System.out.println("[WARN] t3s detected but no truststore configured.Ensure the default JVM truststore trusts the server certificate.");
            }
        }
        
//        String orderXML = Generation.generateOrder();
//        System.out.println(generationObj.prettyFormat(orderXML, 2));
//        Generation.WriteStringToFile(orderXML);
        InitialContext initialContext = null;
        try {
            // Build JNDI environment
        	Hashtable<String, String> env =new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, WL_INITIAL_CONTEXT_FACTORY);
            env.put(Context.PROVIDER_URL, WL_PROVIDER_URL);
            env.put(Context.SECURITY_PRINCIPAL, WL_USERNAME);
            env.put(Context.SECURITY_CREDENTIALS, WL_PASSWORD);

            System.out.println("[INFO] Connecting to WebLogic JNDI at: " + WL_PROVIDER_URL);
            initialContext = new InitialContext(env);
            System.out.println("[INFO]JNDI InitialContext created successfully.");

            // Look up the ConnectionFactory
            System.out.println("[INFO] Looking up ConnectionFactory: " + CF_JNDI_NAME);
            Object cfObj = initialContext.lookup(CF_JNDI_NAME);
            
            if (!(cfObj instanceof ConnectionFactory)) {
            	System.err.println("[ERROR] JNDI object for " + CF_JNDI_NAME + " is not a javax.jms.ConnectionFactory. Actual type: "
            			+ cfObj.getClass().getName());
            	System.exit(3);
            }
            
            ConnectionFactory connectionFactory = (ConnectionFactory) cfObj;
            System.out.println("[INFO] ConnectionFactory lookup successful.");

            // Look up the Queue
            System.out.println("[INFO] Looking up Queue: " + QUEUE_JNDI_NAME);
            Object qObj = initialContext.lookup(QUEUE_JNDI_NAME);
            
            if (!(qObj instanceof Queue)) {
            	System.err.println("[ERROR] JNDI object for " + QUEUE_JNDI_NAME + " is not a javax.jms.Queue.Actual type: " 
            			+ qObj.getClass().getName());
            	System.exit(4);
            }
            
            Queue queue = (Queue) qObj;
            System.out.println("[INFO] Queue lookup successful.");

            // Resolve JMScredentials
            final String jmsUser = isPlaceholderOrBlank(JMS_USERNAME) ? WL_USERNAME : JMS_USERNAME;
            final String jmsPass = isPlaceholderOrBlank(JMS_PASSWORD) ? WL_PASSWORD : JMS_PASSWORD;
            
            ExecutorService senderPool = Executors.newFixedThreadPool(THREAD_COUNT);

            ScheduledExecutorService rateLimiterScheduler = startRateLimiterRefill();

            System.out.println("[INFO] Starting batch send:");
            System.out.println("       Total messages: " + TOTAL_MESSAGES);
            System.out.println("       Batch size:     " + BATCH_SIZE);
            System.out.println("       Threads:        " + THREAD_COUNT);
            System.out.println("       Max msg/sec:    " + MAX_MSGS_PER_SECOND);

            int remaining = TOTAL_MESSAGES;
            List<String> referenceNumbers = new ArrayList<String>();

            while (remaining > 0) {
                int currentBatchSize = Math.min(BATCH_SIZE, remaining);
                List<String> batch = null;

                // Batch generation
                List<List<String>> batchAndRefs = generateOrderBatch(currentBatchSize, referenceNumbers);
                if (!batchAndRefs.isEmpty()) {
                	batch = batchAndRefs.get(0);
                	referenceNumbers = batchAndRefs.get(1);
                }
                else {
                	System.out.println("ERROR: BATCH AND REFERENCE NUMBERS WERE EMPTY, THROW ERROR");
                }

                for (String xml : batch) {
                    senderPool.submit(() -> sendOneMessage(connectionFactory, queue, jmsUser, jmsPass, xml));
                }

                remaining -= currentBatchSize;
            }

            // Shutdown cleanly
            senderPool.shutdown();
            senderPool.awaitTermination(30, TimeUnit.MINUTES);

            rateLimiterScheduler.shutdown();

            System.out.println("[INFO] All messages sent");
            System.out.println("Reference Numbers = " + referenceNumbers);
            

//            // CreateJMSContext with AUTO_ACKNOWLEDGE (per request)
//            System.out.println("[INFO] Creating JMSContext with AUTO_ACKNOWLEDGE...");
//            try (JMSContext jmsContext = connectionFactory.createContext(jmsUser,jmsPass,JMSContext.CLIENT_ACKNOWLEDGE)) {
//            	
//            	// PrepareXML payload
//                String xmlPayload = orderXML;
//                if (isBlank(xmlPayload)) {
//                	System.err.println("[ERROR] No XML payload provided (both inline andfile path are empty).");
//                	System.exit(5);
//                }
//
//                // Create and sendmessage
//                TextMessage message = jmsContext.createTextMessage(xmlPayload);
//
//                // Configureproducer
//                javax.jms.JMSProducer producer = jmsContext.createProducer();
//                message.setJMSCorrelationID("1034260:905");
//                
//                producer.setDeliveryMode(USE_PERSISTENT_DELIVERY ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);
//
//               if (TIME_TO_LIVE_MS > 0) {
//                    producer.setTimeToLive(TIME_TO_LIVE_MS);
//                }
//               if (PRIORITY != Message.DEFAULT_PRIORITY) {
//                    producer.setPriority(PRIORITY);
//               }
//               
//               System.out.println("[INFO] Sending XML message to queue: " + QUEUE_JNDI_NAME);
//               producer.send(queue, message);
//               
//               // Print assigned message ID if available
//               
//               try {
//            	   String msgId = message.getJMSMessageID();
//            	   System.out.println("[SUCCESS] Message sent.JMSMessageID: " + msgId);
//        	   } catch (JMSException jmse) {
//        		   // Not critical; message wasalready sent
//        		   System.out.println("[SUCCESS] Message sent. (Could not read JMSMessageID: " +jmse.getMessage() + ")");
//                }
//            }
//
        } catch (AuthenticationException ae) {
           System.err.println("[AUTH ERROR] Authentication to WebLogic JNDI or JMS failed: " + ae.getMessage());
           ae.printStackTrace(System.err);
            System.exit(11);
        } catch (CommunicationException ce) {
           System.err.println("[NETWORK ERROR] Could not communicate with WebLogic at " + WL_PROVIDER_URL + ": " +ce.getMessage());
            ce.printStackTrace(System.err);
            System.exit(12);
        } catch(NameNotFoundException nnfe) {
            System.err.println("[JNDI ERROR] JNDI name not found: " +nnfe.getMessage());
            nnfe.printStackTrace(System.err);
            System.exit(13);
        } catch(JMSRuntimeException jre) {
            System.err.println("[JMS ERROR] Runtime error while interacting with JMS: " +jre.getMessage());
            jre.printStackTrace(System.err);
            System.exit(14);
        } catch(NamingException ne) {
            System.err.println("[JNDI ERROR] Naming exception: " + ne.getMessage());
           ne.printStackTrace(System.err);
            System.exit(15);
        } catch (Exception e) {
           System.err.println("[ERROR] Unexpected exception: " + e.getMessage());
            e.printStackTrace(System.err);
           System.exit(16);
        } finally {
            // Clean up InitialContext
            if (initialContext != null){
                try {
                    initialContext.close();
                } catch (NamingException ne) {
                   System.err.println("[WARN] Failed to close InitialContext: " + ne.getMessage());
                }
           }
        }

        System.out.println("[INFO] Program completed.");
    }
    
    private static void sendOneMessage(ConnectionFactory connectionFactory, Queue queue, String jmsUser, String jmsPass, String xmlPayload) {
        try {
            // Rate limiting (blocks if limit exceeded)
            RATE_LIMITER.acquire();

            try (JMSContext jmsContext = connectionFactory.createContext(
                                 jmsUser, jmsPass, JMSContext.CLIENT_ACKNOWLEDGE)) {

                TextMessage message = jmsContext.createTextMessage(xmlPayload);
                message.setJMSCorrelationID(UUID.randomUUID().toString());

                JMSProducer producer = jmsContext.createProducer() .setDeliveryMode(USE_PERSISTENT_DELIVERY
                                ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);

                if (TIME_TO_LIVE_MS > 0) {
                    producer.setTimeToLive(TIME_TO_LIVE_MS);
                }
                if (PRIORITY != Message.DEFAULT_PRIORITY) {
                    producer.setPriority(PRIORITY);
                }

                producer.send(queue, message);
                
                try {
               	   String msgId = message.getJMSMessageID();
               	   System.out.println("[SUCCESS] Sent | Thread = "+ Thread.currentThread().getName()
               			   + " | MsgID = " + msgId + "\n");
           	   } catch (JMSException jmse) {
           		   // Not critical; message was already sent
           		   System.out.println("[SUCCESS] Message sent. (Could not read JMSMessageID: " +jmse.getMessage() + ")");
           	   }
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static List<List<String>> generateOrderBatch(int batchSize, List<String> referenceNumbers) {
        List<String> batch = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
        	Map<String, String> order = Generation.generateOrder();
        	
        	if (!order.isEmpty()) {
        		Map.Entry<String, String> entry = order.entrySet().iterator().next();
        		String key = entry.getKey();
        		String value = entry.getValue();
        		
        		referenceNumbers.add(key);
        		batch.add(value);        		
        	}
        	else {
        		System.out.println("ORDER IS EMPTY, ADD SOME ERROR LOGGING / HANDLING HERE");
        	}
        }
        
        List<List<String>> ret = new ArrayList<>();
        ret.add(batch);
        ret.add(referenceNumbers);
        
        return ret;
    }
    
    private static ScheduledExecutorService startRateLimiterRefill() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            int missingPermits = MAX_MSGS_PER_SECOND - RATE_LIMITER.availablePermits();
            
            if (missingPermits > 0) {
                RATE_LIMITER.release(missingPermits);
            }
        }, 0, 1, TimeUnit.SECONDS); // Run refill every 1 second, missing permits will be restored.

        return scheduler;
    }

    private static String resolveXmlPayload(String inlineXml, String filePath) throws Exception {
        if (!isBlank(inlineXml) &&!inlineXml.startsWith("<REPLACE_ME")) {
            return inlineXml;
        }
        if (!isBlank(filePath) &&!filePath.startsWith("REPLACE_ME")) {
            System.out.println("[INFO] Reading XML payload from file: " +filePath);
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            return new String(bytes,StandardCharsets.UTF_8);
        }
        return null;
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
