import apiClient from './apiClient';

export async function getIncomingBorrowRequests(gameOwnerId) {
  return apiClient(`/api/borrowrequests/gameOwner/${gameOwnerId}`, {
    skipPrefix: true
  });
}

export async function getOutgoingBorrowRequests(accountId) {
  return apiClient(`/api/borrowrequests/requester/${accountId}`, {
    skipPrefix: true
  });
}

export async function actOnBorrowRequest(requestId, request) {
  return apiClient(`/api/borrowrequests/${requestId}`, {
    method: "PUT",
    body: request,
    skipPrefix: true
  });
}

export async function getLendingHistory(accountId, isOwner) {
  return apiClient(`/api/lending-records/${isOwner ? "owner" : "borrower"}/${accountId}`, {
    skipPrefix: true
  });
}

export async function markAsReturned(lendingId, information) {
  return apiClient(`/api/lending-records/${lendingId}/mark-returned`, {
    method: "POST",
    body: information,
    skipPrefix: true
  });
}

export async function updateUsernamePassword(request) {
  return apiClient('/api/account', {
    method: "PUT",
    body: request
  });
}

export async function upgradeAccountToGameOwner(email) {
  return apiClient(`/api/account/${email}`, {
    method: "PUT"
  });
}

export async function getHostedEvents(hostId) {
  return apiClient(`/api/events/by-host-id/${hostId}`);
}