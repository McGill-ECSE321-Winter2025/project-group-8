import { useAuth } from '@/context/AuthContext'; // We might need this later for logout, or handle logout in calling components

// TODO: Replace with environment variable
// const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const BASE_URL = 'http://localhost:8080'; // Hardcoded for now
const API_PREFIX = '/api'; // API prefix for most endpoints

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

export class ConnectionError extends ApiError {
  constructor(message = 'Could not connect to server') {
    super(message, 0); // Status 0 for connection errors
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

const apiClient = async (endpoint, { body, method = 'GET', headers = {}, ...customConfig } = {}) => {
  const config = {
    method: method,
    headers: {
      'Content-Type': 'application/json',
      ...headers,
    },
    credentials: 'include', // Always include cookies for authentication
    ...customConfig,
  };

  if (body) {
    config.body = JSON.stringify(body);
  }

  // Add API prefix for non-auth endpoints
  let finalEndpoint = endpoint;
  if (!endpoint.startsWith('/auth')) {
    finalEndpoint = `${API_PREFIX}${endpoint}`;
  }

  const url = `${BASE_URL}${finalEndpoint}`; // Construct full URL
  console.log(`apiClient: ${method} request to ${url}`);

  try {
    // Debug cookies more comprehensively
    const cookies = document.cookie;
    const hasAuthToken = cookies.includes('accessToken');
    const isAuthenticated = cookies.includes('isAuthenticated=true');
    
    // Check authentication status for non-login/logout requests
    if (!endpoint.startsWith('/auth/login') && !endpoint.startsWith('/auth/logout')) {
      // For authenticated endpoints, verify the isAuthenticated cookie exists
      if (!isAuthenticated) {
        console.warn('apiClient: No authentication cookie found before making request to', url);
        console.debug('apiClient: Current cookies:', cookies);
        
        // For endpoints that require authentication, fail early with an UnauthorizedError
        if (!endpoint.startsWith('/games') && !endpoint.includes('/users/') && !endpoint.startsWith('/events')) {
          throw new UnauthorizedError('No authentication cookie found. Please log in.');
        }
      }
    }

    const response = await fetch(url, config);
    console.log(`apiClient: Response status ${response.status} for ${url}`);

    // Debug cookies after response
    const cookiesAfter = document.cookie;
    
    // For login requests, log headers and cookies in detail
    if (endpoint === '/auth/login' && method === 'POST') {
      console.log('apiClient: Cookies after login:', cookiesAfter);
      
      // Log the Set-Cookie header for debugging
      const setCookieHeader = response.headers.get('Set-Cookie');
      console.log('apiClient: Set-Cookie header:', setCookieHeader);
      
      // List all response headers for debugging
      const headers = {};
      response.headers.forEach((value, key) => {
        headers[key] = value;
      });
      console.log('apiClient: Response headers:', headers);
    }

    if (!response.ok) {
      const contentType = response.headers.get("content-type");
      let errorMessage = `Request failed with status ${response.status}`;
      
      try {
        // Try to get error details - could be JSON or text
        if (contentType && contentType.indexOf("application/json") !== -1) {
          const errorData = await response.json();
          errorMessage = errorData.message || errorData.error || errorMessage;
        } else {
          errorMessage = await response.text() || errorMessage;
        }
      } catch (parseError) {
        // If parsing fails, use default error message
        console.warn("Error parsing error response:", parseError);
      }
      
      if (response.status === 401) {
        // For 401 Unauthorized, throw an UnauthorizedError
        console.warn(`apiClient: Unauthorized error for ${url}. Cookie exists: ${document.cookie.includes('isAuthenticated=true')}`);
        throw new UnauthorizedError(errorMessage);
      }
      
      // For other errors, log them
      console.error(`API Error ${response.status}: ${errorMessage} for ${method} ${url}`);
      
      if (response.status === 403) {
        // Throw specific error for forbidden access
        throw new ForbiddenError(errorMessage);
      }
      // Throw generic API error for other non-ok statuses
      throw new ApiError(errorMessage, response.status);
    }

    // Handle successful responses
    // Check if response has content to parse
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.indexOf("application/json") !== -1) {
      try {
        return await response.json(); // Parse JSON body
      } catch (err) {
        console.error("Error parsing JSON response:", err);
        throw new ApiError("Invalid JSON response from server", response.status);
      }
    } else {
      // Handle non-JSON responses (e.g., 204 No Content or text)
      // If status is 204, return null or undefined as there's no body
      if (response.status === 204) {
          return null; 
      }
      // Otherwise, return the text content if needed, or just indicate success
      return await response.text(); // Or return { success: true }; 
    }

  } catch (error) {
    // Check if this is a network error (connection refused)
    if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
      const connectionError = new ConnectionError(
        `Could not connect to server at ${BASE_URL}. Please ensure the backend server is running.`
      );
      console.error('API Connection Error:', connectionError);
      throw connectionError;
    }
    
    // Only log non-401 errors or non-UnauthorizedError instances
    if (!(error instanceof UnauthorizedError)) {
      console.error('API Client Error:', error);
    }

    // Re-throw the error so it can be handled by the calling code
    throw error; 
  }
};

export default apiClient;