package com.healthcare.dashboard.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.healthcare.dashboard.config.FeignClientConfig;

import java.util.List;
import java.util.Map;

@FeignClient(name = "treatment-service-client", 
             url = "${patient-management-service.url:http://localhost:8082}", 
             path = "/api/treatments",
             configuration = FeignClientConfig.class)
public interface TreatmentServiceClient {

    @GetMapping("/statistics")
    Map<String, Object> getTreatmentStatistics();

    @GetMapping("/{id}")
    Map<String, Object> getTreatmentById(@PathVariable Long id);

    @GetMapping
    List<Map<String, Object>> getAllTreatments();

    @GetMapping("/patient/{patientId}")
    List<Map<String, Object>> getTreatmentsByPatientId(@PathVariable Long patientId);

    @GetMapping("/patient/{patientId}/active")
    List<Map<String, Object>> getActiveTreatmentsByPatient(@PathVariable Long patientId);
}
