import apiClient from './apiClient';

/**
 * Logs in a user with email and password
 * @param {string} email - User's email
 * @param {string} password - User's password
 * @returns {Promise<Object>} - Object containing user data
 */
export const loginUser = async (email, password) => {
  console.log('auth-api: Attempting login for', email);
  
  if (!email || !password) {
    throw new Error("Email and password are required");
  }
  
  try {
    const userData = await apiClient('/auth/login', {
      method: 'POST',
      body: { email, password },
      credentials: 'include' // Send and receive cookies
    });
    
    console.log('auth-api: Login successful, user data received');
    
    // Return user data - authentication is handled by cookies
    return { userData };
  } catch (error) {
    console.error('auth-api: Login error', error);
    throw error;
  }
};

/**
 * Requests a password reset token for the specified email
 * @param {string} email - The email to request password reset for
 * @returns {Promise<void>} - A promise that resolves when the request is successful
 */
export const requestPasswordReset = async (email) => {
  if (!email) {
    throw new Error("Email is required to request password reset");
  }
  
  return apiClient('/auth/request-password-reset', {
    method: 'POST',
    body: { email }
  });
};

/**
 * Resets a password using a token
 * @param {string} token - The reset token
 * @param {string} newPassword - The new password
 * @returns {Promise<string>} - A promise that resolves to a success message
 */
export const resetPassword = async (token, newPassword) => {
  if (!token || !newPassword) {
    throw new Error("Token and new password are required");
  }
  
  return apiClient('/auth/perform-password-reset', {
    method: 'POST',
    body: { token, newPassword }
  });
}; 