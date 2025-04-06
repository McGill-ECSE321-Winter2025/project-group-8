import apiClient, { getCookieAuthState, UnauthorizedError, ConnectionError, setAuthInProgress } from './apiClient'; // Import setAuthInProgress

// Base URL from apiClient for consistent configuration
const BASE_URL = 'http://localhost:8080';

/**
 * Utility function to get auth headers with user ID
 * @returns {Object} Headers object with X-User-Id if available
 */
const getAuthHeaders = () => {
  const userId = localStorage.getItem('userId');
  return userId ? { 'X-User-Id': userId } : {};
};

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
    // Set auth in progress flag before making the request
    setAuthInProgress(true);
    console.log('[AuthAPI] Login started. Auth in progress: true');

    // Clear potentially stale client-side auth state indicators *before* login attempt
    // This helps prevent race conditions where old state might interfere.
    document.cookie = "isAuthenticated=false; path=/; max-age=0";
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('user');
    localStorage.removeItem('rememberMe');
    console.log('[AuthAPI] Cleared potential stale client-side auth state.');

    // Make the login request
    const response = await apiClient('/auth/login', {
      method: 'POST',
      body: { email, password, rememberMe },
      credentials: 'include', // Crucial for receiving HttpOnly cookies
      requiresAuth: false, // Login itself doesn't require prior auth
      skipPrefix: true // Auth endpoint is likely not under /api
    });

    // Log full response for debugging
    console.log('[AuthAPI] Login response full structure:', JSON.stringify(response, null, 2));

    // Backend sets HttpOnly accessToken cookie and a non-HttpOnly isAuthenticated cookie
    // We don't need to extract or store tokens manually
    
    // Verify that we have the isAuthenticated cookie now
    const hasAuthCookie = document.cookie.includes('isAuthenticated=true');
    console.log('[AuthAPI] Authentication cookie check:', hasAuthCookie ? 'Cookie present' : 'Cookie missing');
    
    // Small delay to potentially allow browser to process Set-Cookie headers
    await new Promise(resolve => setTimeout(resolve, 100));
    console.log('[AuthAPI] Cookie state after login API call and delay:', getCookieAuthState());

    // The response body should contain user summary data.
    if (response && response.id) {
      console.log('[AuthAPI] Login API call successful. User data received:', response);

      // Store user data in localStorage as a backup/convenience
      localStorage.setItem('userId', response.id);
      localStorage.setItem('userEmail', response.email);
      localStorage.setItem('user', JSON.stringify(response));
      localStorage.setItem('rememberMe', rememberMe ? 'true' : 'false'); // Store rememberMe preference

      // Auth process complete
      setAuthInProgress(false);
      console.log('[AuthAPI] Login finished. Auth in progress: false');

      return response; // Return the user summary DTO
    }

    // If we get here, the login response wasn't valid (e.g., missing user ID)
    console.error('[AuthAPI] Login failed: Invalid response from login endpoint:', response);
    setAuthInProgress(false);
    console.log('[AuthAPI] Login finished (invalid response). Auth in progress: false');
    throw new Error("Invalid response from login endpoint");

  } catch (error) {
    // Ensure auth in progress flag is reset on error
    setAuthInProgress(false);
    console.error('[AuthAPI] Login error caught:', error);
    console.log('[AuthAPI] Login finished (error). Auth in progress: false');
    // Clear potentially partially set cookies/localStorage on error
    document.cookie = "isAuthenticated=false; path=/; max-age=0";
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('user');
    localStorage.removeItem('rememberMe');
    throw error; // Re-throw the error for the caller (e.g., LoginPage) to handle
  }
};

/**
 * Logs out the current user by clearing authentication cookies and state.
 * @returns {Promise<boolean>} - True if logout process completed (even if API call failed)
 */
