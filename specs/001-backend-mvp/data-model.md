# Data Model: Backend MVP

This document outlines the core entities and their relationships for the MiniProntuario MVP.

## 1. Dentist (User)

Represents the authenticated professional.

| Field | Type | Attributes | Description |
|-------|------|------------|-------------|
| `id` | UUID | PK | Primary Key |
| `name` | String | Not Null | Full name of the dentist |
| `email` | String | Unique, Not Null | Login email |
| `password` | String | Not Null | BCrypt hashed password |
| `cpf` | String | Unique, Not Null | Brazilian document |
| `cro` | String | Not Null | Professional license number |
| `phone` | String | Nullable | Contact phone number |
| `createdAt` | Timestamp | Not Null | Audit field |
| `updatedAt` | Timestamp | Not Null | Audit field |

## 2. Patient

Represents a patient belonging to a specific dentist.

| Field | Type | Attributes | Description |
|-------|------|------------|-------------|
| `id` | UUID | PK | Primary Key |
| `dentist_id`| UUID | FK, Not Null | Links patient to their owning dentist |
| `name` | String | Not Null | Full name of the patient |
| `cpf` | String | Not Null | Brazilian document (Unique per dentist) |
| `birthDate` | Date | Not Null | Patient's date of birth |
| `phone` | String | Nullable | Contact phone number |
| `allergies` | Text | Nullable | Clinical data (Allergies) |
| `systemicDiseases` | Text | Nullable| Clinical data (Systemic diseases) |
| `deleted` | Boolean | Default false| Soft delete flag |
| `createdAt` | Timestamp | Not Null | Audit field |
| `updatedAt` | Timestamp | Not Null | Audit field |

**Constraints**: 
- Unique constraint on `(dentist_id, cpf)`.

## 3. Procedure

Represents a clinical action performed on a patient.

| Field | Type | Attributes | Description |
|-------|------|------------|-------------|
| `id` | UUID | PK | Primary Key |
| `patient_id`| UUID | FK, Not Null | Links procedure to patient |
| `date` | Date | Not Null | Date procedure was performed (Cannot be future) |
| `description`| String | Not Null | Short description (e.g., Restoration) |
| `tooth` | String | Nullable | Standard FDI tooth number (e.g., "11") |
| `notes` | Text | Nullable | Detailed clinical notes |
| `deleted` | Boolean | Default false| Soft delete flag |
| `createdAt` | Timestamp | Not Null | Audit field (Used to enforce 24h edit window) |
| `updatedAt` | Timestamp | Not Null | Audit field |

**Relationships**:
- A `Dentist` has many `Patient`s.
- A `Patient` has many `Procedure`s.
