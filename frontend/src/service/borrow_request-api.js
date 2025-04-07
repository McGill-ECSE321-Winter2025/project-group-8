/**
 * BorrowRequest API Module
 *
 * This file provides API functions for managing borrow requests.
 * Follows the application's established API patterns.
 */

// Use the same base URL as other API modules
const API_BASE_URL = "http://localhost:8080/api/v1";
const BORROW_REQUESTS_ENDPOINT = `${API_BASE_URL}/borrowrequests`;

/**
 * Creates a new borrow request
 * @param {Object} requestData - Contains requesterId, requestedGameId, startDate, endDate
 * @returns {Promise<Object>} The created borrow request
 */
export const createBorrowRequest = async (requestData) => {
  const token = localStorage.getItem("token");
  if (!token) {
    throw new Error("Authentication token not found. Please log in.");
  }

  const headers = {
    "Content-Type": "application/json",
    'Authorization': `Bearer ${token}`
  };

  try {
    const response = await fetch(BORROW_REQUESTS_ENDPOINT, {
      method: "POST",
      headers: headers,
      body: JSON.stringify(requestData)
    });

    if (!response.ok) {
      let errorMsg = `HTTP error ${response.status}: ${response.statusText}`;
      try {
        const errorBody = await response.json();
        errorMsg = errorBody.message || errorMsg;
      } catch (e) { /* Ignore parsing error */ }
      console.error("Backend error creating borrow request:", errorMsg);
      throw new Error(errorMsg);
    }
    return await response.json();
  } catch (error) {
    console.error("Failed to create borrow request:", error);
    throw error;
  }
};

/**
 * Gets a borrow request by its ID
 * @param {number} id - The ID of the borrow request
 * @returns {Promise<Object>} The borrow request
 */
export const getBorrowRequestById = async (id) => {
  const token = localStorage.getItem("token");
  if (!token) {
    throw new Error("Authentication token not found. Please log in.");
  }
  if (!id) {
    throw new Error("Borrow request ID is required.");
  }

  const headers = {
    "Content-Type": "application/json",
    'Authorization': `Bearer ${token}`
  };

  try {
    const response = await fetch(`${BORROW_REQUESTS_ENDPOINT}/${id}`, {
      method: "GET",
      headers: headers
    });

    if (!response.ok) {
      let errorMsg = `HTTP error ${response.status}: ${response.statusText}`;
      try {
        const errorBody = await response.json();
        errorMsg = errorBody.message || errorMsg;
      } catch (e) { /* Ignore parsing error */ }
      console.error("Backend error fetching borrow request:", errorMsg);
      throw new Error(errorMsg);
    }
    return await response.json();
  } catch (error) {
    console.error(`Failed to fetch borrow request #${id}:`, error);
    throw error;
  }
};

/**
 * Gets all borrow requests
 * @returns {Promise<Array>} List of all borrow requests
 */
export const getAllBorrowRequests = async () => {
  const token = localStorage.getItem("token");
  const headers = {
    "Content-Type": "application/json",
  };
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  try {
    const response = await fetch(BORROW_REQUESTS_ENDPOINT, {
      method: "GET",
      headers: headers
    });

    if (!response.ok) {
      let errorMsg = `HTTP error ${response.status}: ${response.statusText}`;
      try {
        const errorBody = await response.json();
        errorMsg = errorBody.message || errorMsg;
      } catch (e) { /* Ignore parsing error */ }
      console.error("Backend error fetching all borrow requests:", errorMsg);
      throw new Error(errorMsg);
    }
    return await response.json();
  } catch (error) {
    console.error("Failed to fetch borrow requests:", error);
    throw error;
  }
};

/**
 * Updates a borrow request's status
 * @param {number} id - The ID of the borrow request to update
 * @param {string} status - The new status (e.g., 'APPROVED', 'DECLINED')
 * @returns {Promise<Object>} The updated borrow request
 */
export const updateBorrowRequestStatus = async (id, status) => {
  const token = localStorage.getItem("token");
  if (!token) {
    throw new Error("Authentication token not found. Please log in.");
  }
  if (!id) {
    throw new Error("Borrow request ID is required.");
  }
  if (!status) {
    throw new Error("Status is required.");
  }

  // First get the existing request to retain all fields
  const existingRequest = await getBorrowRequestById(id);
  
  // Only update the status
  existingRequest.status = status;

  const headers = {
    "Content-Type": "application/json",
    'Authorization': `Bearer ${token}`
  };

  try {
    const response = await fetch(`${BORROW_REQUESTS_ENDPOINT}/${id}`, {
      method: "PUT",
      headers: headers,
      body: JSON.stringify(existingRequest)
    });

    if (!response.ok) {
      let errorMsg = `HTTP error ${response.status}: ${response.statusText}`;
      try {
        const errorBody = await response.json();
        errorMsg = errorBody.message || errorMsg;
      } catch (e) { /* Ignore parsing error */ }
      console.error("Backend error updating borrow request status:", errorMsg);
      throw new Error(errorMsg);
    }
    return await response.json();
  } catch (error) {
    console.error(`Failed to update borrow request #${id}:`, error);
    throw error;
  }
};

