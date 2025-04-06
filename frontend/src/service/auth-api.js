import apiClient from './apiClient';

/**
 * Logs in a user with email and password
 * @param {string} email - User's email
 * @param {string} password - User's password
 * @param {boolean} rememberMe - Whether to use a longer-lived session
 * @returns {Promise<Object>} - Object containing user data
 */
export const loginUser = async (email, password, rememberMe = false) => {
  if (!email || !password) {
    throw new Error("Email and password are required");
  }
  
  try {
    const userData = await apiClient('/auth/login', {
      method: 'POST',
      body: { email, password, rememberMe },
      credentials: 'include',
    });
    
    return userData;
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};

/**
 * Logs out the current user by clearing authentication cookies
 * @returns {Promise<void>}
 */
export const logoutUser = async () => {
  try {
    await apiClient('/auth/logout', {
      method: 'POST',
      credentials: 'include',
    });
    return true;
  } catch (error) {
    console.error('Logout error:', error);
    // Even if the server request fails, consider the user logged out
    return true;
  }
};

/**
 * Gets the current authentication status
 * @returns {Promise<boolean>} - Whether the user is authenticated
 */
export const checkAuthStatus = async () => {
  try {
    // Use the user profile endpoint to check authentication
    await apiClient('/api/users/me', {
      method: 'GET',
      credentials: 'include',
    });
    return true;
  } catch (error) {
    // If unauthorized, user is not authenticated
    if (error.status === 401) {
      return false;
    }
    // For network errors, we can't determine auth state
    if (error.status === 0) {
      console.error('Network error during auth check');
      throw error;
    }
    return false;
  }
};

/**
 * Gets the current user's profile
 * @returns {Promise<Object>} - The user's profile data
 */
export const getUserProfile = async () => {
  return apiClient('/api/users/me', {
    method: 'GET',
    credentials: 'include',
  });
};

/**
 * Registers a new user
 * @param {Object} userData - Registration data
 * @returns {Promise<Object>} - The created user
 */
export const registerUser = async (userData) => {
  if (!userData.email || !userData.password || !userData.name) {
    throw new Error("Required fields missing for registration");
  }
  
  return apiClient('/auth/register', {
    method: 'POST',
    body: userData,
  });
};

/**
 * Requests a password reset for a user
 * @param {string} email - The user's email
 * @returns {Promise<Object>} - Response
 */
export const requestPasswordReset = async (email) => {
  if (!email) {
    throw new Error("Email is required");
  }
  
  return apiClient('/auth/request-password-reset', {
    method: 'POST',
    body: { email },
  });
};

/**
 * Resets a user's password using a token
 * @param {string} token - The reset token
 * @param {string} newPassword - The new password
 * @returns {Promise<Object>} - Response
 */
export const resetPassword = async (token, newPassword) => {
  if (!token || !newPassword) {
    throw new Error("Token and new password are required");
  }
  
  return apiClient('/auth/perform-password-reset', {
    method: 'POST',
    body: { token, newPassword },
  });
};

/**
 * Updates the current user's profile
 * @param {Object} profileData - The profile data to update
 * @returns {Promise<Object>} - The updated user
 */
export const updateUserProfile = async (profileData) => {
  return apiClient('/api/users/me', {
    method: 'PUT',
    body: profileData,
    credentials: 'include',
  });
}; 