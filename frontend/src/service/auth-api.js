import apiClient from './apiClient';

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