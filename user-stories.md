## Admin User Stories

### Admin Login

**Title:**
_As an admin, I want to log into the portal with my username and password, so that I can manage the platform securely._

**Acceptance Criteria:**
1. The portal accepts valid admin credentials and grants access to admin features.
2. Invalid credentials are rejected with a clear error message and do not grant access.
3. Authentication credentials are not exposed in plain text, and protected admin pages require an authenticated session.

**Priority:** High
**Story Points:** 5
**Notes:**
- Apply the platform's password storage and session security practices.

### Admin Logout

**Title:**
_As an admin, I want to log out of the portal, so that I can protect system access when I am finished._

**Acceptance Criteria:**
1. The admin can log out from the portal using an available logout action.
2. Logging out ends the authenticated session and returns the admin to the login page.
3. After logout, protected admin pages cannot be accessed using the ended session.

**Priority:** High
**Story Points:** 3
**Notes:**
- The logout action should be available from the admin interface.

### Add Doctor

**Title:**
_As an admin, I want to add doctors to the portal, so that they can use the platform to provide care._

**Acceptance Criteria:**
1. The admin can submit a doctor's required profile information.
2. The portal validates required fields and rejects invalid or duplicate doctor records with a clear message.
3. A valid doctor profile is saved and appears in the doctors list.

**Priority:** High
**Story Points:** 5
**Notes:**
- Required profile fields should follow the existing doctor data model.

### Delete Doctor

**Title:**
_As an admin, I want to delete a doctor's profile from the portal, so that I can remove doctors who should no longer have access to the platform._

**Acceptance Criteria:**
1. The admin can select a doctor and initiate profile deletion.
2. The portal requests confirmation before deleting the profile.
3. After confirmation, the profile is removed from the active doctors list and the doctor can no longer access the portal.

**Priority:** Medium
**Story Points:** 5
**Notes:**
- Handle appointments and other records associated with the doctor according to the platform's retention rules.

### Monthly Appointment Statistics

**Title:**
_As an admin, I want to run a MySQL stored procedure from the MySQL CLI to get the number of appointments per month, so that I can track platform usage statistics._

**Acceptance Criteria:**
1. A documented MySQL stored procedure returns appointment counts grouped by calendar month.
2. The result can be retrieved by invoking the procedure from the MySQL CLI.
3. Months with no appointments are handled consistently, and the results are ordered chronologically.

**Priority:** Medium
**Story Points:** 5
**Notes:**
- Confirm the date range and whether cancelled appointments should be included in the count.

## Patient User Stories

### Browse Doctors

**Title:**
_As a patient, I want to view a list of doctors without logging in, so that I can explore my options before registering._

**Acceptance Criteria:**
1. The doctors list is accessible without an authenticated session.
2. The list displays each doctor's relevant public profile information.
3. Patients can browse available doctors without being prompted to register or log in.

**Priority:** Medium
**Story Points:** 3
**Notes:**
- Only information intended for public viewing should be displayed.

### Patient Sign Up

**Title:**
_As a patient, I want to sign up using my email and password, so that I can book appointments._

**Acceptance Criteria:**
1. The patient can create an account by submitting a valid email address and password.
2. Invalid email addresses, passwords that fail the platform's requirements, and email addresses already in use are rejected with a clear message.
3. After successful registration, the patient can log in and access appointment booking.

**Priority:** High
**Story Points:** 5
**Notes:**
- Passwords must be stored securely and never in plain text.

### Patient Login

**Title:**
_As a patient, I want to log into the portal, so that I can manage my bookings._

**Acceptance Criteria:**
1. The patient can log in using the email and password associated with their account.
2. Valid credentials grant access to the patient's bookings.
3. Invalid credentials are rejected with a clear message and do not grant access.

**Priority:** High
**Story Points:** 3
**Notes:**
- Bookings must be accessible only to the authenticated patient who owns them.

### Patient Logout

**Title:**
_As a patient, I want to log out of the portal, so that I can secure my account._

**Acceptance Criteria:**
1. The patient can log out using an available logout action.
2. Logging out ends the authenticated session and returns the patient to a public page or login page.
3. After logout, protected booking information cannot be accessed using the ended session.

**Priority:** High
**Story Points:** 3
**Notes:**
- The logout action should be available from the patient interface.

### Book a Doctor Appointment

**Title:**
_As a patient, I want to log in and book an hour-long appointment with a doctor, so that I can consult with them._

**Acceptance Criteria:**
1. Only an authenticated patient can submit an appointment booking.
2. The patient can select a doctor and an available appointment slot with a duration of exactly one hour.
3. The portal confirms a successful booking and prevents booking a slot that is no longer available.

**Priority:** High
**Story Points:** 8
**Notes:**
- Confirm how appointment availability and time zone are represented in the platform.

## Doctor User Stories

### Doctor Login

**Title:**
_As a doctor, I want to log into the portal, so that I can manage my appointments._

**Acceptance Criteria:**
1. The doctor can log in using valid credentials associated with their account.
2. Successful login grants access to the doctor's appointments and schedule.
3. Invalid credentials are rejected with a clear message and do not grant access.

**Priority:** High
**Story Points:** 3
**Notes:**
- Appointment information must be accessible only to the authenticated doctor who owns it.

### Doctor Logout

**Title:**
_As a doctor, I want to log out of the portal, so that I can protect my data._

**Acceptance Criteria:**
1. The doctor can log out using an available logout action.
2. Logging out ends the authenticated session and returns the doctor to a public page or login page.
3. After logout, protected appointment and profile information cannot be accessed using the ended session.

**Priority:** High
**Story Points:** 3
**Notes:**
- The logout action should be available from the doctor interface.

### View Appointment Calendar

**Title:**
_As a doctor, I want to view my appointment calendar, so that I can stay organized._

**Acceptance Criteria:**
1. The authenticated doctor can view their appointments in a calendar organized by date and time.
2. Each appointment shows the relevant details needed by the doctor to prepare for the consultation.
3. The calendar displays no appointments belonging to other doctors.

**Priority:** High
**Story Points:** 5
**Notes:**
- Appointment times should use the platform's configured time zone.

### Mark Unavailability

**Title:**
_As a doctor, I want to mark periods when I am unavailable, so that patients are shown only appointment slots I can attend._

**Acceptance Criteria:**
1. The authenticated doctor can mark a date and time period as unavailable.
2. Slots within a marked unavailable period are excluded from the options shown to patients.
3. The doctor can view and remove a previously marked unavailable period.

**Priority:** High
**Story Points:** 5
**Notes:**
- Prevent or explicitly resolve conflicts with appointments that are already confirmed.

### Update Doctor Profile

**Title:**
_As a doctor, I want to update my specialization and contact information, so that patients can see up-to-date information about me._

**Acceptance Criteria:**
1. The authenticated doctor can update their specialization and contact information.
2. The portal validates required fields and contact information before saving changes.
3. After saving, the updated public profile information is displayed to patients.

**Priority:** Medium
**Story Points:** 5
**Notes:**
- Restrict profile edits to the authenticated doctor who owns the profile.
