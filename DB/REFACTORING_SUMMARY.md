# Database Schema Refactoring Summary

## Overview
The database schema has been completely refactored to match the Java Entity Models exactly. This ensures perfect alignment between the application layer and the database layer.

---

## Key Changes Made

### 1. **Table Structure Alignment**
All tables now match their corresponding Java entity classes precisely:
- Column names match Java field annotations (`@Column(name = "...")`)
- Data types align with Java types (INT, VARCHAR, TEXT, FLOAT, DOUBLE, DATE, TIMESTAMP, ENUM)
- Foreign key relationships match `@ManyToOne` and `@JoinColumn` annotations

### 2. **New Tables Added**

#### **TRIPOSHA_STOCK**
- Manages inventory of Triposha nutrition supplement
- Fields: `stock_id`, `quantity_kg`, `batch_number`, `expiry_date`, `received_date`, `supplier`
- Tracks stock levels and batch information

#### **MOTHER_TRIPOSHA_DISTRIBUTION**
- Tracks Triposha distribution to pregnant mothers
- Fields: `distribution_id`, `pregnancy_id`, `midwife_id`, `quantity_kg`, `distribution_date`, `remarks`
- Replaces the generic TRIPOSHA_DISTRIBUTION table

#### **BABY_TRIPOSHA_DISTRIBUTION**
- Tracks Triposha distribution to babies
- Fields: `distribution_id`, `baby_id`, `midwife_id`, `quantity_kg`, `distribution_date`, `remarks`
- Separate from mother distribution for clarity

### 3. **Tables Removed**
The following tables from the old schema are NOT in the Java models and have been removed:
- **MEDICATION** - Not implemented in current Java entities
- **AUDIT_LOG** - Not implemented in current Java entities
- **USER_SESSION** - Not implemented in current Java entities
- **FAMILY_UNIT** - Replaced by MOTHER table directly
- **FAMILY_MEMBER** - Functionality merged into MOTHER table

### 4. **Field Changes**

#### **PHM_AREA**
- ❌ Removed: `description` (not in Java entity)
- ❌ Removed: `created_at` (not in Java entity)

#### **MOTHER**
- Changed `name` from VARCHAR(255) to VARCHAR(50) to match Java entity
- Changed `blood_group` from VARCHAR(10) to match Java entity exactly
- Removed `updated_at` (not in Java entity, only has created_at)

#### **BABY**
- Changed `delivery_type` values in PREGNANCY to: NORMAL, C_SECTION, ASSISTED, OTHER
- Field `pregnancy_id` and `mother_id` are simple INT columns (not foreign keys in Java entity)
- This matches the Java model which uses Integer fields instead of @ManyToOne relationships

#### **MOTHER_RECORD & BABY_RECORD**
- Changed `blood_pressure` from VARCHAR(20) to VARCHAR(50) in MOTHER_RECORD
- All TEXT fields properly defined with `columnDefinition = "TEXT"`

#### **VACCINE_SCHEDULE**
- Added `created_at` timestamp to match Java entity

#### **ClinicSession (SESSION Table)**
- Java class is named `ClinicSession` but table is `SESSION`
- Added proper enum values: SCHEDULED, COMPLETED, CANCELLED

### 5. **ENUM Values Standardized**

#### **UserRole**
```sql
ENUM('ADMIN', 'MIDWIFE', 'DOCTOR', 'MOTHER')
```

#### **TargetGroup**
```sql
ENUM('MOTHER', 'BABY')
```

#### **GrowthStatus**
```sql
ENUM('NORMAL', 'UNDERWEIGHT', 'OVERWEIGHT', 'STUNTED', 'WASTED')
```

#### **DeliveryType (in Pregnancy)**
```sql
ENUM('NORMAL', 'C_SECTION', 'ASSISTED', 'OTHER')
```

#### **PregnancyStatus**
```sql
ENUM('ACTIVE', 'COMPLETED', 'TERMINATED', 'MISCARRIAGE')
```

#### **RiskLevel**
```sql
ENUM('LOW', 'MEDIUM', 'HIGH')
```

#### **SessionStatus**
```sql
ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED')
```

#### **TargetAudience (SessionType)**
```sql
ENUM('PREGNANT_MOTHERS', 'NEW_MOTHERS', 'FAMILIES', 'ALL')
```

#### **NotificationStatus**
```sql
ENUM('PENDING', 'SENT', 'DELIVERED', 'FAILED')
```

#### **DeliveryMethod**
```sql
ENUM('SMS', 'EMAIL', 'WHATSAPP', 'CALL')
```

#### **Priority (NotificationType)**
```sql
ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT')
```

---

## Complete Table List (20 Tables)

### Core Tables
1. **USER** - User authentication and role management
2. **PHM_AREA** - Public Health Midwife areas
3. **MIDWIFE** - Midwife staff details
4. **DOCTOR** - Doctor staff details
5. **MOTHER** - Mother registration and details

### Pregnancy & Baby
6. **PREGNANCY** - Pregnancy tracking
7. **BABY** - Baby birth records

### Medical Records
8. **MOTHER_RECORD** - Prenatal checkup records
9. **BABY_RECORD** - Baby checkup records

### Vaccination
10. **VACCINE_SCHEDULE** - Master vaccine schedule
11. **MOTHER_VACCINATION** - Mother vaccination records
12. **BABY_VACCINATION** - Baby vaccination records

