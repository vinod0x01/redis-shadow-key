package com.shadow.shadow.services;

import com.shadow.shadow.model.SessionEntity;
import com.shadow.shadow.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionPersistenceService {
    private final ChatSessionService chatSessionService;
    private final SessionRepository sessionRepository;

    public Mono<Void> persistSessionFromExpiredTrigger(String expiredTriggerKeyName) {
        // Extract the sessionId from "session:trigger:123"
        String sessionId = expiredTriggerKeyName.replace("session:trigger:", "");
        String dataKey = "session:data:" + sessionId;

        log.info("[LISTENER] Trigger expired for Session ID: " + sessionId + ". Attempting persistence...");

        // Retrieve the data from the permanent data key
        return chatSessionService.getSessionData(dataKey)
                .flatMap(sessionData -> {
                    // Map to Mongo Entity
                    SessionEntity entity = new SessionEntity(
                            sessionId,
                            sessionData.getUserMetadata(),
                            Instant.now().toString(),
                            "TIMED_OUT"
                    );
                    // Save to MongoDB
                    return sessionRepository.save(entity)
                            .doOnSuccess(savedEntity -> log.info("[MONGO] Successfully persisted session " + sessionId))
                            // After successful save, delete the permanent data key from Redis
                            .then(chatSessionService.cleanupSession(sessionId));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("[ERROR] Data key " + dataKey + " was already missing or empty. Cleanup complete.");
                    return chatSessionService.cleanupSession(sessionId); // Cleanup trigger key anyway if data is somehow gone
                }))
                .onErrorResume(e -> {
                    log.info("[ERROR] Persistence failed for session " + sessionId + ": " + e.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}
