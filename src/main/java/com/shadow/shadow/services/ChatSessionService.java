package com.shadow.shadow.services;

import com.shadow.shadow.model.SessionData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class ChatSessionService {

    private final ReactiveRedisTemplate<String, SessionData> reactiveRedisTemplate;
    private final Duration sessionTimeout = Duration.ofSeconds(30);
    private final String sessionTriggerString;
    private final String sessionDataString;

    public ChatSessionService(ReactiveRedisTemplate<String, SessionData> reactiveRedisTemplate,
                              @Value("${sessionKeys.trigger:session:trigger:}") String sessionTriggerString,
                              @Value("${sessionKeys.data:session:data:}") String sessionDataString) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.sessionTriggerString = sessionTriggerString;
        this.sessionDataString = sessionDataString;
    }

    public Mono<Void> updateSessionAndResetTTL(String sessionId, SessionData data) {
        String dataKey = sessionDataString + sessionId;
        String triggerKey = sessionTriggerString + sessionId;
        SessionData triggerData = new SessionData();

        Mono<Boolean> setData = reactiveRedisTemplate.opsForValue()
                .set(dataKey, data)
                .flatMap(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        return reactiveRedisTemplate.opsForValue()
                                .set(triggerKey, triggerData, sessionTimeout);
                    }
                    return Mono.just(false);
                });
        return setData.then();
    }

    public Mono<SessionData> getSessionData(String dataKey) {
        return reactiveRedisTemplate.opsForValue().get(dataKey);
    }

    public Mono<Void> cleanupSession(String sessionId) {
        String dataKey = sessionDataString + sessionId;
        String triggerKey = sessionTriggerString + sessionId;
        return reactiveRedisTemplate.delete(dataKey, triggerKey).then();
    }
}
