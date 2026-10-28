import { openModal } from "./components/modals.js";
import { getDoctors, filterDoctors, saveDoctor } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

document.addEventListener("DOMContentLoaded", () => {
  const addDoctorButton = document.getElementById("addDocBtn");
  if (addDoctorButton) {
    const freshButton = addDoctorButton.cloneNode(true);
    addDoctorButton.replaceWith(freshButton);
    freshButton.addEventListener("click", () => openModal("addDoctor"));
  }

  const searchBar = document.getElementById("searchBar");
  const timeFilter = document.getElementById("filterTime");
  const specialtyFilter = document.getElementById("filterSpecialty");

  if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
  if (timeFilter) timeFilter.addEventListener("change", filterDoctorsOnChange);
  if (specialtyFilter) specialtyFilter.addEventListener("change", filterDoctorsOnChange);

  window.adminAddDoctor = adminAddDoctor;
  loadDoctorCards();
});

async function loadDoctorCards() {
  const doctors = await getDoctors();
  renderDoctorCards(doctors);
}

async function filterDoctorsOnChange() {
  const nameValue = document.getElementById("searchBar")?.value.trim() || "";
  const timeValue = document.getElementById("filterTime")?.value || "";
  const specialtyValue = document.getElementById("filterSpecialty")?.value || "";
  const response = await filterDoctors(
    nameValue || null,
    timeValue || null,
    specialtyValue || null
  );
  const doctors = Array.isArray(response) ? response : response.doctors;

  if (doctors.length > 0) {
    renderDoctorCards(doctors);
  } else {
    const contentDiv = document.getElementById("content");
    if (contentDiv) contentDiv.innerHTML = "<p>No doctors found.</p>";
  }
}

function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  if (!contentDiv) return;

  contentDiv.innerHTML = "";
  doctors.forEach((doctor) => contentDiv.appendChild(createDoctorCard(doctor)));
}

async function adminAddDoctor() {
  const name = document.getElementById("doctorName")?.value.trim();
  const specialty = document.getElementById("specialization")?.value;
  const email = document.getElementById("doctorEmail")?.value.trim();
  const password = document.getElementById("doctorPassword")?.value;
  const phone = document.getElementById("doctorPhone")?.value.trim();
  const availableTimes = Array.from(
    document.querySelectorAll('input[name="availability"]:checked'),
    (input) => input.value
  );
  const token = localStorage.getItem("token");

  if (!token) {
    alert("Admin session expired. Please log in again.");
    window.location.href = "/";
    return;
  }

  if (!name || !specialty || !email || !password || !phone || availableTimes.length === 0) {
    alert("Please complete all doctor fields and select at least one availability time.");
    return;
  }

  const result = await saveDoctor({ name, specialty, email, password, phone, availableTimes }, token);
  if (!result.success) {
    alert(result.message || "Unable to add doctor.");
    return;
  }

  const modal = document.getElementById("modal");
  if (modal) modal.style.display = "none";
  alert(result.message || "Doctor added successfully.");
  await loadDoctorCards();
}
