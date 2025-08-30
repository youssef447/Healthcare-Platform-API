package com.healthcare.ingestion.repository;

import com.healthcare.ingestion.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatientId(Long patientId);

    List<MedicalRecord> findByPatientIdAndStatus(Long patientId, MedicalRecord.RecordStatus status);

    List<MedicalRecord> findByRecordType(String recordType);

    List<MedicalRecord> findByDoctorName(String doctorName);

    List<MedicalRecord> findByHospitalName(String hospitalName);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.visitDate BETWEEN :startDate AND :endDate")
    List<MedicalRecord> findByVisitDateBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patient.id = :patientId AND mr.visitDate >= :fromDate ORDER BY mr.visitDate DESC")
    List<MedicalRecord> findRecentRecordsByPatient(@Param("patientId") Long patientId, 
                                                   @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.followUpDate <= :date AND mr.status = 'ACTIVE'")
    List<MedicalRecord> findRecordsNeedingFollowUp(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(mr) FROM MedicalRecord mr WHERE mr.patient.id = :patientId")
    long countByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.createdAt >= CURRENT_DATE")
    List<MedicalRecord> findRecordsCreatedToday();

    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.description LIKE %:keyword% OR mr.diagnosis LIKE %:keyword%")
    List<MedicalRecord> searchByKeyword(@Param("keyword") String keyword);
}
