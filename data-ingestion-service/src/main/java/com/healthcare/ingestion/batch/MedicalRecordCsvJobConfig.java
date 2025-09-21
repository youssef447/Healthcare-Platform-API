package com.healthcare.ingestion.batch;

import com.healthcare.ingestion.dto.MedicalRecordDto;
import com.healthcare.ingestion.mapper.EntityMapper;
import com.healthcare.ingestion.model.MedicalRecord;
import com.healthcare.ingestion.model.Patient;
import com.healthcare.ingestion.repository.MedicalRecordRepository;
import com.healthcare.ingestion.repository.PatientRepository;
import com.healthcare.ingestion.service.KafkaProducerService;
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
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.BindException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor
public class MedicalRecordCsvJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final KafkaProducerService kafkaProducerService;

    @Bean
    public Job medicalRecordCsvJob(Step medicalRecordCsvStep) {
        return new JobBuilder("medicalRecordCsvJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(medicalRecordCsvStep)
                .build();
    }

    @Bean
    public Step medicalRecordCsvStep(FlatFileItemReader<MedicalRecordDto> medicalRecordCsvReader,
                                     ItemProcessor<MedicalRecordDto, MedicalRecord> medicalRecordProcessor,
                                     ItemWriter<MedicalRecord> medicalRecordWriter) {
        return new StepBuilder("medicalRecordCsvStep", jobRepository)
                .<MedicalRecordDto, MedicalRecord>chunk(100, transactionManager)
                .reader(medicalRecordCsvReader)
                .processor(medicalRecordProcessor)
                .writer(medicalRecordWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(1000)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<MedicalRecordDto> medicalRecordCsvReader(@Value("#{jobParameters['filePath']}") String filePath) {
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
        return new FieldSetMapper<>() {
            @Override
            public MedicalRecordDto mapFieldSet(FieldSet fieldSet) throws BindException {
                MedicalRecordDto dto = new MedicalRecordDto();
                dto.setPatientId(readLong(fieldSet, "patientId"));
                dto.setRecordType(fieldSet.readString("recordType"));
                dto.setDescription(fieldSet.readString("description"));
                dto.setDiagnosis(fieldSet.readString("diagnosis"));
                dto.setTreatment(fieldSet.readString("treatment"));
                dto.setMedications(fieldSet.readString("medications"));
                dto.setDoctorName(fieldSet.readString("doctorName"));
                dto.setHospitalName(fieldSet.readString("hospitalName"));
                dto.setVisitDate(parseDateTime(fieldSet.readString("visitDate")));
                dto.setFollowUpDate(parseDateTime(fieldSet.readString("followUpDate")));
                String status = fieldSet.readString("status");
                if (status != null && !status.isBlank()) {
                    try {
                        dto.setStatus(MedicalRecord.RecordStatus.valueOf(status.trim().toUpperCase()));
                    } catch (Exception e) {
                        log.warn("Invalid record status: {}", status);
                    }
                }
                dto.setNotes(fieldSet.readString("notes"));
                return dto;
            }

            private Long readLong(FieldSet fs, String name) {
                try {
                    return fs.readLong(name);
                } catch (Exception e) {
                    return null;
                }
            }

            private LocalDateTime parseDateTime(String value) {
                if (value == null || value.isBlank()) return null;
                List<DateTimeFormatter> fmts = List.of(
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                        DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"),
                        DateTimeFormatter.ofPattern("MM/dd/yyyy")
                );
                for (DateTimeFormatter f : fmts) {
                    try {
                        return LocalDateTime.parse(value, f);
                    } catch (Exception ignored) {
                    }
                }
                log.warn("Unable to parse dateTime: {}", value);
                return null;
            }
        };
    }

    @Bean
    public ItemProcessor<MedicalRecordDto, MedicalRecord> medicalRecordProcessor() {
        return dto -> {
            if (dto.getPatientId() == null) return null;
            Optional<Patient> patientOpt = patientRepository.findById(dto.getPatientId());
            if (patientOpt.isEmpty()) {
                log.warn("Patient with ID {} not found, skipping record", dto.getPatientId());
                return null; // filtered
            }
            return EntityMapper.toMedicalRecord(dto, patientOpt.get());
        };
    }

    @Bean
    public ItemWriter<MedicalRecord> medicalRecordWriter() {
        return items -> {
            List<MedicalRecord> saved = new ArrayList<>();
            for (MedicalRecord rec : items) {
                MedicalRecord savedRecord = medicalRecordRepository.save(rec);
                saved.add(savedRecord);
                try {
                    kafkaProducerService.publishMedicalRecordCreated(
                            savedRecord.getPatient().getId(),
                            savedRecord.getId(),
                            savedRecord.getRecordType()
                    );
                } catch (Exception e) {
                    log.error("Failed to publish Kafka event for medical record {}", savedRecord.getId(), e);
                }
            }
            log.info("Batch writer saved {} medical records", saved.size());
        };
    }
}
