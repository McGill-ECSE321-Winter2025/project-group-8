import apiClient, { UnauthorizedError, ForbiddenError, NotFoundError } from './apiClient';

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
 * @throws {ApiError} For other API errors (e.g., not found)
 */
export const getUserInfoByEmail = async (email) => {
  if (!email) {
    throw new Error("Email is required to fetch account info");
  }

  // Log the request attempt for debugging
  console.log(`Fetching user info for email: ${email}`);

  try {
    return await apiClient(`/users/search/${encodeURIComponent(email)}`, {
      skipPrefix: false
    });
  } catch (error) {
    // Log the error for debugging purposes
    console.error(`Error fetching user info for ${email}:`, error);
    
    // Propagate specific error types for better error handling
    if (error.status === 404) {
      throw new NotFoundError(`User with email ${email} not found`);
    }
    
    // Rethrow other errors for caller to handle
    throw error;
  }
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
    body: searchParams,
    skipPrefix: false

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


/**
 * Sends a connection request to another user.
 * Requires authentication.
 * @param {string} targetUserEmail - The email of the user to connect with.
 * @returns {Promise<object>} Success response from the backend.
 * @throws {UnauthorizedError} If the user is not authenticated.
 * @throws {ApiError} For other API-related errors (e.g., user not found, already connected).
 */
export const sendConnectionRequest = async (targetUserEmail) => {
  if (!targetUserEmail) {
    throw new Error("Target user email is required to send a connection request.");
  }

  console.log("sendConnectionRequest: Sending request to:", targetUserEmail);

  try {
    // Use apiClient for the POST request. Assumes endpoint is /connections/request
    // and expects { email: targetUserEmail } in the body.
    const response = await apiClient('/connections/request', {
      method: 'POST',
      body: { email: targetUserEmail },
      // Assuming this endpoint is prefixed with /api like others
      skipPrefix: false,

    });
    console.log("sendConnectionRequest: Successfully sent request to", targetUserEmail, response);
    return response; // Return the success response
  } catch (error) {
    console.error(`sendConnectionRequest: Failed to send request to ${targetUserEmail}:`, error);
    // Re-throw the specific error from apiClient
    throw error;
  }
};

// TODO: Add other user-related API functions here if needed, using apiClient
// e.g., searchUsers, getUserById, updateUser, etc.
