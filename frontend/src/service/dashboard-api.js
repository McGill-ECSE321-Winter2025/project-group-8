const BASE_URL = "localhost:8080/api/v1";

export async function addGame(game) {
  return fetch(`${BASE_URL}/games`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(game)
    })
}

export async function getGames(ownerId) {
  return fetch(`${BASE_URL}/users/${ownerId}/games`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json"
      }
  })
}

export async function deleteGame(gameId) {
  return fetch(`${BASE_URL}/games/${gameId}`,
    {
      method: "DELETE",
    }
  )
}

export async function getRegistrations(email) {
  return fetch(`${BASE_URL}/registrations/${email}`,
    {
      method: "GET",
    })
}

export async function cancelRegistration(registrationId) {
  return fetch(`${BASE_URL}/registrations${registrationId}`,{
    method: "DELETE",
  })
}

export async function createEvent(event) {
  return fetch(`${BASE_URL}/events`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(event)
    })
}

export async function getIncomingBorrowRequests(gameOwnerId) {
  return fetch(`${BASE_URL}/borrowrequests/gameOwner/${gameOwnerId}`,
    {
      method: "GET",
    })
}

export async function getOutgoingBorrowRequests(accountId) {
  return fetch( `${BASE_URL}/borrowrequests/requester/${accountId}`,
    {
      method: "GET",
    }
  )
}

export async function actOnBorrowRequest(requestId, request) {
  return fetch(`${BASE_URL}/borrowrequests/${requestId}`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(request)
    })
}

export async function getLendingHistory(accountId, isOwner) {
  return fetch(`${BASE_URL}/lending-records/${isOwner ? "owner" : "borrower"}/${accountId}`,
    {
      method: "GET",
    })
}

export async function markAsReturned(lendingId, information) {
  return fetch(`${BASE_URL}/lending-records/${lendingId}/mark-returned`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(
        information
      )
    })
}

export async function updateUsernamePassword(request) {
  return fetch(`${BASE_URL}/account`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(request)
  })
}

export async function upgradeAccountToGameOwner(email) {
  return fetch(`${BASE_URL}/account/${email}`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json"
      },
    })
}

export async function getHostedEvents(hostId) {
  return fetch(`${BASE_URL}/events/by-host-id/${hostId}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json"
      },
    })
}