package com.healthcare.patient.repository;

import com.healthcare.patient.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByEmail(String email);

    List<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    List<Patient> findByDateOfBirth(LocalDate dateOfBirth);

    List<Patient> findByGender(Patient.Gender gender);

    List<Patient> findByStatus(Patient.PatientStatus status);

    @Query("SELECT p FROM Patient p WHERE p.firstName LIKE %:name% OR p.lastName LIKE %:name%")
    List<Patient> findByNameContaining(@Param("name") String name);

    @Query("SELECT p FROM Patient p WHERE p.bloodType = :bloodType")
    List<Patient> findByBloodType(@Param("bloodType") String bloodType);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.gender = :gender")
    long countByGender(@Param("gender") Patient.Gender gender);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.status = :status")
    long countByStatus(@Param("status") Patient.PatientStatus status);

    boolean existsByEmail(String email);

    @Query("SELECT p FROM Patient p WHERE p.createdAt >= CURRENT_DATE")
    List<Patient> findPatientsCreatedToday();

    @Query("SELECT p FROM Patient p WHERE p.dateOfBirth BETWEEN :startDate AND :endDate")
    List<Patient> findByDateOfBirthBetween(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Patient p WHERE p.allergies IS NOT NULL AND p.allergies != ''")
    List<Patient> findPatientsWithAllergies();

    @Query("SELECT p FROM Patient p WHERE p.emergencyContact IS NULL OR p.emergencyContact = ''")
    List<Patient> findPatientsWithoutEmergencyContact();
}
