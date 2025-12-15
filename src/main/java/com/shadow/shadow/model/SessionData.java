package com.shadow.shadow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionData {
    private String sessionId;
    private String userMetadata;
    private String lastMessageTimestamp;
    private boolean gracefullyClosed = false;
}