export const logoutUser = async () => {
  try {
    // Set auth in progress flag before making the request
    setAuthInProgress(true);
    console.log('[AuthAPI] Logout started. Auth in progress: true');

    // Clear client-side state *first* to ensure immediate logout feel
    document.cookie = "isAuthenticated=false; path=/; max-age=0";
    // The HttpOnly accessToken cookie will be cleared by the backend response
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('user');
    localStorage.removeItem('rememberMe');
    console.log('[AuthAPI] Cleared client-side auth state.');

    // Make the logout request to the backend
    await apiClient('/auth/logout', {
      method: 'POST',
      credentials: 'include', // Needed to potentially clear HttpOnly cookie via backend response
      requiresAuth: false, // Logout should work even if not fully authenticated
      skipPrefix: true // Auth endpoint
    });
    console.log('[AuthAPI] Logout API call successful.');

    // Auth process complete
    setAuthInProgress(false);
    console.log('[AuthAPI] Logout finished. Auth in progress: false');
    console.log('[AuthAPI] Cookie state after logout:', getCookieAuthState());
    return true;

  } catch (error) {
    // Ensure auth in progress flag is reset on error
    setAuthInProgress(false);
    console.error('[AuthAPI] Logout API error:', error);
    console.log('[AuthAPI] Logout finished (API error). Auth in progress: false');

    // Even if the server request fails, ensure client-side state is cleared
    // Force removal again just in case
    document.cookie = "isAuthenticated=false; path=/; max-age=0";
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('user');
    localStorage.removeItem('rememberMe');
    console.log('[AuthAPI] Ensured client-side state cleared after logout API error.');
    console.log('[AuthAPI] Cookie state after failed logout API call:', getCookieAuthState());

    // Consider the user logged out on the client even if the backend call failed
    return true;
  }
};

/**
 * Gets the current authentication status by fetching the user profile.
 * Relies on the browser automatically sending the HttpOnly accessToken cookie.
 * @returns {Promise<Object|null>} - User profile object if authenticated, null otherwise
 */
export const checkAuthStatus = async () => {
  try {
    console.log('[AuthAPI] Checking auth status via /users/me...');
    console.log('[AuthAPI] Cookie state before /users/me check:', getCookieAuthState());

    // Use the user profile endpoint to check authentication
    // apiClient handles credentials: 'include' automatically
    const userProfile = await apiClient('/users/me', {
      method: 'GET',
      skipPrefix: true, // Endpoint is likely /users/me, not /api/users/me
      suppressErrors: true, // Don't throw ApiError for 401, let us handle it
      requiresAuth: true, // This endpoint definitely requires auth
      headers: getAuthHeaders() // Use consistent auth headers
    });

    // If we get a valid response, user is authenticated
    if (userProfile && userProfile.id) {
      console.log('[AuthAPI] Auth status check successful. User:', userProfile.email);
      // *** REMOVED client-side cookie setting here ***
      // Backend should have already set necessary cookies if needed.
      // Frontend should not try to manage hasAccessToken cookie.
      // It can set isAuthenticated if needed by other parts, but ideally AuthContext handles this.

      // Ensure localStorage is consistent (backup)
      if (!localStorage.getItem('userId')) {
        localStorage.setItem('userId', userProfile.id);
      }
      if (!localStorage.getItem('userEmail')) {
         localStorage.setItem('userEmail', userProfile.email);
      }
       // Optionally update the full user object in localStorage
      localStorage.setItem('user', JSON.stringify(userProfile));

      return userProfile; // Return the user profile object on success
    }

    // If response was invalid or empty, user is not authenticated.
    console.log('[AuthAPI] Auth status check returned no valid user profile.');
    // *** REMOVED client-side cookie clearing here ***
    // Let login/logout functions manage cookie state. A failed check doesn't
    // necessarily mean cookies *should* be cleared (e.g., could be temporary network issue).
    return null;

  } catch (error) {
    // Handle specific error types from apiClient
    if (error instanceof UnauthorizedError) {
      // 401 Unauthorized: User is definitely not logged in according to the backend.
      console.log('[AuthAPI] Auth status check failed: Unauthorized (401)');
      // *** REMOVED client-side cookie clearing here ***
      // Let login/logout functions handle cookie state.
      return null;
    } else if (error instanceof ConnectionError) {
      // Network error: Cannot reach the server to verify.
      console.error('[AuthAPI] Connection error during auth status check:', error.message);
      // We cannot confirm auth status. Returning null is safest.
      // We could potentially trust existing cookies here, but it's risky.
      // *** REMOVED client-side cookie clearing here ***
      return null;
    } else {
      // Other errors (e.g., 500 Internal Server Error, unexpected issues)
      console.error('[AuthAPI] Unexpected error during auth status check:', error);
      // *** REMOVED client-side cookie clearing here ***
      return null;
    }
  }
};

