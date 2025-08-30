package com.healthcare.patient.repository;

import com.healthcare.patient.model.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    List<Treatment> findByPatientId(Long patientId);

    List<Treatment> findByPatientIdAndStatus(Long patientId, Treatment.TreatmentStatus status);

    List<Treatment> findByStatus(Treatment.TreatmentStatus status);

    List<Treatment> findByDoctorName(String doctorName);

    List<Treatment> findByHospitalName(String hospitalName);

    @Query("SELECT t FROM Treatment t WHERE t.startDate BETWEEN :startDate AND :endDate")
    List<Treatment> findByStartDateBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Treatment t WHERE t.endDate <= :date AND t.status = 'ACTIVE'")
    List<Treatment> findActiveTreatmentsEndingBefore(@Param("date") LocalDateTime date);

    @Query("SELECT t FROM Treatment t WHERE t.patient.id = :patientId AND t.status = 'ACTIVE'")
    List<Treatment> findActiveTreatmentsByPatient(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(t) FROM Treatment t WHERE t.patient.id = :patientId")
    long countByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT t FROM Treatment t WHERE t.createdAt >= CURRENT_DATE")
    List<Treatment> findTreatmentsCreatedToday();

    @Query("SELECT t FROM Treatment t WHERE t.treatmentName LIKE %:keyword% OR t.description LIKE %:keyword%")
    List<Treatment> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT AVG(t.cost) FROM Treatment t WHERE t.cost IS NOT NULL")
    Double findAverageTreatmentCost();

    @Query("SELECT t FROM Treatment t WHERE t.medications LIKE %:medication%")
    List<Treatment> findByMedicationContaining(@Param("medication") String medication);
}
