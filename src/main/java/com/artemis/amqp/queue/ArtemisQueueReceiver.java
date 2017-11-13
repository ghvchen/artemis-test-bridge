/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.artemis.amqp.queue;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.amqp.AMQPConnection;

public abstract class ArtemisQueueReceiver implements MessageListener {
	final static Logger logger = LoggerFactory.getLogger(ArtemisQueueReceiver.class);
	private static Connection connection = null;
	Session session;
	Context context = null;

	public ArtemisQueueReceiver() throws Exception {
		try {
			context = new InitialContext();
//			Hashtable<Object, Object> env = new Hashtable<Object, Object>();
//		    env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory"); 
//		    env.put(Context.PROVIDER_URL, "file:///root/jndi.properties"); 
//		    context = new javax.naming.InitialContext(env);
		
			connection = AMQPConnection.getInstance().getConnection(context);

			if (connection == null) {
				logger.error("No connection");
				throw new Exception("No connection establish to JMS server");
			} else {
				logger.info("get connection");
			}

			this.receiveMessage();
		} catch (JMSException jmse) {
			logger.error("No connection established, exit");
			throw new Exception(jmse);
		} catch (Exception ex) {
            logger.info("Exception:" + ex);
            throw new Exception(ex);
        }
	}

	private void receiveMessage() {
		try {
			// create session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Step3: Queue
			Queue queue = (Queue) context.lookup("myReceiverQueue");

			logger.info("Receiving Queue name:" + queue.getQueueName());

			// Step 5. create a moving receiver, this means the message will be removed from
			// the queue
			MessageConsumer consumer = session.createConsumer(queue);

			// Asynchronouos listener
			consumer.setMessageListener(this);

		} catch (JMSException e) {
			logger.error("Exception:" + e.getMessage());
		} catch (NamingException exp) {
            logger.error("Naming exception:" + exp.getMessage());
        }
	}

	public void onMessage(Message message) {
		try {
			if (message instanceof TextMessage) {
				TextMessage m = (TextMessage) message;

				logger.info("message received from request queue = " + m.getText());

				logger.info("Received Message ID:" + m.getJMSMessageID());

				String output = executeRequestedTask(m.getText());

				logger.info("Output after execute command: \n" + output);
				Queue replyQueue = (Queue) context.lookup("mySenderQueue");
				
				if(replyQueue != null && output != null && output.length() > 0) {
					//Queue replyQueue = session.createQueue(replyD.getQueueName());
					logger.info("Reply Queue name:" + replyQueue.getQueueName());

					MessageProducer replyproducer = session.createProducer(replyQueue);

					TextMessage replyMsg = session.createTextMessage(output);

					// Step 12. Set the ReplyTo header so that the request receiver knows where to
					// send the reply.
					replyMsg.setJMSCorrelationID(m.getJMSMessageID());

					// Step 13. Sent the request message
					replyproducer.send(replyMsg);
					logger.info("Send out reply");
				} else {
					logger.info("No response needed");
				}
			} else {
				logger.info("Message is not TextMessage");
			}
		} catch (JMSException e) {
			logger.error("JMSException in onMessage:" + e.getMessage());
		} catch (Exception ex) {
			logger.error("Exception onMessage:" + ex.getMessage());
		}
	}

	// this is base functionality, and each derived class should implement their
	// only functionality
	public String executeRequestedTask(String command) {
		String output = "This is place holder and please write your own implementation in derived class";

		return output;
	}

	public void cleanUpConnection() {
		logger.info("Close Connection");
		AMQPConnection.getInstance().closeConnection();

		connection = null;
	}
}
