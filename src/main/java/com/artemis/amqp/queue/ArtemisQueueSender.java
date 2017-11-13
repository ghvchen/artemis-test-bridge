package com.artemis.amqp.queue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Future;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artemis.amqp.AMQPConnection;

public class ArtemisQueueSender {
    final static Logger logger = LoggerFactory.getLogger(ArtemisQueueSender.class);
    final Map<String, TextMessage> requestMap = new HashMap<>();
    private static Connection connection = null;

    public ArtemisQueueSender() {
    }

    public <T extends Serializable> Future<T> sendMessage(String command) throws Exception {
	try {
	    //This is for externalize jndi.property file
	    //            System.out.println("Inside send message");
	    //            Hashtable<Object, Object> env = new Hashtable<Object, Object>();
	    //            env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory"); 
	    //            env.put(Context.PROVIDER_URL, "file:///home/vchen/jndi.properties"); 
	    //            javax.naming.Context context = new javax.naming.InitialContext(env);

	    //This is used when jndi.property file is under /src/main/resources
	    Context context = new InitialContext();

	    connection = AMQPConnection.getInstance().getConnection(context);

	    if (connection == null) {
		logger.error("No connection");
		throw new Exception("No connection establish to JMS server");
	    } else {
		logger.info("get connection");
	    }
	    logger.info("Command to run:" + command);

	    // Step 2. Create a session
	    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

	    // Step3: Queue
	    Queue queue = (Queue) context.lookup("mySenderQueue");

	    MessageProducer sender = session.createProducer(queue);

	    //  TemporaryQueue replyQueue = session.createTemporaryQueue();
	    Queue replyQueue = (Queue) context.lookup("myReceiverQueue");

	    logger.info("reply queue name:" + replyQueue.getQueueName());

	    logger.info("sending Queue name:" + queue.getQueueName());

	    TextMessage message = session.createTextMessage(command);

	    // This line is used for last value queue
	    //   message.setStringProperty("_AMQ_LVQ_NAME", "STOCK_NAME");

	    message.setJMSReplyTo(replyQueue);

	    sender.send(message);
	    // get message id after sent message
	    String correlationID = message.getJMSMessageID();
	    logger.info("CorrelationID set should in response map: " + correlationID);

	    return new JmsReplyQueueFuture<T>(session, replyQueue, correlationID, command);
	} catch (JMSException jmsex) {
	    logger.info("JMSEX: " + jmsex.getMessage());
	    throw new Exception(jmsex);
	} catch (Exception ex) {
	    logger.info("Exception:" + ex);
	    throw new Exception(ex);
	}
    }

    public void sendMessageWithoutResponse(String command) throws Exception {
	try {
	   Context context = new InitialContext();

//	    Hashtable<Object, Object> env = new Hashtable<Object, Object>();
//	    env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory"); 
//	    env.put(Context.PROVIDER_URL, "file:///root/jndi.properties"); 
//	    javax.naming.Context context = new javax.naming.InitialContext(env);

	    connection = AMQPConnection.getInstance().getConnection(context);

	    if (connection == null) {
		logger.error("No connection");
		throw new Exception("No connection establish to JMS server");
	    } else {
		logger.info("get connection");
	    }
	    logger.info("In send message without response.Command to run:" + command);

	    // Step 2. Create a session
	    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

	    // Step3: Queue
	    Queue queue = (Queue) context.lookup("mySenderQueue");

	    MessageProducer sender = session.createProducer(queue);

	    logger.info("sending Queue name:" + queue.getQueueName());

	    TextMessage message = session.createTextMessage(command);

	    //  message.setStringProperty("_AMQ_LVQ_NAME", "STOCK_NAME");

	    sender.send(message);

	    //close session
	    session.close();
	} catch (JMSException jmsex) {
	    logger.info("JMSEX: " + jmsex.getMessage());
	    throw new Exception(jmsex);
	} catch (Exception ex) {
	    logger.info("Exception:" + ex);
	    throw new Exception(ex);
	}
    }

    public void cleanUpConnection() {
	AMQPConnection.getInstance().closeConnection();
    }
}
