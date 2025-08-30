package com.healthcare.ingestion.repository;

import com.healthcare.ingestion.model.Patient;
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

    @Query("SELECT p FROM Patient p WHERE p.firstName LIKE %:name% OR p.lastName LIKE %:name%")
    List<Patient> findByNameContaining(@Param("name") String name);

    @Query("SELECT p FROM Patient p WHERE p.bloodType = :bloodType")
    List<Patient> findByBloodType(@Param("bloodType") String bloodType);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.gender = :gender")
    long countByGender(@Param("gender") Patient.Gender gender);

    boolean existsByEmail(String email);

    @Query("SELECT p FROM Patient p WHERE p.createdAt >= CURRENT_DATE")
    List<Patient> findPatientsCreatedToday();
}
