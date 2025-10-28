package com.healthcare.ingestion.service;

import com.healthcare.ingestion.model.OutboxIngestionEvent;
import com.healthcare.ingestion.repository.OutboxIngestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxPollingService {

    private final OutboxIngestionRepository outboxRepository;
    private final KafkaProducerService kafkaProducer;

    @Scheduled(fixedDelay = 15000) // Poll every 15 seconds
    @Transactional
    public void publishPendingEvents() {
        List<OutboxIngestionEvent> events = outboxRepository.
                findPendingEventsForUpdate(100);


        for (OutboxIngestionEvent event : events) {
            if (event.isConsumed()) {
                continue;
            }
            kafkaProducer.publishEvent(event);
            // if failed or server crashed before it, data will be published again (at least once)
            event.setConsumed(true);
        }
    }
}
