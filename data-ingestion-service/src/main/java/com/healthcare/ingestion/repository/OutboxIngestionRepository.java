package com.healthcare.ingestion.repository;

import com.healthcare.ingestion.model.OutboxIngestionEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxIngestionRepository extends JpaRepository<OutboxIngestionEvent, Long> {



    @Query(
            value = """
        SELECT * FROM outbox_ingestion_event
        WHERE consumed = false
        ORDER BY created_at ASC
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """,
            nativeQuery = true
    )
    List<OutboxIngestionEvent> findPendingEventsForUpdate(@Param("limit") int limit);
}
