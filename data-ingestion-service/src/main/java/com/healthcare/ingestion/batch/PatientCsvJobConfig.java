package com.healthcare.ingestion.batch;

import com.healthcare.ingestion.dto.PatientDto;
import com.healthcare.ingestion.mapper.EntityMapper;
import com.healthcare.ingestion.model.Patient;
import com.healthcare.ingestion.repository.PatientRepository;
import com.healthcare.ingestion.service.KafkaProducerService;
import com.healthcare.ingestion.service.PatientIngestionService;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor
public class PatientCsvJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PatientIngestionService patientIngestionService;

    @Bean
    public Job patientCsvJob(Step patientCsvStep) {
        return new JobBuilder("patientCsvJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(patientCsvStep)
                .build();
    }

    @Bean
    public Step patientCsvStep(FlatFileItemReader<PatientDto> patientCsvReader,
                               ItemProcessor<PatientDto, Patient> patientProcessor,
                               ItemWriter<Patient> patientWriter) {


        return new StepBuilder("patientCsvStep", jobRepository)
                .<PatientDto, Patient>chunk(100, transactionManager)
                .reader(patientCsvReader)
                .processor(patientProcessor)
                .writer(patientWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<PatientDto> patientCsvReader(@Value("#{jobParameters['filePath']}") String filePath) {
        FlatFileItemReader<PatientDto> reader = new FlatFileItemReader<>();
        reader.setName("patientCsvReader");
        reader.setResource(new FileSystemResource(filePath));
        reader.setLinesToSkip(1); // skip header
        reader.setRecordSeparatorPolicy(new DefaultRecordSeparatorPolicy());
        reader.setLineMapper(patientLineMapper());
        return reader;
    }

    @Bean
    public LineMapper<PatientDto> patientLineMapper() {
        DefaultLineMapper<PatientDto> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames("firstName", "lastName", "dateOfBirth", "gender", "phoneNumber", "email", "address");
        tokenizer.setStrict(false);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(patientFieldSetMapper());
        return lineMapper;
    }

    @Bean
    public FieldSetMapper<PatientDto> patientFieldSetMapper() {
        return new FieldSetMapper<>() {
            @Override
            public PatientDto mapFieldSet(FieldSet fieldSet) {
                PatientDto dto = new PatientDto();
                dto.setFirstName(fieldSet.readString("firstName"));
                dto.setLastName(fieldSet.readString("lastName"));
                String dob = fieldSet.readString("dateOfBirth");
                dto.setDateOfBirth(parseDate(dob));
                String gender = fieldSet.readString("gender");
                if (!gender.isBlank()) {
                    try {
                        dto.setGender(Patient.Gender.valueOf(gender.trim().toUpperCase()));
                    } catch (Exception e) {
                        log.warn("Invalid gender value: {}", gender);
                    }
                }
                dto.setPhoneNumber(fieldSet.readString("phoneNumber"));
                dto.setEmail(fieldSet.readString("email"));
                dto.setAddress(fieldSet.readString("address"));
                return dto;
            }

            private LocalDate parseDate(String value) {
                if (value == null || value.isBlank()) return null;

                List<DateTimeFormatter> formatters = List.of(
                        DateTimeFormatter.ISO_LOCAL_DATE,
                        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                        DateTimeFormatter.ofPattern("dd-MM-yyyy")
                );

                for (DateTimeFormatter formatter : formatters) {
                    try {
                        return LocalDate.parse(value, formatter);
                    } catch (Exception ignored) {
                    }
                }

                log.warn("Unable to parse date: {}", value);
                return null;
            }
        };
    }


    /// Convert PatientDto to Patient to be wrote to DB
    @Bean
    public ItemProcessor<PatientDto, Patient> patientProcessor() {
        return EntityMapper::toPatient;
    }

    @Bean
    public ItemWriter<Patient> patientWriter() {
        return items -> {
            for (Patient patient : items) {
                patientIngestionService.ingestPatient(patient);

            }
        };
    }
}
