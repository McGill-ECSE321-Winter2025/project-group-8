// Base API client for making HTTP requests to the backend
// This is a simplified version that removes complex refresh token logic
// and focuses on clean error handling and proper URL construction

// Base URL for API requests - should be configured from environment in production
const BASE_URL = 'http://localhost:8080';
const API_PREFIX = '/api';
const DEFAULT_TIMEOUT_MS = 8000; // 8 second timeout

// Indicator for auth in progress
let authInProgress = false;
let lastAuthCheck = 0;

// Custom error classes for better error handling
export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

export class ConnectionError extends ApiError {
  constructor(message = 'Could not connect to server') {
    super(message, 0);
    this.name = 'ConnectionError';
  }
}

export class UnauthorizedError extends ApiError {
  constructor(message = 'Authentication required') {
    super(message, 401);
    this.name = 'UnauthorizedError';
  }
}

export class ForbiddenError extends ApiError {
  constructor(message = 'Forbidden') {
    super(message, 403);
    this.name = 'ForbiddenError';
  }
}

export class NotFoundError extends ApiError {
  constructor(message = 'Resource not found') {
    super(message, 404);
    this.name = 'NotFoundError';
  }
}

/**
 * Set the authentication in progress state
 * @param {boolean} inProgress - Whether authentication is in progress
 */
export const setAuthInProgress = (inProgress) => {
  authInProgress = inProgress;
  // Record timestamp of last auth state change
  if (inProgress) {
    lastAuthCheck = Date.now();
  }
};

/**
 * Utility to build a complete URL from an endpoint
 * @param {string} endpoint - The API endpoint
 * @param {boolean} skipPrefix - Whether to skip adding the API prefix (defaults to true)
 * @returns {string} The complete URL
 */
const buildUrl = (endpoint, skipPrefix = true) => {
  // If endpoint already starts with http, it's already a full URL
  if (endpoint.startsWith('http')) {
    return endpoint;
  }

  // If endpoint already includes the base URL
  if (endpoint.includes(BASE_URL)) {
    return endpoint;
  }

  // Auth endpoints and some others don't need API prefix
  const noApiPrefixPaths = ['/auth', '/profile'];
  const hasPrefix = endpoint.startsWith(API_PREFIX);
  const shouldNotPrefix = noApiPrefixPaths.some(path => endpoint.startsWith(path));
  
  // Skip prefix if explicitly requested or if it's a special path
  if (skipPrefix || shouldNotPrefix || hasPrefix) {
    return `${BASE_URL}${endpoint}`;
  }
  
  // Add API prefix for regular API endpoints
  return `${BASE_URL}${API_PREFIX}${endpoint}`;
};

/**
 * Check if an endpoint is an authentication endpoint
 * @param {string} endpoint - The API endpoint
 * @returns {boolean} - Whether this is an auth endpoint
 */
const isAuthEndpoint = (endpoint) => {
  return endpoint.includes('/auth/login') || 
         endpoint.includes('/auth/logout') || 
         endpoint.includes('/users/me') ||
         endpoint.includes('/api/users/me');
};

/**
 * Get all cookies as an object
 * @returns {Object} Object containing all cookies
 */
const getCookies = () => {
  return document.cookie.split('; ').reduce((prev, current) => {
    const [name, ...value] = current.split('=');
    prev[name] = value.join('=');
    return prev;
  }, {});
};

/**
 * Get the authentication token from storage
 * @returns {string|null} The token or null if not found
 */