/**
 * Deletes a borrow request
 * @param {number} id - The ID of the borrow request to delete
 * @returns {Promise<boolean>} True if deletion was successful
 */
export const deleteBorrowRequest = async (id) => {
  const token = localStorage.getItem("token");
  if (!token) {
    throw new Error("Authentication token not found. Please log in.");
  }
  if (!id) {
    throw new Error("Borrow request ID is required.");
  }

  const headers = {
    "Content-Type": "application/json",
    'Authorization': `Bearer ${token}`
  };

  try {
    const response = await fetch(`${BORROW_REQUESTS_ENDPOINT}/${id}`, {
      method: "DELETE",
      headers: headers
    });

    if (!response.ok) {
      let errorMsg = `HTTP error ${response.status}: ${response.statusText}`;
      try {
        const errorBody = await response.json();
        errorMsg = errorBody.message || errorMsg;
      } catch (e) { /* Ignore parsing error */ }
      console.error("Backend error deleting borrow request:", errorMsg);
      throw new Error(errorMsg);
    }
    return true;
  } catch (error) {
    console.error(`Failed to delete borrow request #${id}:`, error);
    throw error;
  }
};

/**
 * Gets borrow requests by status
 * @param {string} status - The status to filter by (e.g., 'PENDING', 'APPROVED')
 * @returns {Promise<Array>} List of borrow requests with the specified status
 */
export const getBorrowRequestsByStatus = async (status) => {
  const token = localStorage.getItem("token");
  if (!token) {
    throw new Error("Authentication token not found. Please log in.");
  }
  if (!status) {
    throw new Error("Status parameter is required.");
  }

  const headers = {
    "Content-Type": "application/json",
    'Authorization': `Bearer ${token}`
  };

  try {
    const response = await fetch(`${BORROW_REQUESTS_ENDPOINT}/status/${encodeURIComponent(status)}`, {
      method: "GET",
      headers: headers
    });

    if (!response.ok) {
      let errorMsg = `HTTP error ${response.status}: ${response.statusText}`;
      try {
        const errorBody = await response.json();
        errorMsg = errorBody.message || errorMsg;
      } catch (e) { /* Ignore parsing error */ }
      console.error(`Backend error fetching requests with status ${status}:`, errorMsg);
      throw new Error(errorMsg);
    }
    return await response.json();
  } catch (error) {
    console.error(`Failed to fetch requests with status ${status}:`, error);
    throw error;
  }
};

/**
 * Gets borrow requests by requester ID
 * @param {number} requesterId - The ID of the requester
 * @returns {Promise<Array>} List of borrow requests made by the specified requester
 */
export const getBorrowRequestsByRequester = async (requesterId) => {
  const token = localStorage.getItem("token");
  if (!token) {
    throw new Error("Authentication token not found. Please log in.");
  }
  if (!requesterId) {
    throw new Error("Requester ID is required.");
  }

  const headers = {
    "Content-Type": "application/json",
    'Authorization': `Bearer ${token}`
  };

  try {
    const response = await fetch(`${BORROW_REQUESTS_ENDPOINT}/requester/${requesterId}`, {
      method: "GET",
      headers: headers
    });

    if (!response.ok) {
      let errorMsg = `HTTP error ${response.status}: ${response.statusText}`;
      try {
        const errorBody = await response.json();
        errorMsg = errorBody.message || errorMsg;
      } catch (e) { /* Ignore parsing error */ }
      console.error(`Backend error fetching requests for requester #${requesterId}:`, errorMsg);
      throw new Error(errorMsg);
    }
    return await response.json();
  } catch (error) {
    console.error(`Failed to fetch requests for requester #${requesterId}:`, error);
    throw error;
  }
};

/**
 * Gets borrow requests by game owner ID
 * @param {number} ownerId - The ID of the game owner
 * @returns {Promise<Array>} List of borrow requests associated with the specified game owner
 */
export const getBorrowRequestsByOwner = async (ownerId) => {
    const token = localStorage.getItem("token");
    if (!token) {
        throw new Error("Authentication token not found. Please log in.");
    }
    if (!ownerId) {
        throw new Error("Owner ID is required.");
    }

    const headers = {
        "Content-Type": "application/json",
        'Authorization': `Bearer ${token}`
    };

    try {
        const response = await fetch(`${BORROW_REQUESTS_ENDPOINT}/owner/${ownerId}`, {
            method: "GET",
            headers: headers
        });

        if (!response.ok) {
            let errorMsg = `HTTP error ${response.status}: ${response.statusText}`;
            try {
                const errorBody = await response.json();
                errorMsg = errorBody.message || errorMsg;
            } catch (e) { /* Ignore parsing error */ }
            console.error(`Backend error fetching requests for owner #${ownerId}:`, errorMsg);
            throw new Error(errorMsg);
        }
        return await response.json();
    } catch (error) {
        console.error(`Failed to fetch requests for owner #${ownerId}:`, error);
        throw error;
    }
};