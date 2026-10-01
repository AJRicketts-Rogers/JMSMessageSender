package com.rogers.jms.loadtest;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rogers.jms.util.ConfigLoader;
import com.rogers.jms.util.MessageSender;
import com.rogers.jms.util.WebLogicJmsXmlSender;

public class LoadTest {
	
	public static void main(String[] args) {
		
		Properties props = ConfigLoader.load();
	    final Logger logger = LoggerFactory.getLogger(LoadTest.class);
		
		//Legacy DTV
		int totalMessagesDTV = Integer.parseInt(props.getProperty("dtv.totalMessages"));
		
		//Legacy RHPC
		int totalMessagesRHPC = Integer.parseInt(props.getProperty("rhpc.totalMessages"));

		//Legacy HSI
		int totalMessagesSSHSI = Integer.parseInt(props.getProperty("sshsi.totalMessages"));
		
		//Legacy DOCSIS
		int totalMessagesDOCSIS = Integer.parseInt(props.getProperty("docsis.totalMessages"));	

		// SOMR 1P ORDERS
		int TotalMessages1p = Integer.parseInt(props.getProperty("somr.1p.total_messages"));
		int ThreadCount1p = Integer.parseInt(props.getProperty("somr.1p.thread_count"));
		int sleepIntervalMillis1p = Integer.parseInt(props.getProperty("somr.1p.sleep_interval_millis"));

		// SOMR 2P ORDERS
		int TotalMessages2p = Integer.parseInt(props.getProperty("somr.2p.total_messages"));
		int ThreadCount2p = Integer.parseInt(props.getProperty("somr.2p.thread_count"));
		int sleepIntervalMillis2p = Integer.parseInt(props.getProperty("somr.2p.sleep_interval_millis"));

		// SOMR 3P ORDERS
		int TotalMessages3p = Integer.parseInt(props.getProperty("somr.3p.total_messages"));
		int ThreadCount3p = Integer.parseInt(props.getProperty("somr.3p.thread_count"));
		int sleepIntervalMillis3p = Integer.parseInt(props.getProperty("somr.3p.sleep_interval_millis"));

		
        String username = props.getProperty("username");
        String password = props.getProperty("password");
        
		Date startDate = new Date();
		long startTime = System.nanoTime();	
        
		// -------------------- DTV --------------------
		Thread t1 = null;

		if (totalMessagesDTV > 0) {
		    final LoadTestDTV loadTestDTV = new LoadTestDTV(props);

		    final MessageSender senderDTV = new MessageSender(username, password);
		    senderDTV.init(MessageSender.EAI_REQ_QUEUE);

		    t1 = new Thread(new Runnable() {
		        @Override
		        public void run() {
		        	loadTestDTV.sendMessages(senderDTV);
		        }
		    });
		}

        // -------------------- RHPC --------------------
		Thread t2 = null;

		if (totalMessagesRHPC > 0) {
		    final LoadTestRHPC loadTestRHPC = new LoadTestRHPC(props);

		    final MessageSender senderRHPC = new MessageSender(username, password);
		    senderRHPC.init(MessageSender.EAI_REQ_QUEUE);

		    t2 = new Thread(new Runnable() {
		        @Override
		        public void run() {
		        	loadTestRHPC.sendMessages(senderRHPC);
		        }
		    });
		}

        // -------------------- SSHSI --------------------
		Thread t3 = null;

		if (totalMessagesSSHSI > 0) {
		    final LoadTestSSHSI loadTestSSHSI = new LoadTestSSHSI(props);

		    final MessageSender senderSSHSI = new MessageSender(username, password);
		    senderSSHSI.init(MessageSender.EAI_REQ_QUEUE);

		    t3 = new Thread(new Runnable() {
		        @Override
		        public void run() {
		        	loadTestSSHSI.sendMessages(senderSSHSI);
		        }
		    });
		}

        // -------------------- DOCSIS --------------------
		Thread t4 = null;

		if (totalMessagesDOCSIS > 0) {
		    final LoadTestDOCSIS loadTestDOCSIS = new LoadTestDOCSIS(props);

		    final MessageSender senderDOCSIS = new MessageSender(username, password);
		    senderDOCSIS.init(MessageSender.EAI_REQ_QUEUE);

		    t4 = new Thread(new Runnable() {
		        @Override
		        public void run() {
		        	loadTestDOCSIS.sendMessages(senderDOCSIS);
		        }
		    });
		}
		
		// -------------------- 1P ORDERS --------------------
		Thread t5 = null;

		if (TotalMessages1p > 0) {
			final MessageSender newSender = new MessageSender(username, password);
		    newSender.init(MessageSender.OMS_REQ_QUEUE);
		    
			List<Integer> batchProps = getBatchProperties(props, "1p");
			int BatchSize1p = batchProps.getFirst();
			int BatchWaitTimeMs1p = batchProps.getLast();
		    
            logger.debug("Starting 1P SOMR Message Send:");
            logger.debug("       Total messages: {}", TotalMessages1p);
            logger.debug("       Batch size:     {}", BatchSize1p);
            logger.debug("       Batch wait(ms): {}", BatchWaitTimeMs1p);
            logger.debug("       Threads:        {}", ThreadCount1p);
            logger.debug("       Sleep Interval: {} Milliseconds\n", sleepIntervalMillis1p);

		    t5 = new Thread(new Runnable() {
		        @Override
		        public void run() {		        	
		            WebLogicJmsXmlSender.SendSOMRMessages(newSender.getJmsResources(), TotalMessages1p, BatchSize1p, ThreadCount1p, 
		            		sleepIntervalMillis1p, BatchWaitTimeMs1p, "1p");
		        }
		    });
		}	
		
		// -------------------- 2P ORDERS --------------------
		Thread t6 = null;
		
		if (TotalMessages2p > 0) {
			final MessageSender newSender = new MessageSender(username, password);
			newSender.init(MessageSender.OMS_REQ_QUEUE);
			
			List<Integer> batchProps = getBatchProperties(props, "2p");
			int BatchSize2p = batchProps.getFirst();
			int BatchWaitTimeMs2p = batchProps.getLast();

            logger.debug("Starting 2P SOMR Message Send:");
            logger.debug("       Total messages: {}", TotalMessages2p);
            logger.debug("       Batch size:     {}", BatchSize2p);
            logger.debug("       Batch wait(ms): {}", BatchWaitTimeMs2p);
            logger.debug("       Threads:        {}", ThreadCount2p);
            logger.debug("       Sleep Interval: {} Milliseconds\n", sleepIntervalMillis2p);
			
			t6 = new Thread(new Runnable() {
				@Override
				public void run() {
					WebLogicJmsXmlSender.SendSOMRMessages(newSender.getJmsResources(), TotalMessages2p, BatchSize2p, ThreadCount2p, 
							sleepIntervalMillis2p, BatchWaitTimeMs2p, "2p");
				}
			});
		}	
		
		// -------------------- 3P ORDERS --------------------
		Thread t7 = null;
		
		if (TotalMessages3p > 0) {
			final MessageSender newSender = new MessageSender(username, password);
			newSender.init(MessageSender.OMS_REQ_QUEUE);
			
			List<Integer> batchProps = getBatchProperties(props, "3p");
			int BatchSize3p = batchProps.getFirst();
			int BatchWaitTimeMs3p = batchProps.getLast();
			
            logger.debug("Starting 3P SOMR Message Send:");
            logger.debug("       Total messages: {}", TotalMessages3p);
            logger.debug("       Batch size:     {}", BatchSize3p);
            logger.debug("       Batch wait(ms): {}", BatchWaitTimeMs3p);
            logger.debug("       Threads:        {}", ThreadCount3p);
            logger.debug("       Sleep Interval: {} Milliseconds\n", sleepIntervalMillis3p);
			
			t7 = new Thread(new Runnable() {
				@Override
				public void run() {
					WebLogicJmsXmlSender.SendSOMRMessages(newSender.getJmsResources(), TotalMessages3p, BatchSize3p, ThreadCount3p, 
							sleepIntervalMillis3p, BatchWaitTimeMs3p, "3p");
				}
			});
		}	
		
		if (t1 != null) t1.start();
		if (t2 != null) t2.start();
		if (t3 != null) t3.start();
		if (t4 != null) t4.start();
		if (t5 != null) t5.start();
		if (t6 != null) t6.start();
		if (t7 != null) t7.start();
		
		try {
		    if (t1 != null) t1.join();
		    if (t2 != null) t2.join();
		    if (t3 != null) t3.join();
		    if (t4 != null) t4.join();
		    if (t5 != null) t5.join();
		    if (t6 != null) t6.join();
		    if (t7 != null) t7.join();
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
                
		long endTime = System.nanoTime();
		long elapsedTime = endTime - startTime;
		double seconds = (double)elapsedTime / 1000000000.0;

		logger.info("Start time : " + startDate);
		logger.info("End time : " + new Date());		
		logger.info("All messages sent in : " + seconds + " seconds" + System.lineSeparator() + System.lineSeparator());		
	}
	
	public static List<Integer> getBatchProperties(Properties props, String orderType) {
		String batch_size = props.getProperty("somr." + orderType + ".batch_size");
		String batch_wait_time_ms = props.getProperty("somr." + orderType + ".batch_wait_time_ms");
		
		if (batch_size == null || batch_wait_time_ms == null) {
			return List.of(1, 0);
		}
		else {
			return List.of(Integer.parseInt(batch_size), Integer.parseInt(batch_wait_time_ms));
		}
	}

}
