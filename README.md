# Healthcare Management Platform

A comprehensive microservices-based healthcare management system for processing Electronic Health Records (EHRs) and medical data.

## Architecture Overview

The platform consists of 5 microservices:

1. **Eureka Server** - Service discovery and registration
2. **API Gateway** - Single entry point with routing and security
3. **Data Ingestion Service** - Processes medical files (CSV/JSON), stores in PostgreSQL, publishes to Kafka
4. **Patient Management Service** - Core patient data management with REST APIs
5. **Medical Dashboard Service** - Web interface for patient records and medical reporting

## Technology Stack

- **Java 17**
- **Spring Boot 3.4.3**
- **Spring Cloud**
- **Spring Data JPA & Hibernate**
- **PostgreSQL**
- **Apache Kafka**
- **Keycloak** (Authentication & Authorization)
- **Docker & Docker Compose**
- **Maven**

## Features

- Multi-format medical data processing (CSV, JSON)
- Real-time patient status updates via Kafka
- HIPAA compliance with role-based access control
- Medical analytics and reporting
- Secure REST APIs with JWT authentication
- Microservices architecture with service discovery


## Service Ports

- Eureka Server: 8761
- API Gateway: 8080
- Data Ingestion Service: 8081
- Patient Management Service: 8082
- Medical Dashboard Service: 8085
- PostgreSQL: 5432
- Kafka: 9092
- Keycloak: 8180


## Security

The platform uses Keycloak for authentication and authorization with the following roles:
- `ADMIN` - Full system access
- `DOCTOR` - Patient data access and medical records
- `NURSE` - Limited patient data access
- `PATIENT` - Own data access only

## API Documentation

Swagger UI is available for each service:
- Data Ingestion: http://localhost:8081/swagger-ui.html
- Patient Management: http://localhost:8082/swagger-ui.html
