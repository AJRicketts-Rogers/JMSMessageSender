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
		
		//Legacy VOD
		int totalMessagesVOD = Integer.parseInt(props.getProperty("vod.totalMessages"));
		
		//Legacy RHPC
		int totalMessagesRHPC = Integer.parseInt(props.getProperty("rhpc.totalMessages"));

		//Legacy HSI
		int totalMessagesSSHSI = Integer.parseInt(props.getProperty("sshsi.totalMessages"));
		
		//Legacy DOCSIS
		int totalMessagesDOCSIS = Integer.parseInt(props.getProperty("docsis.totalMessages"));
		
		//Maestro Email
		int totalMessagesEmail = Integer.parseInt(props.getProperty("email.totalMessages"));

		//Maestro GW (Equipment)
		int totalMessagesGW = Integer.parseInt(props.getProperty("gw.totalMessages"));
		
		//Maestro HSI
		int totalMessagesHSI = Integer.parseInt(props.getProperty("hsi.totalMessages"));
		
		//Maestro IPTV
		int totalMessagesIPTV = Integer.parseInt(props.getProperty("iptv.totalMessages"));		

		//SOMR ORDERS
		int total_messages = Integer.parseInt(props.getProperty("somr.total_messages"));
		int batch_size = Integer.parseInt(props.getProperty("somr.batch_size"));
		int thread_count = Integer.parseInt(props.getProperty("somr.thread_count"));
		int max_msgs_per_second = Integer.parseInt(props.getProperty("somr.max_msgs_per_second"));
	
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

        // -------------------- VOD --------------------
		Thread t2 = null;

		if (totalMessagesVOD > 0) {
		    final LoadTestVOD loadTestVOD = new LoadTestVOD(props);

		    final MessageSender senderVOD = new MessageSender(username, password);
		    senderVOD.init(MessageSender.EAI_REQ_QUEUE);

		    t2 = new Thread(new Runnable() {
		        @Override
		        public void run() {
		        	loadTestVOD.sendMessages(senderVOD);
		        }
		    });
		}

        // -------------------- RHPC --------------------
		Thread t3 = null;

		if (totalMessagesRHPC > 0) {
		    final LoadTestRHPC loadTestRHPC = new LoadTestRHPC(props);

		    final MessageSender senderRHPC = new MessageSender(username, password);
		    senderRHPC.init(MessageSender.EAI_REQ_QUEUE);

		    t3 = new Thread(new Runnable() {
		        @Override
		        public void run() {
		        	loadTestRHPC.sendMessages(senderRHPC);
		        }
		    });
		}

        // -------------------- SSHSI --------------------
		Thread t4 = null;

		if (totalMessagesSSHSI > 0) {
		    final LoadTestSSHSI loadTestSSHSI = new LoadTestSSHSI(props);

		    final MessageSender senderSSHSI = new MessageSender(username, password);
		    senderSSHSI.init(MessageSender.EAI_REQ_QUEUE);

		    t4 = new Thread(new Runnable() {
		        @Override
		        public void run() {
		        	loadTestSSHSI.sendMessages(senderSSHSI);
		        }
		    });
		}

        // -------------------- DOCSIS --------------------
		Thread t5 = null;

		if (totalMessagesDOCSIS > 0) {
		    final LoadTestDOCSIS loadTestDOCSIS = new LoadTestDOCSIS(props);

		    final MessageSender senderDOCSIS = new MessageSender(username, password);
		    senderDOCSIS.init(MessageSender.EAI_REQ_QUEUE);

		    t5 = new Thread(new Runnable() {
		        @Override
		        public void run() {
		        	loadTestDOCSIS.sendMessages(senderDOCSIS);
		        }
		    });
		}

        // -------------------- EMAIL --------------------
		Thread t6 = null;

		if (totalMessagesEmail > 0) {
			final LoadTestEmail loadTestEmail = new LoadTestEmail(props);
		    
			final MessageSender senderEmail = new MessageSender(username, password);
		    senderEmail.init(MessageSender.ASMS_REQ_QUEUE);


		    t6 = new Thread(new Runnable() {
		        @Override
		        public void run() {
		        	loadTestEmail.sendMessages(senderEmail);
		        }
		    });
		}
                
		// -------------------- GW --------------------
		Thread t7 = null;

		if (totalMessagesGW > 0 && totalMessagesHSI == 0) {
			final LoadTestGW loadTestGW = new LoadTestGW(props);
			
			final MessageSender senderGW = new MessageSender(username, password);
			senderGW.init(MessageSender.ASMS_REQ_QUEUE);

		    t7 = new Thread(new Runnable() {
		        @Override
		        public void run() {
		        	loadTestGW.sendMessages(senderGW);
		        }
		    });
		}
		
		// -------------------- HSI --------------------
		Thread t8 = null;

		if (totalMessagesHSI > 0 && totalMessagesGW == 0) {
			final LoadTestHSI HSIloadTestHSI = new LoadTestHSI(props);
			
			final MessageSender senderHSI = new MessageSender(username, password);
			senderHSI.init(MessageSender.ASMS_REQ_QUEUE);

		    t8 = new Thread(new Runnable() {
		        @Override
		        public void run() {
		        	HSIloadTestHSI.sendMessages(senderHSI);
		        }
		    });
		}
		
		// -------------------- IPTV --------------------
//		LoadTestIPTV loadTestIPTV = null;
		Thread t9 = null;

		if (totalMessagesIPTV > 0) {
		    final LoadTestIPTV loadTestIPTV = new LoadTestIPTV(props);
		    
		    final MessageSender senderIPTV = new MessageSender(username, password);
		    senderIPTV.init(MessageSender.ASMS_REQ_QUEUE);

		    t9 = new Thread(new Runnable() {
		        @Override
		        public void run() {
		        	loadTestIPTV.sendMessages(senderIPTV);
		        }
		    });
		}
		
		// -------------------- SOMR ORDERS --------------------
		Thread t10 = null;

		if (total_messages > 0) {
			final MessageSender newSender = new MessageSender(username, password);
		    newSender.init(MessageSender.OMS_REQ_QUEUE);

		    t10 = new Thread(new Runnable() {
		        @Override
		        public void run() {
		            WebLogicJmsXmlSender.SendSOMRMessages(newSender.getJmsResources(), total_messages, batch_size, thread_count, 
		            		max_msgs_per_second);
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
		if (t8 != null) t8.start();
		if (t9 != null) t9.start();
		if (t10 != null) t10.start();
		
		try {
		    if (t1 != null) t1.join();
		    if (t2 != null) t2.join();
		    if (t3 != null) t3.join();
		    if (t4 != null) t4.join();
		    if (t5 != null) t5.join();
		    if (t6 != null) t6.join();
		    if (t7 != null) t7.join();
		    if (t8 != null) t8.join();
		    if (t9 != null) t9.join();
		    if (t10 != null) t10.join();
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
                
		long endTime = System.nanoTime();
		long elapsedTime = endTime - startTime;
		double seconds = (double)elapsedTime / 1000000000.0;

		logger.info("Start time : " + startDate);
		logger.info("End time : " + new Date());		
		logger.info("\tAll messages sent in : " + seconds + " seconds");		
	}

}