### Triposha Distribution
13. **TRIPOSHA_STOCK** - Triposha inventory management
14. **MOTHER_TRIPOSHA_DISTRIBUTION** - Mother triposha distribution
15. **BABY_TRIPOSHA_DISTRIBUTION** - Baby triposha distribution

### Sessions
16. **SESSION_TYPE** - Types of health education sessions
17. **SESSION** - Individual session scheduling
18. **SESSION_ATTENDANCE** - Session attendance tracking

### Notifications
19. **NOTIFICATION_TYPE** - Notification templates
20. **NOTIFICATION** - Notification records

---

## File Structure

### New Files (Refactored)
- **00_refactored_schema.sql** - Complete schema matching Java entities
- **00_refactored_data.sql** - Sample data for all 20 tables (5+ rows each)

### Old Files (Keep for reference)
- **01_schema.sql** - Original schema (has extra tables not in Java)
- **02_data.sql** - Original sample data
- **03_vaccination_schema.sql** - Vaccination tables (now integrated)
- **04_vaccination_data.sql** - Vaccination sample data
- **05_comprehensive_data.sql** - Previous comprehensive data

---

## Migration Notes

### To Use the Refactored Schema:

1. **Fresh Installation:**
   ```bash
   mysql -u root -p < 00_refactored_schema.sql
   mysql -u root -p moh_connect < 00_refactored_data.sql
   ```

2. **From Existing Database:**
   - Backup your existing data first
   - Review differences carefully
   - The refactored schema removes MEDICATION, AUDIT_LOG, and USER_SESSION tables
   - Triposha distribution is now split into two separate tables

### Important Breaking Changes:

1. **TRIPOSHA_DISTRIBUTION split into two tables**
   - Old: Single table with `baby_id`
   - New: `MOTHER_TRIPOSHA_DISTRIBUTION` with `pregnancy_id`
   - New: `BABY_TRIPOSHA_DISTRIBUTION` with `baby_id`

2. **BABY table structure**
   - `pregnancy_id` and `mother_id` are simple INT columns
   - Not using foreign key constraints (matches Java entity design)

3. **PHM_AREA simplified**
   - Removed `description` and `created_at` fields

4. **MOTHER table fields adjusted**
   - `name` reduced from VARCHAR(255) to VARCHAR(50)
   - Removed `updated_at` timestamp

---

## Data Consistency

All sample data in `00_refactored_data.sql` includes:
- ✅ 11 users (1 Admin, 3 Midwives, 2 Doctors, 5 Mothers)
- ✅ 5 PHM areas
- ✅ 5 Mothers
- ✅ 5 Pregnancies (all ACTIVE)
- ✅ 5 Babies
- ✅ 5 Mother records
- ✅ 5 Baby records
- ✅ 20 Vaccine schedules (comprehensive EPI schedule)
- ✅ 5 Mother vaccinations
- ✅ 5 Baby vaccinations
- ✅ 5 Triposha stock entries
- ✅ 5 Mother triposha distributions
- ✅ 5 Baby triposha distributions
- ✅ 5 Session types
- ✅ 5 Sessions
- ✅ 5 Session attendances
- ✅ 5 Notification types
- ✅ 5 Notifications

---

## Validation

### Schema Validation
All tables and columns match the Java entity annotations:
- ✅ `@Table(name = "...")` matches table names
- ✅ `@Column(name = "...")` matches column names
- ✅ `@Enumerated(EnumType.STRING)` uses VARCHAR/ENUM correctly
- ✅ `@ManyToOne` relationships have proper foreign keys
- ✅ `columnDefinition = "TEXT"` properly defined
- ✅ Nullable/Not Nullable constraints match Java

### Foreign Key Relationships
- ✅ ON DELETE CASCADE for dependent records
- ✅ ON DELETE SET NULL for optional references
- ✅ ON DELETE RESTRICT for reference data

### Indexes
- ✅ Primary keys on all *_id columns
- ✅ Unique constraints on unique fields
- ✅ Foreign key indexes for join performance
- ✅ Composite indexes for common queries (e.g., `idx_mother_status`)

---

## Testing Checklist

After applying the refactored schema:

1. ☐ Verify all tables created successfully
2. ☐ Check all foreign key constraints work
3. ☐ Test INSERT operations on all tables
4. ☐ Verify cascade deletes work correctly
5. ☐ Confirm ENUM values accepted
6. ☐ Run Spring Boot application
7. ☐ Test all CRUD operations through API
8. ☐ Verify JPA entity mappings work
9. ☐ Check no Hibernate mapping errors
10. ☐ Validate data integrity constraints

---

## Recommendations

1. **Use the refactored schema** (`00_refactored_schema.sql`) for new deployments
2. **Keep old files** for reference and migration purposes
3. **Update application.properties** to point to `moh_connect` database
4. **Run validation queries** after schema creation
5. **Test thoroughly** before production deployment

---

## Contact & Support

For questions about the refactoring:
- Review Java entity files in `src/main/java/com/moh/moh_backend/model/`
- Check this document for alignment details
- Verify field mappings match `@Column` annotations

---

**Generated:** February 20, 2026  
**Version:** 1.0.0 (Refactored)
