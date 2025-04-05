import { useAuth } from '@/context/AuthContext'; // We might need this later for logout, or handle logout in calling components

// TODO: Replace with environment variable
// const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const BASE_URL = 'http://localhost:8080'; // Hardcoded for now

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
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

// Get auth token from localStorage
const getAuthToken = () => {
  return localStorage.getItem('authToken');
};

const apiClient = async (endpoint, { body, method = 'GET', headers = {}, ...customConfig } = {}) => {
  const authToken = getAuthToken();
  
  const config = {
    method: method,
    headers: {
      'Content-Type': 'application/json',
      ...headers,
    },
    credentials: 'include', // Keep this to send cookies (server is using cookies for JWT)
    ...customConfig,
  };

  // Add Authorization header if token exists
  // NOTE: The server is currently using cookies, but we'll include the header as a fallback
  if (authToken) {
    config.headers['Authorization'] = `Bearer ${authToken}`;
  }

  if (body) {
    config.body = JSON.stringify(body);
  }

  const url = `${BASE_URL}${endpoint}`; // Construct full URL

  try {
    const response = await fetch(url, config);

    if (!response.ok) {
      const errorData = await response.text(); // Try to get error details
      
      if (response.status === 401) {
        // For 401 Unauthorized, clear token as it might be expired
        if (authToken) {
          localStorage.removeItem('authToken');
        }
        throw new UnauthorizedError(errorData || 'Authentication required');
      }
      
      // For other errors, log them
      console.error(`API Error ${response.status}: ${errorData} for ${method} ${url}`);
      
      if (response.status === 403) {
        // Throw specific error for forbidden access
        throw new ForbiddenError(errorData || 'Permission denied');
      }
      // Throw generic API error for other non-ok statuses
      throw new ApiError(errorData || `Request failed with status ${response.status}`, response.status);
    }

    // Handle successful responses
    // Check if response has content to parse
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.indexOf("application/json") !== -1) {
      return await response.json(); // Parse JSON body
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
    // Only log non-401 errors or non-UnauthorizedError instances
    if (!(error instanceof UnauthorizedError)) {
      console.error('API Client Error:', error);
    }

    // Re-throw the error so it can be handled by the calling code
    throw error; 
  }
};

export default apiClient;