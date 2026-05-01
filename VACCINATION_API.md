# Vaccination System API Documentation

## Overview
Complete vaccination management system for MOH Connect, supporting both mother and baby immunization tracking.

---

## 📋 Table of Contents
1. [Vaccine Schedule Endpoints](#vaccine-schedule-endpoints)
2. [Mother Vaccination Endpoints](#mother-vaccination-endpoints)
3. [Baby Vaccination Endpoints](#baby-vaccination-endpoints)

---

## Vaccine Schedule Endpoints

### 1. Create Vaccine Schedule
**Endpoint:** `POST /api/vaccine-schedules`

**Request Body:**
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

### 2. Get Vaccine Schedule by ID
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

---

### 3. Get All Vaccine Schedules
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

### 4. Get Schedules by Target Group
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

---

### 5. Update Vaccine Schedule
**Endpoint:** `PUT /api/vaccine-schedules/{id}`

**Request Body:** (all fields optional)
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

---

### 6. Delete Vaccine Schedule
**Endpoint:** `DELETE /api/vaccine-schedules/{id}`

**Success Response (204 No Content)**

---

## Mother Vaccination Endpoints

### 7. Administer Mother Vaccine
**Endpoint:** `POST /api/mother-vaccinations`

**Request Body:**
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
- `400 Bad Request`: Invalid pregnancy, schedule, or validation errors
- `400 Bad Request`: "Selected vaccine schedule is not for mothers"

---

### 8. Get Mother Vaccination by ID
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

---

### 9. Get Mother Vaccinations by Pregnancy
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

### 10. Get Mother Vaccinations by Mother ID
**Endpoint:** `GET /api/mother-vaccinations/by-mother/{motherId}`

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

### 11. Delete Mother Vaccination
**Endpoint:** `DELETE /api/mother-vaccinations/{id}`

**Success Response (204 No Content)**

---

## Baby Vaccination Endpoints

### 12. Administer Baby Vaccine
**Endpoint:** `POST /api/baby-vaccinations`

**Request Body:**
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
- `400 Bad Request`: Invalid baby, schedule, or validation errors
- `400 Bad Request`: "Selected vaccine schedule is not for babies"

---

### 13. Get Baby Vaccination by ID
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

---

### 14. Get Baby Vaccinations by Baby
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

### 15. Get Baby Vaccinations by Mother ID
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

### 16. Delete Baby Vaccination
**Endpoint:** `DELETE /api/baby-vaccinations/{id}`

**Success Response (204 No Content)**

---

## Enums

### TargetGroup
```
MOTHER
BABY
```

---

## Business Logic

### Vaccine Schedule Validation
- `vaccineName` is required and cannot be empty
- `targetGroup` must be either MOTHER or BABY
- `doseNumber` must be >= 1
- `recommendedAgeDays` is optional (can be null)

### Mother Vaccination Validation
- `pregnancyId` must exist in database
- `scheduleId` must exist and have `targetGroup = MOTHER`
- `vaccinationDate` is required
- `midwifeId` is optional (can be null)
- Batch number and manufacturer are optional for tracking

### Baby Vaccination Validation
- `babyId` must exist in database
- `scheduleId` must exist and have `targetGroup = BABY`
- `vaccinationDate` is required
- `midwifeId` is optional (can be null)
- System tracks adverse reactions for safety monitoring

---

## Notes

### Sri Lanka EPI Schedule
The sample data includes vaccines from Sri Lanka's Expanded Programme of Immunization (EPI):
- **Birth**: BCG, Hepatitis B (1st), OPV (1st)
- **2 months**: DTP (1st), Hib (1st), Hepatitis B (2nd), OPV (2nd)
- **4 months**: DTP (2nd), Hib (2nd), OPV (3rd)
- **6 months**: DTP (3rd), Hib (3rd), Hepatitis B (3rd), OPV (4th)
- **9 months**: Measles (1st)
- **12 months**: MMR (1st)
- **18 months**: DTP (4th booster), OPV (5th booster)
- **3 years**: Japanese Encephalitis (1st)
- **5 years**: DT booster, MMR (2nd)

### Features
- **Comprehensive Tracking**: Full vaccination history for mothers and babies
- **Schedule Management**: Flexible vaccine schedule with dose tracking
- **Safety Monitoring**: Adverse reaction recording
- **Batch Traceability**: Batch numbers and manufacturer tracking
- **Next Dose Reminders**: Automatic next dose date tracking
- **Query Flexibility**: Filter by pregnancy, mother, or baby

### Best Practices
- Always verify `targetGroup` matches the vaccination type
- Record batch numbers for vaccine traceability
- Document any adverse reactions for safety monitoring
- Set `nextDoseDate` for multi-dose vaccines
- Use `recommendedAgeDays` to help schedule follow-up vaccinations
