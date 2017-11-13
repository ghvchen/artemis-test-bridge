package com.artemis.amqp;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AMQPConnection {
    private static Logger logger = LoggerFactory.getLogger(AMQPConnection.class);
    private static Connection connection = null;
    private static AMQPConnection amqpInstance = null;
    
    private AMQPConnection () {
        
    }
    
    public static AMQPConnection getInstance() {
        if (amqpInstance == null) {
            amqpInstance = new AMQPConnection();
        }
        
        return amqpInstance;
    }

    public Connection getConnection(Context context) {
        try {
            if (connection == null) {
                ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");

                Properties prop = new Properties();
                
                //load from local project
                prop.load(new FileReader("src/main/resources/jndi.properties"));
                
                //load from externalized jndi.properties file
              //  prop.load(new FileReader("/home/vchen/jndi.properties"));
                
                logger.info("Username:"+prop.getProperty("ARTEMISUSER"));

                connection = factory.createConnection(prop.getProperty("ARTEMISUSER"), prop.getProperty("PASSWORD"));

                logger.info("User: " + prop.getProperty("ARTEMISUSER") + ", password: " + prop.getProperty("PASSWORD"));

                connection.setExceptionListener(new ExceptionListener() {
                    public void onException(JMSException exception) {
                        logger.error("Connection error:" + exception.getMessage());
                        try {
                            if (connection != null) {
                                connection.close();
                                connection = null;
                            }
                            // System.exit(1);
                        } catch (JMSException ex) {
                            logger.error("Error while closing connection" + ex.getMessage());
                        }
                    }
                });
                connection.start();
            }else {
                logger.info("Already has connection, no need to create");
            }
        } catch (JMSException jmse) {
            logger.error("Connection error during connect to artemis:" + jmse);
        } catch (FileNotFoundException exp) {
            logger.error("Naming exception:" + exp.getMessage());
        } catch (NamingException exp) {
            logger.error("Naming exception:" + exp.getMessage());
        } catch (Exception ex) {
            logger.error("Naming exception:" + ex.getMessage());
        }

        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (Exception ex) {
                logger.error("Error while close connection" + ex.getMessage());
            }
        }
    }
}
