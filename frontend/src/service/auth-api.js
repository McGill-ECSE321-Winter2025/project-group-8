import apiClient, { getCookieAuthState } from './apiClient';

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
    // This will prevent other requests from running before login completes
    const { setAuthInProgress } = await import('./apiClient');
    setAuthInProgress(true);
    
    // Clear any existing auth cookies before attempting login
    document.cookie = "isAuthenticated=false; path=/; max-age=0";
    document.cookie = "hasAccessToken=false; path=/; max-age=0";
    
    // Make the login request
    const response = await apiClient('/auth/login', {
      method: 'POST',
      body: { email, password, rememberMe },
      credentials: 'include',
    });
    
    // With HttpOnly cookie auth, we don't need to look for a token in the response
    // We just need to make sure we got a valid user object back
    if (response && response.id) {
      // Log cookie state for debugging
      console.log('Cookie state after login:', getCookieAuthState());
      
      // Set both auth cookies simultaneously to ensure a consistent state
      // Use SameSite=Lax for better compatibility and security
      const maxAge = rememberMe ? 7 * 24 * 60 * 60 : 24 * 60 * 60; // 7 days if remember me, otherwise 24 hours
      const cookieOptions = `path=/; max-age=${maxAge}; SameSite=Lax`;
      document.cookie = `isAuthenticated=true; ${cookieOptions}`;
      document.cookie = `hasAccessToken=true; ${cookieOptions}`;
      
      // Small delay to ensure cookies are set before continuing
      await new Promise(resolve => setTimeout(resolve, 200));
      
      // Store user data in localStorage as backup
      if (response.id) {
        localStorage.setItem('userId', response.id);
      }
      if (response.email) {
        localStorage.setItem('userEmail', response.email);
      }
      
      // Auth process complete
      setAuthInProgress(false);
      
      return response;
    }
    
    // If we get here, the login response wasn't valid
    setAuthInProgress(false);
    throw new Error("Invalid response from login endpoint");
  } catch (error) {
    // Ensure auth in progress flag is reset on error
    const { setAuthInProgress } = await import('./apiClient');
    setAuthInProgress(false);
    
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
    // Set auth in progress flag before making the request
    const { setAuthInProgress } = await import('./apiClient');
    setAuthInProgress(true);
    
    // Clear auth cookies first to prevent race conditions
    console.log('Clearing auth cookies before logout request');
    document.cookie = "isAuthenticated=false; path=/; max-age=0";
    document.cookie = "hasAccessToken=false; path=/; max-age=0";
    
    // Clear localStorage
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    
    // Make the logout request
    await apiClient('/auth/logout', {
      method: 'POST',
      credentials: 'include',
      // Skip auth checks for logout - it should work even if auth is not ready
      requiresAuth: false
    });
    
    // Log cookie state after logout
    console.log('Cookie state after logout:', getCookieAuthState());
    
    // Double-check that cookies are cleared
    if (getCookieAuthState().isAuthenticated || getCookieAuthState().hasAccessToken) {
      console.log('Forcing cookie removal after logout');
      document.cookie = "isAuthenticated=false; path=/; max-age=0";
      document.cookie = "hasAccessToken=false; path=/; max-age=0";
    }
    
    // Auth process complete
    setAuthInProgress(false);
    return true;
  } catch (error) {
    // Ensure auth in progress flag is reset on error
    const { setAuthInProgress } = await import('./apiClient');
    
    console.error('Logout error:', error);
    // Even if the server request fails, consider the user logged out
    // Force cookies to be removed
    document.cookie = "isAuthenticated=false; path=/; max-age=0";
    document.cookie = "hasAccessToken=false; path=/; max-age=0";
    
    // Clear localStorage
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    
    setAuthInProgress(false);
    return true;
  }
};

/**
 * Gets the current authentication status with retry logic
 * @param {number} retryCount - Number of retry attempts made (default 0)
 * @returns {Promise<boolean>} - Whether the user is authenticated
 */
