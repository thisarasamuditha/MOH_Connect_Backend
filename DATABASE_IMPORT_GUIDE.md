# Database Import Instructions

## Quick Start

The error "Session type not found with id: 2" means your database needs reference data populated, including SESSION_TYPE records.

### Option 1: Using Batch Script (Easiest - Windows)

1. **Double-click** the `import_database.bat` file in the project root directory
2. **Wait** for the import to complete (should see "SUCCESS" message)
3. **Restart** your Spring Boot application

### Option 2: Manual MySQL Command

Open Command Prompt or PowerShell and run:

```bash
"C:\xampp\mysql\bin\mysql.exe" -u root moh_db < "C:\Users\ASUS\Desktop\MOH_Connect_Backend-main\DB\00_refactored_data.sql"
```

### Option 3: Using MySQL Workbench or PHPMyAdmin

1. Open PHPMyAdmin at `http://localhost/phpmyadmin`
2. Select database `moh_db`
3. Go to **Import** tab
4. Choose file: `DB/00_refactored_data.sql`
5. Click **Import**

---

## What Gets Imported

The `00_refactored_data.sql` file populates:

✓ **SESSION_TYPE** (5 types):
- Prenatal Care Workshop
- Breastfeeding Support
- Child Development
- Nutrition Education
- Vaccination Awareness

✓ **Reference Data**:
- MIDWIFE (3 records)
- DOCTOR (2 records)
- MOTHER (5 records)
- PHM_AREA (5 areas)
- SESSION (5 sessions)
- USER (11 users)
- BABY (5 babies)
- PREGNANCY (5 pregnancies)
- VACCINE_SCHEDULE (20 types)
- And all related data

✓ **Test Credentials**:
- Username: `midwife_nimal`
- Password: `password123`
- Role: MIDWIFE

---

## Troubleshooting

### Error: "MySQL not found"
- Ensure **XAMPP is running** (Apache AND MySQL)
- Check MySQL is active in XAMPP Control Panel

### Error: "Access denied for user 'root'@'localhost'"
- Your MySQL root password may be set
- Modify the script to include: `-p YOUR_PASSWORD`

### Error: "Database 'moh_db' doesn't exist"
- Run this first to create the database:
  ```sql
  CREATE DATABASE moh_db;
  ```

### Application still shows "Session type not found"
1. Verify data was imported: Check PHPMyAdmin → `moh_db` → `SESSION_TYPE` table
2. Restart Spring Boot application
3. Clear browser cache (Ctrl+Shift+Delete)
4. Check if dropdown shows IDs 1-5

---

## Database Schema Files

- **00_refactored_schema.sql** - Creates database tables
- **00_refactored_data.sql** - Populates reference data (USE THIS)
- **01_schema.sql** - Alternative schema
- **03_vaccination_schema.sql** - Vaccination-specific schema
- **04_vaccination_data.sql** - Vaccination data

---

## Verify Import Success

In PHPMyAdmin or MySQL:

```sql
SELECT COUNT(*) FROM SESSION_TYPE;  -- Should return 5
SELECT COUNT(*) FROM SESSION;        -- Should return 5
SELECT COUNT(*) FROM MIDWIFE;        -- Should return 3
```

---

## Next Steps

After successful import:

1. ✓ Restart Spring Boot application
2. ✓ Login with test credentials
3. ✓ Dropdowns should now populate correctly
4. ✓ Demo sessions should be visible

---

**Generated**: May 2, 2026
**Backend Port**: 8082
**Database**: moh_db
