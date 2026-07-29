package com.ride_service.kafka;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;


@Configuration
public class KafkaTopicConfig {

    public static final String RIDE_REQUESTED_TOPIC = "ride.requested";
    public static final String DRIVER_FOUND_TOPIC   = "driver.found";
    public static final String RIDE_COMPENSATION_TOPIC = "ride.compensation";
    public static final String MATCHING_FAILED_TOPIC = "ride.matching.failed";


    @Bean
    public NewTopic rideRequestedTopic() {
        return TopicBuilder
                .name(RIDE_REQUESTED_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic driverFoundTopic() {
        return TopicBuilder
                .name(DRIVER_FOUND_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic rideCompensationTopic() {
        return TopicBuilder
                .name(RIDE_COMPENSATION_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic matchingFailedTopic() {
        return TopicBuilder
                .name(MATCHING_FAILED_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
