import apiClient, { UnauthorizedError } from './apiClient';

/**
 * Fetch incoming borrow requests with retry for auth issues
 */
export async function getIncomingBorrowRequests(gameOwnerId, retryCount = 0) {
  const MAX_RETRIES = 2;
  
  // Add delay for retry attempts
  if (retryCount > 0) {
    await new Promise(resolve => setTimeout(resolve, 800));
  }
  
  try {
    return await apiClient(`/api/borrowrequests/gameOwner/${gameOwnerId}?userId=${gameOwnerId}`, {
      skipPrefix: true,
      retryOnAuth: true,
      credentials: 'include'
    });
  } catch (error) {
    if (error instanceof UnauthorizedError && retryCount < MAX_RETRIES) {
      console.log(`Auth not ready, retrying incoming requests fetch (attempt ${retryCount + 1})`);
      return getIncomingBorrowRequests(gameOwnerId, retryCount + 1);
    }
    throw error;
  }
}

/**
 * Fetch outgoing borrow requests with retry for auth issues
 */
export async function getOutgoingBorrowRequests(accountId, retryCount = 0) {
  const MAX_RETRIES = 3;  // Increase max retries
  
  // Add delay for retry attempts with increasing delay times
  if (retryCount > 0) {
    const delay = retryCount * 1000;  // Progressive delay: 1s, 2s, 3s
    console.log(`Waiting ${delay}ms before retry ${retryCount}/${MAX_RETRIES}`);
    await new Promise(resolve => setTimeout(resolve, delay));
  }
  
  try {
    console.log(`[API Request] Fetching borrow requests for user ${accountId}`);
    
    // Make sure we have proper credentials and authentication
    return await apiClient(`/api/borrowrequests/requester/${accountId}`, {
      skipPrefix: true,
      retryOnAuth: true,
      credentials: 'include',
      headers: {
        'X-User-Id': accountId
      }
    });
  } catch (error) {
    if (error instanceof UnauthorizedError && retryCount < MAX_RETRIES) {
      console.log(`Auth not ready, retrying outgoing requests fetch (attempt ${retryCount + 1})`);
      return getOutgoingBorrowRequests(accountId, retryCount + 1);
    }
    throw error;
  }
}

export async function actOnBorrowRequest(requestId, request) {
  const userId = localStorage.getItem('userId');
  return apiClient(`/api/borrowrequests/${requestId}`, {
    method: "PUT",
    body: request,
    skipPrefix: true,
    credentials: 'include'
  });
}

/**
 * Fetch lending history with retry for auth issues
 */
export async function getLendingHistory(accountId, isOwner, retryCount = 0) {
  const MAX_RETRIES = 3;  // Increase max retries
  
  // Add delay for retry attempts with increasing delay times
  if (retryCount > 0) {
    const delay = retryCount * 1000;  // Progressive delay: 1s, 2s, 3s
    console.log(`Waiting ${delay}ms before retry ${retryCount}/${MAX_RETRIES}`);
    await new Promise(resolve => setTimeout(resolve, delay));
  }
  
  try {
    console.log(`[API Request] Fetching lending history for user ${accountId}`);
    
    // Make sure we have proper credentials and authentication
    return await apiClient(`/api/lending-records/${isOwner ? "owner" : "borrower"}/${accountId}`, {
      skipPrefix: true,
      retryOnAuth: true,
      credentials: 'include',
      headers: {
        'X-User-Id': accountId
      }
    });
  } catch (error) {
    if (error instanceof UnauthorizedError && retryCount < MAX_RETRIES) {
      console.log(`Auth not ready, retrying lending history fetch (attempt ${retryCount + 1})`);
      return getLendingHistory(accountId, isOwner, retryCount + 1);
    }
    throw error;
  }
}

export async function markAsReturned(lendingId, information) {
  const userId = localStorage.getItem('userId');
  return apiClient(`/api/lending-records/${lendingId}/mark-returned?userId=${userId}`, {
    method: "POST",
    body: information,
    skipPrefix: true
  });
}

export async function updateUsernamePassword(request) {
  const userId = localStorage.getItem('userId');
  return apiClient(`/account?userId=${userId}`, {
    method: "PUT",
    body: request,
    skipPrefix: true
  });
}

export async function upgradeAccountToGameOwner(email) {
  const userId = localStorage.getItem('userId');
  return apiClient(`/account/${email}?userId=${userId}`, {
    method: "PUT",
    skipPrefix: true
  });
}

export async function getHostedEvents(hostId) {
  return apiClient(`/events/by-host-id/${hostId}?userId=${hostId}`, {
    skipPrefix: true
  });
}