const getAuthToken = () => {
  // Since we're using HttpOnly cookies, we shouldn't be accessing the token directly
  // Check for both isAuthenticated and hasAccessToken cookies for complete auth state
  const cookies = getCookies();
  const hasAuthCookie = cookies.isAuthenticated === 'true';
  const hasAccessTokenCookie = cookies.hasAccessToken === 'true';
  
  // If we have both cookies, we have a valid auth state via HttpOnly cookies
  if (hasAuthCookie && hasAccessTokenCookie) {
    console.log('Auth token: Using cookie-based authentication');
    return 'cookie-auth-present';
  } else if (hasAuthCookie) {
    // If we only have isAuthenticated but not hasAccessToken, we might have a partial state
    // Still return a token indicator but log the inconsistency
    console.log('Auth token: Using partial cookie authentication (missing hasAccessToken)');
    return 'cookie-auth-present';
  }
  
  // Fallback to localStorage for backwards compatibility
  const token = localStorage.getItem('token');
  if (!token) return null;
  
  // Check if the token might be malformed or corrupted
  if (token.length < 10) {
    console.warn('Potentially invalid token detected');
  }
  
  return token;
};

/**
 * Check if authentication is fully ready
 * @returns {boolean} Whether authentication is ready for API requests
 */
const isAuthReady = () => {
  if (authInProgress) {
    console.log('Auth in progress, not ready');
    return false;
  }
  
  // Check for isAuthenticated cookie first
  const cookies = getCookies();
  const hasAuthCookie = cookies.isAuthenticated === 'true';
  const hasAccessTokenCookie = cookies.hasAccessToken === 'true';
  
  // If we have both authentication cookies, we're good to go
  if (hasAuthCookie && hasAccessTokenCookie) {
    console.log('Auth ready: Found both isAuthenticated and hasAccessToken cookies');
    return true;
  }
  
  // If isAuthenticated without hasAccessToken, attempt to repair by setting both
  if (hasAuthCookie && !hasAccessTokenCookie) {
    console.log('Auth state partially ready: Setting missing hasAccessToken cookie');
    document.cookie = "hasAccessToken=true; path=/; max-age=86400; SameSite=Lax"; // 24 hours
    return true;
  }
  
  // If hasAccessToken without isAuthenticated, attempt to repair by setting both
  if (!hasAuthCookie && hasAccessTokenCookie) {
    console.log('Auth state partially ready: Setting missing isAuthenticated cookie');
    document.cookie = "isAuthenticated=true; path=/; max-age=86400; SameSite=Lax"; // 24 hours
    return true;
  }
  
  // Fallback to localStorage for backwards compatibility
  const token = localStorage.getItem('token');
  const userId = localStorage.getItem('userId');
  
  if (token && userId) {
    console.log('Auth ready: Found token and userId in localStorage');
    return true;
  }
  
  console.log('Auth not ready: No authentication data found');
  return false;
};

/**
 * Get cookie auth state for debugging
 * @returns {Object} Cookie authentication state
 */
export const getCookieAuthState = () => {
  const cookies = getCookies();
  return {
    isAuthenticated: cookies.isAuthenticated === 'true',
    hasAccessToken: cookies.hasAccessToken === 'true',
    allCookies: document.cookie
  };
};

/**
 * Main API client function for making HTTP requests
 * @param {string} endpoint - The API endpoint
 * @param {Object} options - Request options
 * @returns {Promise<any>} - Response data
 */
