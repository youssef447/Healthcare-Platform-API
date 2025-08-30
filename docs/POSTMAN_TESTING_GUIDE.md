# Healthcare Management Platform - Postman Testing Guide

## 🚀 Quick Setup

### 1. Import Postman Collection & Environment

1. **Open Postman**
2. **Import Collection**:
   - Click "Import" → "Upload Files"
   - Select: `postman/Healthcare-Management-Platform.postman_collection.json`
3. **Import Environment**:
   - Click "Import" → "Upload Files" 
   - Select: `postman/Healthcare-Platform-Environment.postman_environment.json`
4. **Select Environment**: Choose "Healthcare Platform Environment" from dropdown

### 2. Start the Platform

```bash
cd D:\Healthcare-Platform-Api
docker-compose up -d
```

Wait 2-3 minutes for all services to start, then verify:
- Eureka: http://localhost:8761 (should show registered services)
- API Gateway: http://localhost:8080/actuator/health
- Dashboard: http://localhost:8084

## 🔐 Authentication Testing

### Step 1: Get JWT Token

**Request**: `Authentication → Get Keycloak Token`

This request automatically:
- Authenticates with Keycloak using admin credentials
- Extracts the JWT token from response
- Sets it as `{{jwt_token}}` variable for all subsequent requests

**Expected Response**:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIs...",
  "expires_in": 300,
  "token_type": "Bearer"
}
```

**Troubleshooting**:
- If 404: Keycloak not ready, wait 1-2 minutes
- If 401: Check username/password in request body

## 📊 Data Ingestion Testing

### Step 2: Test File Upload

**Request**: `Data Ingestion Service → Upload CSV File`

1. **Attach CSV File**:
   - In request body, click "Select Files" for `file` parameter
   - Choose: `test-data/patients_sample.csv`
   - Set `type` parameter to: `patients`

2. **Send Request**

**Expected Response**:
```json
{
  "message": "File processed successfully",
  "recordsProcessed": 15,
  "errors": []
}
```

### Step 3: Create Individual Patient

**Request**: `Data Ingestion Service → Create Patient`

Uses pre-filled JSON body with sample patient data.

**Expected Response**:
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "status": "ACTIVE"
}
```

### Step 4: Create Medical Record

**Request**: `Data Ingestion Service → Create Medical Record`

Links to patient ID 1 created in previous step.

## 👥 Patient Management Testing

### Step 5: Retrieve All Patients

**Request**: `Patient Management Service → Patients → Get All Patients`

**Expected Response**: Array of patient objects with full details

### Step 6: Search Patients

**Request**: `Patient Management Service → Patients → Search Patients`

Tests search functionality with query parameters.

### Step 7: Get Patient Statistics

**Request**: `Patient Management Service → Patients → Get Patient Statistics`

**Expected Response**:
```json
{
  "totalPatients": 15,
  "activePatients": 15,
  "malePatients": 8,
  "femalePatients": 7,
  "patientsCreatedToday": 15
}
```

## 💊 Treatment Management Testing

### Step 8: Create Treatment

**Request**: `Patient Management Service → Treatments → Create Treatment`

Creates a treatment for patient ID 1.

### Step 9: Get All Treatments

**Request**: `Patient Management Service → Treatments → Get All Treatments`

### Step 10: Get Treatment Statistics

**Request**: `Patient Management Service → Treatments → Get Treatment Statistics`

## 📈 Dashboard & Analytics Testing

### Step 11: Get Dashboard Data

**Request**: `Medical Dashboard Service → Dashboard → Get Dashboard Data`

**Expected Response**:
```json
{
  "patientStatistics": {
    "totalPatients": 15,
    "activePatients": 15,
    "newPatientsToday": 15
  },
  "treatmentStatistics": {
    "totalTreatments": 15,
    "newTreatmentsToday": 5
  },
  "chartData": {...},
  "recentActivities": [...]
}
```

### Step 12: Get Analytics

**Requests**:
- `Medical Dashboard Service → Dashboard → Get Patient Analytics`
- `Medical Dashboard Service → Dashboard → Get Treatment Analytics`

## 📋 Report Generation Testing

### Step 13: Generate Patient Summary Report

**Request**: `Medical Dashboard Service → Reports → Generate Patient Summary Report`

**Expected Response**:
```json
{
  "id": "uuid-string",
  "title": "Monthly Patient Summary - January 2024",
  "type": "PATIENT_SUMMARY",
  "status": "COMPLETED",
  "data": {...}
}
```

