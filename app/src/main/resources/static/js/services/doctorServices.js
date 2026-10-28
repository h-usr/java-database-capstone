import { API_BASE_URL } from "../config/config.js";

const DOCTOR_API = `${API_BASE_URL}/doctor`;

export async function getDoctors() {
  try {
    const response = await fetch(DOCTOR_API);
    if (!response.ok) throw new Error(`Request failed: ${response.status}`);

    const data = await response.json();
    return Array.isArray(data.doctors) ? data.doctors : [];
  } catch (error) {
    console.error("Failed to fetch doctors:", error);
    return [];
  }
}

export async function deleteDoctor(id, token) {
  try {
    const response = await fetch(
      `${DOCTOR_API}/${encodeURIComponent(id)}/${encodeURIComponent(token)}`,
      { method: "DELETE" }
    );
    const data = await response.json().catch(() => ({}));

    return {
      success: response.ok,
      message: data.message || (response.ok ? "Doctor deleted successfully." : "Failed to delete doctor.")
    };
  } catch (error) {
    console.error("Failed to delete doctor:", error);
    return { success: false, message: "Network error. Failed to delete doctor." };
  }
}

export async function saveDoctor(doctor, token) {
  try {
    const response = await fetch(`${DOCTOR_API}/${encodeURIComponent(token)}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(doctor)
    });
    const data = await response.json().catch(() => ({}));

    return {
      success: response.ok,
      message: data.message || (response.ok ? "Doctor saved successfully." : "Failed to save doctor.")
    };
  } catch (error) {
    console.error("Failed to save doctor:", error);
    return { success: false, message: "Network error. Failed to save doctor." };
  }
}

export async function filterDoctors(name, time, specialty) {
  const filters = [name, time, specialty].map((value) =>
    encodeURIComponent(value == null || value === "" ? "null" : value)
  );

  try {
    const response = await fetch(`${DOCTOR_API}/filter/${filters.join("/")}`);
    if (!response.ok) throw new Error(`Request failed: ${response.status}`);

    const data = await response.json();
    return { doctors: Array.isArray(data.doctors) ? data.doctors : [] };
  } catch (error) {
    console.error("Failed to filter doctors:", error);
    return { doctors: [] };
  }
}