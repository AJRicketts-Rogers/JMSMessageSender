package com.rogers.jms.loadtest;

import java.util.Date;
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
		int BatchSize1p = Integer.parseInt(props.getProperty("somr.1p.batch_size"));
		int BatchWaitTimeMs1p = Integer.parseInt(props.getProperty("somr.1p.batch_wait_time_ms"));
		int ThreadCount1p = Integer.parseInt(props.getProperty("somr.1p.thread_count"));
		int MaxMsgsPerSecond1p = Integer.parseInt(props.getProperty("somr.1p.max_msgs_per_second"));

		// SOMR 2P ORDERS
		int TotalMessages2p = Integer.parseInt(props.getProperty("somr.2p.total_messages"));
		int BatchSize2p = Integer.parseInt(props.getProperty("somr.2p.batch_size"));
		int BatchWaitTimeMs2p = Integer.parseInt(props.getProperty("somr.2p.batch_wait_time_ms"));
		int ThreadCount2p = Integer.parseInt(props.getProperty("somr.2p.thread_count"));
		int MaxMsgsPerSecond2p = Integer.parseInt(props.getProperty("somr.2p.max_msgs_per_second"));

		// SOMR 3P ORDERS

		int TotalMessages3p = Integer.parseInt(props.getProperty("somr.3p.total_messages"));
		int BatchSize3p = Integer.parseInt(props.getProperty("somr.3p.batch_size"));
		int BatchWaitTimeMs3p = Integer.parseInt(props.getProperty("somr.3p.batch_wait_time_ms"));
		int ThreadCount3p = Integer.parseInt(props.getProperty("somr.3p.thread_count"));
		int MaxMsgsPerSecond3p = Integer.parseInt(props.getProperty("somr.3p.max_msgs_per_second"));
		
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
		    
            logger.debug("Starting 1P SOMR Message Send:");
            logger.debug("       Total messages: {}", TotalMessages1p);
            logger.debug("       Batch size:     {}", BatchSize1p);
            logger.debug("       Batch wait(ms): {}", BatchWaitTimeMs1p);
            logger.debug("       Threads:        {}", ThreadCount1p);
            logger.debug("       Max msg/sec:    {}\n", MaxMsgsPerSecond1p);

		    t5 = new Thread(new Runnable() {
		        @Override
		        public void run() {		        	
		            WebLogicJmsXmlSender.SendSOMRMessages(newSender.getJmsResources(), TotalMessages1p, BatchSize1p, ThreadCount1p, 
		            		MaxMsgsPerSecond1p, BatchWaitTimeMs1p, "1p");
		        }
		    });
		}	
		
		// -------------------- 2P ORDERS --------------------
		Thread t6 = null;
		
		if (TotalMessages2p > 0) {
			final MessageSender newSender = new MessageSender(username, password);
			newSender.init(MessageSender.OMS_REQ_QUEUE);

            logger.debug("Starting 2P SOMR Message Send:");
            logger.debug("       Total messages: {}", TotalMessages2p);
            logger.debug("       Batch size:     {}", BatchSize2p);
            logger.debug("       Batch wait(ms): {}", BatchWaitTimeMs2p);
            logger.debug("       Threads:        {}", ThreadCount2p);
            logger.debug("       Max msg/sec:    {}\n", MaxMsgsPerSecond2p);
			
			t6 = new Thread(new Runnable() {
				@Override
				public void run() {
					WebLogicJmsXmlSender.SendSOMRMessages(newSender.getJmsResources(), TotalMessages2p, BatchSize2p, ThreadCount2p, 
							MaxMsgsPerSecond2p, BatchWaitTimeMs2p, "2p");
				}
			});
		}	
		
		// -------------------- 3P ORDERS --------------------
		Thread t7 = null;
		
		if (TotalMessages3p > 0) {
			final MessageSender newSender = new MessageSender(username, password);
			newSender.init(MessageSender.OMS_REQ_QUEUE);
			
            logger.info("Starting 3P SOMR Message Send:");
            logger.debug("       Total messages: {}", TotalMessages3p);
            logger.debug("       Batch size:     {}", BatchSize3p);
            logger.debug("       Batch wait(ms): {}", BatchWaitTimeMs3p);
            logger.debug("       Threads:        {}", ThreadCount3p);
            logger.debug("       Max msg/sec:    {}\n", MaxMsgsPerSecond3p);
			
			t7 = new Thread(new Runnable() {
				@Override
				public void run() {
					WebLogicJmsXmlSender.SendSOMRMessages(newSender.getJmsResources(), TotalMessages3p, BatchSize3p, ThreadCount3p, 
							MaxMsgsPerSecond3p, BatchWaitTimeMs3p, "3p");
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

}