/**
 * Gets the current user's profile (similar to checkAuthStatus but throws errors).
 * @returns {Promise<Object>} - The user's profile data
 * @throws {UnauthorizedError} If not authenticated
 * @throws {ApiError} For other errors
 */
export const getUserProfile = async () => {
  try {
    console.log('[AuthAPI] Getting user profile via /users/me...');
    const userProfile = await apiClient('/users/me', {
      method: 'GET',
      skipPrefix: true, // Endpoint is likely /users/me
      suppressErrors: false, // Let errors propagate
      requiresAuth: true,
      headers: getAuthHeaders() // Use consistent auth headers
    });

    // If we get a valid response, ensure localStorage is consistent
    if (userProfile && userProfile.id) {
       if (!localStorage.getItem('userId')) {
         localStorage.setItem('userId', userProfile.id);
       }
       if (!localStorage.getItem('userEmail')) {
          localStorage.setItem('userEmail', userProfile.email);
       }
       localStorage.setItem('user', JSON.stringify(userProfile));
       // *** REMOVED client-side cookie setting ***
    } else {
       // Should not happen if API call succeeded, but as a safeguard:
       console.warn('[AuthAPI] getUserProfile succeeded but returned invalid data:', userProfile);
       throw new ApiError("Invalid user profile data received", 500);
    }

    return userProfile;
  } catch (error) {
    console.error('[AuthAPI] Error fetching user profile:', error);
    // If unauthorized, ensure client-side state reflects this (though logout should handle it)
    if (error instanceof UnauthorizedError) {
       // Optionally trigger a logout here if needed, but AuthContext usually handles this
       // document.cookie = "isAuthenticated=false; path=/; max-age=0";
       // localStorage.clear(); // Or specific items
    }
    throw error; // Re-throw the error
  }
};

/**
 * Registers a new user
 * @param {Object} userData - Registration data (name, email, password, gameOwner boolean)
 * @returns {Promise<Object>} - The created user account details (or success message)
 */
export const registerUser = async (userData) => {
  if (!userData.email || !userData.password || !userData.name) {
    throw new Error("Required fields missing for registration (name, email, password)");
  }

  // Backend expects 'username', map 'name' to it
  const payload = {
    username: userData.name,
    email: userData.email,
    password: userData.password,
    gameOwner: userData.gameOwner || false // Default to false if not provided
  };

  // Use POST /account endpoint
  return apiClient('/account', {
    method: 'POST',
    body: payload,
    requiresAuth: false, // Registration doesn't require prior auth
    skipPrefix: true // Endpoint is likely /account
  });
};

/**
 * Requests a password reset for a user
 * @param {string} email - The user's email
 * @returns {Promise<Object>} - Response from the backend
 */
export const requestPasswordReset = async (email) => {
  if (!email) {
    throw new Error("Email is required");
  }

  return apiClient('/auth/request-password-reset', {
    method: 'POST',
    body: { email },
    requiresAuth: false,
    skipPrefix: true
  });
};

/**
 * Resets a user's password using a token
 * @param {string} token - The reset token
 * @param {string} newPassword - The new password
 * @returns {Promise<Object>} - Response from the backend
 */
export const resetPassword = async (token, newPassword) => {
  if (!token || !newPassword) {
    throw new Error("Token and new password are required");
  }

  return apiClient('/auth/perform-password-reset', {
    method: 'POST',
    body: { token, newPassword },
    requiresAuth: false,
    skipPrefix: true
  });
};

/**
 * Updates the current user's profile (e.g., username, password)
 * @param {Object} profileData - The profile data to update (e.g., { username, password, newPassword })
 * @returns {Promise<Object>} - Success message or updated user data
 */
export const updateUserProfile = async (profileData) => {
  // Backend endpoint seems to be PUT /account
  // It requires email, username, password (current), and optionally newPassword
  if (!profileData || !profileData.email || !profileData.username || !profileData.password) {
     throw new Error("Email, username, and current password are required to update profile.");
  }
  return apiClient('/account', { // Use PUT /account based on backend controller
    method: 'PUT',
    body: profileData,
    requiresAuth: true, // Requires authentication
    skipPrefix: true
  });
};