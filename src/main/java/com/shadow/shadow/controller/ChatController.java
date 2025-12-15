package com.shadow.shadow.controller;

import com.shadow.shadow.model.SessionData;
import com.shadow.shadow.services.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatSessionService chatSessionService;

    @PostMapping("/activity/{sessionId}")
    public Mono<String> recordActivity(@PathVariable String sessionId, @RequestBody String userMessage) {
        SessionData data = new SessionData(
                sessionId,
                "User A Metadata",
                Instant.now().toString(),
                false
        );

        return chatSessionService.updateSessionAndResetTTL(sessionId, data)
                .thenReturn("Activity recorded for session " + sessionId + ". TTL reset to 30 seconds.");
    }
}
