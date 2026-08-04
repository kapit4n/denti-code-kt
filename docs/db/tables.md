# Database Tables

Source: `src/.../data/Tables.kt`
SQLite schema aligned with denti-code-desktop `V1__init_schema.sql`.

## Tables Overview

| # | Table | Object | Primary Key |
|---|---|---|---|
| 1 | `users` | `UsersTable` | `user_id` |
| 2 | `user_roles` | `UserRolesTable` | `(user_id, role)` |
| 3 | `doctors` | `DoctorsTable` | `doctor_id` |
| 4 | `patients` | `PatientsTable` | `patient_id` |
| 5 | `procedure_types` | `ProcedureTypesTable` | `procedure_type_id` |
| 6 | `treatment_facilities` | `TreatmentFacilitiesTable` | `facility_id` |
| 7 | `consultories` | `ConsultoriesTable` | `consultory_id` |
| 8 | `appointments` | `AppointmentsTable` | `appointment_id` |
| 9 | `appointment_notes` | `AppointmentNotesTable` | `note_id` |
| 10 | `appointment_audit_log` | `AppointmentAuditLogTable` | `log_id` |
| 11 | `performed_actions` | `PerformedActionsTable` | `performed_action_id` |
| 12 | `material_inventory_lines` | `MaterialInventoryLinesTable` | `line_id` |
| 13 | `inventory_movements` | `InventoryMovementsTable` | `movement_id` |
| 14 | `payments` | `PaymentsTable` | `payment_id` |
| 15 | `suppliers` | `SuppliersTable` | `supplier_id` |
| 16 | `purchase_orders` | `PurchaseOrdersTable` | `order_id` |
| 17 | `purchase_order_items` | `PurchaseOrderItemsTable` | `order_item_id` |

---

## 1. `users`

System users (staff, doctors, patients with login access).

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `user_id` | INTEGER | PK, AUTO_INCREMENT | |
| `email` | VARCHAR(255) | UNIQUE INDEX | Login email |
| `password_hash` | VARCHAR(512) | NOT NULL | Hashed password |
| `display_name` | VARCHAR(255) | NULLABLE | Friendly name |
| `preferred_locale` | VARCHAR(32) | NULLABLE | e.g. "es", "en" |
| `avatar_url` | TEXT | NULLABLE | Profile image URL |
| `is_active` | BOOLEAN | DEFAULT true | Account enabled |
| `created_at` | BIGINT | NOT NULL | Epoch milliseconds |

---

## 2. `user_roles`

Role assignments for users (composite PK).

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `user_id` | INTEGER | PK, FK → `users.user_id` (CASCADE) | |
| `role` | VARCHAR(64) | PK | e.g. "ADMIN", "DOCTOR", "RECEPTIONIST" |

---

## 3. `doctors`

Dentist/doctor profiles.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `doctor_id` | INTEGER | PK, AUTO_INCREMENT | |
| `user_id` | INTEGER | FK → `users.user_id` (SET_NULL), NULLABLE | Linked login account |
| `first_name` | VARCHAR(128) | NOT NULL | |
| `last_name` | VARCHAR(128) | NOT NULL | |
| `email` | VARCHAR(255) | NOT NULL | Contact email |
| `contact_phone` | VARCHAR(64) | NULLABLE | |
| `license_number` | VARCHAR(128) | NULLABLE | Medical license |
| `office_room` | VARCHAR(64) | NULLABLE | Assigned room |
| `specialization` | VARCHAR(255) | NULLABLE | e.g. "Ortodoncia" |
| `avatar_url` | TEXT | NULLABLE | |
| `is_active` | BOOLEAN | DEFAULT true | |

---

## 4. `patients`

