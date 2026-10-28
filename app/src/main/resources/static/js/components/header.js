function renderHeader() {
  const headerDiv = document.getElementById("header");
  if (!headerDiv) return;

  const pathname = window.location.pathname;
  const isHomePage = pathname === "/" || pathname.endsWith("/index.html");
  if (isHomePage) {
    localStorage.removeItem("userRole");
    localStorage.removeItem("token");
    headerDiv.innerHTML = createHeaderMarkup("");
    return;
  }

  const role = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");

  if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
    localStorage.removeItem("userRole");
    localStorage.removeItem("token");
    alert("Session expired or invalid login. Please log in again.");
    window.location.href = "/";
    return;
  }

  let headerContent = "";
  if (role === "admin") {
    headerContent = `
      <button type="button" id="addDocBtn" class="adminBtn">Add Doctor</button>
      <button type="button" id="logoutButton" class="adminBtn">Logout</button>`;
  } else if (role === "doctor") {
    headerContent = `
      <button type="button" id="doctorHome" class="adminBtn">Home</button>
      <button type="button" id="logoutButton" class="adminBtn">Logout</button>`;
  } else if (role === "patient") {
    headerContent = `
      <button type="button" id="patientLogin" class="adminBtn">Login</button>
      <button type="button" id="patientSignup" class="adminBtn">Sign Up</button>`;
  } else if (role === "loggedPatient") {
    headerContent = `
      <button type="button" id="home" class="adminBtn">Home</button>
      <button type="button" id="patientAppointments" class="adminBtn">Appointments</button>
      <button type="button" id="logoutPatientButton" class="adminBtn">Logout</button>`;
  }

  headerDiv.innerHTML = createHeaderMarkup(headerContent);
  attachHeaderButtonListeners();
}

function createHeaderMarkup(headerContent) {
  return `
    <header class="header">
      <a class="logo-link" href="/" aria-label="Hospital CMS home">
        <img src="/assets/images/logo/logo.png" alt="Hospital CMS" class="logo-img">
        <span class="logo-title">Hospital CMS</span>
      </a>
      ${headerContent ? `<nav aria-label="Main navigation">${headerContent}</nav>` : ""}
    </header>`;
}

function attachHeaderButtonListeners() {
  const addDoctorButton = document.getElementById("addDocBtn");
  if (addDoctorButton) {
    addDoctorButton.addEventListener("click", () => openModal("addDoctor"));
  }

  const logoutButton = document.getElementById("logoutButton");
  if (logoutButton) logoutButton.addEventListener("click", logout);

  const doctorHomeButton = document.getElementById("doctorHome");
  if (doctorHomeButton) {
    doctorHomeButton.addEventListener("click", () => {
      const token = localStorage.getItem("token");
      if (token) window.location.href = `/doctorDashboard/${encodeURIComponent(token)}`;
    });
  }

  const patientLoginButton = document.getElementById("patientLogin");
  const patientDashboardPage = Boolean(
    document.getElementById("filterTime") &&
    document.getElementById("filterSpecialty") &&
    document.getElementById("content")
  );
  if (patientLoginButton && !patientDashboardPage) {
    patientLoginButton.addEventListener("click", () => openModal("patientLogin"));
  }

  const patientSignupButton = document.getElementById("patientSignup");
  if (patientSignupButton && !patientDashboardPage) {
    patientSignupButton.addEventListener("click", () => openModal("patientSignup"));
  }

  const patientHomeButton = document.getElementById("home");
  if (patientHomeButton) {
    patientHomeButton.addEventListener("click", () => {
      window.location.href = "/pages/loggedPatientDashboard.html";
    });
  }

  const appointmentsButton = document.getElementById("patientAppointments");
  if (appointmentsButton) {
    appointmentsButton.addEventListener("click", () => {
      window.location.href = "/pages/patientAppointments.html";
    });
  }

  const patientLogoutButton = document.getElementById("logoutPatientButton");
  if (patientLogoutButton) patientLogoutButton.addEventListener("click", logoutPatient);
}

function openModal(type) {
  import("./modals.js")
    .then(({ openModal: showModal }) => showModal(type))
    .catch((error) => console.error("Unable to open modal:", error));
}

function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("userRole");
  window.location.href = "/";
}

function logoutPatient() {
  localStorage.removeItem("token");
  localStorage.setItem("userRole", "patient");
  window.location.href = "/pages/patientDashboard.html";
}

renderHeader();

