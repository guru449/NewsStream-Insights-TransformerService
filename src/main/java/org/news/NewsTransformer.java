package org.news;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Properties;

public class NewsTransformer {
    private static final String RAW_TOPIC = "raw-news";
    private static final String PROCESSED_TOPIC = "processed-news";
    private static final String KAFKA_BROKER = "localhost:9092";

    public static void main(String[] args) {
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", KAFKA_BROKER);
        consumerProps.put("group.id", "news-transformer-group");
        consumerProps.put("key.deserializer", StringDeserializer.class.getName());
        consumerProps.put("value.deserializer", StringDeserializer.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(RAW_TOPIC));

        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", KAFKA_BROKER);
        producerProps.put("key.serializer", StringSerializer.class.getName());
        producerProps.put("value.serializer", StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : records) {
                String transformedData = performTopicModeling(record.value());
                producer.send(new ProducerRecord<>(PROCESSED_TOPIC, transformedData));
            }
        }
    }

    private static String performTopicModeling(String article) {
        // Implement your topic modeling logic here (e.g., using LDA)
        // For demonstration, we will just add a dummy topic field
        JSONObject jsonArticle = new JSONObject(article);
        jsonArticle.put("topic", "sample_topic");
        return jsonArticle.toString();
    }
}