Patient demographic and medical records.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `patient_id` | INTEGER | PK, AUTO_INCREMENT | |
| `user_id` | INTEGER | FK → `users.user_id` (SET_NULL), NULLABLE | Linked login account |
| `first_name` | VARCHAR(128) | NOT NULL | |
| `last_name` | VARCHAR(128) | NOT NULL | |
| `date_of_birth` | VARCHAR(32) | NOT NULL | Stored as string (YYYY-MM-DD) |
| `gender` | VARCHAR(32) | NULLABLE | |
| `address` | TEXT | NULLABLE | |
| `contact_phone` | VARCHAR(64) | NOT NULL | |
| `email` | VARCHAR(255) | NULLABLE | |
| `avatar_url` | TEXT | NULLABLE | |
| `medical_history_summary` | TEXT | NULLABLE | Free-text summary |
| `created_at` | BIGINT | NOT NULL | Epoch milliseconds |

---

## 5. `procedure_types`

Catalog of dental procedures/treatments.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `procedure_type_id` | INTEGER | PK, AUTO_INCREMENT | |
| `name` | VARCHAR(255) | NOT NULL | e.g. "Limpieza dental" |
| `description` | TEXT | NULLABLE | |
| `default_duration_minutes` | INTEGER | NULLABLE | Estimated duration |
| `standard_price` | DOUBLE | NULLABLE | Base price |
| `requires_tooth_specification` | BOOLEAN | DEFAULT false | Needs tooth number |
| `category` | VARCHAR(128) | NULLABLE | Grouping category |
| `is_active` | BOOLEAN | DEFAULT true | |

---

## 6. `treatment_facilities`

Materials/supplies catalog used in treatments.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `facility_id` | INTEGER | PK, AUTO_INCREMENT | |
| `facility_code` | VARCHAR(64) | UNIQUE INDEX | Short code |
| `category_key` | VARCHAR(64) | NOT NULL | Grouping key |
| `display_name` | VARCHAR(255) | NOT NULL | |
| `sort_order` | INTEGER | DEFAULT 0 | Display ordering |
| `is_active` | BOOLEAN | DEFAULT true | |

---

## 7. `consultories`

Physical consultation rooms/offices.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `consultory_id` | INTEGER | PK, AUTO_INCREMENT | |
| `name` | VARCHAR(255) | NOT NULL | e.g. "Consultorio 1" |
| `short_code` | VARCHAR(32) | NULLABLE | e.g. "C1" |
| `sort_order` | INTEGER | DEFAULT 0 | Display ordering |
| `is_active` | BOOLEAN | DEFAULT true | |

---

## 8. `appointments`

Patient appointment scheduling.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `appointment_id` | INTEGER | PK, AUTO_INCREMENT | |
| `patient_id` | INTEGER | FK → `patients.patient_id` (RESTRICT) | Required |
| `primary_doctor_id` | INTEGER | FK → `doctors.doctor_id` (RESTRICT) | Required |
| `scheduled_at` | VARCHAR(64) | NOT NULL | ISO datetime string |
| `estimated_duration_minutes` | INTEGER | NULLABLE | |
| `purpose` | TEXT | NULLABLE | Visit reason |
| `notes` | TEXT | NULLABLE | |
| `procedure_type_id` | INTEGER | FK → `procedure_types.procedure_type_id` (SET_NULL), NULLABLE | |
| `status` | VARCHAR(32) | NOT NULL | See AppointmentStatus enum |
| `created_at` | BIGINT | NULLABLE | Epoch milliseconds |
| `updated_at` | BIGINT | NULLABLE | Epoch milliseconds |
| `source` | VARCHAR(32) | DEFAULT 'MANUAL' | Appointment origin |
| `cancellation_reason` | TEXT | NULLABLE | |
| `follow_up_appointment_id` | INTEGER | FK → `appointments.appointment_id` (SET_NULL), NULLABLE | Self-reference |

### Appointment Status Values

| Status | Description |
|---|---|
| `SCHEDULED` | Newly created |
| `CONFIRMED` | Patient confirmed |
| `IN_PROGRESS` | Currently happening |
| `COMPLETED` | Finished |
| `CANCELLED` | Cancelled by patient/clinic |
| `NO_SHOW` | Patient did not arrive |
| `RESCHEDULED` | Moved to new date |

