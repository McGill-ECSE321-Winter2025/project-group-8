import apiClient from './apiClient'; // Import the centralized API client

/**
 * Fetches all event registrations for a given user email.
 * Requires authentication (via HttpOnly cookie).
 * @param {string} email - The email of the user.
 * @returns {Promise<Array>} A promise that resolves to an array of registration objects.
 * @throws {UnauthorizedError} If the user is not authenticated.
 * @throws {ApiError} For other API-related errors.
 */
export const getRegistrationsByEmail = async (email) => {
  if (!email) {
     throw new Error("Email is required to fetch registrations.");
  }

  try {
    // Use the updated backend path /registrations/user/{email}
    const registrations = await apiClient(`/registrations/user/${encodeURIComponent(email)}`, {
      method: "GET"
    });
    return registrations;
  } catch (error) {
    console.error(`Failed to fetch registrations for user ${email}:`, error);
    throw error; // Re-throw the specific error from apiClient
  }
};

// TODO: Add other registration-related API functions if needed
// e.g., deleteRegistration (unregister) - might already be in event-api.js? Check consistency.
