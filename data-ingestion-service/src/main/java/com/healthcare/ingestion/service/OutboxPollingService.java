package com.healthcare.ingestion.service;

import com.healthcare.ingestion.entity.OutboxIngestionEvent;
import com.healthcare.ingestion.repository.OutboxIngestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxPollingService {
    private final OutboxIngestionRepository outboxRepository;
    private final KafkaProducerService kafkaProducer;
    private final OutboxTransactionService outboxTransactionService;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        long count = outboxRepository.count();
        log.info("Total events in outbox: {}", count);
        // 1: Lock & mark as PROCESSING
        List<OutboxIngestionEvent> events = outboxTransactionService.lockAndMarkProcessing(100);
        if (events.isEmpty()) {
            log.debug("No pending events found to process.");
            return;
        }

        log.info("Fetched {} pending events for publishing", events.size());

        // 2: Publish outside transaction
        for (OutboxIngestionEvent event : events) {
            try {
                kafkaProducer.publishEvent(event);
                event.setStatus(OutboxIngestionEvent.Status.COMPLETED);
            } catch (Exception e) {
                log.error("Failed to publish event {}: {}", event.getId(), e.getMessage());
                event.setStatus(OutboxIngestionEvent.Status.PENDING);
            }
        }

        // 3: Save status updates
        outboxRepository.saveAll(events);
    }


}