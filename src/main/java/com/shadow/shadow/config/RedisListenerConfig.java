package com.shadow.shadow.config;

import com.shadow.shadow.services.SessionPersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class RedisListenerConfig {

    private final SessionPersistenceService persistenceService;
    private final String sessionTriggerString;
    private final String expiredEventsChannel;

    // Use constructor injection for all required dependencies (Autowired is implicit here)
    public RedisListenerConfig(
            SessionPersistenceService persistenceService,
            // Inject values here with defaults if necessary
            @Value("${sessionKeys.trigger:session:trigger:}") String sessionTriggerString,
            @Value("${sessionKeys.expired:__keyevent@0__:expired}") String expiredEventsChannel) {

        this.persistenceService = persistenceService;
        this.sessionTriggerString = sessionTriggerString;
        this.expiredEventsChannel = expiredEventsChannel;
    }

    // Define the listener container bean managed by Spring
    @Bean
    public ReactiveRedisMessageListenerContainer reactiveRedisMessageListenerContainer(ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisMessageListenerContainer(factory);
    }

    // Define a Flux bean that starts the background listening process automatically
    @Bean
    public Mono<Void> startRedisExpirationListener(ReactiveRedisMessageListenerContainer listenerContainer) {

        // Removed the problematic @PostConstruct method entirely

        log.info("Starting Redis listener on channel: " + expiredEventsChannel);

        return listenerContainer
                .receive(ChannelTopic.of(expiredEventsChannel))
                .map(message -> (String) message.getMessage()) // Get the key name
                .filter(key -> key.startsWith(sessionTriggerString)) // Filter for our triggers
                .flatMap(persistenceService::persistSessionFromExpiredTrigger) // Process the event
                .doOnError(error -> System.err.println("Error in Redis listener: " + error.getMessage()))
                .share() // Ensure the Flux runs in the background
                .then() // Indicate completion is irrelevant; the stream runs forever
                .cache();
    }
}