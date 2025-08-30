package com.healthcare.patient.service;

import com.healthcare.patient.dto.PatientDto;
import com.healthcare.patient.model.Patient;
import com.healthcare.patient.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    @Autowired
    private PatientRepository patientRepository;

    public List<PatientDto> getAllPatients() {
        logger.info("Retrieving all patients");
        List<Patient> patients = patientRepository.findAll();
        return patients.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<PatientDto> getAllPatients(Pageable pageable) {
        logger.info("Retrieving patients with pagination");
        Page<Patient> patients = patientRepository.findAll(pageable);
        return patients.map(this::convertToDto);
    }

    public Optional<PatientDto> getPatientById(Long id) {
        logger.info("Retrieving patient by ID: {}", id);
        Optional<Patient> patient = patientRepository.findById(id);
        return patient.map(this::convertToDto);
    }

    public Optional<PatientDto> getPatientByEmail(String email) {
        logger.info("Retrieving patient by email: {}", email);
        Optional<Patient> patient = patientRepository.findByEmail(email);
        return patient.map(this::convertToDto);
    }

    public List<PatientDto> searchPatientsByName(String name) {
        logger.info("Searching patients by name: {}", name);
        List<Patient> patients = patientRepository.findByNameContaining(name);
        return patients.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PatientDto> getPatientsByGender(Patient.Gender gender) {
        logger.info("Retrieving patients by gender: {}", gender);
        List<Patient> patients = patientRepository.findByGender(gender);
        return patients.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PatientDto> getPatientsByStatus(Patient.PatientStatus status) {
        logger.info("Retrieving patients by status: {}", status);
        List<Patient> patients = patientRepository.findByStatus(status);
        return patients.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PatientDto> getPatientsByBloodType(String bloodType) {
        logger.info("Retrieving patients by blood type: {}", bloodType);
        List<Patient> patients = patientRepository.findByBloodType(bloodType);
        return patients.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public PatientDto createPatient(PatientDto patientDto) {
        logger.info("Creating new patient: {}", patientDto.getFullName());
        
        if (patientRepository.existsByEmail(patientDto.getEmail())) {
            throw new IllegalArgumentException("Patient with email " + patientDto.getEmail() + " already exists");
        }

        Patient patient = convertToEntity(patientDto);
        Patient savedPatient = patientRepository.save(patient);
        
        logger.info("Successfully created patient with ID: {}", savedPatient.getId());
        return convertToDto(savedPatient);
    }

    public PatientDto updatePatient(Long id, PatientDto patientDto) {
        logger.info("Updating patient with ID: {}", id);
        
        Optional<Patient> existingPatient = patientRepository.findById(id);
        if (existingPatient.isEmpty()) {
            throw new IllegalArgumentException("Patient with ID " + id + " not found");
        }

        Patient patient = existingPatient.get();
        updatePatientFromDto(patient, patientDto);
        Patient savedPatient = patientRepository.save(patient);
        
        logger.info("Successfully updated patient with ID: {}", savedPatient.getId());
        return convertToDto(savedPatient);
    }

    public void deletePatient(Long id) {
        logger.info("Deleting patient with ID: {}", id);
        
        if (!patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Patient with ID " + id + " not found");
        }

        patientRepository.deleteById(id);
        logger.info("Successfully deleted patient with ID: {}", id);
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
        logger.info("Retrieving patients created today");
        List<Patient> patients = patientRepository.findPatientsCreatedToday();
        return patients.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PatientDto> getPatientsByDateOfBirthRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Retrieving patients by date of birth range: {} to {}", startDate, endDate);
        List<Patient> patients = patientRepository.findByDateOfBirthBetween(startDate, endDate);
        return patients.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PatientDto> getPatientsWithAllergies() {
        logger.info("Retrieving patients with allergies");
        List<Patient> patients = patientRepository.findPatientsWithAllergies();
        return patients.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PatientDto> getPatientsWithoutEmergencyContact() {
        logger.info("Retrieving patients without emergency contact");
        List<Patient> patients = patientRepository.findPatientsWithoutEmergencyContact();
        return patients.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private PatientDto convertToDto(Patient patient) {
        PatientDto dto = new PatientDto();
        dto.setId(patient.getId());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setEmail(patient.getEmail());
        dto.setPhoneNumber(patient.getPhoneNumber());
        dto.setAddress(patient.getAddress());
        dto.setEmergencyContact(patient.getEmergencyContact());
        dto.setEmergencyPhone(patient.getEmergencyPhone());
        dto.setInsuranceNumber(patient.getInsuranceNumber());
        dto.setBloodType(patient.getBloodType());
        dto.setAllergies(patient.getAllergies());
        dto.setMedicalHistory(patient.getMedicalHistory());
        dto.setStatus(patient.getStatus());
        dto.setCreatedAt(patient.getCreatedAt());
        dto.setUpdatedAt(patient.getUpdatedAt());
        dto.setAge(patient.getAge());
        return dto;
    }

    private Patient convertToEntity(PatientDto dto) {
        Patient patient = new Patient();
        patient.setFirstName(dto.getFirstName());
        patient.setLastName(dto.getLastName());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setEmail(dto.getEmail());
        patient.setPhoneNumber(dto.getPhoneNumber());
        patient.setAddress(dto.getAddress());
        patient.setEmergencyContact(dto.getEmergencyContact());
        patient.setEmergencyPhone(dto.getEmergencyPhone());
        patient.setInsuranceNumber(dto.getInsuranceNumber());
        patient.setBloodType(dto.getBloodType());
        patient.setAllergies(dto.getAllergies());
        patient.setMedicalHistory(dto.getMedicalHistory());
        patient.setStatus(dto.getStatus() != null ? dto.getStatus() : Patient.PatientStatus.ACTIVE);
        return patient;
    }

    private void updatePatientFromDto(Patient patient, PatientDto dto) {
        if (dto.getFirstName() != null) patient.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) patient.setLastName(dto.getLastName());
        if (dto.getDateOfBirth() != null) patient.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) patient.setGender(dto.getGender());
        if (dto.getEmail() != null) patient.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) patient.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getAddress() != null) patient.setAddress(dto.getAddress());
        if (dto.getEmergencyContact() != null) patient.setEmergencyContact(dto.getEmergencyContact());
        if (dto.getEmergencyPhone() != null) patient.setEmergencyPhone(dto.getEmergencyPhone());
        if (dto.getInsuranceNumber() != null) patient.setInsuranceNumber(dto.getInsuranceNumber());
        if (dto.getBloodType() != null) patient.setBloodType(dto.getBloodType());
        if (dto.getAllergies() != null) patient.setAllergies(dto.getAllergies());
        if (dto.getMedicalHistory() != null) patient.setMedicalHistory(dto.getMedicalHistory());
        if (dto.getStatus() != null) patient.setStatus(dto.getStatus());
    }
}
