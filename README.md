# MOH Connect - Backend API Documentation

## Table of Contents
1. [Authentication Endpoints](#authentication-endpoints)
2. [Mother Management Endpoints](#mother-management-endpoints)
3. [Pregnancy Endpoints](#pregnancy-endpoints)
4. [Baby Endpoints](#baby-endpoints)
5. [Mother Record Endpoints](#mother-record-endpoints)
6. [Baby Record Endpoints](#baby-record-endpoints)

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

### Best Practices
- Use query parameters for filtering (e.g., `?motherId=1&pregnancyId=2`)
- Update operations support partial updates (only send fields to update)
- Delete operations return `204 No Content` on success
- All timestamps are in ISO 8601 format with timezone
- List endpoints return empty arrays `[]` when no records found
