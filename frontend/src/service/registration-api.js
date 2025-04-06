import apiClient, { UnauthorizedError } from './apiClient'; // Import the centralized API client and UnauthorizedError

/**
 * Fetches all event registrations for a given user email.
 * Requires authentication (via HttpOnly cookie).
 * @param {string} email - The email of the user.
 * @param {number} [retryCount=0] - Number of times this request has been retried
 * @returns {Promise<Array>} A promise that resolves to an array of registration objects.
 * @throws {UnauthorizedError} If the user is not authenticated.
 * @throws {ApiError} For other API-related errors.
 */
export const getRegistrationsByEmail = async (email, retryCount = 0) => {
  if (!email) {
     throw new Error("Email is required to fetch registrations.");
  }

  // Max retry count to prevent infinite loops
  const MAX_RETRIES = 2;
  
  // Add a small delay to allow authentication to complete if this is a retry
  if (retryCount > 0) {
    await new Promise(resolve => setTimeout(resolve, 800));
  }

  try {
    // Use the correct API endpoint path
    const registrations = await apiClient(`/registrations/user/${encodeURIComponent(email)}`, {
      method: "GET",
      skipPrefix: true
    });
    return registrations;
  } catch (error) {
    // If unauthorized error and we haven't exceeded max retries, try again
    if (error instanceof UnauthorizedError && retryCount < MAX_RETRIES) {
      console.log(`Auth not ready, retrying registration fetch for ${email} (attempt ${retryCount + 1})`);
      return getRegistrationsByEmail(email, retryCount + 1);
    }
    
    console.error(`Failed to fetch registrations for user ${email}:`, error);
    throw error; // Re-throw the specific error from apiClient
  }
};

// TODO: Add other registration-related API functions if needed
// e.g., deleteRegistration (unregister) - might already be in event-api.js? Check consistency.
