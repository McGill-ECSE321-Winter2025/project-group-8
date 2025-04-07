/**
 * BorrowRequest API Module
 *
 * This file provides API functions for managing borrow requests.
 * Follows the application's established API patterns.
 */

import apiClient from './apiClient'; // Import the centralized API client
// Use the same base URL as other API modules
// Use apiClient which handles base URL and prefix
const BORROW_REQUESTS_ENDPOINT = '/borrowrequests'; // Relative path for apiClient

/**
 * Creates a new borrow request
 * @param {Object} requestData - Contains requesterId, requestedGameId, startDate, endDate
 * @returns {Promise<Object>} The created borrow request
 */
export const createBorrowRequest = async (requestData) => {
  // Remove manual token check and Authorization header
  // Removed extra brace

  // Use apiClient
  return apiClient(BORROW_REQUESTS_ENDPOINT, {
    method: "POST",
    body: requestData,
    requiresAuth: true,
    skipPrefix: false // Use /api prefix
  });
};

/**
 * Gets a borrow request by its ID
 * @param {number} id - The ID of the borrow request
 * @returns {Promise<Object>} The borrow request
 */
export const getBorrowRequestById = async (id) => {
  if (!id) {
    throw new Error("Borrow request ID is required.");
  }
  // Remove manual token check and Authorization header
  // Removed extra brace

  // Use apiClient
  return apiClient(`${BORROW_REQUESTS_ENDPOINT}/${id}`, {
    method: "GET",
    requiresAuth: true,
    skipPrefix: false // Use /api prefix
  });
};

/**
 * Gets all borrow requests
 * @returns {Promise<Array>} List of all borrow requests
 */
export const getAllBorrowRequests = async () => {
  // Remove manual token check and Authorization header
  // Removed extra brace

  // Use apiClient
  return apiClient(BORROW_REQUESTS_ENDPOINT, {
    method: "GET",
    requiresAuth: true, // Assuming this needs auth, adjust if not
    skipPrefix: false // Use /api prefix
  });
};

/**
 * Updates a borrow request's status
 * @param {number} id - The ID of the borrow request to update
 * @param {string} status - The new status (e.g., 'APPROVED', 'DECLINED')
 * @returns {Promise<Object>} The updated borrow request
 */
export const updateBorrowRequestStatus = async (id, status) => {
  if (!id) {
    throw new Error("Borrow request ID is required.");
  }
  if (!status) {
    throw new Error("Status is required.");
  }
  // Remove manual token check and Authorization header
  
  // Note: Sending the full existingRequest might not be ideal for a status update.
  // Ideally, the backend endpoint should accept just the status.
  // Assuming the backend PUT /api/borrowrequests/{id} expects the full object for now.
  
  // Fetch existing request using the refactored function
  const existingRequest = await getBorrowRequestById(id);
  // Update status
  existingRequest.status = status;
  // Removed extra brace

  // Use apiClient
  return apiClient(`${BORROW_REQUESTS_ENDPOINT}/${id}`, {
    method: "PUT",
    body: existingRequest, // Sending full object as per original logic
    requiresAuth: true,
    skipPrefix: false // Use /api prefix
  });
};

/**
 * Deletes a borrow request
 * @param {number} id - The ID of the borrow request to delete
 * @returns {Promise<boolean>} True if deletion was successful
 */
export const deleteBorrowRequest = async (id) => {
  if (!id) {
    throw new Error("Borrow request ID is required.");
  }
  // Remove manual token check and Authorization header
  // Removed extra brace

  // Use apiClient
  // DELETE often returns 204 No Content, apiClient handles this
  await apiClient(`${BORROW_REQUESTS_ENDPOINT}/${id}`, {
    method: "DELETE",
    requiresAuth: true,
    skipPrefix: false // Use /api prefix
  });
  return true; // Assume success if apiClient doesn't throw
};

/**
 * Gets borrow requests by status
 * @param {string} status - The status to filter by (e.g., 'PENDING', 'APPROVED')
 * @returns {Promise<Array>} List of borrow requests with the specified status
 */
export const getBorrowRequestsByStatus = async (status) => {
  if (!status) {
    throw new Error("Status parameter is required.");
  }
  // Remove manual token check and Authorization header
  // Removed extra brace

  // Use apiClient
  return apiClient(`${BORROW_REQUESTS_ENDPOINT}/status/${encodeURIComponent(status)}`, {
    method: "GET",
    requiresAuth: true,
    skipPrefix: false // Use /api prefix
  });
};

/**
 * Gets borrow requests by requester ID
 * @param {number} requesterId - The ID of the requester
 * @returns {Promise<Array>} List of borrow requests made by the specified requester
 */
export const getBorrowRequestsByRequester = async (requesterId) => {
  if (!requesterId) {
    throw new Error("Requester ID is required.");
  }
  // Remove manual token check and Authorization header
  // Removed extra brace

  // Use apiClient
  return apiClient(`${BORROW_REQUESTS_ENDPOINT}/requester/${requesterId}`, {
    method: "GET",
    requiresAuth: true,
    skipPrefix: false // Use /api prefix
  });
};

/**
 * Gets borrow requests by game owner ID
 * @param {number} ownerId - The ID of the game owner
 * @returns {Promise<Array>} List of borrow requests associated with the specified game owner
 */
export const getBorrowRequestsByOwner = async (ownerId) => {
    if (!ownerId) {
        throw new Error("Owner ID is required.");
    }
    // Remove manual token check and Authorization header
    // Removed extra brace

    // Use apiClient
    return apiClient(`${BORROW_REQUESTS_ENDPOINT}/by-owner/${ownerId}`, { // Changed path for troubleshooting
        method: "GET",
        requiresAuth: true,
        skipPrefix: false // Use /api prefix
    });
};