package fr.lapinrose.kstream.kstreamdemo;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedKafka(ports=9092)
@ExtendWith(SpringExtension.class)
@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IKStreamProcessorTest {

    private static String topicIn = "input";
    private static String topicOut= "output";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    private BlockingQueue<ConsumerRecord<String, String>> records;
    private KafkaMessageListenerContainer<String, String> container;

    @BeforeAll
    public  void setUp() {
        System.setProperty("spring.cloud.stream.kafka.streams.binder.brokers", embeddedKafkaBroker.getBrokersAsString());
        System.setProperty("spring.kafka.bootstrap-servers", embeddedKafkaBroker.getBrokersAsString());
        System.setProperty("spring.cloud.stream.kafka.binder.zkNodes", embeddedKafkaBroker.getZookeeperConnectionString());
        System.setProperty("server.port","0");
        System.setProperty("spring.jmx.enabled" , "false");
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("group", "false", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(topicOut);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

    }

    @AfterEach
    void tearDown() {
        container.stop();
    }


    @Test
    void process() throws IOException, InterruptedException {
        Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        DefaultKafkaProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(senderProps);
        try {
            KafkaTemplate<String, String> template = new KafkaTemplate<>(pf, true);
            template.setDefaultTopic(topicIn);
            template.sendDefault("test");
            ConsumerRecord<String, String> singleRecord = records.poll(2000, TimeUnit.MILLISECONDS);
            assertThat(singleRecord).isNotNull();
        }
        finally {
            pf.destroy();
        }
    }
}