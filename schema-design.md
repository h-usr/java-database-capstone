
# MySQL Database Design

## Appointments table

- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id)
- patient_id: INT, Foreign Key → patients(id)
- appointment_time: DATETIME, Not Null
- status: INT (0 = Scheduled, 1 = Completed, 2 = Cancelled)

## Patients table

- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- email: VARCHAR(100), Not Null, Unique
- date_of_birth: DATE

## Doctors table

- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- email: VARCHAR(100), Not Null, Unique

## Admin table

- id: INT, Primary Key, Auto Increment
- username: VARCHAR(50), Not Null, Unique
- password_hash: VARCHAR(255), Not Null
- email: VARCHAR(100), Not Null, Unique

# MongoDB Collection Design

Unstructured or semi-structured data such as prescriptions benefit from schema flexibility.  
This example uses embedded documents and supports future extensibility.

---

## Prescriptions collections

```json
{
  "_id": "ObjectId('64fabc91234f')",
  "appointmentId": 103,
  "patientId": 23,
  "doctorId": 7,
  "patientName": "Elena Ruiz",
  "medications": [
    {
      "name": "Amoxicillin",
      "dosage": "500mg",
      "frequency": "3 times a day",
      "duration": "7 days"
    },
    {
      "name": "Ibuprofen",
      "dosage": "200mg",
      "frequency": "As needed"
    }
  ],
  "doctorNotes": "Advise rest and hydration. Follow up in 1 week.",
  "issuedAt": "2025-07-03T10:15:00Z",
  "pharmacy": {
    "name": "Greenleaf Pharmacy",
    "location": "432 Oak Blvd, Charleston, SC"
  },
  "refillCount": 1,
  "tags": ["antibiotic", "pain-relief"],
  "metadata": {
    "writtenBy": "Dr. Amanda Singh",
    "signedDigitally": true
  }
}
````