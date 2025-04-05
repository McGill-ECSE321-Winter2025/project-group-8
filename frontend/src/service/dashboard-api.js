import apiClient from './apiClient';

const BASE_URL = "localhost:8080/";


export async function getIncomingBorrowRequests(gameOwnerId) {
  return apiClient(`/borrowrequests/gameOwner/${gameOwnerId}`);
}

export async function getOutgoingBorrowRequests(accountId) {
  return apiClient(`/borrowrequests/requester/${accountId}`);
}

export async function actOnBorrowRequest(requestId, request) {
  return apiClient(`/borrowrequests/${requestId}`, {
    method: "PUT",
    body: request
  });
}

export async function getLendingHistory(accountId, isOwner) {
  return apiClient(`/lending-records/${isOwner ? "owner" : "borrower"}/${accountId}`);
}

export async function markAsReturned(lendingId, information) {
  return apiClient(`/lending-records/${lendingId}/mark-returned`, {
    method: "POST",
    body: information
  });
}

export async function updateUsernamePassword(request) {
  return apiClient('/account', {
    method: "PUT",
    body: request
  });
}

export async function upgradeAccountToGameOwner(email) {
  return apiClient(`/account/${email}`, {
    method: "PUT"
  });
}

export async function getHostedEvents(hostId) {
  return apiClient(`/events/by-host-id/${hostId}`);
}