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
- For MOTHER, `wifeNic` links the user to an existing FAMILY_MEMBER (role=WIFE).
- Only authentication endpoints are implemented in the current project.