const apiClient = async (endpoint, { 
  body, 
  method = 'GET',
  headers = {},
  timeout = DEFAULT_TIMEOUT_MS,
  skipPrefix = true,
  suppressErrors = false,
  retryOnAuth = true,
  requiresAuth = true,  // New parameter to indicate if endpoint requires authentication
  ...customConfig 
} = {}) => {
  // Special handling for /users/me endpoint - don't suppress errors by default
  const isUserMeEndpoint = endpoint.includes('/users/me');
  const effectiveSuppressErrors = isUserMeEndpoint ? false : suppressErrors;

  // Skip auth checks for auth endpoints
  const isAuthRelatedEndpoint = isAuthEndpoint(endpoint);

  // If auth is in progress and this isn't an auth endpoint, wait a bit or abort
  if (authInProgress && !isAuthRelatedEndpoint) {
    if (!effectiveSuppressErrors) {
      console.log(`Auth in progress for request to ${endpoint}`);
    }
    
    // If retry is enabled and auth just started, wait longer and retry
    if (retryOnAuth && Date.now() - lastAuthCheck < 10000) { // Increased from 5000ms to 10000ms
      if (!effectiveSuppressErrors) {
        console.log(`Waiting for auth to complete before retrying...`);
      }
      // Wait longer - increase from 700ms to 1000ms
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Check again - if auth is still in progress but we haven't timed out yet, wait more
      if (authInProgress && Date.now() - lastAuthCheck < 10000) {
        console.log(`Auth still in progress, waiting a bit more...`);
        await new Promise(resolve => setTimeout(resolve, 1000));
      }
      
      // If auth is still in progress after waiting, abort
      if (authInProgress) {
        throw new UnauthorizedError('Authentication in progress');
      }
    } else {
      throw new UnauthorizedError('Authentication in progress');
    }
  }
  
  // Only check auth readiness for endpoints that require authentication
  // Make an exception for auth-related endpoints (login, logout, users/me)
  const shouldCheckAuth = requiresAuth && !isAuthRelatedEndpoint;
  
  // Check if auth is ready for secured endpoints
  if (shouldCheckAuth && !isAuthReady()) {
    if (!effectiveSuppressErrors) {
      console.log(`Auth not ready for request to ${endpoint}`);
    }
    
    if (retryOnAuth) {
      // Wait longer and check again - increase from 500ms to 1000ms
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // If still not ready, try one more time with a longer delay
      if (!isAuthReady()) {
        console.log(`Auth still not ready, waiting a bit more...`);
        await new Promise(resolve => setTimeout(resolve, 1500));
        
        // Final check
        if (!isAuthReady()) {
          throw new UnauthorizedError('Authentication not ready');
        }
      }
    } else {
      throw new UnauthorizedError('Authentication not ready');
    }
  }
  
  // Ensure credentials: 'include' for endpoints that need cookies
  if (isAuthRelatedEndpoint || requiresAuth) {
    customConfig.credentials = 'include';
  }
  
  // Build request configuration
  const config = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...headers,
    },
    credentials: 'include', // Always include cookies for all requests
    ...customConfig,
  };

  // Add Authorization header with token if available
  const token = getAuthToken();
  if (token) {
    // Use Bearer token format for the Authorization header
    config.headers.Authorization = `Bearer ${token}`;
    
    // Also add userId as query param for endpoints that need it
    if (endpoint.includes('?')) {
      // If endpoint already has query params, add userId if it doesn't exist
      if (!endpoint.includes('userId=')) {
        const userId = localStorage.getItem('userId');
        if (userId && !isAuthEndpoint(endpoint)) {
          endpoint = `${endpoint}&userId=${userId}`;
        }
      }
    } else {
      // If endpoint doesn't have query params, add userId
      const userId = localStorage.getItem('userId');
      if (userId && !isAuthEndpoint(endpoint)) {
        endpoint = `${endpoint}?userId=${userId}`;
      }
    }
  } else if (requiresAuth && !isAuthEndpoint(endpoint)) {
    // If token is missing but endpoint requires auth, try to get auth state from cookies
    const cookies = getCookies();
    if (cookies.isAuthenticated === 'true') {
      // Use a placeholder bearer token to indicate we're using cookie auth
      config.headers.Authorization = 'Bearer cookie-auth';
      
      // Add userId param if available in localStorage
      const userId = localStorage.getItem('userId');
      if (userId) {
        if (endpoint.includes('?')) {
          if (!endpoint.includes('userId=')) {
            endpoint = `${endpoint}&userId=${userId}`;
          }
        } else {
          endpoint = `${endpoint}?userId=${userId}`;
        }
      }
    } else {
      // If no token and no auth cookie, log the issue
      console.warn(`No authentication found for protected endpoint: ${endpoint}`);
    }
  }

  // Add body if provided
  if (body) {
    config.body = JSON.stringify(body);
  }

  // Build the complete URL
  const url = buildUrl(endpoint, skipPrefix);
  
  // Setup request timeout
  const controller = new AbortController();
  config.signal = customConfig.signal || controller.signal;
  const timeoutId = setTimeout(() => controller.abort(), timeout);
  
  try {
    // Make the request
    if (!effectiveSuppressErrors) {
      console.log(`API Request: ${method} ${url}`);
    }
    const response = await fetch(url, config);
    
    // Clear the timeout
    clearTimeout(timeoutId);
    
    // Handle different response statuses
    if (!response.ok) {
      await handleErrorResponse(response, effectiveSuppressErrors);
    }
    
    // Check for no content responses
    if (response.status === 204) {
      return null;
    }
    
    // Parse JSON response if available
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return await response.json();
    }
    
    // Return text for non-JSON responses
    return await response.text();
  } catch (error) {
    // Clear the timeout
    clearTimeout(timeoutId);
    
    // Handle aborted requests (timeout)
    if (error.name === 'AbortError') {
      throw new TimeoutError(`Request to ${url} timed out after ${timeout}ms`);
    }
    
    // Handle network errors
    if (error.message === 'Failed to fetch' || error.message.includes('NetworkError')) {
      throw new ConnectionError(`Could not connect to ${url}`);
    }
    
    // Rethrow API errors
    if (error instanceof ApiError) {
      throw error;
    }
    
    // Handle other errors
    if (!effectiveSuppressErrors) {
      console.error('API request error:', error);
    }
    throw new ApiError(error.message || 'Unknown error occurred', 500);
  }
};

