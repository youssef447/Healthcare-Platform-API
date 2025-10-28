package com.healthcare.ingestion.service;

import com.healthcare.ingestion.entity.OutboxIngestionEvent;
import com.healthcare.ingestion.repository.OutboxIngestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxTransactionService {
    private final OutboxIngestionRepository outboxRepository;

    @Transactional
    public List<OutboxIngestionEvent> lockAndMarkProcessing(int limit) {
        List<OutboxIngestionEvent> events = outboxRepository.findPendingEventsForUpdate(limit);
        for (OutboxIngestionEvent event : events) {
            event.setStatus(OutboxIngestionEvent.Status.PROCESSING);
        }
        outboxRepository.saveAll(events);
        return events;
    }
}
