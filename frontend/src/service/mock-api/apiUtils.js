/**
 * Mock API Utilities
 * 
 * This file contains utility functions for simulating API behavior,
 * including delays, errors, and standard API response formatting.
 */

// Simulate network delay (between 200-800ms)
export const delay = (ms = Math.floor(Math.random() * 600) + 200) => {
  return new Promise(resolve => setTimeout(resolve, ms));
};

// Success response formatter
export const createSuccessResponse = (data) => ({
  status: 'success',
  data,
  error: null,
  timestamp: new Date().toISOString()
});

// Error response formatter
export const createErrorResponse = (message, code = 400) => ({
  status: 'error',
  data: null,
  error: {
    message,
    code
  },
  timestamp: new Date().toISOString()
});

// Simulate API call with potential for errors
export const simulateApiCall = async (handler, errorRate = 0.05) => {
  // Wait for a random delay to simulate network latency
  await delay();
  
  // Randomly generate errors based on errorRate
  if (Math.random() < errorRate) {
    const errorMessages = [
      'Network error occurred',
      'Service temporarily unavailable',
      'Request timed out',
      'Server error'
    ];
    const randomErrorMessage = errorMessages[Math.floor(Math.random() * errorMessages.length)];
    return createErrorResponse(randomErrorMessage, 500);
  }
  
  try {
    // Call the handler function which contains the actual logic
    const result = await handler();
    return createSuccessResponse(result);
  } catch (error) {
    return createErrorResponse(error.message || 'An unexpected error occurred');
  }
};

// Helper for filtering arrays
export const filterItems = (items, filterFn) => {
  return items.filter(filterFn);
};

// Helper for pagination
export const paginateItems = (items, page = 1, pageSize = 10) => {
  const startIndex = (page - 1) * pageSize;
  const endIndex = startIndex + pageSize;
  const paginatedItems = items.slice(startIndex, endIndex);
  
  return {
    items: paginatedItems,
    pagination: {
      page,
      pageSize,
      totalItems: items.length,
      totalPages: Math.ceil(items.length / pageSize),
      hasMore: endIndex < items.length
    }
  };
};

// Deep clone objects to avoid mutation
export const clone = (obj) => JSON.parse(JSON.stringify(obj));

// Generate a random ID (simple implementation)
export const generateId = (prefix) => {
  return `${prefix}-${Math.random().toString(36).substring(2, 10)}`;
};

// Search in object properties
export const searchInObject = (item, searchTerm, fields) => {
  if (!searchTerm) return true;
  
  const lowerSearchTerm = searchTerm.toLowerCase();
  
  return fields.some(field => {
    const value = item[field];
    if (!value) return false;
    
    if (typeof value === 'string') {
      return value.toLowerCase().includes(lowerSearchTerm);
    }
    
    if (Array.isArray(value) && value.every(v => typeof v === 'string')) {
      return value.some(v => v.toLowerCase().includes(lowerSearchTerm));
    }
    
    return false;
  });
};

// Standard error messages
export const errorMessages = {
  notFound: 'Resource not found',
  unauthorized: 'Unauthorized access',
  badRequest: 'Invalid request data',
  conflict: 'Resource already exists',
  serverError: 'Internal server error'
}; 