# Healthcare Management Platform - Complete Application Guide

## 🏥 Overview

The Healthcare Management Platform is a comprehensive, enterprise-grade microservices-based system designed for modern healthcare organizations. It provides secure, scalable, and HIPAA-compliant solutions for patient management, medical data processing, and healthcare analytics.

## 🎯 What This Application Does

### Core Functionality

1. **Patient Management**
   - Complete patient lifecycle management (CRUD operations)
   - Secure patient data storage with encryption
   - Medical history tracking and allergies management
   - Emergency contact information management
   - Advanced search and filtering capabilities

2. **Medical Data Ingestion**
   - Bulk import of patient data via CSV/JSON files
   - Real-time data validation and processing
   - Event-driven architecture with Kafka messaging
   - Automated data transformation and normalization

3. **Treatment Management**
   - Comprehensive treatment tracking and monitoring
   - Medication management and prescription tracking
   - Treatment outcome analysis and reporting
   - Cost tracking and financial analytics

4. **Medical Dashboard & Analytics**
   - Real-time healthcare analytics and insights
   - Interactive charts and data visualizations
   - Comprehensive reporting system
   - Executive dashboards for decision making

5. **Security & Compliance**
   - OAuth2/JWT authentication via Keycloak
   - Role-based access control (RBAC)
   - HIPAA-compliant data handling
   - Audit trails and access logging

## 🏗️ Architecture Overview

### Microservices Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Browser   │    │   Mobile App    │    │  External APIs  │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────┴─────────────┐
                    │      API Gateway          │
                    │   (Port 8080)            │
                    └─────────────┬─────────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
┌─────────┴─────────┐  ┌─────────┴─────────┐  ┌─────────┴─────────┐
│ Data Ingestion    │  │ Patient Mgmt      │  │ Medical Dashboard │
│ Service           │  │ Service           │  │ Service           │
│ (Port 8081)       │  │ (Port 8082)       │  │ (Port 8084)       │
└─────────┬─────────┘  └─────────┬─────────┘  └─────────┬─────────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────┴─────────────┐
                    │   Eureka Server           │
                    │   (Port 8761)            │
                    └───────────────────────────┘
```

### Infrastructure Components

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   PostgreSQL    │  │     Kafka       │  │    Keycloak     │
│   Database      │  │   Messaging     │  │  Authentication │
│   (Port 5432)   │  │   (Port 9092)   │  │   (Port 8180)   │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

## 🚀 Getting Started

### Prerequisites

- **Java 17+** - Required for all services
- **Docker & Docker Compose** - For containerized deployment
- **Maven 3.6+** - For building the services
- **Postman** - For API testing (collection provided)

### Quick Start Guide

1. **Clone and Navigate**
   ```bash
   cd D:\Healthcare-Platform-Api
   ```

2. **Start Infrastructure Services**
   ```bash
   docker-compose up -d postgres kafka zookeeper keycloak
   ```

3. **Build All Services**
   ```bash
   # Build each service
   cd eureka-server && mvn clean package -DskipTests
   cd ../api-gateway && mvn clean package -DskipTests
   cd ../data-ingestion-service && mvn clean package -DskipTests
   cd ../patient-management-service && mvn clean package -DskipTests
   cd ../medical-dashboard-service && mvn clean package -DskipTests
   ```

4. **Start All Services**
   ```bash
   docker-compose up -d
   ```

5. **Verify Deployment**
   - Eureka Server: http://localhost:8761
   - API Gateway: http://localhost:8080
   - Medical Dashboard: http://localhost:8084
   - Keycloak Admin: http://localhost:8180/admin

## 🔐 Security & Authentication

### User Roles & Permissions

| Role | Permissions | Access Level |
|------|-------------|--------------|
| **ADMIN** | Full system access, user management, all reports | Complete |
| **DOCTOR** | Patient management, treatments, medical reports | High |
| **NURSE** | Patient viewing, basic treatments, limited reports | Medium |
| **PATIENT** | Own data viewing only | Limited |

### Default Test Users

| Username | Password | Role | Description |
|----------|----------|------|-------------|
| admin | admin123 | ADMIN | System administrator |
| doctor | doctor123 | DOCTOR | Medical practitioner |
| nurse | nurse123 | NURSE | Healthcare staff |

### Authentication Flow

1. **Get JWT Token**
   ```bash
   POST /realms/healthcare/protocol/openid-connect/token
   Content-Type: application/x-www-form-urlencoded
   
   grant_type=password&client_id=healthcare-client&username=admin&password=admin123
   ```

2. **Use Token in Requests**
   ```bash
   Authorization: Bearer <jwt_token>
   ```

## 📊 API Endpoints

### Service Ports

| Service | Port | Base URL | Purpose |
|---------|------|----------|---------|
| API Gateway | 8080 | http://localhost:8080 | Main entry point |
| Eureka Server | 8761 | http://localhost:8761 | Service discovery |
| Data Ingestion | 8081 | http://localhost:8081 | Direct access (dev) |
| Patient Management | 8082 | http://localhost:8082 | Direct access (dev) |
| Medical Dashboard | 8084 | http://localhost:8084 | Web interface |
| Keycloak | 8180 | http://localhost:8180 | Authentication |

### Key API Endpoints

#### Data Ingestion Service
```
POST /data-ingestion/api/ingestion/upload          # Upload CSV/JSON files
POST /data-ingestion/api/ingestion/patients        # Create patient
POST /data-ingestion/api/ingestion/medical-records # Create medical record
GET  /data-ingestion/health                        # Health check
```

#### Patient Management Service
```
GET    /patient-management/api/patients            # Get all patients
GET    /patient-management/api/patients/{id}       # Get patient by ID
POST   /patient-management/api/patients            # Create patient
PUT    /patient-management/api/patients/{id}       # Update patient
DELETE /patient-management/api/patients/{id}       # Delete patient
GET    /patient-management/api/patients/search     # Search patients
GET    /patient-management/api/patients/statistics # Patient statistics

