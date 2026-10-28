import { openModal } from "../components/modals.js";
import { API_BASE_URL } from "../config/config.js";

const ADMIN_API = API_BASE_URL + "/admin";
const DOCTOR_API = API_BASE_URL + "/doctor/login";

window.onload = function () {
  const adminRoleButton = document.getElementById("adminRole");
  const doctorRoleButton = document.getElementById("doctorRole");
  const adminLoginButton = document.getElementById("adminLogin");
  const doctorLoginButton = document.getElementById("doctorLogin");

  if (adminRoleButton) {
    adminRoleButton.removeAttribute("onclick");
    adminRoleButton.addEventListener("click", () => openModal("adminLogin"));
  }
  if (doctorRoleButton) {
    doctorRoleButton.removeAttribute("onclick");
    doctorRoleButton.addEventListener("click", () => openModal("doctorLogin"));
  }
  if (adminLoginButton) {
    adminLoginButton.addEventListener("click", () => openModal("adminLogin"));
  }
  if (doctorLoginButton) {
    doctorLoginButton.addEventListener("click", () => openModal("doctorLogin"));
  }
};

async function adminLoginHandler() {
  const username = document.getElementById("username")?.value.trim();
  const password = document.getElementById("password")?.value;
  if (!username || !password) {
    alert("Invalid credentials!");
    return;
  }

  try {
    const response = await fetch(ADMIN_API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });
    const result = await response.json();

    if (!response.ok || !result.token) {
      alert("Invalid credentials!");
      return;
    }

    localStorage.setItem("token", result.token);
    window.selectRole("admin");
  } catch (error) {
    console.error("Admin login failed:", error);
    alert("Unable to log in. Please try again.");
  }
}

async function doctorLoginHandler() {
  const email = document.getElementById("email")?.value.trim();
  const password = document.getElementById("password")?.value;
  if (!email || !password) {
    alert("Invalid credentials!");
    return;
  }

  try {
    const response = await fetch(DOCTOR_API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password })
    });
    const result = await response.json();

    if (!response.ok || !result.token) {
      alert("Invalid credentials!");
      return;
    }

    localStorage.setItem("token", result.token);
    window.selectRole("doctor");
  } catch (error) {
    console.error("Doctor login failed:", error);
    alert("Unable to log in. Please try again.");
  }
}

window.adminLoginHandler = adminLoginHandler;
window.doctorLoginHandler = doctorLoginHandler;
