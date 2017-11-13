package com.artemis.amqp.queue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractFuture;


public class JmsReplyQueueFuture<T extends Serializable> extends AbstractFuture<T> implements MessageListener {
    final static Logger logger = LoggerFactory.getLogger(JmsReplyQueueFuture.class);

    private final Session session;

    private final Queue replyQueue;

    private final MessageConsumer replyConsumer;

    private String correlationID;

    private final String requestMsg;

    public JmsReplyQueueFuture(Session session, Queue replyQueue, String correlationID,
            String requestMsg) throws JMSException {

        this.session = session;

        this.correlationID = correlationID;

        this.requestMsg = requestMsg;

        this.replyQueue = replyQueue;

        replyConsumer = session.createConsumer(replyQueue);

        replyConsumer.setMessageListener(this);

    }

    @Override
    protected void interruptTask() {
        cleanUp();
    }

    @Override

    public void onMessage(Message message) {
        try {
            if (message != null && message instanceof TextMessage) {
                TextMessage replyMessageReceived = (TextMessage) message;
                logger.info("Correlation Id in replied message: " + replyMessageReceived.getJMSCorrelationID());

                logger.info("Correlation ID in sent message" + this.correlationID);

                if (this.correlationID != null && this.correlationID.equals(replyMessageReceived.getJMSCorrelationID())) {
                    Map<String, TextMessage> replyMap = new HashMap<String, TextMessage>();
                    replyMap.put(requestMsg, replyMessageReceived);
                    set((T) replyMap);
                    logger.info("We found matched message reply");
                    cleanUp();
                } else {
                    logger.info("Received reply from reply queue, but not the one we are looking for: \n"
                            + replyMessageReceived.getText() + ", Correlation ID: " + replyMessageReceived.getJMSCorrelationID());
                }
            } else {
                logger.info("New Message is not TextMessage");
            }
        } catch (JMSException e) {
            logger.error("JMS Exception in onMessage:" + e.getMessage());
        } catch (Exception ex) {
            logger.error("Exception in onMessage:" + ex.getMessage());
        }
    }

    private void cleanUp() {
        try {
            if(replyConsumer != null) {
                replyConsumer.close();
            }

//            if (replyQueue != null) {
//                replyQueue.delete();
//            }
            
            Thread.sleep(100);
        }catch (JMSException jmse) {
            logger.error("Exception while closing consumer and queue" + jmse.getMessage());
            new Exception (jmse);
        }catch (Exception ex) {
            logger.error("Exception when close consumer and queu"+ ex.getMessage());
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (session != null)
                    try {
                        session.close();
                    } catch (JMSException e) {
                        logger.error("Error while close session:"+e.getMessage());
                        e.printStackTrace();
                    }
            }
        }).start();
    }
}
