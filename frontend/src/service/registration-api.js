// Define API_BASE_URL or import from a central config
const API_BASE_URL = "http://localhost:8080/api/v1";

/**
 * Fetches all event registrations for a given user email.
 * Requires authentication.
 * @param {string} email - The email of the user.
 * @returns {Promise<Array>} A promise that resolves to an array of registration objects.
 */
export const getRegistrationsByEmail = async (email) => {
  const token = localStorage.getItem("token");
  if (!token) {
    throw new Error("Authentication token not found. Please log in.");
  }
  if (!email) {
     throw new Error("Email is required to fetch registrations.");
  }

  const headers = {
    "Content-Type": "application/json",
    'Authorization': `Bearer ${token}`
  };

  // Use the updated backend path /registrations/user/{email}
  const url = `${API_BASE_URL}/registrations/user/${encodeURIComponent(email)}`;

  try {
    const response = await fetch(url, {
      method: "GET",
      headers: headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to fetch registrations: ${response.status} ${response.statusText} - ${errorText}`);
    }

    return response.json(); // Returns RegistrationResponseDto[]
  } catch (error) {
      console.error("Error fetching registrations:", error);
      throw error; // Re-throw for the component to handle
  }
};

// TODO: Add other registration-related API functions if needed
// e.g., deleteRegistration (unregister) - might already be in event-api.js? Check consistency.