---

## 9. `appointment_notes`

Free-form notes attached to appointments.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `note_id` | INTEGER | PK, AUTO_INCREMENT | |
| `appointment_id` | INTEGER | FK → `appointments.appointment_id` (CASCADE) | Required |
| `body` | TEXT | NOT NULL | Note content |
| `author_label` | VARCHAR(128) | DEFAULT 'Recepción' | Who wrote it |
| `created_at` | BIGINT | NOT NULL | Epoch milliseconds |

---

## 10. `appointment_audit_log`

Audit trail for appointment state changes.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `log_id` | INTEGER | PK, AUTO_INCREMENT | |
| `appointment_id` | INTEGER | FK → `appointments.appointment_id` (CASCADE) | Required |
| `action` | VARCHAR(64) | NOT NULL | e.g. "STATUS_CHANGE", "RESCHEDULE" |
| `actor_label` | VARCHAR(128) | DEFAULT 'Recepción' | Who performed action |
| `detail` | TEXT | NULLABLE | JSON or free-text detail |
| `created_at` | BIGINT | NOT NULL | Epoch milliseconds |

---

## 11. `performed_actions`

Treatments/procedures performed during appointments.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `performed_action_id` | INTEGER | PK, AUTO_INCREMENT | |
| `patient_id` | INTEGER | FK → `patients.patient_id` (CASCADE), NULLABLE | |
| `appointment_id` | INTEGER | FK → `appointments.appointment_id` (CASCADE) | Required |
| `procedure_type_id` | INTEGER | FK → `procedure_types.procedure_type_id` (RESTRICT) | Required |
| `performing_doctor_id` | INTEGER | FK → `doctors.doctor_id` (RESTRICT) | Required |
| `action_at` | VARCHAR(64) | NOT NULL | ISO datetime string |
| `status` | VARCHAR(32) | DEFAULT 'PLANNED' | PLANNED / COMPLETED |
| `standard_price` | DOUBLE | NULLABLE | Reference price |
| `tooth_involved` | VARCHAR(64) | NULLABLE | Tooth number(s) |
| `surfaces_involved` | VARCHAR(128) | NULLABLE | Tooth surface(s) |
| `anesthesia_used` | VARCHAR(128) | NULLABLE | Anesthesia type |
| `facilities_used` | TEXT | NULLABLE | Materials consumed |
| `description_notes` | TEXT | NULLABLE | Treatment notes |
| `quantity` | INTEGER | DEFAULT 1 | |
| `unit_price` | DOUBLE | DEFAULT 0.0 | |
| `total_price` | DOUBLE | DEFAULT 0.0 | quantity × unit_price |

---

## 12. `material_inventory_lines`

Current stock levels per consultory per material.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `line_id` | INTEGER | PK, AUTO_INCREMENT | |
| `consultory_id` | INTEGER | FK → `consultories.consultory_id` (CASCADE) | Required |
| `facility_id` | INTEGER | FK → `treatment_facilities.facility_id` (CASCADE) | Required |
| `quantity` | INTEGER | DEFAULT 0 | Current stock count |

---

## 13. `inventory_movements`

Stock movement history (in/out adjustments).

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `movement_id` | INTEGER | PK, AUTO_INCREMENT | |
| `consultory_id` | INTEGER | FK → `consultories.consultory_id` (CASCADE) | Required |
| `facility_id` | INTEGER | FK → `treatment_facilities.facility_id` (CASCADE) | Required |
| `quantity_change` | INTEGER | NOT NULL | Positive = in, negative = out |
| `type` | VARCHAR(32) | NOT NULL | e.g. "RESTOCK", "USAGE", "ADJUSTMENT" |
| `note` | TEXT | NULLABLE | Reason for movement |
| `created_at` | BIGINT | NOT NULL | Epoch milliseconds |

---

## 14. `payments`

