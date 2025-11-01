package com.healthcare.ingestion.batch.medical_record;

import com.healthcare.ingestion.dto.MedicalRecordDto;
import com.healthcare.ingestion.entity.OutboxIngestionEvent;
import com.healthcare.ingestion.repository.OutboxIngestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;


@Configuration
@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor
public class MedicalRecordCsvJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job medicalRecordCsvJob(Step medicalRecordCsvStep) {
        return new JobBuilder("medicalRecordCsvJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(medicalRecordCsvStep)
                .build();
    }

    @Bean
    public Step medicalRecordCsvStep(FlatFileItemReader<MedicalRecordDto> medicalRecordCsvReader,
                                     ItemProcessor<MedicalRecordDto, OutboxIngestionEvent> medicalRecordProcessor,
                                     ItemWriter<OutboxIngestionEvent> recordWriter) {
        return new StepBuilder("medicalRecordCsvStep", jobRepository)
                .<MedicalRecordDto, OutboxIngestionEvent>chunk(100, transactionManager)
                .reader(medicalRecordCsvReader)
                .processor(medicalRecordProcessor)
                .writer(recordWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(1000)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<MedicalRecordDto>
    medicalRecordCsvReader(@Value("#{jobParameters['filePath']}") String filePath) {
        FlatFileItemReader<MedicalRecordDto> reader = new FlatFileItemReader<>();
        reader.setName("medicalRecordCsvReader");
        reader.setResource(new FileSystemResource(filePath));
        reader.setLinesToSkip(1); // header
        reader.setRecordSeparatorPolicy(new DefaultRecordSeparatorPolicy());
        reader.setLineMapper(medicalRecordLineMapper());
        return reader;
    }

    @Bean
    public LineMapper<MedicalRecordDto> medicalRecordLineMapper() {
        DefaultLineMapper<MedicalRecordDto> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames(
                "patientId",
                "recordType",
                "description",
                "diagnosis",
                "treatment",
                "medications",
                "doctorName",
                "hospitalName",
                "visitDate",
                "followUpDate",
                "status",
                "notes"
        );
        tokenizer.setStrict(false);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(medicalRecordFieldSetMapper());
        return lineMapper;
    }

    @Bean
    public FieldSetMapper<MedicalRecordDto> medicalRecordFieldSetMapper() {
        return new MedicalRecordFieldSetMapper();
    }

    @Bean
    public ItemProcessor<MedicalRecordDto, OutboxIngestionEvent> recordProcessor() {
        return (dto) -> OutboxIngestionEvent.builder()
                .source("data-ingestion-service")
                .eventType(OutboxIngestionEvent.EventType.MEDICAL_RECORD_CREATED)
                .payload(dto.toString())
                .build();

    }

    @Bean
    @Transactional
    public ItemWriter<OutboxIngestionEvent> recordWriter(
            OutboxIngestionRepository outboxIngestionRepository) {
        return outboxIngestionRepository::saveAll;

    }

}
