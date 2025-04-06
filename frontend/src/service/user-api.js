import apiClient, { UnauthorizedError, ForbiddenError } from './apiClient';

/**
 * Fetches the profile information of the currently logged-in user.
 * Relies on the HttpOnly cookie for authentication.
 * @returns {Promise<object>} A promise that resolves to the user summary object (e.g., UserSummaryDto).
 * @throws {UnauthorizedError} If the user is not authenticated (no valid cookie).
 * @throws {ApiError} For other API-related errors.
 */
export const getUserProfile = async () => {
  console.log('getUserProfile: Attempting to fetch user profile');
  try {
    // Use the API endpoint for user profile with explicit credentials inclusion
    const userProfile = await apiClient('/users/me', {
      // Ensure credentials are included (should already be handled by apiClient but being explicit)
      credentials: 'include'
    }); 
    
    if (!userProfile) {
      console.error('getUserProfile: Received empty response from server');
      throw new Error('Empty user profile response');
    }
    
    console.log('getUserProfile: Successfully fetched profile', userProfile);
    
    // Ensure critical user fields exist
    if (!userProfile.id) {
      console.warn('getUserProfile: User profile missing ID field', userProfile);
    }
    
    return userProfile;
  } catch (error) {
    // For UnauthorizedError (401), user isn't logged in or session expired
    if (error instanceof UnauthorizedError) {
      console.log('getUserProfile: Unauthorized error (user not logged in or session expired)');
      // This is expected behavior for unauthenticated users
      // Still need to rethrow so AuthContext can handle it
      throw error;
    }
    
    // For unexpected errors, log them with more details
    console.error("getUserProfile: Error fetching user profile:", {
      name: error.name,
      message: error.message,
      status: error.status || 'unknown',
      stack: error.stack
    });
    
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
      console.log("Attempting to logout user");
      
      // Using the API prefix for consistency
      await apiClient('/auth/logout', { 
        method: 'POST',
        credentials: 'include' // Important: Include credentials to ensure cookies are sent
      }); 
      console.log("Logout API call successful");
      
      // Clear any locally stored user data
      localStorage.removeItem('boardgame_connect_user');
      
      // For client-side cleanup, also attempt to expire the cookie
      document.cookie = 'accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
      document.cookie = 'accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/api;';
      document.cookie = 'isAuthenticated=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
      document.cookie = 'isAuthenticated=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/api;';
      
      // Method 2: With domain
      const domain = window.location.hostname;
      document.cookie = `accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=${domain};`;
      document.cookie = `isAuthenticated=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=${domain};`;
      
      // Method 3: For root path
      document.cookie = 'accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/api;';
      document.cookie = 'isAuthenticated=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/api;';
      
      console.log("Client-side cookie deletion attempted. Current cookies:", document.cookie);
    } catch (error) {
      console.error("Error calling logout API:", error);
      
      // Clear local storage even if the API call fails
      localStorage.removeItem('boardgame_connect_user');
      
      // For client-side cleanup, also attempt to expire the cookie
      document.cookie = 'accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
      document.cookie = 'accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/api;';
      document.cookie = 'isAuthenticated=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
      document.cookie = 'isAuthenticated=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/api;';
      
      // Log current state of cookies
      console.log("After error, current cookies:", document.cookie);
      
      // Depending on requirements, you might still want to proceed with frontend logout
      // even if the backend call fails. Or re-throw if it's critical.
      // throw error; 
    }
};


// TODO: Add other user-related API functions here if needed, using apiClient
// e.g., searchUsers, getUserById, updateUser, etc.
