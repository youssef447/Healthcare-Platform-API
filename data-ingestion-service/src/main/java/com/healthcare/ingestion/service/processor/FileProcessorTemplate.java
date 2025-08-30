package com.healthcare.ingestion.service.processor;

import com.healthcare.ingestion.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class FileProcessorTemplate<T> implements FileProcessor<T> {

    private final KafkaProducerService kafkaProducerService;

    @Override
    public List<T> process(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        log.info("Processing file: {}", fileName);

        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            List<T> items = parseFile(reader, fileName);
            kafkaProducerService.publishFileProcessed(fileName, items.size());
            return items;
        } catch (Exception e) {
            log.error("Error processing file: {}", fileName, e);
            kafkaProducerService.publishProcessingError(fileName, e.getMessage());
            throw e;
        }
    }

    protected abstract List<T> parseFile(Reader reader, String fileName) throws IOException;
}

