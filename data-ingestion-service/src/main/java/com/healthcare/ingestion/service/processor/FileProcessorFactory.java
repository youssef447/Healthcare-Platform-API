package com.healthcare.ingestion.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FileProcessorFactory {

    private final List<FileProcessor<?>> processors;

    @SuppressWarnings("unchecked")
    public <T> FileProcessor<T> getProcessor(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        return (FileProcessor<T>) processors.stream()
                .filter(p -> p.supports(contentType, fileName))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException(
                        "Invalid File Format"
                ));
    }
}

