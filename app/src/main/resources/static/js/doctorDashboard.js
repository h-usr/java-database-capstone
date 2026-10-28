import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

const tableBody = document.getElementById("patientTableBody");
let selectedDate = getTodayDate();
const token = localStorage.getItem("token");
let patientName = null;

document.addEventListener("DOMContentLoaded", () => {
  const searchBar = document.getElementById("searchBar");
  const todayButton = document.getElementById("todayButton");
  const datePicker = document.getElementById("datePicker");

  if (datePicker) datePicker.value = selectedDate;

  searchBar?.addEventListener("input", () => {
    patientName = searchBar.value.trim() || "null";
    loadAppointments();
  });

  todayButton?.addEventListener("click", () => {
    selectedDate = getTodayDate();
    if (datePicker) datePicker.value = selectedDate;
    loadAppointments();
  });

  datePicker?.addEventListener("change", () => {
    if (!datePicker.value) return;
    selectedDate = datePicker.value;
    loadAppointments();
  });

  loadAppointments();
});

function getTodayDate() {
  const today = new Date();
  const localYear = today.getFullYear();
  const localMonth = String(today.getMonth() + 1).padStart(2, "0");
  const localDay = String(today.getDate()).padStart(2, "0");
  return `${localYear}-${localMonth}-${localDay}`;
}

function showTableMessage(message) {
  if (!tableBody) return;
  const row = document.createElement("tr");
  const cell = document.createElement("td");
  cell.colSpan = 5;
  cell.classList.add("noPatientRecord");
  cell.textContent = message;
  row.appendChild(cell);
  tableBody.replaceChildren(row);
}

async function loadAppointments() {
  if (!tableBody) return;
  tableBody.replaceChildren();

  if (!token) {
    showTableMessage("Session expired. Please log in again.");
    return;
  }

  try {
    const result = await getAllAppointments(selectedDate, patientName || "null", token);
    const appointments = Array.isArray(result)
      ? result
      : Array.isArray(result?.appointments)
        ? result.appointments
        : [];

    if (appointments.length === 0) {
      showTableMessage("No Appointments found for today.");
      return;
    }

    appointments.forEach((appointment) => {
      const patient = {
        id: appointment.patientId ?? appointment.patient?.id,
        name: appointment.patientName ?? appointment.patient?.name,
        phone: appointment.patientPhone ?? appointment.patient?.phone,
        email: appointment.patientEmail ?? appointment.patient?.email
      };
      const doctorId = appointment.doctorId ?? appointment.doctor?.id;
      const row = createPatientRow(patient, appointment.id, doctorId);
      tableBody.appendChild(row);
    });
  } catch (error) {
    console.error("Failed to load appointments:", error);
    showTableMessage("Error loading appointments. Try again later.");
  }
}
