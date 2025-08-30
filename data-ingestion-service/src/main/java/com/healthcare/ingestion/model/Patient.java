package com.healthcare.ingestion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patients")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)

    private Gender gender;

    @Email(message = "Email should be valid")
    @Column(unique = true)
    private String email;


    private String phoneNumber;


    private String address;


    private String emergencyContact;


    private String emergencyPhone;


    private String insuranceNumber;


    private String bloodType;


    private String allergies;


    private String medicalHistory;


    private LocalDateTime createdAt;


    private LocalDateTime updatedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors


}