Patient payment records.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `payment_id` | INTEGER | PK, AUTO_INCREMENT | |
| `patient_id` | INTEGER | FK → `patients.patient_id` (CASCADE) | Required |
| `appointment_id` | INTEGER | FK → `appointments.appointment_id` (SET_NULL), NULLABLE | Optional link |
| `amount` | DOUBLE | NOT NULL | Payment amount |
| `method` | VARCHAR(32) | NULLABLE | e.g. "CASH", "CARD", "TRANSFER" |
| `note` | TEXT | NULLABLE | |
| `paid_at` | VARCHAR(64) | NOT NULL | ISO datetime string |
| `procedure_type_id` | INTEGER | FK → `procedure_types.procedure_type_id` (SET_NULL), NULLABLE | |
| `performed_action_id` | INTEGER | FK → `performed_actions.performed_action_id` (SET_NULL), NULLABLE | |

---

## 15. `suppliers`

Supplier catalog used by purchase orders.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `supplier_id` | INTEGER | PK, AUTO_INCREMENT | |
| `name` | VARCHAR(255) | NOT NULL | Supplier name |
| `contact_name` | VARCHAR(64) | NULLABLE | Contact person |
| `phone` | VARCHAR(64) | NULLABLE | |
| `email` | VARCHAR(255) | NULLABLE | |
| `address` | TEXT | NULLABLE | |
| `notes` | TEXT | NULLABLE | |
| `is_active` | BOOLEAN | DEFAULT true | |
| `is_archived` | BOOLEAN | DEFAULT false | |
| `created_at` | BIGINT | NULLABLE | Epoch milliseconds |
| `updated_at` | BIGINT | NULLABLE | Epoch milliseconds |

---

## 16. `purchase_orders`

Purchase orders to suppliers; receiving a pending order credits per-consultory stock.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `order_id` | INTEGER | PK, AUTO_INCREMENT | |
| `supplier_id` | INTEGER | FK → `suppliers.supplier_id` (SET_NULL), NULLABLE | |
| `status` | VARCHAR(32) | DEFAULT `PENDING` | `PENDING` / `RECEIVED` |
| `order_date` | BIGINT | NOT NULL | Epoch milliseconds |
| `received_at` | BIGINT | NULLABLE | Epoch milliseconds |
| `notes` | TEXT | NULLABLE | |
| `total_cost` | DOUBLE | DEFAULT 0 | Σ qty · unit_cost |

---

## 17. `purchase_order_items`

Line items of a purchase order (one per consultory + insumo).

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `order_item_id` | INTEGER | PK, AUTO_INCREMENT | |
| `order_id` | INTEGER | FK → `purchase_orders.order_id` (CASCADE) | Required |
| `consultory_id` | INTEGER | FK → `consultories.consultory_id` (RESTRICT) | Required |
| `facility_id` | INTEGER | FK → `treatment_facilities.facility_id` (RESTRICT) | Required |
| `quantity` | INTEGER | NOT NULL | Quantity to credit on receipt |
| `unit_cost` | DOUBLE | DEFAULT 0 | Unit cost per item |

---

## Entity Relationships

```
users ──< user_roles
users ──< doctors        (nullable FK)
users ──< patients       (nullable FK)
patients ──< appointments
doctors  ──< appointments
doctors  ──< performed_actions
patients ──< performed_actions
patients ──< payments
procedure_types ──< appointments (nullable)
procedure_types ──< performed_actions
procedure_types ──< payments (nullable)
appointments ──< appointment_notes
appointments ──< appointment_audit_log
appointments ── performed_actions
appointments ──< payments (nullable)
appointments ─> appointments (self-ref follow-up)
performed_actions ──< payments (nullable)
consultories ──< material_inventory_lines
consultories ──< inventory_movements
treatment_facilities ──< material_inventory_lines
treatment_facilities ──< inventory_movements
suppliers ──< purchase_orders (nullable)
purchase_orders ──< purchase_order_items
purchase_order_items ── consultories
purchase_order_items ── treatment_facilities
```
