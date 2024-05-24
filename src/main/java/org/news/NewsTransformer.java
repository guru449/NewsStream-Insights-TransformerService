package org.news;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

import java.util.Collections;
import java.util.Properties;

@SpringBootApplication
@EnableScheduling
public class NewsTransformer {

    @Value("${app.kafka.raw-topic}")
    private String rawTopic;

    @Value("${app.kafka.processed-topic}")
    private String processedTopic;

    @Value("${app.kafka.broker}")
    private String kafkaBroker;

    public static void main(String[] args) {
        SpringApplication.run(NewsTransformer.class, args);
    }

    @Bean
    public CommandLineRunner run(KafkaConsumer<String, String> consumer, KafkaProducer<String, String> producer) {
        return args -> {
            consumer.subscribe(Collections.singletonList(rawTopic));
            processNews(consumer, producer);
        };
    }

    public void processNews(KafkaConsumer<String, String> consumer, KafkaProducer<String, String> producer) {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : records) {
                String transformedData = extractFields(record.value());
                producer.send(new ProducerRecord<>(processedTopic, transformedData));
            }
        }
    }

    private String extractFields(String article) {
        JSONObject jsonArticle = new JSONObject(article);
        JSONObject newJson = new JSONObject();

        newJson.put("author", jsonArticle.optString("author"));
        newJson.put("publishedAt", jsonArticle.optString("publishedAt"));
        newJson.put("title", jsonArticle.optString("title"));
        newJson.put("content", jsonArticle.optString("content"));

        return newJson.toString();
    }

    @Bean
    public KafkaConsumer<String, String> kafkaConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaBroker);
        props.put("group.id", "transformer-service-group");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("auto.offset.reset", "earliest");
        return new KafkaConsumer<>(props);
    }

    @Bean
    public KafkaProducer<String, String> kafkaProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaBroker);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "transformer-service");
    }
}
