package com.healthcare.ingestion.service.processor;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileProcessor<T> {
    boolean supports(String contentType, String fileName);
    List<T> process(MultipartFile file) throws IOException;
}

