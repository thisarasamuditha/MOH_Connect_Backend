# MOH Backend API Documentation

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
- **Mother:**
```json
{
  "username": "priya_silva",
  "email": "priya@example.com",
  "password": "MotherPass@123",
  "role": "MOTHER",
  "wifeNic": "856234567V"
}
```

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
"Mother registered successfully"
```

**Error Responses:**
- `401 Unauthorized`: Missing or invalid Bearer token
- `403 Forbidden`: Only midwives can register mothers
- `400 Bad Request`: NIC/username/email already exists, or midwife not assigned to this PHM area

**Notes:**
- Only authenticated midwives can register mothers.
- The `phmAreaId` in the request must match the midwife's assigned PHM area.
- Mothers cannot self-register; they are registered by midwives.
- After registration, mothers can log in using the `/api/auth/login` endpoint.

---

## Common Response Format
**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "priya_silva",
  "role": "MOTHER"
}
```

**Error Response (400/500):**
```json
{
  "error": "Error message description"
}
```

---

## Notes
- Registration for DOCTOR and MIDWIFE requires their respective details objects.
- For MIDWIFE, `phmAreaId` must reference an existing PHM_AREA.
- Mothers do NOT self-register via `/api/auth/register`. They are registered by midwives using `/api/mothers/register`.
- JWT tokens include `userId`, `email`, and `role` claims for authentication and authorization.