### Step 14: Get All Reports

**Request**: `Medical Dashboard Service → Reports → Get All Reports`

### Step 15: Generate Treatment Analysis Report

**Request**: `Medical Dashboard Service → Reports → Generate Treatment Analysis Report`

## 🔍 Service Discovery Testing

### Step 16: Check Eureka Status

**Request**: `Service Discovery → Eureka Server Status`

Should show Eureka dashboard in HTML format.

### Step 17: View Registered Services

**Request**: `Service Discovery → Registered Services`

**Expected Response**: XML showing all registered microservices.

## 📝 Complete Testing Workflow

### Full End-to-End Test Sequence

1. **Authentication** → Get Keycloak Token ✅
2. **Data Ingestion** → Upload CSV File ✅
3. **Data Ingestion** → Create Patient ✅
4. **Data Ingestion** → Create Medical Record ✅
5. **Patient Management** → Get All Patients ✅
6. **Patient Management** → Get Patient Statistics ✅
7. **Patient Management** → Create Treatment ✅
8. **Patient Management** → Get Treatment Statistics ✅
9. **Dashboard** → Get Dashboard Data ✅
10. **Dashboard** → Get Patient Analytics ✅
11. **Reports** → Generate Patient Summary Report ✅
12. **Reports** → Get All Reports ✅

### Batch Testing with Postman Runner

1. **Select Collection**: Healthcare Management Platform API
2. **Click "Run"** → Select all requests
3. **Set Environment**: Healthcare Platform Environment
4. **Run Collection**: Execute all tests in sequence

## 🧪 Test Data Overview

### Patients Sample (15 records)
- **File**: `test-data/patients_sample.csv`
- **Contains**: Complete patient demographics, contact info, medical history
- **Blood Types**: A+, A-, B+, B-, AB+, AB-, O+, O-
- **Genders**: Male (8), Female (7)
- **Age Range**: 29-49 years old

### Medical Records Sample (15 records)
- **File**: `test-data/medical_records_sample.csv`
- **Types**: CONSULTATION, LAB_RESULTS, IMAGING, PRESCRIPTION
- **Covers**: Various medical conditions and treatments
- **Date Range**: January 2024

### Treatments Sample (15 records)
- **File**: `test-data/treatments_sample.csv`
- **Types**: MEDICATION, THERAPY, SURGERY, CONSULTATION
- **Statuses**: ACTIVE, COMPLETED
- **Cost Range**: $80 - $5,500

## 🔧 Troubleshooting

### Common Issues & Solutions

| Issue | Symptom | Solution |
|-------|---------|----------|
| **401 Unauthorized** | All API calls fail | Re-run "Get Keycloak Token" |
| **404 Not Found** | Service endpoints not found | Check if all services are running |
| **500 Internal Error** | Database connection issues | Verify PostgreSQL is running |
| **Connection Refused** | Cannot reach services | Check Docker containers are up |

### Service Health Checks

Before testing, verify all services are healthy:

```bash
# Check all containers
docker-compose ps

# Check specific service logs
docker-compose logs -f patient-management-service

# Test health endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8081/health
curl http://localhost:8082/health
curl http://localhost:8084/api/dashboard/health
```

### Environment Variables

Verify these are set correctly in Postman environment:

| Variable | Value | Purpose |
|----------|-------|---------|
| `base_url` | http://localhost:8080 | API Gateway URL |
| `keycloak_url` | http://localhost:8180 | Keycloak server |
| `jwt_token` | (auto-set) | Authentication token |

## 📊 Expected Test Results

### Success Metrics

After running the complete test suite, you should see:

- **✅ 20+ API endpoints tested successfully**
- **✅ 15 patients imported via CSV**
- **✅ 15 medical records created**
- **✅ 15 treatments tracked**
- **✅ Dashboard analytics populated**
- **✅ Reports generated successfully**

### Performance Benchmarks

| Operation | Expected Response Time |
|-----------|----------------------|
| Authentication | < 500ms |
| Patient CRUD | < 200ms |
| File Upload (15 records) | < 2s |
| Dashboard Data | < 1s |
| Report Generation | < 3s |

## 🎯 Next Steps

1. **Run the complete test suite** using Postman Runner
2. **Explore the web dashboard** at http://localhost:8084
3. **Test with your own data** by modifying the CSV files
4. **Customize the platform** for your specific healthcare needs
5. **Set up monitoring** and alerting for production use

This testing guide ensures comprehensive validation of all Healthcare Management Platform features and provides a solid foundation for further development and customization.
