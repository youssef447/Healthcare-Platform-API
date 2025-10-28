package com.healthcare.patient.service;

import com.healthcare.patient.dto.PatientDto;
import com.healthcare.patient.mapper.PatientMapper;
import com.healthcare.patient.model.Patient;
import com.healthcare.patient.repository.PatientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PatientService {



    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;



    public Page<PatientDto> getAllPatients(Pageable pageable) {
        log.info("Retrieving patients with pagination");
        Page<Patient> patients = patientRepository.findAll(pageable);
        return patients.map(patientMapper::convertToDto);
    }

    public Optional<PatientDto> getPatientById(Long id) {
        log.info("Retrieving patient by ID: {}", id);
        Optional<Patient> patient = patientRepository.findById(id);
        return patient.map(patientMapper::convertToDto);
    }

    public Optional<PatientDto> getPatientByEmail(String email) {
        log.info("Retrieving patient by email: {}", email);
        Optional<Patient> patient = patientRepository.findByEmail(email);
        return patient.map(patientMapper::convertToDto);
    }

    public List<PatientDto> searchPatientsByName(String name) {
        log.info("Searching patients by name: {}", name);
        List<Patient> patients = patientRepository.findByNameContaining(name);
        return patients.stream()
                .map(patientMapper::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PatientDto> getPatientsByGender(Patient.Gender gender) {
        log.info("Retrieving patients by gender: {}", gender);
        List<Patient> patients = patientRepository.findByGender(gender);
        return patients.stream()
                .map(patientMapper::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PatientDto> getPatientsByStatus(Patient.PatientStatus status) {
        log.info("Retrieving patients by status: {}", status);
        List<Patient> patients = patientRepository.findByStatus(status);
        return patients.stream()
                .map(patientMapper::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PatientDto> getPatientsByBloodType(String bloodType) {
        log.info("Retrieving patients by blood type: {}", bloodType);
        List<Patient> patients = patientRepository.findByBloodType(bloodType);
        return patients.stream()
                .map(patientMapper::convertToDto)
                .collect(Collectors.toList());
    }

    public PatientDto createPatient(PatientDto patientDto) {
        log.info("Creating new patient: {}", patientDto.getFullName());
        
        if (patientRepository.existsByEmail(patientDto.getEmail())) {
            throw new IllegalArgumentException("Patient with email " + patientDto.getEmail() + " already exists");
        }

        Patient patient = patientMapper.convertToEntity(patientDto);
        Patient savedPatient = patientRepository.save(patient);
        
        log.info("Successfully created patient with ID: {}", savedPatient.getId());
        return patientMapper.convertToDto(savedPatient);
    }

    public PatientDto updatePatient(Long id, PatientDto patientDto) {
        log.info("Updating patient with ID: {}", id);
        
        Optional<Patient> existingPatient = patientRepository.findById(id);
        if (existingPatient.isEmpty()) {
            throw new IllegalArgumentException("Patient with ID " + id + " not found");
        }

        Patient patient = existingPatient.get();
        patientMapper.updatePatientFromDto(patient, patientDto);
        Patient savedPatient = patientRepository.save(patient);
        
        log.info("Successfully updated patient with ID: {}", savedPatient.getId());
        return patientMapper.convertToDto(savedPatient);
    }

    public void deletePatient(Long id) {
        log.info("Deleting patient with ID: {}", id);
        
        if (!patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Patient with ID " + id + " not found");
        }

        patientRepository.deleteById(id);
        log.info("Successfully deleted patient with ID: {}", id);
    }

    public long getPatientCount() {
        return patientRepository.count();
    }

    public long getPatientCountByGender(Patient.Gender gender) {
        return patientRepository.countByGender(gender);
    }

    public long getPatientCountByStatus(Patient.PatientStatus status) {
        return patientRepository.countByStatus(status);
    }

    public List<PatientDto> getPatientsCreatedToday() {
        log.info("Retrieving patients created today");
        List<Patient> patients = patientRepository.findPatientsCreatedToday();
        return patients.stream()
                .map(patientMapper::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PatientDto> getPatientsByDateOfBirthRange(LocalDate startDate, LocalDate endDate) {
        log.info("Retrieving patients by date of birth range: {} to {}", startDate, endDate);
        List<Patient> patients = patientRepository.findByDateOfBirthBetween(startDate, endDate);
        return patients.stream()
                .map(patientMapper::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PatientDto> getPatientsWithAllergies() {
        log.info("Retrieving patients with allergies");
        List<Patient> patients = patientRepository.findPatientsWithAllergies();
        return patients.stream()
                .map(patientMapper::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PatientDto> getPatientsWithoutEmergencyContact() {
        log.info("Retrieving patients without emergency contact");
        List<Patient> patients = patientRepository.findPatientsWithoutEmergencyContact();
        return patients.stream()
                .map(patientMapper::convertToDto)
                .collect(Collectors.toList());
    }


}