export const checkAuthStatus = async (retryCount = 0) => {
  const MAX_RETRIES = 3; // Increase from 2 to 3
  
  try {
    // Log cookie state before auth check
    const cookieState = getCookieAuthState();
    console.log('Cookie state before auth check:', cookieState);
    
    // If we have both cookies and we've already retried,
    // trust the cookies and return true to prevent endless retries
    if (cookieState.isAuthenticated && cookieState.hasAccessToken && retryCount > 0) {
      console.log('Using cookie auth state after retry:', cookieState.isAuthenticated);
      return true;
    }
    
    // Use the user profile endpoint to check authentication
    const userProfile = await apiClient('/users/me', {
      method: 'GET',
      credentials: 'include',
      skipPrefix: true,
      suppressErrors: true
    });
    
    // If we get a valid response, user is authenticated
    // Also ensure cookies are set
    if (userProfile && userProfile.id) {
      // Set both cookies with identical options to ensure a consistent state
      const cookieOptions = `path=/; max-age=86400; SameSite=Lax`; // 24 hours
      console.log('Setting auth cookies after successful profile check');
      document.cookie = `isAuthenticated=true; ${cookieOptions}`;
      document.cookie = `hasAccessToken=true; ${cookieOptions}`;
      
      // Store user ID in localStorage for backup
      if (userProfile.id && !localStorage.getItem('userId')) {
        localStorage.setItem('userId', userProfile.id);
      }
      
      return true;
    }
    
    // If we get here but no error was thrown,
    // the response was empty or invalid, so user is not authenticated
    console.log('Auth check: User profile check returned but no valid user data');
    document.cookie = "isAuthenticated=false; path=/; max-age=0";
    document.cookie = "hasAccessToken=false; path=/; max-age=0";
    return false;
  } catch (error) {
    // If unauthorized, user is not authenticated
    if (error.status === 401) {
      console.log('Auth check: User is not authenticated (401)');
      document.cookie = "isAuthenticated=false; path=/; max-age=0";
      document.cookie = "hasAccessToken=false; path=/; max-age=0";
      return false;
    }
    
    // For network errors, we can't determine auth state directly
    // Check if we have cookies that say we're authenticated
    if (error.status === 0 || error.name === 'ConnectionError') {
      console.error('Network error during auth check');
      
      // If we have both cookies, trust them during network issues
      const cookieState = getCookieAuthState();
      if (cookieState.isAuthenticated && cookieState.hasAccessToken) {
        console.log('Network error, but using cookie auth state:', true);
        return true;
      }
      
      // If we have partial cookie state, try to recover by setting both cookies
      if (cookieState.isAuthenticated || cookieState.hasAccessToken) {
        console.log('Partial cookie state during network error, attempting to fix');
        const cookieOptions = `path=/; max-age=86400; SameSite=Lax`; // 24 hours
        document.cookie = `isAuthenticated=true; ${cookieOptions}`;
        document.cookie = `hasAccessToken=true; ${cookieOptions}`;
        return true;
      }
      
      // If we've reached max retries, use whatever auth state we have
      if (retryCount >= MAX_RETRIES) {
        console.log(`Max retries (${MAX_RETRIES}) reached during network error`);
        // If we have a userId in localStorage, assume we might be authenticated
        if (localStorage.getItem('userId')) {
          console.log('Using localStorage userId as fallback for auth state');
          return true;
        }
        throw error;
      }
      
      // Retry after a delay with exponential backoff
      const delay = 1000 * Math.pow(2, retryCount);
      console.log(`Network error during auth check, retrying in ${delay}ms (${retryCount + 1}/${MAX_RETRIES})...`);
      await new Promise(resolve => setTimeout(resolve, delay));
      return checkAuthStatus(retryCount + 1);
    }
    
    console.log('Auth check: Failed with error', error.status, error.message);
    return false;
  }
};

/**
 * Gets the current user's profile
 * @returns {Promise<Object>} - The user's profile data
 */
export const getUserProfile = async () => {
  try {
    const userProfile = await apiClient('/users/me', {
      method: 'GET',
      credentials: 'include',
      skipPrefix: true
    });
    
    // If we get a valid response, ensure cookies are set
    if (userProfile && userProfile.id) {
      if (!getCookieAuthState().isAuthenticated) {
        console.log('Setting isAuthenticated cookie after successful profile fetch');
        document.cookie = "isAuthenticated=true; path=/; max-age=86400"; // 24 hours
      }
      if (!getCookieAuthState().hasAccessToken) {
        console.log('Setting hasAccessToken cookie after successful profile fetch');
        document.cookie = "hasAccessToken=true; path=/; max-age=86400"; // 24 hours
      }
    }
    
    return userProfile;
  } catch (error) {
    // If unauthorized, ensure cookies reflect this state
    if (error.status === 401) {
      document.cookie = "isAuthenticated=false; path=/; max-age=0";
      document.cookie = "hasAccessToken=false; path=/; max-age=0";
    }
    throw error;
  }
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
  return apiClient('/users/me', {
    method: 'PUT',
    body: profileData,
    credentials: 'include',
    skipPrefix: true
  });
}; 