// Define API_BASE_URL or import from a central config
const API_BASE_URL = "http://localhost:8080/api/v1";

/**
 * Fetches account information for a given email.
 * Requires authentication.
 * @param {string} email - The email of the user.
 * @returns {Promise<object>} A promise that resolves to the account information object.
 */
export const getUserInfoByEmail = async (email) => {
  const token = localStorage.getItem("token");
  if (!token) {
    throw new Error("Authentication token not found. Please log in.");
  }
  if (!email) {
     throw new Error("Email is required to fetch account info.");
  }

  const headers = {
    "Content-Type": "application/json",
    'Authorization': `Bearer ${token}`
  };

  const url = `${API_BASE_URL}/account/${encodeURIComponent(email)}`;

  try {
    const response = await fetch(url, {
      method: "GET",
      headers: headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to fetch account info: ${response.status} ${response.statusText} - ${errorText}`);
    }

    return response.json();
  } catch (error) {
      console.error("Error fetching account info:", error);
      throw error; // Re-throw for the component to handle
  }
};

/**
 * Fetches user account information by ID.
 * Requires authentication.
 * @param {number|string} userId - The user ID to fetch
 * @returns {Promise<Object>} - The user account data
 */
export const getUserById = async (userId) => {
  const token = localStorage.getItem("token");
  if (!token) {
    throw new Error("Authentication token not found. Please log in.");
  }
  if (!userId) {
    throw new Error("User ID is required to fetch account info.");
  }

  const headers = {
    "Content-Type": "application/json",
    'Authorization': `Bearer ${token}`
  };

  const url = `${API_BASE_URL}/account/id/${userId}`;

  try {
    const response = await fetch(url, {
      method: "GET",
      headers: headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to fetch user: ${response.status} ${response.statusText} - ${errorText}`);
    }

    return response.json();
  } catch (error) {
    console.error("Error fetching user data:", error);
    throw error; // Re-throw for the component to handle
  }
};