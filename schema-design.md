
# MySQL Database Design

## Appointment table

- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id)
- patient_id: INT, Foreign Key → patients(id)
- appointment_time: DATETIME, Not Null
- status: INT (0 = Scheduled, 1 = Completed, 2 = Cancelled)

## Patient table

- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null, 3-100 characters
- email: VARCHAR(255), Not Null, Unique, Valid email address
- password: VARCHAR(255), Not Null, At least 6 characters, write-only in API responses
- phone: VARCHAR(10), Not Null, Exactly 10 digits
- address: VARCHAR(255), Not Null

## Doctor table

- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null, 3-100 characters
- specialty: VARCHAR(50), Not Null, 3-50 characters
- email: VARCHAR(255), Not Null, Unique, Valid email address
- password: VARCHAR(255), Not Null, At least 6 characters, write-only in API responses
- phone: VARCHAR(10), Not Null, Exactly 10 digits

## DoctorAvailability times table

- doctor_id: INT, Foreign Key -> doctor(id)
- available_time: VARCHAR, Not Null (for example, "09:00 - 10:00")

## Admin table

- id: INT, Primary Key, Auto Increment
- username: VARCHAR(50), Not Null, Unique
- password: VARCHAR(255), Not Null

# MongoDB Collection Design

## Prescriptions collections

```json
{
  "_id": "ObjectId('64abc123456')",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours.",
  "refillCount": 2,
  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street"
  }
}
```