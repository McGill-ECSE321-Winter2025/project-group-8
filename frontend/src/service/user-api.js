import apiClient, { UnauthorizedError, ForbiddenError } from './apiClient';

/**
 * Fetches the profile information of the currently logged-in user.
 * @returns {Promise<object>} User profile object
 * @throws {UnauthorizedError} If the user is not authenticated
 */
export const getUserProfile = async () => {
  return apiClient('/profile', {
    credentials: 'include'
  });
};

/**
 * Fetches account information for a given email.
 * @param {string} email - The email of the user
 * @returns {Promise<object>} Account information
 * @throws {UnauthorizedError} If the user is not authenticated
 * @throws {ForbiddenError} If the user doesn't have permission
 */
export const getUserInfoByEmail = async (email) => {
  if (!email) {
    throw new Error("Email is required to fetch account info");
  }

  return apiClient(`/users/search/${encodeURIComponent(email)}`);
};

/**
 * Logs out the current user
 * @returns {Promise<void>}
 */
export const logoutUser = async () => {
  try {
    await apiClient('/auth/logout', { 
      method: 'POST',
      credentials: 'include',
      skipRefresh: true // Skip token refresh for logout
    });
  } catch (error) {
    console.error("Error during logout:", error);
    // Continue with client-side logout even if API call fails
    throw error;
  }
};

/**
 * Updates user profile
 * @param {object} userData - User data to update
 * @returns {Promise<object>} Updated user profile
 */
export const updateUserProfile = async (userData) => {
  if (!userData) {
    throw new Error("User data is required");
  }
  
  return apiClient('/profile/update', {
    method: 'POST',
    body: userData
  });
};

/**
 * Searches for users
 * @param {object} searchParams - Search parameters
 * @returns {Promise<Array>} Array of users matching search criteria
 */
export const searchUsers = async (searchParams) => {
  return apiClient('/users/search', {
    method: 'POST',
    body: searchParams
  });
};

/**
 * Changes user password
 * @param {string} currentPassword - Current password
 * @param {string} newPassword - New password
 * @returns {Promise<object>} Success response
 */
export const changePassword = async (currentPassword, newPassword) => {
  if (!currentPassword || !newPassword) {
    throw new Error("Current and new passwords are required");
  }
  
  return apiClient('/profile/password', {
    method: 'POST',
    body: { currentPassword, newPassword }
  });
};

// TODO: Add other user-related API functions here if needed, using apiClient
// e.g., searchUsers, getUserById, updateUser, etc.
