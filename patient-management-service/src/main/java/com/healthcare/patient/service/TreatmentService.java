package com.healthcare.patient.service;

import com.healthcare.patient.dto.TreatmentDto;
import com.healthcare.patient.model.Patient;
import com.healthcare.patient.model.Treatment;
import com.healthcare.patient.repository.PatientRepository;
import com.healthcare.patient.repository.TreatmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TreatmentService {

    private static final Logger logger = LoggerFactory.getLogger(TreatmentService.class);

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    public List<TreatmentDto> getAllTreatments() {
        logger.info("Retrieving all treatments");
        List<Treatment> treatments = treatmentRepository.findAll();
        return treatments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<TreatmentDto> getAllTreatments(Pageable pageable) {
        logger.info("Retrieving treatments with pagination");
        Page<Treatment> treatments = treatmentRepository.findAll(pageable);
        return treatments.map(this::convertToDto);
    }

    public Optional<TreatmentDto> getTreatmentById(Long id) {
        logger.info("Retrieving treatment by ID: {}", id);
        Optional<Treatment> treatment = treatmentRepository.findById(id);
        return treatment.map(this::convertToDto);
    }

    public List<TreatmentDto> getTreatmentsByPatientId(Long patientId) {
        logger.info("Retrieving treatments for patient ID: {}", patientId);
        List<Treatment> treatments = treatmentRepository.findByPatientId(patientId);
        return treatments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TreatmentDto> getTreatmentsByPatientIdAndStatus(Long patientId, Treatment.TreatmentStatus status) {
        logger.info("Retrieving treatments for patient ID: {} with status: {}", patientId, status);
        List<Treatment> treatments = treatmentRepository.findByPatientIdAndStatus(patientId, status);
        return treatments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TreatmentDto> getTreatmentsByStatus(Treatment.TreatmentStatus status) {
        logger.info("Retrieving treatments by status: {}", status);
        List<Treatment> treatments = treatmentRepository.findByStatus(status);
        return treatments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TreatmentDto> getTreatmentsByDoctor(String doctorName) {
        logger.info("Retrieving treatments by doctor: {}", doctorName);
        List<Treatment> treatments = treatmentRepository.findByDoctorName(doctorName);
        return treatments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TreatmentDto> getTreatmentsByHospital(String hospitalName) {
        logger.info("Retrieving treatments by hospital: {}", hospitalName);
        List<Treatment> treatments = treatmentRepository.findByHospitalName(hospitalName);
        return treatments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TreatmentDto> getActiveTreatmentsByPatient(Long patientId) {
        logger.info("Retrieving active treatments for patient ID: {}", patientId);
        List<Treatment> treatments = treatmentRepository.findActiveTreatmentsByPatient(patientId);
        return treatments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TreatmentDto> getTreatmentsEndingBefore(LocalDateTime date) {
        logger.info("Retrieving active treatments ending before: {}", date);
        List<Treatment> treatments = treatmentRepository.findActiveTreatmentsEndingBefore(date);
        return treatments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TreatmentDto> searchTreatments(String keyword) {
        logger.info("Searching treatments by keyword: {}", keyword);
        List<Treatment> treatments = treatmentRepository.searchByKeyword(keyword);
        return treatments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TreatmentDto createTreatment(TreatmentDto treatmentDto) {
        logger.info("Creating new treatment for patient ID: {}", treatmentDto.getPatientId());
        
        Optional<Patient> patient = patientRepository.findById(treatmentDto.getPatientId());
        if (patient.isEmpty()) {
            throw new IllegalArgumentException("Patient with ID " + treatmentDto.getPatientId() + " not found");
        }

        Treatment treatment = convertToEntity(treatmentDto, patient.get());
        Treatment savedTreatment = treatmentRepository.save(treatment);
        
        logger.info("Successfully created treatment with ID: {}", savedTreatment.getId());
        return convertToDto(savedTreatment);
    }

    public TreatmentDto updateTreatment(Long id, TreatmentDto treatmentDto) {
        logger.info("Updating treatment with ID: {}", id);
        
        Optional<Treatment> existingTreatment = treatmentRepository.findById(id);
        if (existingTreatment.isEmpty()) {
            throw new IllegalArgumentException("Treatment with ID " + id + " not found");
        }

        Treatment treatment = existingTreatment.get();
        updateTreatmentFromDto(treatment, treatmentDto);
        Treatment savedTreatment = treatmentRepository.save(treatment);
        
        logger.info("Successfully updated treatment with ID: {}", savedTreatment.getId());
        return convertToDto(savedTreatment);
    }

    public void deleteTreatment(Long id) {
        logger.info("Deleting treatment with ID: {}", id);
        
        if (!treatmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Treatment with ID " + id + " not found");
        }

        treatmentRepository.deleteById(id);
        logger.info("Successfully deleted treatment with ID: {}", id);
    }

    public long getTreatmentCount() {
        return treatmentRepository.count();
    }

    public long getTreatmentCountByPatient(Long patientId) {
        return treatmentRepository.countByPatientId(patientId);
    }

    public Double getAverageTreatmentCost() {
        return treatmentRepository.findAverageTreatmentCost();
    }

    public List<TreatmentDto> getTreatmentsCreatedToday() {
        logger.info("Retrieving treatments created today");
        List<Treatment> treatments = treatmentRepository.findTreatmentsCreatedToday();
        return treatments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TreatmentDto> getTreatmentsByMedication(String medication) {
        logger.info("Retrieving treatments by medication: {}", medication);
        List<Treatment> treatments = treatmentRepository.findByMedicationContaining(medication);
        return treatments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TreatmentDto convertToDto(Treatment treatment) {
        TreatmentDto dto = new TreatmentDto();
        dto.setId(treatment.getId());
        dto.setPatientId(treatment.getPatient().getId());
        dto.setTreatmentName(treatment.getTreatmentName());
        dto.setDescription(treatment.getDescription());
        dto.setDoctorName(treatment.getDoctorName());
        dto.setHospitalName(treatment.getHospitalName());
        dto.setStartDate(treatment.getStartDate());
        dto.setEndDate(treatment.getEndDate());
        dto.setStatus(treatment.getStatus());
        dto.setMedications(treatment.getMedications());
        dto.setDosage(treatment.getDosage());
        dto.setFrequency(treatment.getFrequency());
        dto.setSideEffects(treatment.getSideEffects());
        dto.setNotes(treatment.getNotes());
        dto.setCost(treatment.getCost());
        dto.setCreatedAt(treatment.getCreatedAt());
        dto.setUpdatedAt(treatment.getUpdatedAt());
        return dto;
    }

    private Treatment convertToEntity(TreatmentDto dto, Patient patient) {
        Treatment treatment = new Treatment();
        treatment.setPatient(patient);
        treatment.setTreatmentName(dto.getTreatmentName());
        treatment.setDescription(dto.getDescription());
        treatment.setDoctorName(dto.getDoctorName());
        treatment.setHospitalName(dto.getHospitalName());
        treatment.setStartDate(dto.getStartDate());
        treatment.setEndDate(dto.getEndDate());
        treatment.setStatus(dto.getStatus() != null ? dto.getStatus() : Treatment.TreatmentStatus.ACTIVE);
        treatment.setMedications(dto.getMedications());
        treatment.setDosage(dto.getDosage());
        treatment.setFrequency(dto.getFrequency());
        treatment.setSideEffects(dto.getSideEffects());
        treatment.setNotes(dto.getNotes());
        treatment.setCost(dto.getCost());
        return treatment;
    }

    private void updateTreatmentFromDto(Treatment treatment, TreatmentDto dto) {
        if (dto.getTreatmentName() != null) treatment.setTreatmentName(dto.getTreatmentName());
        if (dto.getDescription() != null) treatment.setDescription(dto.getDescription());
        if (dto.getDoctorName() != null) treatment.setDoctorName(dto.getDoctorName());
        if (dto.getHospitalName() != null) treatment.setHospitalName(dto.getHospitalName());
        if (dto.getStartDate() != null) treatment.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) treatment.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) treatment.setStatus(dto.getStatus());
        if (dto.getMedications() != null) treatment.setMedications(dto.getMedications());
        if (dto.getDosage() != null) treatment.setDosage(dto.getDosage());
        if (dto.getFrequency() != null) treatment.setFrequency(dto.getFrequency());
        if (dto.getSideEffects() != null) treatment.setSideEffects(dto.getSideEffects());
        if (dto.getNotes() != null) treatment.setNotes(dto.getNotes());
        if (dto.getCost() != null) treatment.setCost(dto.getCost());
    }
}
