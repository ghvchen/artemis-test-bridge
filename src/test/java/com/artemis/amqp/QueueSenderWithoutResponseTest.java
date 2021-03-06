package com.artemis.amqp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.amqp.queue.ArtemisQueueReceiver;
import com.artemis.amqp.queue.ArtemisQueueSender;

/**
 * @author vchen
 * The test will only send message, no response will received
 */
public class QueueSenderWithoutResponseTest {
    final static Logger logger = LoggerFactory.getLogger(QueueSenderWithoutResponseTest.class);

    public static void main(String[] args) {
        ArtemisQueueSender senderQueue = new ArtemisQueueSender();
        try {          
            senderQueue.sendMessageWithoutResponse("ls");
            
            ArtemisQueueReceiver amqpReceiver = new ArtemisQueueReceiverDerived();
            
            while (true) {
        	Thread.sleep(1000);
            }
        } catch (Exception ex) {
            logger.error("Error for sending message:" + ex.getMessage());
        } finally {
            logger.info("Close connection");
            senderQueue.cleanUpConnection();
        }
    }
}
