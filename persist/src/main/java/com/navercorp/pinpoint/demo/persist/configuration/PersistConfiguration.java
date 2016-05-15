/*
 * Copyright 2016 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.demo.persist.configuration;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * @author HyunGil Jeong
 */
@Configuration
public class PersistConfiguration {

    @Value("${broker.url}")
    private String brokerUrl;

    @Value("${persist.queue.name}")
    private String persistQueueName;

    @Bean
    ConnectionFactory amqConnectionFactory() {
        return new ActiveMQConnectionFactory(this.brokerUrl);
    }

    @Bean
    Connection amqConnection(ConnectionFactory amqConnectionFactory) throws JMSException {
        Connection connection = amqConnectionFactory.createConnection();
        connection.start();
        connection.setExceptionListener(e -> System.out.println("JMS Exception occurred. Shutting down client."));
        return connection;
    }

    @Bean
    MessageConsumer persistQueuePrintingConsumer(Connection amqConnection) throws JMSException {
        final Session session = amqConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        final Queue persistQueue = session.createQueue(this.persistQueueName);
        final MessageConsumer printingConsumer = session.createConsumer(persistQueue);

        printingConsumer.setMessageListener(message -> {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String text;
                try {
                    text = textMessage.getText();
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Persist : " + text);
            } else {
                System.out.println("Persist : " + message);
            }
        });
        return printingConsumer;
    }
}
