package com.healthcare.patient.repository;

import com.healthcare.patient.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByPatientIdAndStatus(Long patientId, Appointment.AppointmentStatus status);

    List<Appointment> findByStatus(Appointment.AppointmentStatus status);

    List<Appointment> findByDoctorName(String doctorName);

    List<Appointment> findByDepartment(String department);

    List<Appointment> findByHospitalName(String hospitalName);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByAppointmentDateBetween(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate >= :fromDate AND a.appointmentDate <= :toDate AND a.status = 'SCHEDULED'")
    List<Appointment> findScheduledAppointmentsBetween(@Param("fromDate") LocalDateTime fromDate, 
                                                       @Param("toDate") LocalDateTime toDate);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate >= :fromDate ORDER BY a.appointmentDate ASC")
    List<Appointment> findUpcomingAppointmentsByPatient(@Param("patientId") Long patientId, 
                                                        @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId")
    long countByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT a FROM Appointment a WHERE a.createdAt >= CURRENT_DATE")
    List<Appointment> findAppointmentsCreatedToday();

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate <= :reminderTime AND a.reminderSent = false AND a.status = 'SCHEDULED'")
    List<Appointment> findAppointmentsNeedingReminder(@Param("reminderTime") LocalDateTime reminderTime);

    @Query("SELECT a FROM Appointment a WHERE a.doctorName = :doctorName AND a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findDoctorAppointmentsBetween(@Param("doctorName") String doctorName,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status AND a.appointmentDate >= CURRENT_DATE")
    long countByStatusAndAppointmentDateAfter(@Param("status") Appointment.AppointmentStatus status);
}
