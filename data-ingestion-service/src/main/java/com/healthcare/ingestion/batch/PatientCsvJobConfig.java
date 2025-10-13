package com.healthcare.ingestion.batch;

import com.healthcare.ingestion.dto.PatientDto;
import com.healthcare.ingestion.mapper.PatientMapper;
import com.healthcare.ingestion.model.Patient;

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
    private final PatientMapper patientMapper;

    @Bean
    public Job patientCsvJob(Step patientCsvStep) {
        return new JobBuilder("patientCsvJob", jobRepository) // the repo is used to store the job metadata (state of steps and job and so on)
                .incrementer(new RunIdIncrementer()) //adds id for the job in case of I wanted to start the same job again with same params
                .start(patientCsvStep)
                //.next(step2)
                .build();
    }


    //each step consist of (reader, processor and writer)
    @Bean
    public Step patientCsvStep(FlatFileItemReader<PatientDto> patientCsvReader,
                               ItemProcessor<PatientDto, Patient> patientProcessor,
                               ItemWriter<Patient> patientWriter) {


        return new StepBuilder("patientCsvStep", jobRepository)
                .<PatientDto, Patient>chunk(100, transactionManager) // transactionManager controls the transaction for each chunk, important for atomicity
                .reader(patientCsvReader)
                .processor(patientProcessor)
                .writer(patientWriter)
                // to ignore not valid records instead of failing the whole job,
                // only if it's not subtype of exception class and if it didn't occur more than 100, then the whole job will fail
                // note: the exception here or any failure is considered in any of (read, process, write)
                .faultTolerant().skip(Exception.class).skipLimit(100)
                .build();
    }

    @Bean
    // This makes the bean shadowed by the lifetime of the Step, meaning it is created when the Step runs, not when the global context starts.
    // Practical reason: So we can add "#{jobParameters['filePath']}" — if there was no StepScope, Spring wouldn't be able to inject the job parameter at runtime.
    @StepScope
    public FlatFileItemReader<PatientDto> patientCsvReader(@Value("#{jobParameters['filePath']}") String filePath) {
        FlatFileItemReader<PatientDto> reader = new FlatFileItemReader<>();
        reader.setName("patientCsvReader"); // used by Spring Batch in logs and in the JobRepository.
        reader.setResource(new FileSystemResource(filePath)); //FileSystemResource means a local file
        // skip header
        reader.setLinesToSkip(1);
        reader.setRecordSeparatorPolicy(new DefaultRecordSeparatorPolicy());
        // link the line mapper which will convert each line to PatientDTO
        reader.setLineMapper(patientLineMapper());
        return reader;
    }


    @Bean
    public LineMapper<PatientDto> patientLineMapper() {
        // Line mapper consist of (tokenizer and fields set mapper)
        // this is a default implementation of LineMapper that deals with both
        DefaultLineMapper<PatientDto> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        // FieldSetMapper will use them which called fieldSet,
        // they are the keys to access ordered columns in each row,
        // the names can be different from the original csv, but the order is important
        tokenizer.setNames("firstName", "lastName", "dateOfBirth", "gender", "phoneNumber", "email", "address");
        // if the number of columns is smaller, the missing value will be null instead of Exception,
        // This is useful if the data isn't always complete,
        // but be careful because the FieldSetMapper must handle null values.
        tokenizer.setStrict(false);
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(patientFieldSetMapper());
        return lineMapper;
    }

    @Bean
    public FieldSetMapper<PatientDto> patientFieldSetMapper() {
        return new FieldSetMapper<PatientDto>() {
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


    /// Convert PatientDto to Patient to be written to DB, we can do other processing here or validation
    @Bean
    public ItemProcessor<PatientDto, Patient> patientProcessor() {


        return patientMapper::toPatient;

    }

    @Bean
    public ItemWriter<Patient> patientWriter(KafkaProducerService kafkaProducer,
                                             PatientRepository patientRepository) {
        return patients -> {
            List<? extends Patient> savedPatients = patientRepository.saveAll(patients);
            for (Patient saved : savedPatients) {
                kafkaProducer.publishPatientCreated(saved.getId());
            }
        };

    }
}
