# MOH Connect - Backend API Documentation

## Table of Contents
1. [Authentication Endpoints](#authentication-endpoints)
2. [Mother Management Endpoints](#mother-management-endpoints)
3. [Pregnancy Endpoints](#pregnancy-endpoints)
4. [Baby Endpoints](#baby-endpoints)
5. [Mother Record Endpoints](#mother-record-endpoints)
6. [Baby Record Endpoints](#baby-record-endpoints)
7. [Vaccination Endpoints](#vaccination-endpoints)
   - [Vaccine Schedule](#vaccine-schedule-endpoints)
   - [Mother Vaccination](#mother-vaccination-endpoints)
   - [Baby Vaccination](#baby-vaccination-endpoints)
8. [Notification Endpoints](#notification-endpoints)
   - [Notification Types](#notification-type-endpoints)
   - [Notifications](#notification-crud-endpoints)
9. [Session Endpoints](#session-endpoints)
   - [Session Types](#session-type-endpoints)
   
   - [Session Attendance](#session-attendance-endpoints)
10. [Triposha Distribution Endpoints](#triposha-distribution-endpoints)
    - [Triposha Stock](#triposha-stock-endpoints)
    - [Mother Triposha Distribution](#mother-triposha-distribution-endpoints)
    - [Baby Triposha Distribution](#baby-triposha-distribution-endpoints)

---

## Authentication Endpoints

### 1. Register User
**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "username": "string (required)",
  "email": "string (required, valid email)",
  "password": "string (required)",
  "role": "ADMIN | MIDWIFE | DOCTOR | MOTHER (required)",
  "doctorDetails": {
    "name": "string (required for DOCTOR)",
    "specialization": "string",
    "contactNumber": "string",
    "email": "string",
    "licenseNumber": "string (required for DOCTOR)"
  },
  "midwifeDetails": {
    "name": "string (required for MIDWIFE)",
    "phmAreaId": "integer (required for MIDWIFE)",
    "contactNumber": "string",
    "email": "string",
    "assignmentDate": "string (optional)",
    "qualifications": "string"
  },
  "wifeNic": "string (optional, for MOTHER only)"
}
```

**Examples:**
- **Admin:**
```json
{
  "username": "admin_colombo",
  "email": "admin@moh.gov.lk",
  "password": "AdminSecure@123",
  "role": "ADMIN"
}
```
- **Doctor:**
```json
{
  "username": "dr_fernando",
  "email": "fernando@moh.gov.lk",
  "password": "DoctorPass@123",
  "role": "DOCTOR",
  "doctorDetails": {
    "name": "Dr. Sunil Fernando",
    "specialization": "Obstetrics and Gynecology",
    "contactNumber": "0771234568",
    "email": "fernando@moh.gov.lk",
    "licenseNumber": "SLMC12345"
  }
}
```
- **Midwife:**
```json
{
  "username": "midwife_nimal",
  "email": "nimal@moh.gov.lk",
  "password": "MidwifePass@123",
  "role": "MIDWIFE",
  "midwifeDetails": {
    "name": "Nimal Perera",
    "phmAreaId": 1,
    "contactNumber": "0771234567",
    "email": "nimal@moh.gov.lk",
    "assignmentDate": "2024-01-15",
    "qualifications": "Diploma in Midwifery, BSc Nursing"
  }
}
```

**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin_colombo",
  "role": "ADMIN"
}
```

**Error Responses:**
- `400 Bad Request`: Validation errors or duplicate username/email

---

### 2. Login
**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "string (required, user email)",
  "password": "string (required)"
}
```

**Example:**
```json
{
  "email": "priya@example.com",
  "password": "MotherPass@123"
}
```

**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "priya_silva",
  "role": "MOTHER"
}
```

**Error Responses:**
- `401 Unauthorized`: Invalid email or password

---

## Mother Management Endpoints

### 3. Register Mother (Midwife Only)
**Endpoint:** `POST /api/mothers/register`

**Headers:**
- `Authorization: Bearer <midwife_token>` (required)
- `Content-Type: application/json`

**Request Body:**
```json
{
  "username": "string (required)",
  "email": "string (required, valid email)",
  "password": "string (required)",
  "nic": "string (required, unique)",
  "name": "string (required)",
  "phmAreaId": "integer (required, must match midwife's PHM area)",
  "dateOfBirth": "string (YYYY-MM-DD format)",
  "occupation": "string",
  "contactNumber": "string",
  "bloodGroup": "string",
  "address": "string",
  "registrationDate": "string (YYYY-MM-DD format)"
}
```

**Example:**
```json
{
  "username": "priya_silva",
  "email": "priya@example.com",
  "password": "MotherPass@123",
  "nic": "856234567V",
  "name": "Priya Silva",
  "phmAreaId": 1,
  "dateOfBirth": "1990-05-10",
  "occupation": "Teacher",
  "contactNumber": "0771234567",
  "bloodGroup": "A+",
  "address": "123 Main Street, Colombo",
  "registrationDate": "2025-12-06"
}
```

**Success Response (200 OK):**
```json
"Mother registered"
```

**Error Responses:**
- `401 Unauthorized`: Missing or invalid Bearer token
- `403 Forbidden`: Only midwives can register mothers
- `400 Bad Request`: NIC/username/email already exists, or midwife not assigned to this PHM area

---

## Pregnancy Endpoints

### 4. Create Pregnancy (Midwife/Doctor Only)
**Endpoint:** `POST /api/pregnancies?motherId={motherId}`

**Headers:**
- `Authorization: Bearer <midwife_or_doctor_token>` (required)

**Request Body:**
```json
{
  "pregnancyNumber": "string (unique, required)",
  "lmpDate": "string (YYYY-MM-DD, required)",
  "eddDate": "string (YYYY-MM-DD, required)",
  "deliveryDate": "string (YYYY-MM-DD, optional)",
  "deliveryType": "NORMAL | CESAREAN | ASSISTED | null",
  "pregnancyStatus": "ACTIVE | COMPLETED | TERMINATED",
  "gravida": "integer (default: 1)",
  "para": "integer (default: 0)",
  "riskLevel": "LOW | MEDIUM | HIGH",
  "riskFactors": "string (text)"
}
```

**Example:**
```json
{
  "pregnancyNumber": "PRG-2026-001",
  "lmpDate": "2025-11-01",
  "eddDate": "2026-08-08",
  "pregnancyStatus": "ACTIVE",
  "gravida": 2,
  "para": 1,
  "riskLevel": "LOW",
  "riskFactors": "None identified"
}
```

**Success Response (201 Created):**
```json
{
  "pregnancyId": 1,
  "mother": { /* mother object */ },
  "pregnancyNumber": "PRG-2026-001",
  "lmpDate": "2025-11-01",
  "eddDate": "2026-08-08",
  "deliveryDate": null,
  "deliveryType": null,
  "pregnancyStatus": "ACTIVE",
  "gravida": 2,
  "para": 1,
  "riskLevel": "LOW",
  "riskFactors": "None identified",
  "createdAt": "2026-02-09T10:30:00"
}
```

**Error Responses:**
- `401 Unauthorized`: Missing or invalid Bearer token
- `403 Forbidden`: Only midwives and doctors can create pregnancies
- `400 Bad Request`: Mother not found or validation errors

---

### 5. Get Pregnancy by ID
**Endpoint:** `GET /api/pregnancies/{pregnancyId}`

**Headers:**
- `Authorization: Bearer <token>` (required)

**Success Response (200 OK):**
```json
{
  "pregnancyId": 1,
  "mother": { /* mother object */ },
  "pregnancyNumber": "PRG-2026-001",
  "lmpDate": "2025-11-01",
  "eddDate": "2026-08-08",
  "deliveryDate": null,
  "deliveryType": null,
  "pregnancyStatus": "ACTIVE",
  "gravida": 2,
  "para": 1,
  "riskLevel": "LOW",
  "riskFactors": "None identified",
  "createdAt": "2026-02-09T10:30:00"
}
```

**Error Responses:**
- `401 Unauthorized`: Missing or invalid Bearer token
- `404 Not Found`: Pregnancy not found

---

### 6. Get Pregnancies by Mother ID
**Endpoint:** `GET /api/pregnancies/mother/{motherId}`

**Headers:**
- `Authorization: Bearer <token>` (required)

**Success Response (200 OK):**
```json
[
  {
    "pregnancyId": 1,
    "mother": { /* mother object */ },
    "pregnancyNumber": "PRG-2026-001",
    "lmpDate": "2025-11-01",
    "eddDate": "2026-08-08",
    "pregnancyStatus": "ACTIVE",
    "gravida": 2,
    "para": 1,
    "riskLevel": "LOW"
  }
]
```

---

### 7. Get All Active Pregnancies (Midwife/Doctor Only)
**Endpoint:** `GET /api/pregnancies/active`

**Headers:**
- `Authorization: Bearer <midwife_or_doctor_token>` (required)

**Success Response (200 OK):**
```json
[
  {
    "pregnancyId": 1,
    "mother": { /* mother object */ },
    "pregnancyNumber": "PRG-2026-001",
    "pregnancyStatus": "ACTIVE",
    "eddDate": "2026-08-08"
  }
]
```

**Error Responses:**
- `403 Forbidden`: Only midwives and doctors can view all active pregnancies

---

### 8. Update Pregnancy (Midwife/Doctor Only)
**Endpoint:** `PUT /api/pregnancies/{pregnancyId}`

**Headers:**
- `Authorization: Bearer <midwife_or_doctor_token>` (required)

**Request Body:** (all fields optional)
```json
{
  "lmpDate": "string (YYYY-MM-DD)",
  "eddDate": "string (YYYY-MM-DD)",
  "deliveryDate": "string (YYYY-MM-DD)",
  "deliveryType": "NORMAL | CESAREAN | ASSISTED",
  "pregnancyStatus": "ACTIVE | COMPLETED | TERMINATED",
  "gravida": "integer",
  "para": "integer",
  "riskLevel": "LOW | MEDIUM | HIGH",
  "riskFactors": "string"
}
```

**Success Response (200 OK):**
```json
{
  "pregnancyId": 1,
  "mother": { /* mother object */ },
  "pregnancyNumber": "PRG-2026-001",
  "lmpDate": "2025-11-01",
  "eddDate": "2026-08-08",
  "deliveryDate": "2026-08-05",
  "deliveryType": "NORMAL",
  "pregnancyStatus": "COMPLETED",
  "gravida": 2,
  "para": 2,
  "riskLevel": "LOW"
}
```

---

### 9. Delete Pregnancy (Doctor Only)
**Endpoint:** `DELETE /api/pregnancies/{pregnancyId}`

**Headers:**
- `Authorization: Bearer <doctor_token>` (required)

**Success Response (200 OK):**
```json
"Pregnancy deleted successfully"
```

**Error Responses:**
- `403 Forbidden`: Only doctors can delete pregnancies
- `404 Not Found`: Pregnancy not found

---

## Baby Endpoints

### 10. Create Baby
**Endpoint:** `POST /api/babies`

**Request Body:**
```json
{
  "pregnancyId": "integer (required)",
  "motherId": "integer (required)",
  "name": "string",
  "dateOfBirth": "string (YYYY-MM-DD, required)",
  "gender": "MALE | FEMALE (required)",
  "birthWeight": "float",
  "birthHeight": "float",
  "birthComplications": "string (text)",
  "apgarScore": "string",
  "birthOrder": "integer (default: 1)",
  "isAlive": "boolean (default: true)"
}
```

**Example:**
```json
{
  "pregnancyId": 1,
  "motherId": 1,
  "name": "Baby Silva",
  "dateOfBirth": "2026-08-05",
  "gender": "FEMALE",
  "birthWeight": 3.2,
  "birthHeight": 49.5,
  "birthComplications": null,
  "apgarScore": "9/10",
  "birthOrder": 1,
  "isAlive": true
}
```

**Success Response (201 Created):**
```json
{
  "babyId": 1,
  "pregnancyId": 1,
  "motherId": 1,
  "name": "Baby Silva",
  "dateOfBirth": "2026-08-05",
  "gender": "FEMALE",
  "birthWeight": 3.2,
  "birthHeight": 49.5,
  "birthComplications": null,
  "apgarScore": "9/10",
  "birthOrder": 1,
  "isAlive": true,
  "createdAt": "2026-08-05T14:30:00"
}
```

---

### 11. Get Baby by ID
**Endpoint:** `GET /api/babies/{id}`

**Success Response (200 OK):**
```json
{
  "babyId": 1,
  "pregnancyId": 1,
  "motherId": 1,
  "name": "Baby Silva",
  "dateOfBirth": "2026-08-05",
  "gender": "FEMALE",
  "birthWeight": 3.2,
  "birthHeight": 49.5,
  "birthComplications": null,
  "apgarScore": "9/10",
  "birthOrder": 1,
  "isAlive": true,
  "createdAt": "2026-08-05T14:30:00",
  "updatedAt": "2026-08-05T14:30:00"
}
```

**Error Responses:**
- `404 Not Found`: Baby not found

---

### 12. List Babies
**Endpoint:** `GET /api/babies?motherId={motherId}&pregnancyId={pregnancyId}`

**Query Parameters:**
- `motherId` (optional): Filter by mother ID
- `pregnancyId` (optional): Filter by pregnancy ID

**Success Response (200 OK):**
```json
[
  {
    "babyId": 1,
    "pregnancyId": 1,
    "motherId": 1,
    "name": "Baby Silva",
    "dateOfBirth": "2026-08-05",
    "gender": "FEMALE",
    "birthWeight": 3.2,
    "isAlive": true
  }
]
```

---

### 13. Delete Baby
**Endpoint:** `DELETE /api/babies/{id}`

**Success Response (204 No Content)**

**Error Responses:**
- `404 Not Found`: Baby not found

---

## Mother Record Endpoints

### 14. Create Mother Record
**Endpoint:** `POST /api/mother-records?pregnancyId={pregnancyId}&midwifeId={midwifeId}&doctorId={doctorId}`

**Query Parameters:**
- `pregnancyId` (required): ID of the pregnancy
- `midwifeId` (optional): ID of the attending midwife
- `doctorId` (optional): ID of the attending doctor

**Request Body:**
```json
{
  "recordDate": "string (YYYY-MM-DD, required)",
  "gestationalAge": "integer (weeks)",
  "weight": "float (kg)",
  "bmi": "float",
  "bloodPressure": "string (e.g., '120/80')",
  "shf": "float (Symphysis-Fundal Height in cm)",
  "findings": "string (text)",
  "recommendations": "string (text)",
  "complications": "string (text)",
  "notes": "string (text)",
  "nextVisitDate": "string (YYYY-MM-DD)"
}
```

**Example:**
```json
{
  "recordDate": "2026-02-09",
  "gestationalAge": 14,
  "weight": 58.5,
  "bmi": 22.3,
  "bloodPressure": "115/75",
  "shf": 14.0,
  "findings": "Fetal heartbeat detected, normal development",
  "recommendations": "Continue prenatal vitamins, adequate rest",
  "complications": null,
  "notes": "Mother reports feeling well, no concerns",
  "nextVisitDate": "2026-03-09"
}
```

**Success Response (201 Created):**
```json
{
  "recordId": 1,
  "pregnancy": { /* pregnancy object */ },
  "midwife": { /* midwife object */ },
  "doctor": null,
  "recordDate": "2026-02-09",
  "gestationalAge": 14,
  "weight": 58.5,
  "bmi": 22.3,
  "bloodPressure": "115/75",
  "shf": 14.0,
  "findings": "Fetal heartbeat detected, normal development",
  "recommendations": "Continue prenatal vitamins, adequate rest",
  "complications": null,
  "notes": "Mother reports feeling well, no concerns",
  "nextVisitDate": "2026-03-09",
  "createdAt": "2026-02-09T10:30:00"
}
```

**Error Responses:**
- `400 Bad Request`: Pregnancy not found, midwife not found, or doctor not found

---

### 15. Get Mother Record by ID
**Endpoint:** `GET /api/mother-records/{id}`

**Success Response (200 OK):**
```json
{
  "recordId": 1,
  "pregnancy": { /* pregnancy object */ },
  "midwife": { /* midwife object */ },
  "doctor": null,
  "recordDate": "2026-02-09",
  "gestationalAge": 14,
  "weight": 58.5,
  "bmi": 22.3,
  "bloodPressure": "115/75",
  "shf": 14.0,
  "findings": "Fetal heartbeat detected, normal development",
  "recommendations": "Continue prenatal vitamins, adequate rest",
  "complications": null,
  "notes": "Mother reports feeling well, no concerns",
  "nextVisitDate": "2026-03-09",
  "createdAt": "2026-02-09T10:30:00"
}
```

**Error Responses:**
- `404 Not Found`: Mother record not found

---

### 16. Get Mother Records by Pregnancy ID
**Endpoint:** `GET /api/mother-records/by-pregnancy/{pregnancyId}`

**Success Response (200 OK):**
```json
[
  {
    "recordId": 1,
    "pregnancy": { /* pregnancy object */ },
    "midwife": { /* midwife object */ },
    "doctor": null,
    "recordDate": "2026-02-09",
    "gestationalAge": 14,
    "weight": 58.5,
    "bmi": 22.3,
    "bloodPressure": "115/75",
    "nextVisitDate": "2026-03-09"
  },
  {
    "recordId": 2,
    "pregnancy": { /* pregnancy object */ },
    "midwife": { /* midwife object */ },
    "doctor": { /* doctor object */ },
    "recordDate": "2026-03-09",
    "gestationalAge": 18,
    "weight": 60.2,
    "bmi": 22.9,
    "bloodPressure": "118/76",
    "nextVisitDate": "2026-04-09"
  }
]
```

---

### 17. Update Mother Record
**Endpoint:** `PUT /api/mother-records/{id}?midwifeId={midwifeId}&doctorId={doctorId}`

**Query Parameters:**
- `midwifeId` (optional): Update attending midwife
- `doctorId` (optional): Update attending doctor

**Request Body:** (all fields optional)
```json
{
  "recordDate": "string (YYYY-MM-DD)",
  "gestationalAge": "integer",
  "weight": "float",
  "bmi": "float",
  "bloodPressure": "string",
  "shf": "float",
  "findings": "string",
  "recommendations": "string",
  "complications": "string",
  "notes": "string",
  "nextVisitDate": "string (YYYY-MM-DD)"
}
```

**Example:**
```json
{
  "weight": 60.5,
  "bmi": 23.0,
  "bloodPressure": "120/78",
  "findings": "All parameters normal, good fetal movement",
  "nextVisitDate": "2026-03-15"
}
```

**Success Response (200 OK):**
```json
{
  "recordId": 1,
  "pregnancy": { /* pregnancy object */ },
  "midwife": { /* midwife object */ },
  "doctor": null,
  "recordDate": "2026-02-09",
  "gestationalAge": 14,
  "weight": 60.5,
  "bmi": 23.0,
  "bloodPressure": "120/78",
  "shf": 14.0,
  "findings": "All parameters normal, good fetal movement",
  "recommendations": "Continue prenatal vitamins, adequate rest",
  "complications": null,
  "notes": "Mother reports feeling well, no concerns",
  "nextVisitDate": "2026-03-15",
  "createdAt": "2026-02-09T10:30:00"
}
```

**Error Responses:**
- `400 Bad Request`: Validation errors or referenced entities not found
- `404 Not Found`: Mother record not found

---

### 18. Delete Mother Record
**Endpoint:** `DELETE /api/mother-records/{id}`

**Success Response (204 No Content)**

**Error Responses:**
- `404 Not Found`: Mother record not found

---

## Baby Record Endpoints

### 19. Create Baby Record
**Endpoint:** `POST /api/baby-records?babyId={babyId}&midwifeId={midwifeId}&doctorId={doctorId}`

**Query Parameters:**
- `babyId` (required): ID of the baby
- `midwifeId` (optional): ID of the attending midwife
- `doctorId` (optional): ID of the attending doctor

**Request Body:**
```json
{
  "recordDate": "string (YYYY-MM-DD, required)",
  "ageMonths": "integer",
  "weight": "float (kg)",
  "height": "float (cm)",
  "headCircumference": "float (cm)",
  "bmi": "float",
  "developmentalMilestones": "string (text)",
  "growthStatus": "NORMAL | UNDERWEIGHT | OVERWEIGHT | STUNTED | WASTED",
  "findings": "string (text)",
  "recommendations": "string (text)",
  "healthStatus": "string",
  "notes": "string (text)",
  "nextVisitDate": "string (YYYY-MM-DD)"
}
```

**Example:**
```json
{
  "recordDate": "2026-11-05",
  "ageMonths": 3,
  "weight": 5.8,
  "height": 60.5,
  "headCircumference": 40.2,
  "bmi": 15.8,
  "developmentalMilestones": "Good head control, responds to sounds, social smiling",
  "growthStatus": "NORMAL",
  "findings": "Healthy development, meeting all milestones",
  "recommendations": "Continue breastfeeding, tummy time exercises",
  "healthStatus": "Excellent",
  "notes": "Baby is active and responsive",
  "nextVisitDate": "2026-12-05"
}
```

**Success Response (201 Created):**
```json
{
  "recordId": 1,
  "baby": { /* baby object */ },
  "midwife": { /* midwife object */ },
  "doctor": null,
  "recordDate": "2026-11-05",
  "ageMonths": 3,
  "weight": 5.8,
  "height": 60.5,
  "headCircumference": 40.2,
  "bmi": 15.8,
  "developmentalMilestones": "Good head control, responds to sounds, social smiling",
  "growthStatus": "NORMAL",
  "findings": "Healthy development, meeting all milestones",
  "recommendations": "Continue breastfeeding, tummy time exercises",
  "healthStatus": "Excellent",
  "notes": "Baby is active and responsive",
  "nextVisitDate": "2026-12-05",
  "createdAt": "2026-11-05T10:30:00"
}
```

**Error Responses:**
- `400 Bad Request`: Baby not found, midwife not found, or doctor not found

---

### 20. Get Baby Record by ID
**Endpoint:** `GET /api/baby-records/{id}`

**Success Response (200 OK):**
```json
{
  "recordId": 1,
  "baby": { /* baby object */ },
  "midwife": { /* midwife object */ },
  "doctor": null,
  "recordDate": "2026-11-05",
  "ageMonths": 3,
  "weight": 5.8,
  "height": 60.5,
  "headCircumference": 40.2,
  "bmi": 15.8,
  "developmentalMilestones": "Good head control, responds to sounds, social smiling",
  "growthStatus": "NORMAL",
  "findings": "Healthy development, meeting all milestones",
  "recommendations": "Continue breastfeeding, tummy time exercises",
  "healthStatus": "Excellent",
  "notes": "Baby is active and responsive",
  "nextVisitDate": "2026-12-05",
  "createdAt": "2026-11-05T10:30:00"
}
```

**Error Responses:**
- `404 Not Found`: Baby record not found

---

### 21. Get Baby Records by Baby ID
**Endpoint:** `GET /api/baby-records/by-baby/{babyId}`

**Success Response (200 OK):**
```json
[
  {
    "recordId": 1,
    "baby": { /* baby object */ },
    "midwife": { /* midwife object */ },
    "doctor": null,
    "recordDate": "2026-11-05",
    "ageMonths": 3,
    "weight": 5.8,
    "height": 60.5,
    "growthStatus": "NORMAL",
    "nextVisitDate": "2026-12-05"
  },
  {
    "recordId": 2,
    "baby": { /* baby object */ },
    "midwife": { /* midwife object */ },
    "doctor": { /* doctor object */ },
    "recordDate": "2026-12-05",
    "ageMonths": 4,
    "weight": 6.5,
    "height": 63.0,
    "growthStatus": "NORMAL",
    "nextVisitDate": "2027-01-05"
  }
]
```

---

### 22. Update Baby Record
**Endpoint:** `PUT /api/baby-records/{id}?midwifeId={midwifeId}&doctorId={doctorId}`

**Query Parameters:**
- `midwifeId` (optional): Update attending midwife
- `doctorId` (optional): Update attending doctor

**Request Body:** (all fields optional)
```json
{
  "recordDate": "string (YYYY-MM-DD)",
  "ageMonths": "integer",
  "weight": "float",
  "height": "float",
  "headCircumference": "float",
  "bmi": "float",
  "developmentalMilestones": "string",
  "growthStatus": "NORMAL | UNDERWEIGHT | OVERWEIGHT | STUNTED | WASTED",
  "findings": "string",
  "recommendations": "string",
  "healthStatus": "string",
  "notes": "string",
  "nextVisitDate": "string (YYYY-MM-DD)"
}
```

**Example:**
```json
{
  "weight": 6.2,
  "height": 62.0,
  "headCircumference": 41.0,
  "developmentalMilestones": "Can roll over, grasps objects, babbling",
  "findings": "Continued healthy development",
  "nextVisitDate": "2027-01-05"
}
```

**Success Response (200 OK):**
```json
{
  "recordId": 1,
  "baby": { /* baby object */ },
  "midwife": { /* midwife object */ },
  "doctor": null,
  "recordDate": "2026-11-05",
  "ageMonths": 3,
  "weight": 6.2,
  "height": 62.0,
  "headCircumference": 41.0,
  "bmi": 16.1,
  "developmentalMilestones": "Can roll over, grasps objects, babbling",
  "growthStatus": "NORMAL",
  "findings": "Continued healthy development",
  "recommendations": "Continue breastfeeding, tummy time exercises",
  "healthStatus": "Excellent",
  "notes": "Baby is active and responsive",
  "nextVisitDate": "2027-01-05",
  "createdAt": "2026-11-05T10:30:00"
}
```

**Error Responses:**
- `400 Bad Request`: Validation errors or referenced entities not found
- `404 Not Found`: Baby record not found

---

### 23. Delete Baby Record
**Endpoint:** `DELETE /api/baby-records/{id}`

**Success Response (204 No Content)**

**Error Responses:**
- `404 Not Found`: Baby record not found

---

## Vaccination Endpoints

## Vaccine Schedule Endpoints

### 24. Create Vaccine Schedule
**Endpoint:** `POST /api/vaccine-schedules`

**Request Body:**
```json
{
  "vaccineName": "string (required)",
  "targetGroup": "MOTHER | BABY (required)",
  "doseNumber": "integer (required, >= 1)",
  "recommendedAgeDays": "integer (optional)",
  "description": "string (optional)"
}
```

**Example:**
```json
{
  "vaccineName": "Tetanus Toxoid (TT)",
  "targetGroup": "MOTHER",
  "doseNumber": 1,
  "recommendedAgeDays": 0,
  "description": "First dose of Tetanus Toxoid during pregnancy"
}
```

**Success Response (201 Created):**
```json
{
  "scheduleId": 1,
  "vaccineName": "Tetanus Toxoid (TT)",
  "targetGroup": "MOTHER",
  "doseNumber": 1,
  "recommendedAgeDays": 0,
  "description": "First dose of Tetanus Toxoid during pregnancy",
  "createdAt": "2026-02-10T10:30:00"
}
```

---

### 25. Get Vaccine Schedule by ID
**Endpoint:** `GET /api/vaccine-schedules/{id}`

**Success Response (200 OK):**
```json
{
  "scheduleId": 1,
  "vaccineName": "Tetanus Toxoid (TT)",
  "targetGroup": "MOTHER",
  "doseNumber": 1,
  "recommendedAgeDays": 0,
  "description": "First dose of Tetanus Toxoid during pregnancy",
  "createdAt": "2026-02-10T10:30:00"
}
```

**Error Responses:**
- `404 Not Found`: Vaccine schedule not found

---

### 26. Get All Vaccine Schedules
**Endpoint:** `GET /api/vaccine-schedules`

**Success Response (200 OK):**
```json
[
  {
    "scheduleId": 1,
    "vaccineName": "Tetanus Toxoid (TT)",
    "targetGroup": "MOTHER",
    "doseNumber": 1,
    "recommendedAgeDays": 0,
    "description": "First dose of Tetanus Toxoid during pregnancy"
  },
  {
    "scheduleId": 2,
    "vaccineName": "BCG",
    "targetGroup": "BABY",
    "doseNumber": 1,
    "recommendedAgeDays": 0,
    "description": "BCG vaccine at birth"
  }
]
```

---

### 27. Get Schedules by Target Group
**Endpoint:** `GET /api/vaccine-schedules/by-target-group/{targetGroup}`

**Path Parameters:**
- `targetGroup`: `MOTHER` or `BABY`

**Example:** `GET /api/vaccine-schedules/by-target-group/BABY`

**Success Response (200 OK):**
```json
[
  {
    "scheduleId": 7,
    "vaccineName": "BCG",
    "targetGroup": "BABY",
    "doseNumber": 1,
    "recommendedAgeDays": 0,
    "description": "BCG vaccine at birth"
  },
  {
    "scheduleId": 10,
    "vaccineName": "DTP",
    "targetGroup": "BABY",
    "doseNumber": 1,
    "recommendedAgeDays": 60,
    "description": "First dose of DTP at 2 months"
  }
]
```

**Error Responses:**
- `400 Bad Request`: Invalid target group value

---

### 28. Update Vaccine Schedule
**Endpoint:** `PUT /api/vaccine-schedules/{id}`

**Request Body:** (all fields optional)
```json
{
  "vaccineName": "string",
  "description": "string",
  "recommendedAgeDays": "integer"
}
```

**Example:**
```json
{
  "vaccineName": "Tetanus Toxoid (TT) - Updated",
  "description": "Updated description",
  "recommendedAgeDays": 7
}
```

**Success Response (200 OK):**
```json
{
  "scheduleId": 1,
  "vaccineName": "Tetanus Toxoid (TT) - Updated",
  "targetGroup": "MOTHER",
  "doseNumber": 1,
  "recommendedAgeDays": 7,
  "description": "Updated description",
  "createdAt": "2026-02-10T10:30:00"
}
```

**Error Responses:**
- `404 Not Found`: Vaccine schedule not found

---

### 29. Delete Vaccine Schedule
**Endpoint:** `DELETE /api/vaccine-schedules/{id}`

**Success Response (204 No Content)**

**Error Responses:**
- `404 Not Found`: Vaccine schedule not found

---

## Mother Vaccination Endpoints

### 30. Administer Mother Vaccine
**Endpoint:** `POST /api/mother-vaccinations`

**Request Body:**
```json
{
  "pregnancyId": "integer (required)",
  "midwifeId": "integer (optional)",
  "scheduleId": "integer (required)",
  "vaccinationDate": "string (YYYY-MM-DD, required)",
  "batchNumber": "string (optional)",
  "manufacturer": "string (optional)",
  "nextDoseDate": "string (YYYY-MM-DD, optional)",
  "adverseReaction": "string (optional)"
}
```

**Example:**
```json
{
  "pregnancyId": 1,
  "midwifeId": 2,
  "scheduleId": 1,
  "vaccinationDate": "2026-02-10",
  "batchNumber": "TT-2026-001",
  "manufacturer": "Serum Institute of India",
  "nextDoseDate": "2026-03-10",
  "adverseReaction": null
}
```

**Success Response (201 Created):**
```json
{
  "vaccinationId": 1,
  "pregnancyId": 1,
  "motherName": "Priya Silva",
  "midwifeId": 2,
  "midwifeName": "Nimal Perera",
  "schedule": {
    "scheduleId": 1,
    "vaccineName": "Tetanus Toxoid (TT)",
    "targetGroup": "MOTHER",
    "doseNumber": 1,
    "recommendedAgeDays": 0,
    "description": "First dose of Tetanus Toxoid during pregnancy"
  },
  "vaccinationDate": "2026-02-10",
  "batchNumber": "TT-2026-001",
  "manufacturer": "Serum Institute of India",
  "nextDoseDate": "2026-03-10",
  "adverseReaction": null,
  "createdAt": "2026-02-10T11:00:00"
}
```

**Error Responses:**
- `400 Bad Request`: Pregnancy not found
- `400 Bad Request`: Vaccine schedule not found
- `400 Bad Request`: "Selected vaccine schedule is not for mothers"
- `400 Bad Request`: Midwife not found (if midwifeId provided)

---

### 31. Get Mother Vaccination by ID
**Endpoint:** `GET /api/mother-vaccinations/{id}`

**Success Response (200 OK):**
```json
{
  "vaccinationId": 1,
  "pregnancyId": 1,
  "motherName": "Priya Silva",
  "midwifeId": 2,
  "midwifeName": "Nimal Perera",
  "schedule": {
    "scheduleId": 1,
    "vaccineName": "Tetanus Toxoid (TT)",
    "targetGroup": "MOTHER",
    "doseNumber": 1
  },
  "vaccinationDate": "2026-02-10",
  "batchNumber": "TT-2026-001",
  "manufacturer": "Serum Institute of India",
  "nextDoseDate": "2026-03-10",
  "adverseReaction": null,
  "createdAt": "2026-02-10T11:00:00"
}
```

**Error Responses:**
- `404 Not Found`: Mother vaccination not found

---

### 32. Get Mother Vaccinations by Pregnancy
**Endpoint:** `GET /api/mother-vaccinations/by-pregnancy/{pregnancyId}`

**Success Response (200 OK):**
```json
[
  {
    "vaccinationId": 1,
    "pregnancyId": 1,
    "motherName": "Priya Silva",
    "schedule": {
      "vaccineName": "Tetanus Toxoid (TT)",
      "doseNumber": 1
    },
    "vaccinationDate": "2026-02-10",
    "nextDoseDate": "2026-03-10"
  },
  {
    "vaccinationId": 2,
    "pregnancyId": 1,
    "motherName": "Priya Silva",
    "schedule": {
      "vaccineName": "Influenza Vaccine",
      "doseNumber": 1
    },
    "vaccinationDate": "2026-02-15",
    "nextDoseDate": null
  }
]
```

---

### 33. Get Mother Vaccinations by Mother ID
**Endpoint:** `GET /api/mother-vaccinations/by-mother/{motherId}`

**Description:** Get all vaccinations across all pregnancies for a specific mother.

**Success Response (200 OK):**
```json
[
  {
    "vaccinationId": 1,
    "pregnancyId": 1,
    "motherName": "Priya Silva",
    "schedule": {
      "vaccineName": "Tetanus Toxoid (TT)",
      "doseNumber": 1
    },
    "vaccinationDate": "2026-02-10"
  }
]
```

---

### 34. Delete Mother Vaccination
**Endpoint:** `DELETE /api/mother-vaccinations/{id}`

**Success Response (204 No Content)**

**Error Responses:**
- `404 Not Found`: Mother vaccination not found

---

## Baby Vaccination Endpoints

### 35. Administer Baby Vaccine
**Endpoint:** `POST /api/baby-vaccinations`

**Request Body:**
```json
{
  "babyId": "integer (required)",
  "midwifeId": "integer (optional)",
  "scheduleId": "integer (required)",
  "vaccinationDate": "string (YYYY-MM-DD, required)",
  "batchNumber": "string (optional)",
  "manufacturer": "string (optional)",
  "nextDoseDate": "string (YYYY-MM-DD, optional)",
  "adverseReaction": "string (optional)"
}
```

**Example:**
```json
{
  "babyId": 1,
  "midwifeId": 2,
  "scheduleId": 7,
  "vaccinationDate": "2026-08-05",
  "batchNumber": "BCG-2026-050",
  "manufacturer": "Japan BCG Laboratory",
  "nextDoseDate": null,
  "adverseReaction": null
}
```

**Success Response (201 Created):**
```json
{
  "vaccinationId": 1,
  "babyId": 1,
  "babyName": "Baby Silva",
  "midwifeId": 2,
  "midwifeName": "Nimal Perera",
  "schedule": {
    "scheduleId": 7,
    "vaccineName": "BCG",
    "targetGroup": "BABY",
    "doseNumber": 1,
    "recommendedAgeDays": 0,
    "description": "BCG vaccine at birth"
  },
  "vaccinationDate": "2026-08-05",
  "batchNumber": "BCG-2026-050",
  "manufacturer": "Japan BCG Laboratory",
  "nextDoseDate": null,
  "adverseReaction": null,
  "createdAt": "2026-08-05T14:30:00"
}
```

**Error Responses:**
- `400 Bad Request`: Baby not found
- `400 Bad Request`: Vaccine schedule not found
- `400 Bad Request`: "Selected vaccine schedule is not for babies"
- `400 Bad Request`: Midwife not found (if midwifeId provided)

---

### 36. Get Baby Vaccination by ID
**Endpoint:** `GET /api/baby-vaccinations/{id}`

**Success Response (200 OK):**
```json
{
  "vaccinationId": 1,
  "babyId": 1,
  "babyName": "Baby Silva",
  "midwifeId": 2,
  "midwifeName": "Nimal Perera",
  "schedule": {
    "scheduleId": 7,
    "vaccineName": "BCG",
    "targetGroup": "BABY",
    "doseNumber": 1
  },
  "vaccinationDate": "2026-08-05",
  "batchNumber": "BCG-2026-050",
  "manufacturer": "Japan BCG Laboratory",
  "nextDoseDate": null,
  "adverseReaction": null,
  "createdAt": "2026-08-05T14:30:00"
}
```

**Error Responses:**
- `404 Not Found`: Baby vaccination not found

---

### 37. Get Baby Vaccinations by Baby
**Endpoint:** `GET /api/baby-vaccinations/by-baby/{babyId}`

**Success Response (200 OK):**
```json
[
  {
    "vaccinationId": 1,
    "babyId": 1,
    "babyName": "Baby Silva",
    "schedule": {
      "vaccineName": "BCG",
      "doseNumber": 1
    },
    "vaccinationDate": "2026-08-05"
  },
  {
    "vaccinationId": 2,
    "babyId": 1,
    "babyName": "Baby Silva",
    "schedule": {
      "vaccineName": "DTP",
      "doseNumber": 1
    },
    "vaccinationDate": "2026-10-05",
    "nextDoseDate": "2026-12-05"
  }
]
```

---

### 38. Get Baby Vaccinations by Mother ID
**Endpoint:** `GET /api/baby-vaccinations/by-mother/{motherId}`

**Description:** Get all vaccinations for all babies of a specific mother.

**Success Response (200 OK):**
```json
[
  {
    "vaccinationId": 1,
    "babyId": 1,
    "babyName": "Baby Silva",
    "schedule": {
      "vaccineName": "BCG",
      "doseNumber": 1
    },
    "vaccinationDate": "2026-08-05"
  }
]
```

---

### 39. Delete Baby Vaccination
**Endpoint:** `DELETE /api/baby-vaccinations/{id}`

**Success Response (204 No Content)**

**Error Responses:**
- `404 Not Found`: Baby vaccination not found

---

## Common Error Responses

**400 Bad Request:**
```json
{
  "error": "Validation error message or business logic error"
}
```

**401 Unauthorized:**
```json
{
  "error": "Missing or invalid Bearer token"
}
```

**403 Forbidden:**
```json
{
  "error": "Role-based access denied message"
}
```

**404 Not Found:**
```json
{
  "error": "Resource not found with id: {id}"
}
```

---

## Notes

### Authentication & Authorization
- All protected endpoints require `Authorization: Bearer <token>` header
- JWT tokens are obtained from `/api/auth/login` or `/api/auth/register`
- Tokens include `userId`, `username`, `email`, and `role` claims
- Role-based access control:
  - **ADMIN**: Full access
  - **DOCTOR**: Can create/update/delete pregnancies, view all active pregnancies
  - **MIDWIFE**: Can register mothers, create pregnancies, view assigned PHM area data
  - **MOTHER**: Can view own data

### Data Validation
- All date fields use `YYYY-MM-DD` format
- Required fields are validated at the service layer
- Foreign key references (pregnancy, baby, midwife, doctor) are validated before saving
- Enum values must match exactly (case-sensitive)

### Entity Relationships
- **Mother** belongs to **PhmArea**
- **Pregnancy** belongs to **Mother**
- **Baby** belongs to **Pregnancy** and **Mother**
- **MotherRecord** belongs to **Pregnancy** (required), **Midwife** (optional), **Doctor** (optional)
- **BabyRecord** belongs to **Baby** (required), **Midwife** (optional), **Doctor** (optional)

### GrowthStatus Enum Values
- `NORMAL`: Baby is growing normally within expected parameters
- `UNDERWEIGHT`: Below expected weight for age
- `OVERWEIGHT`: Above expected weight for age
- `STUNTED`: Below expected height for age
- `WASTED`: Low weight for height ratio

### TargetGroup Enum Values (Vaccination System)
- `MOTHER`: Vaccines administered to pregnant mothers
- `BABY`: Vaccines administered to babies/children

### Sri Lanka EPI Vaccination Schedule
The vaccination system includes Sri Lanka's Expanded Programme of Immunization (EPI) schedule:

**Mother Vaccines:**
- Tetanus Toxoid (TT): 3 doses during pregnancy
- Influenza Vaccine: 1 dose during pregnancy
- COVID-19 Vaccine: 2 doses during pregnancy

**Baby Vaccines:**
- **Birth**: BCG, Hepatitis B (1st dose), OPV (1st dose)
- **2 months**: DTP (1st), Hib (1st), Hepatitis B (2nd), OPV (2nd)
- **4 months**: DTP (2nd), Hib (2nd), OPV (3rd)
- **6 months**: DTP (3rd), Hib (3rd), Hepatitis B (3rd), OPV (4th)
- **9 months**: Measles (1st)
- **12 months**: MMR (1st)
- **18 months**: DTP (4th booster), OPV (5th booster)
- **3 years**: Japanese Encephalitis (1st)
- **5 years**: DT booster, MMR (2nd)

### Best Practices
- Use query parameters for filtering (e.g., `?motherId=1&pregnancyId=2`)
- Update operations support partial updates (only send fields to update)
- Delete operations return `204 No Content` on success
- All timestamps are in ISO 8601 format with timezone
- List endpoints return empty arrays `[]` when no records found

**Vaccination Best Practices:**
- Always verify `targetGroup` matches the vaccination type (MOTHER vs BABY)
- Record batch numbers for vaccine traceability and safety recalls
- Document any adverse reactions immediately for safety monitoring
- Set `nextDoseDate` for multi-dose vaccines to track follow-ups
- Use `recommendedAgeDays` to help schedule vaccinations appropriately
- Query vaccinations by pregnancy or mother ID to view complete immunization history
- Midwife association is optional but recommended for accountability

---

## Notification Endpoints

### Notification Type Endpoints

#### Create Notification Type
**Endpoint:** `POST /api/notification-types`

**Request Body:**
```json
{
  "typeName": "string (required, unique)",
  "description": "string",
  "template": "string",
  "priority": "LOW | MEDIUM | HIGH | URGENT"
}
```

**Example:**
```json
{
  "typeName": "APPOINTMENT_REMINDER",
  "description": "Reminder for upcoming clinic appointments",
  "template": "Dear {motherName}, your next appointment is on {eventDate}.",
  "priority": "MEDIUM"
}
```

**Success Response (201 Created):**
```json
{
  "typeId": 1,
  "typeName": "APPOINTMENT_REMINDER",
  "description": "Reminder for upcoming clinic appointments",
  "template": "Dear {motherName}, your next appointment is on {eventDate}.",
  "priority": "MEDIUM"
}
```

---

#### List All Notification Types
**Endpoint:** `GET /api/notification-types`

**Success Response (200 OK):**
```json
[
  {
    "typeId": 1,
    "typeName": "APPOINTMENT_REMINDER",
    "description": "Reminder for upcoming clinic appointments",
    "template": "Dear {motherName}, your next appointment is on {eventDate}.",
    "priority": "MEDIUM"
  }
]
```

---

#### Get Notification Type by ID
**Endpoint:** `GET /api/notification-types/{id}`

**Success Response (200 OK):** Same as single object above.

---

#### Update Notification Type
**Endpoint:** `PUT /api/notification-types/{id}`

**Request Body:** (partial update supported)
```json
{
  "typeName": "VACCINATION_REMINDER",
  "priority": "HIGH"
}
```

**Success Response (200 OK):** Returns updated notification type object.

---

#### Delete Notification Type
**Endpoint:** `DELETE /api/notification-types/{id}`

**Success Response:** `204 No Content`

---

### Notification CRUD Endpoints

#### Create Notification
**Endpoint:** `POST /api/notifications`

**Request Body:**
```json
{
  "typeId": "integer (required)",
  "midwifeId": "integer (optional)",
  "motherId": "integer (required)",
  "message": "string (required)",
  "deliveryMethod": "SMS | EMAIL | WHATSAPP | CALL",
  "eventDate": "string (YYYY-MM-DD)",
  "eventType": "string"
}
```

**Example:**
```json
{
  "typeId": 1,
  "midwifeId": 1,
  "motherId": 1,
  "message": "Dear Priya, your next clinic visit is on 2026-03-01.",
  "deliveryMethod": "SMS",
  "eventDate": "2026-03-01",
  "eventType": "CLINIC_VISIT"
}
```

**Success Response (201 Created):**
```json
{
  "notificationId": 1,
  "typeId": 1,
  "typeName": "APPOINTMENT_REMINDER",
  "priority": "MEDIUM",
  "midwifeId": 1,
  "midwifeName": "Nimal Perera",
  "motherId": 1,
  "motherName": "Priya Silva",
  "message": "Dear Priya, your next clinic visit is on 2026-03-01.",
  "status": "PENDING",
  "deliveryMethod": "SMS",
  "sentDate": "2026-02-11T12:00:00",
  "eventDate": "2026-03-01",
  "eventType": "CLINIC_VISIT",
  "readAt": null,
  "respondedAt": null,
  "createdAt": "2026-02-11T12:00:00"
}
```

---

#### Get Notification by ID
**Endpoint:** `GET /api/notifications/{id}`

**Success Response (200 OK):** Returns notification response object.

---

#### Get All Notifications for a Mother
**Endpoint:** `GET /api/notifications/mother/{motherId}`

**Success Response (200 OK):** Returns array of notification response objects, ordered by sent date descending.

---

#### Get Unread Notifications for a Mother
**Endpoint:** `GET /api/notifications/mother/{motherId}/unread`

**Success Response (200 OK):** Returns array of notifications where `readAt` is null.

---

#### Get Unread Notification Count
**Endpoint:** `GET /api/notifications/mother/{motherId}/unread-count`

**Success Response (200 OK):**
```json
{
  "motherId": 1,
  "unreadCount": 5
}
```

---

#### Get Notifications Sent by a Midwife
**Endpoint:** `GET /api/notifications/midwife/{midwifeId}`

**Success Response (200 OK):** Returns array of notification response objects.

---

#### Get Notifications by Status
**Endpoint:** `GET /api/notifications/status/{status}`

**Path Parameters:**
- `status`: `PENDING` | `SENT` | `DELIVERED` | `FAILED`

**Success Response (200 OK):** Returns array of notification response objects.

---

#### Mark Notification as Read
**Endpoint:** `PUT /api/notifications/{id}/read`

**Success Response (200 OK):** Returns updated notification with `readAt` timestamp set.

---

#### Mark Notification as Responded
**Endpoint:** `PUT /api/notifications/{id}/responded`

**Success Response (200 OK):** Returns updated notification with `respondedAt` timestamp set.

---

#### Update Notification Status
**Endpoint:** `PUT /api/notifications/{id}/status`

**Request Body:**
```json
{
  "status": "SENT"
}
```

**Allowed values:** `PENDING`, `SENT`, `DELIVERED`, `FAILED`

**Success Response (200 OK):** Returns updated notification response object.

---

#### Delete Notification
**Endpoint:** `DELETE /api/notifications/{id}`

**Success Response:** `204 No Content`

---

### Notification Best Practices
- Always provide a `typeId` that matches a valid `NOTIFICATION_TYPE` record
- Use `deliveryMethod` to specify the preferred communication channel
- Set `eventDate` and `eventType` for appointment/event-based notifications
- Use `GET /mother/{motherId}/unread-count` for badge counts in frontend
- Mark notifications as read via `PUT /{id}/read` when the mother views them
- Track delivery lifecycle: `PENDING` → `SENT` → `DELIVERED` (or `FAILED`)
- `midwifeId` is optional — system-generated notifications may not have a midwife
- List endpoints return empty arrays `[]` when no notifications found

---

## Session Endpoints

### Session Type Endpoints

#### Create Session Type
**Endpoint:** `POST /api/session-types`

**Request Body:**
```json
{
  "typeName": "string (required, unique)",
  "description": "string",
  "targetAudience": "PREGNANT_MOTHERS | NEW_MOTHERS | FAMILIES | ALL"
}
```

**Example:**
```json
{
  "typeName": "ANTENATAL_CLINIC",
  "description": "Regular antenatal care clinic for pregnant mothers",
  "targetAudience": "PREGNANT_MOTHERS"
}
```

**Success Response (201 Created):**
```json
{
  "sessionTypeId": 1,
  "typeName": "ANTENATAL_CLINIC",
  "description": "Regular antenatal care clinic for pregnant mothers",
  "targetAudience": "PREGNANT_MOTHERS"
}
```

---

#### List All Session Types
**Endpoint:** `GET /api/session-types`

**Success Response (200 OK):**
```json
[
  {
    "sessionTypeId": 1,
    "typeName": "ANTENATAL_CLINIC",
    "description": "Regular antenatal care clinic for pregnant mothers",
    "targetAudience": "PREGNANT_MOTHERS"
  }
]
```

---

#### Get Session Type by ID
**Endpoint:** `GET /api/session-types/{id}`

**Success Response (200 OK):** Returns single session type object.

---

#### Update Session Type
**Endpoint:** `PUT /api/session-types/{id}`

**Request Body:** (partial update supported)
```json
{
  "typeName": "POSTNATAL_CLINIC",
  "targetAudience": "NEW_MOTHERS"
}
```

**Success Response (200 OK):** Returns updated session type object.

---

#### Delete Session Type
**Endpoint:** `DELETE /api/session-types/{id}`

**Success Response:** `204 No Content`

---

### Session CRUD Endpoints

#### Create Session
**Endpoint:** `POST /api/sessions`

**Request Body:**
```json
{
  "midwifeId": "integer (required)",
  "sessionTypeId": "integer (required)",
  "phmAreaId": "integer (required)",
  "sessionDate": "string (YYYY-MM-DD, required)",
  "startTime": "string (HH:mm, required)",
  "endTime": "string (HH:mm)",
  "topic": "string",
  "venue": "string",
  "description": "string",
  "capacity": "integer"
}
```

**Example:**
```json
{
  "midwifeId": 1,
  "sessionTypeId": 1,
  "phmAreaId": 1,
  "sessionDate": "2026-03-15",
  "startTime": "09:00",
  "endTime": "12:00",
  "topic": "Nutrition during pregnancy",
  "venue": "MOH Office, Colombo",
  "description": "Monthly antenatal clinic covering nutrition and exercise",
  "capacity": 30
}
```

**Success Response (201 Created):**
```json
{
  "sessionId": 1,
  "midwifeId": 1,
  "midwifeName": "Nimal Perera",
  "sessionTypeId": 1,
  "sessionTypeName": "ANTENATAL_CLINIC",
  "phmAreaId": 1,
  "phmAreaName": "Area A",
  "sessionDate": "2026-03-15",
  "startTime": "09:00:00",
  "endTime": "12:00:00",
  "topic": "Nutrition during pregnancy",
  "venue": "MOH Office, Colombo",
  "description": "Monthly antenatal clinic covering nutrition and exercise",
  "capacity": 30,
  "status": "SCHEDULED",
  "createdAt": "2026-02-11T12:00:00"
}
```

---

#### Get Session by ID
**Endpoint:** `GET /api/sessions/{sessionId}`

**Success Response (200 OK):** Returns session response object.

---

#### Get Sessions by Midwife
**Endpoint:** `GET /api/sessions/midwife/{midwifeId}`

**Success Response (200 OK):** Returns array of sessions ordered by date descending.

---

#### Get Scheduled Sessions by Midwife
**Endpoint:** `GET /api/sessions/midwife/{midwifeId}/scheduled`

**Success Response (200 OK):** Returns array of sessions with status `SCHEDULED`, ordered by date ascending.

---

#### Get Sessions by PHM Area
**Endpoint:** `GET /api/sessions/phm-area/{phmAreaId}`

**Success Response (200 OK):** Returns array of sessions ordered by date descending.

---

#### Get Sessions by Status
**Endpoint:** `GET /api/sessions/status/{status}`

**Path Parameters:**
- `status`: `SCHEDULED` | `COMPLETED` | `CANCELLED`

**Success Response (200 OK):** Returns array of session response objects.

---

#### Get Sessions by Date Range
**Endpoint:** `GET /api/sessions/date-range?from={date}&to={date}`

**Query Parameters:**
- `from`: Start date (YYYY-MM-DD, required)
- `to`: End date (YYYY-MM-DD, required)

**Example:** `GET /api/sessions/date-range?from=2026-03-01&to=2026-03-31`

**Success Response (200 OK):** Returns array of sessions within the date range, ordered ascending.

---

#### Update Session
**Endpoint:** `PUT /api/sessions/{sessionId}`

**Request Body:** (partial update supported)
```json
{
  "sessionDate": "2026-03-20",
  "startTime": "10:00",
  "endTime": "13:00",
  "topic": "Updated topic",
  "venue": "Community Hall",
  "description": "Updated description",
  "capacity": 40,
  "status": "COMPLETED"
}
```

**Success Response (200 OK):** Returns updated session response object.

---

#### Delete Session
**Endpoint:** `DELETE /api/sessions/{sessionId}`

**Success Response:** `204 No Content`

---

### Session Attendance Endpoints

#### Register Mother for Session
**Endpoint:** `POST /api/session-attendance`

**Request Body:**
```json
{
  "sessionId": "integer (required)",
  "motherId": "integer (required)",
  "notes": "string"
}
```

**Example:**
```json
{
  "sessionId": 1,
  "motherId": 1,
  "notes": "First-time attendee"
}
```

**Success Response (201 Created):**
```json
{
  "attendanceId": 1,
  "sessionId": 1,
  "sessionTopic": "Nutrition during pregnancy",
  "sessionDate": "2026-03-15",
  "motherId": 1,
  "motherName": "Priya Silva",
  "attended": false,
  "attendanceTime": null,
  "notes": "First-time attendee",
  "feedback": null,
  "createdAt": "2026-02-11T12:00:00"
}
```

**Error Responses:**
- `400 Bad Request`: Session or Mother not found
- `400 Bad Request`: Session is at full capacity
- `400 Bad Request`: Mother is already registered for this session
- `400 Bad Request`: Cannot register for a cancelled session

---

#### Mark Attendance
**Endpoint:** `PUT /api/session-attendance/{attendanceId}/mark`

**Request Body:**
```json
{
  "attended": true,
  "feedback": "Very informative session"
}
```

**Success Response (200 OK):** Returns updated attendance with `attended: true` and `attendanceTime` set.

---

#### Get Attendees by Session
**Endpoint:** `GET /api/session-attendance/session/{sessionId}`

**Success Response (200 OK):** Returns array of attendance records for a session.

---

#### Get Sessions Attended by Mother
**Endpoint:** `GET /api/session-attendance/mother/{motherId}`

**Success Response (200 OK):** Returns array of attendance records for a mother.

---

#### Delete Attendance Record
**Endpoint:** `DELETE /api/session-attendance/{attendanceId}`

**Success Response:** `204 No Content`

---

### Session Best Practices
- Sessions are created with status `SCHEDULED` by default
- Update status to `COMPLETED` or `CANCELLED` via `PUT /api/sessions/{id}`
- Use `GET /midwife/{midwifeId}/scheduled` to show upcoming sessions for a midwife
- Use `GET /date-range?from=&to=` for calendar views
- Register mothers before the session; mark attendance during or after
- Capacity is enforced at registration time — registrations are rejected when full
- Duplicate registrations (same mother + same session) are prevented
- Cannot register mothers for cancelled sessions
- `attendanceTime` is auto-set when `attended` is marked `true`
- Use `feedback` field to collect post-session feedback from mothers
- List endpoints return empty arrays `[]` when no records found

---

## Triposha Distribution Endpoints

Manage Triposha nutritional supplement stock and distribution to pregnant mothers and babies.

### Triposha Stock Endpoints

#### Add Stock
**Endpoint:** `POST /api/triposha/stock`

**Request Body:**
```json
{
  "quantityKg": 50.0,
  "batchNumber": "BATCH-2026-001",
  "expiryDate": "2027-06-30",
  "receivedDate": "2026-02-01",
  "supplier": "National Nutrition Bureau"
}
```

**Success Response (201 Created):**
```json
{
  "stockId": 1,
  "quantityKg": 50.0,
  "batchNumber": "BATCH-2026-001",
  "expiryDate": "2027-06-30",
  "receivedDate": "2026-02-01",
  "supplier": "National Nutrition Bureau",
  "createdAt": "2026-02-01T10:00:00",
  "updatedAt": "2026-02-01T10:00:00"
}
```

---

#### Update Stock Quantity
**Endpoint:** `PUT /api/triposha/stock/{stockId}/quantity?quantity={value}`

**Example:** `PUT /api/triposha/stock/1/quantity?quantity=45.5`

**Success Response (200 OK):**
```json
{
  "stockId": 1,
  "quantityKg": 45.5,
  "batchNumber": "BATCH-2026-001",
  "expiryDate": "2027-06-30",
  "receivedDate": "2026-02-01",
  "supplier": "National Nutrition Bureau",
  "createdAt": "2026-02-01T10:00:00",
  "updatedAt": "2026-02-11T14:30:00"
}
```

---

#### List Available Stock
**Endpoint:** `GET /api/triposha/stock`

**Success Response (200 OK):** Returns array of stock items with quantity > 0 and expiry date >= today, ordered by expiry date (FIFO).
```json
[
  {
    "stockId": 1,
    "quantityKg": 45.5,
    "batchNumber": "BATCH-2026-001",
    "expiryDate": "2027-06-30",
    "receivedDate": "2026-02-01",
    "supplier": "National Nutrition Bureau",
    "createdAt": "2026-02-01T10:00:00",
    "updatedAt": "2026-02-11T14:30:00"
  }
]
```

---

#### Get Stock by ID
**Endpoint:** `GET /api/triposha/stock/{stockId}`

**Success Response (200 OK):** Returns a single stock item.

---

#### Get Total Available Quantity
**Endpoint:** `GET /api/triposha/stock/total`

**Success Response (200 OK):**
```json
{
  "totalAvailableKg": 95.5
}
```

---

### Mother Triposha Distribution Endpoints

#### Distribute Triposha to Mother
**Endpoint:** `POST /api/triposha/mother`

**Request Body:**
```json
{
  "pregnancyId": 1,
  "midwifeId": 2,
  "quantityKg": 2.5,
  "distributionDate": "2026-02-11",
  "remarks": "Monthly distribution - 6th month"
}
```

**Success Response (201 Created):**
```json
{
  "distributionId": 1,
  "pregnancyId": 1,
  "pregnancyNumber": "PREG-001",
  "motherId": 5,
  "motherName": "Kumari Perera",
  "midwifeId": 2,
  "midwifeName": "Nimal Silva",
  "quantityKg": 2.5,
  "distributionDate": "2026-02-11",
  "remarks": "Monthly distribution - 6th month",
  "createdAt": "2026-02-11T09:00:00"
}
```

**Validation Rules:**
- Pregnancy must have status `ACTIVE`
- Stock is deducted automatically using FIFO (earliest expiry first)
- Returns `400 Bad Request` if pregnancy is not active
- Returns `400 Bad Request` if insufficient stock available

---

#### Get Distributions by Pregnancy
**Endpoint:** `GET /api/triposha/mother/pregnancy/{pregnancyId}`

**Success Response (200 OK):** Returns array of distribution records for a specific pregnancy.

---

#### Get Distributions by Mother
**Endpoint:** `GET /api/triposha/mother/mother/{motherId}`

**Success Response (200 OK):** Returns array of all distribution records across all pregnancies for a mother.

---

### Baby Triposha Distribution Endpoints

#### Distribute Triposha to Baby
**Endpoint:** `POST /api/triposha/baby`

**Request Body:**
```json
{
  "babyId": 1,
  "midwifeId": 2,
  "quantityKg": 1.0,
  "distributionDate": "2026-02-11",
  "remarks": "Monthly distribution - 8 months old"
}
```

**Success Response (201 Created):**
```json
{
  "distributionId": 1,
  "babyId": 1,
  "babyName": "Baby Perera",
  "motherId": 5,
  "midwifeId": 2,
  "midwifeName": "Nimal Silva",
  "quantityKg": 1.0,
  "distributionDate": "2026-02-11",
  "remarks": "Monthly distribution - 8 months old",
  "createdAt": "2026-02-11T09:15:00"
}
```

**Validation Rules:**
- Baby must be alive (`isAlive = true`)
- Stock is deducted automatically using FIFO (earliest expiry first)
- Returns `400 Bad Request` if baby is not alive
- Returns `400 Bad Request` if insufficient stock available

---

#### Get Distributions by Baby
**Endpoint:** `GET /api/triposha/baby/baby/{babyId}`

**Success Response (200 OK):** Returns array of distribution records for a specific baby.

---

#### Get Distributions by Mother (Baby)
**Endpoint:** `GET /api/triposha/baby/mother/{motherId}`

**Success Response (200 OK):** Returns array of all baby distribution records for babies belonging to a specific mother.

---

### Triposha Distribution Best Practices
- Stock is managed using **FIFO** (First In, First Out) — batches closest to expiry are used first
- Always check `GET /api/triposha/stock/total` before distributing to verify sufficient stock
- Expired stock batches are automatically excluded from available stock
- Each stock batch has a unique `batchNumber` for traceability
- Mother distributions require an **active pregnancy** — completed or cancelled pregnancies are rejected
- Baby distributions require the baby to be **alive** (`isAlive = true`)
- Use `remarks` field to record distribution context (e.g., month of pregnancy, baby age)
- List endpoints return empty arrays `[]` when no records found
- Error responses follow the standard `ErrorResponse` format with `message` and `success` fields
