
package com.artemis.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.amqp.queue.ArtemisQueueReceiver;

public class ArtemisQueueReceiverTest {
	final static Logger logger = LoggerFactory.getLogger(ArtemisQueueReceiverTest.class);

	public static void main(String[] args) {
		try {
		    ArtemisQueueReceiver amqpReceiver = new ArtemisQueueReceiverDerived();

			// in case need to close connection
			// amqpReceiver.cleanUpConnection();
		} catch (Exception ex) {
			logger.error("Error while receiving message: " + ex.getMessage());
		}
	}
}