/**
 * Handle error responses from the API
 * @param {Response} response - The fetch response object
 * @param {boolean} suppressErrors - Whether to suppress error logging
 * @throws {ApiError} - Different types of API errors based on status code
 */
async function handleErrorResponse(response, suppressErrors = false) {
  let errorMessage = `Request failed with status ${response.status}`;
  let errorData = null;
  
  // Try to parse error response body
  try {
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      errorData = await response.json();
      if (errorData.message) {
        errorMessage = errorData.message;
      }
    } else {
      const text = await response.text();
      if (text) {
        errorMessage = text;
      }
    }
  } catch (err) {
    // Ignore error parsing errors, use default message
    console.warn('Could not parse error response', err);
  }
  
  // Log error unless suppressed
  if (!suppressErrors) {
    console.log(`\n ${response.status} (${response.statusText}) Response:`, errorData || errorMessage);
  }
  
  // Throw appropriate error based on status code
  switch (response.status) {
    case 401:
      // Check if this is a token expiration or authentication required error
      if (errorMessage.includes('expired') || errorMessage.includes('Invalid token')) {
        // Clear auth state on token expiration
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('userEmail');
        localStorage.removeItem('user');
        // Also set isAuthenticated cookie to false
        document.cookie = "isAuthenticated=false; path=/";
        throw new UnauthorizedError('Your session has expired. Please log in again.');
      } else {
        // Try to refresh authentication state
        const cookies = getCookies();
        if (cookies.isAuthenticated === 'true') {
          // If cookie says we're authenticated but API says we're not,
          // there might be a cookie issue. Update the cookie.
          document.cookie = "isAuthenticated=false; path=/";
        }
        throw new UnauthorizedError(
          errorMessage || 'Authentication required. Please ensure you are logged in.'
        );
      }
    case 403:
      throw new ForbiddenError(errorMessage || 'You do not have permission to access this resource.');
    case 404:
      throw new NotFoundError(errorMessage || 'The requested resource was not found.');
    default:
      throw new ApiError(errorMessage, response.status);
  }
}

// Export TimeoutError class
export class TimeoutError extends ApiError {
  constructor(message = 'Request timed out') {
    super(message, 0);
    this.name = 'TimeoutError';
  }
}

export default apiClient;