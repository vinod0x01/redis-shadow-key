package com.shadow.shadow.repository;

import com.shadow.shadow.model.SessionEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends ReactiveMongoRepository<SessionEntity, String> {
}
