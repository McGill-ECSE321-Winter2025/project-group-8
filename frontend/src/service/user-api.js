import apiClient, { UnauthorizedError, ForbiddenError } from './apiClient';

/**
 * Fetches the profile information of the currently logged-in user.
 * Relies on the HttpOnly cookie for authentication.
 * @returns {Promise<object>} A promise that resolves to the user summary object (e.g., UserSummaryDto).
 * @throws {UnauthorizedError} If the user is not authenticated (no valid cookie).
 * @throws {ApiError} For other API-related errors.
 */
export const getUserProfile = async () => {
  try {
    // Assuming the backend has an endpoint like '/api/users/me' or similar
    // that returns the current user's info based on the session cookie.
    const userProfile = await apiClient('/api/users/me'); 
    return userProfile;
  } catch (error) {
    // apiClient throws specific errors like UnauthorizedError
    console.error("Error fetching user profile:", error);
    // Re-throw the error for the caller (e.g., AuthContext) to handle
    throw error; 
  }
};

/**
 * Fetches account information for a given email.
 * Requires authentication (via HttpOnly cookie).
 * @param {string} email - The email of the user.
 * @returns {Promise<object>} A promise that resolves to the account information object.
 * @throws {UnauthorizedError} If the user is not authenticated.
 * @throws {ForbiddenError} If the user is not allowed to access this info.
 * @throws {ApiError} For other API-related errors.
 */
export const getUserInfoByEmail = async (email) => {
  if (!email) {
     throw new Error("Email is required to fetch account info.");
  }

  try {
    const userInfo = await apiClient(`/account/${encodeURIComponent(email)}`);
    return userInfo;
  } catch (error) {
    console.error(`Error fetching user info for ${email}:`, error);
    // Re-throw the specific error (UnauthorizedError, ForbiddenError, ApiError)
    throw error;
  }
};

/**
 * Logs out the current user by calling the backend logout endpoint.
 * This endpoint is expected to clear the HttpOnly cookie.
 * @returns {Promise<void>} A promise that resolves when logout is complete.
 */
export const logoutUser = async () => {
    try {
      // Assuming a POST request to /auth/logout clears the cookie
      await apiClient('/auth/logout', { method: 'POST' }); 
      console.log("Logout API call successful");
    } catch (error) {
        // Log the error but don't necessarily block frontend logout
        console.error("Error calling logout API:", error);
        // Depending on requirements, you might still want to proceed with frontend logout
        // even if the backend call fails. Or re-throw if it's critical.
        // throw error; 
    }
};


// TODO: Add other user-related API functions here if needed, using apiClient
// e.g., searchUsers, getUserById, updateUser, etc.
