package com.shadow.shadow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "archived_sessions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionEntity {
    @Id
    private String id; // This will be the sessionId
    private String metadata;
    private String archivedTimestamp;
    private String status = "TIMED_OUT";
}