GET    /patient-management/api/treatments          # Get all treatments
POST   /patient-management/api/treatments          # Create treatment
GET    /patient-management/api/treatments/{id}     # Get treatment by ID
PUT    /patient-management/api/treatments/{id}     # Update treatment
GET    /patient-management/api/treatments/patient/{id} # Get patient treatments
```

#### Medical Dashboard Service
```
GET  /medical-dashboard/api/dashboard/data         # Dashboard data
GET  /medical-dashboard/api/dashboard/analytics/patients    # Patient analytics
GET  /medical-dashboard/api/dashboard/analytics/treatments  # Treatment analytics

GET  /medical-dashboard/api/reports                # Get all reports
POST /medical-dashboard/api/reports                # Generate report
GET  /medical-dashboard/api/reports/{id}           # Get report by ID
DELETE /medical-dashboard/api/reports/{id}         # Delete report
```

## 🧪 Testing with Postman

### Import Collections

1. **Import Collection**
   - File: `postman/Healthcare-Management-Platform.postman_collection.json`
   - Contains all API endpoints with sample requests

2. **Import Environment**
   - File: `postman/Healthcare-Platform-Environment.postman_environment.json`
   - Pre-configured variables for all services

### Test Data Files

Located in `test-data/` directory:

- **patients_sample.csv** - 15 sample patients with complete medical information
- **medical_records_sample.csv** - 15 sample medical records with various types
- **treatments_sample.csv** - 15 sample treatments with different statuses

### Testing Workflow

1. **Authentication**
   ```
   Run: Authentication → Get Keycloak Token
   This sets the JWT token for all subsequent requests
   ```

2. **Data Ingestion**
   ```
   Run: Data Ingestion Service → Upload CSV File
   Upload: test-data/patients_sample.csv
   ```

3. **Patient Management**
   ```
   Run: Patient Management → Get All Patients
   Run: Patient Management → Create Patient
   Run: Patient Management → Get Patient Statistics
   ```

4. **Dashboard & Reports**
   ```
   Run: Medical Dashboard → Get Dashboard Data
   Run: Medical Dashboard → Generate Patient Summary Report
   ```

## 💾 Database Schema

### Core Tables

#### Patients Table
```sql
CREATE TABLE patients (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    blood_type VARCHAR(5),
    allergies TEXT,
    medical_history TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Medical Records Table
```sql
CREATE TABLE medical_records (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT REFERENCES patients(id),
    record_type VARCHAR(50) NOT NULL,
    diagnosis TEXT,
    treatment TEXT,
    medications TEXT,
    notes TEXT,
    doctor_name VARCHAR(100),
    record_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Treatments Table
```sql
CREATE TABLE treatments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT REFERENCES patients(id),
    type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    doctor_name VARCHAR(100),
    start_date DATE,
    end_date DATE,
    medications TEXT,
    cost DECIMAL(10,2),
    notes TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🔄 Event-Driven Architecture

### Kafka Topics

| Topic | Purpose | Producers | Consumers |
|-------|---------|-----------|-----------|
| patient-events | Patient lifecycle events | Data Ingestion | Patient Management |
| medical-record-events | Medical record events | Data Ingestion | Patient Management |
| ingestion-events | File processing events | Data Ingestion | Patient Management |
| treatment-events | Treatment updates | Patient Management | Dashboard |

### Event Types

```json
{
  "eventType": "PATIENT_CREATED",
  "eventId": "uuid",
  "timestamp": "2024-01-27T10:30:00Z",
  "source": "data-ingestion-service",
  "data": {
    "patientId": 123,
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

## 📈 Monitoring & Health Checks

### Health Endpoints

All services expose health check endpoints:

```
GET /health           # Basic health status
GET /actuator/health  # Detailed health information
GET /actuator/info    # Service information
GET /actuator/metrics # Service metrics
```

### Service Discovery

Monitor registered services via Eureka:
```
GET http://localhost:8761/eureka/apps
```

## 🐳 Docker Deployment

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| SPRING_PROFILES_ACTIVE | local | Active Spring profile |
| DB_HOST | localhost | Database host |
| DB_PORT | 5432 | Database port |
| DB_NAME | healthcare_db | Database name |
| DB_USERNAME | postgres | Database username |
| DB_PASSWORD | STILLSTRONG2333 | Database password |
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka servers |
| KEYCLOAK_URL | http://localhost:8180 | Keycloak URL |
| EUREKA_URL | http://localhost:8761 | Eureka server URL |

### Docker Commands

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f [service-name]

# Scale services
docker-compose up -d --scale patient-management-service=2

# Stop all services
docker-compose down

# Rebuild and restart
docker-compose up -d --build
```

## 🔧 Configuration

### Application Profiles

- **local** - Development with local database and Kafka
- **docker** - Containerized deployment
- **prod** - Production configuration (to be customized)

### Key Configuration Files

- `application.yml` - Main configuration for each service
- `docker-compose.yml` - Container orchestration
- `pom.xml` - Maven dependencies and build configuration

## 📋 Troubleshooting

### Common Issues

1. **Service Discovery Issues**
   ```
   Problem: Services not registering with Eureka
   Solution: Check Eureka server is running on port 8761
   ```

2. **Authentication Failures**
   ```
   Problem: JWT token invalid or expired
   Solution: Re-authenticate via Keycloak token endpoint
   ```

3. **Database Connection Issues**
   ```
   Problem: Cannot connect to PostgreSQL
   Solution: Verify database is running and credentials are correct
   ```

4. **Kafka Connection Issues**
   ```
   Problem: Cannot connect to Kafka broker
   Solution: Ensure Kafka and Zookeeper are running
   ```

### Logs Location

```bash
# Docker logs
docker-compose logs -f [service-name]

# Application logs (if running locally)
tail -f logs/application.log
```

## 🚀 Production Deployment

### Pre-Production Checklist

- [ ] Update default passwords and secrets
- [ ] Configure SSL/TLS certificates
- [ ] Set up proper database backups
- [ ] Configure monitoring and alerting
- [ ] Review and update security configurations
- [ ] Set up log aggregation
- [ ] Configure auto-scaling policies
- [ ] Test disaster recovery procedures

### Security Hardening

1. **Change Default Credentials**
2. **Enable HTTPS/SSL**
3. **Configure Firewall Rules**
4. **Set up Database Encryption**
5. **Enable Audit Logging**
6. **Configure Rate Limiting**

## 📚 Additional Resources

### API Documentation

- Swagger UI available at: `http://localhost:8080/swagger-ui.html`
- OpenAPI specs available at: `http://localhost:8080/v3/api-docs`

### Development Tools

- **Postman Collection**: Complete API testing suite
- **Docker Compose**: Local development environment
- **Maven**: Build and dependency management
- **Spring Boot DevTools**: Hot reloading during development

### Support & Maintenance

- **Logs**: Centralized logging via Docker Compose
- **Metrics**: Actuator endpoints for monitoring
- **Health Checks**: Automated service health monitoring
- **Database Migrations**: Flyway or Liquibase (to be implemented)

---

## 🎯 Next Steps

1. **Import Postman collection and environment**
2. **Start the platform using Docker Compose**
3. **Test authentication and basic operations**
4. **Upload sample data using provided CSV files**
5. **Explore the medical dashboard interface**
6. **Generate sample reports**
7. **Customize for your specific healthcare needs**

This Healthcare Management Platform provides a solid foundation for modern healthcare organizations, with enterprise-grade security, scalability, and compliance features built-in.
