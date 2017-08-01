package com.embedded.rabbitmq;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMq;
import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class EmbeddedRabbitMqTest {

	private static final  Logger logger = LoggerFactory.getLogger(EmbeddedRabbitMqTest.class);
	private static final String SAMPLE_MESSAGE_DATA = "Sample message data";
	private static final String TEST_QUEUE = "test-queue";


	private EmbeddedRabbitMq rabbitMq;
	private ConnectionFactory connectionFactory;
	
	@Before
	public void before() {
		EmbeddedRabbitMqConfig config = new EmbeddedRabbitMqConfig.Builder().build();
		rabbitMq = new EmbeddedRabbitMq(config);
		rabbitMq.start();
		
		connectionFactory = new ConnectionFactory();
		connectionFactory.setHost("localhost");
		connectionFactory.setPort(config.getRabbitMqPort());
		connectionFactory.setVirtualHost("/");
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");
	}

	@Test
	public void simpleConnection() {

		Connection connection = null;
		Channel channel = null;
		CountDownLatch latch = new CountDownLatch(1);
		try {
			connection = connectionFactory.newConnection();
			assertThat(connection.isOpen(), equalTo(true));
			channel = connection.createChannel();
			assertThat(channel.isOpen(), equalTo(true));

			channel.queueDeclare(TEST_QUEUE, false, false, false, null);
			channel.basicPublish("", TEST_QUEUE, null,SAMPLE_MESSAGE_DATA.getBytes());
			logger.info("Sent '" + SAMPLE_MESSAGE_DATA + "'");

			Consumer consumer = new DefaultConsumer(channel) {
				
				@Override
				public void handleDelivery(String consumerTag,Envelope envelope, AMQP.BasicProperties properties,byte[] body) throws IOException {
					String message = new String(body, "UTF-8");
					logger.info("Received '" + message + "'");
					assertThat(message, equalTo(SAMPLE_MESSAGE_DATA));
					latch.countDown();
				}
			};
			channel.basicConsume(TEST_QUEUE, true, consumer);

			latch.await();
			
		} catch (Exception e) {
			logger.error("Exception", e);
		} finally {
			try {
				if (channel != null)channel.close();
				if (connection != null)connection.close();
			} catch (Exception e) {
				//ignore
			}
		}

	}

	@After
	public void tearDown() throws Exception {
		rabbitMq.stop();
	}
}