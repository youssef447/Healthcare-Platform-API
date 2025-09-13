package com.healthcare.dashboard.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.healthcare.dashboard.config.FeignClientConfig;

import java.util.List;
import java.util.Map;

@FeignClient(name = "patient-service-client",
        url = "${patient-management-service.url:http://localhost:8082}",
        path = "/api/patients",
        configuration = FeignClientConfig.class)
public interface PatientServiceClient {

    @GetMapping("/statistics")
    Map<String, Object> getPatientStatistics();

    @GetMapping("/{id}")
    Map<String, Object> getPatientById(@PathVariable Long id);

    @GetMapping
    List<Map<String, Object>> getAllPatients();

    @GetMapping("/allergies")
    List<Map<String, Object>> getPatientsWithAllergies();

    @GetMapping("/missing-emergency-contact")
    List<Map<String, Object>> getPatientsWithoutEmergencyContact();
}
