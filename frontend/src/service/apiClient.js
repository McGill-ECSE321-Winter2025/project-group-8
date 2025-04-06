// Base API client for making HTTP requests to the backend
// This is a simplified version that removes complex refresh token logic
// and focuses on clean error handling and proper URL construction

// Base URL for API requests - should be configured from environment in production
const BASE_URL = 'http://localhost:8080';
const API_PREFIX = '/api';
const DEFAULT_TIMEOUT_MS = 8000; // 8 second timeout

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
 * Utility to build a complete URL from an endpoint
 * @param {string} endpoint - The API endpoint
 * @param {boolean} skipPrefix - Whether to skip adding the API prefix
 * @returns {string} The complete URL
 */
const buildUrl = (endpoint, skipPrefix = false) => {
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
  skipPrefix = false,
  ...customConfig 
} = {}) => {
  // Build request configuration
  const config = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...headers,
    },
    credentials: 'include', // Always include cookies
    ...customConfig,
  };

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
    console.log(`API Request: ${method} ${url}`);
    const response = await fetch(url, config);
    
    // Clear the timeout
    clearTimeout(timeoutId);
    
    // Handle different response statuses
    if (!response.ok) {
      await handleErrorResponse(response);
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
    console.error('API request error:', error);
    throw new ApiError(error.message || 'Unknown error occurred', 500);
  }
};

/**
 * Handle error responses from the API
 * @param {Response} response - The fetch response object
 * @throws {ApiError} - Different types of API errors based on status code
 */
async function handleErrorResponse(response) {
  let errorMessage = `Request failed with status ${response.status}`;
  let errorData = null;
  
  // Try to parse error details from response
  try {
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      errorData = await response.json();
      errorMessage = errorData.message || errorMessage;
    } else {
      errorMessage = await response.text() || errorMessage;
    }
  } catch (e) {
    console.warn('Could not parse error response', e);
  }
  
  // Handle different error status codes
  switch (response.status) {
    case 400:
      throw new ApiError(errorMessage, 400);
    case 401:
      throw new UnauthorizedError(errorMessage);
    case 403:
      throw new ForbiddenError(errorMessage);
    case 404:
      throw new NotFoundError(errorMessage);
    case 410:
      throw new ApiError('Resource gone', 410);
    case 422:
      throw new ApiError(errorMessage, 422);
    case 429:
      throw new ApiError('Too many requests, please try again later', 429);
    case 500:
    case 502:
    case 503:
    case 504:
      throw new ApiError('Server error, please try again later', response.status);
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