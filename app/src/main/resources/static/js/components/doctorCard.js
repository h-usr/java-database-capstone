import { API_BASE_URL } from "../config/config.js";
import { getPatientData } from "../services/patientServices.js";

export function createDoctorCard(doctor) {
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  const role = localStorage.getItem("userRole");
  const infoDiv = document.createElement("div");
  infoDiv.classList.add("doctor-info");

  const name = document.createElement("h3");
  name.textContent = doctor.name || "Doctor";

  const specialization = document.createElement("p");
  specialization.textContent = `Specialty: ${doctor.specialty || doctor.specialization || "Not specified"}`;

  const email = document.createElement("p");
  email.textContent = `Email: ${doctor.email || "Not specified"}`;

  const availability = document.createElement("p");
  const availableTimes = Array.isArray(doctor.availableTimes)
    ? doctor.availableTimes.join(", ")
    : doctor.availableTimes || "Not specified";
  availability.textContent = `Availability: ${availableTimes}`;

  infoDiv.appendChild(name);
  infoDiv.appendChild(specialization);
  infoDiv.appendChild(email);
  infoDiv.appendChild(availability);

  const actionsDiv = document.createElement("div");
  actionsDiv.classList.add("card-actions");

  if (role === "admin") {
    const removeBtn = document.createElement("button");
    removeBtn.type = "button";
    removeBtn.textContent = "Delete";
    removeBtn.addEventListener("click", async () => {
      if (!window.confirm(`Delete ${doctor.name || "this doctor"}?`)) return;

      const token = localStorage.getItem("token");
      if (!token) {
        alert("Your session has expired. Please log in again.");
        window.location.href = "/";
        return;
      }

      try {
        const response = await fetch(
          `${API_BASE_URL}/doctor/${encodeURIComponent(doctor.id)}/${encodeURIComponent(token)}`,
          { method: "DELETE" }
        );
        const result = await response.json().catch(() => ({}));

        if (!response.ok) {
          alert(result.message || "Unable to delete this doctor.");
          return;
        }

        card.remove();
        alert(result.message || "Doctor deleted successfully.");
      } catch (error) {
        console.error("Failed to delete doctor:", error);
        alert("Unable to delete this doctor. Please try again.");
      }
    });
    actionsDiv.appendChild(removeBtn);
  } else if (role === "patient") {
    const bookNow = document.createElement("button");
    bookNow.type = "button";
    bookNow.textContent = "Book Now";
    bookNow.addEventListener("click", () => {
      alert("Please log in before booking an appointment.");
    });
    actionsDiv.appendChild(bookNow);
  } else if (role === "loggedPatient") {
    const bookNow = document.createElement("button");
    bookNow.type = "button";
    bookNow.textContent = "Book Now";
    bookNow.addEventListener("click", async (event) => {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("Your session has expired. Please log in again.");
        window.location.href = "/";
        return;
      }

      try {
        const patientData = await getPatientData(token);
        if (!patientData) {
          alert("Unable to load your patient details. Please log in again.");
          return;
        }

        const { showBookingOverlay } = await import("../loggedPatient.js");
        showBookingOverlay(event, doctor, patientData);
      } catch (error) {
        console.error("Unable to start appointment booking:", error);
        alert("Unable to start booking. Please try again.");
      }
    });
    actionsDiv.appendChild(bookNow);
  }

  card.appendChild(infoDiv);
  card.appendChild(actionsDiv);
  return card;
}
