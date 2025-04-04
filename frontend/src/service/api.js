/**
 * API Service
 * 
 * This file provides API functions for the application.
 * Includes both mock data and real API endpoints.
 */

// Import mock API services
import mockApi from './mock-api';

// === USER API FUNCTIONS ===

// Search users
export const searchUsers = async (query, options = {}) => {
  console.log(`Searching users with query: "${query}"`, options);
  
  // Use the mock API service
  const response = await mockApi.users.searchUsers(query, options);
  
  if (response.status === 'error') {
    console.error('Error searching users:', response.error);
    throw new Error(response.error.message);
  }
  
  return response.data;
};

// Fetch user recommendations
export const fetchUserRecommendations = async (userId) => {
  console.log("Fetching user recommendations for user:", userId);
  
  // If no userId is provided, use a default for development
  const currentUserId = userId || 'user-1';
  
  // Use the mock API service
  const response = await mockApi.users.getRecommendedUsers(currentUserId);
  
  if (response.status === 'error') {
    console.error('Error fetching recommendations:', response.error);
    throw new Error(response.error.message);
  }
  
  return response.data;
};

// Get user by ID
export const getUserById = async (userId) => {
  console.log("Fetching user details for:", userId);
  
  // Use the mock API service
  const response = await mockApi.users.getUserById(userId);
  
  if (response.status === 'error') {
    console.error('Error fetching user:', response.error);
    throw new Error(response.error.message);
  }
  
  return response.data;
};

// Search games
export const searchGames = async (query, options = {}) => {
  console.log(`Searching games with query: "${query}"`, options);
  
  // Use the mock API service
  const response = await mockApi.games.searchGames(query, options);
  
  if (response.status === 'error') {
    console.error('Error searching games:', response.error);
    throw new Error(response.error.message);
  }
  
  return response.data;
};

// Search events (mock version)
export const searchEvents = async (query, options = {}) => {
  console.log(`Searching events with query: "${query}"`, options);
  
  // Use the mock API service
  const response = await mockApi.events.searchEvents(query, options);
  
  if (response.status === 'error') {
    console.error('Error searching events:', response.error);
    throw new Error(response.error.message);
  }
  
  return response.data;
};

// === EVENT API FUNCTIONS ===

// Register for an event (mock implementation)
export async function registerForEvent(attendeeId, eventId) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const responses = [
        { status: 200, ok: true, json: () => Promise.resolve({ message: "Success" }) },
        { status: 400, ok: false, json: () => Promise.resolve({ message: "Event is at full capacity!" }) },
        { status: 403, ok: false, json: () => Promise.resolve({ message: "You are already registered for this event!" }) }
      ];

      // Simulate random API response
      const randomResponse = responses[Math.floor(Math.random() * responses.length)];

      if (randomResponse.ok) {
        resolve(randomResponse);
      } else {
        reject(randomResponse);
      }
    }, 500); 
  });
}

// Create an event
export const createEvent = async (eventData) => {
  const formattedData = {
    ...eventData,
    dateTime: new Date(eventData.dateTime).toISOString(),
    maxParticipants: parseInt(eventData.maxParticipants, 10),
  };

  const response = await fetch("http://localhost:8080/api/v1/events", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(formattedData),
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || "Failed to create event");
  }

  return response.json();
};

// Search events by title (actual API implementation)
export const searchEventsByTitle = async (title) => {
  const response = await fetch(`http://localhost:8080/api/v1/events/by-title?title=${encodeURIComponent(title)}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    throw new Error("Failed to search events");
  }

  return response.json();
};

// Unregister from an event
export async function unregisterFromEvent(registrationId) {
  const response = await fetch(`/api/v1/registrations/${registrationId}`, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || "Failed to unregister");
  }

  return response.json(); // Assuming the backend returns some response data
}

// Export mock API for direct access when needed
export { mockApi };
