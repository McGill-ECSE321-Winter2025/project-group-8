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

// Get all events (real API implementation)
export const getAllEvents = async () => {
  const token = localStorage.getItem("token");
  const headers = {
    "Content-Type": "application/json",
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  try {
    const response = await fetch("http://localhost:8080/api/v1/events", {
      method: "GET",
      headers: headers,
    });

    if (!response.ok) {
      const errorBody = await response.text();
      console.error("Backend error fetching events:", errorBody);
      throw new Error(`HTTP error ${response.status}: ${response.statusText}`);
    }
    return await response.json();
  } catch (error) {
    console.error("Failed to fetch events:", error);
    throw error;
  }
};


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
  // Expects eventData to contain featuredGameId now
  if (!eventData.featuredGameId) {
      throw new Error("Featured Game ID is missing in event data for createEvent");
  }

  // Construct the payload according to backend DTO expectations
  const payload = {
    title: eventData.title,
    dateTime: new Date(eventData.dateTime).toISOString(), // Format date
    location: eventData.location,
    description: eventData.description,
    maxParticipants: parseInt(eventData.maxParticipants, 10),
    featuredGame: { id: parseInt(eventData.featuredGameId, 10) } // Send game ID nested
    // Host is determined by the backend via token, not sent from frontend
  };

  const token = localStorage.getItem("token"); // Get token
  const headers = {
    "Content-Type": "application/json",
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`; // Add token if exists
  }

  const response = await fetch("http://localhost:8080/api/v1/events", {
    method: "POST",
    headers: headers, // Use headers object
    body: JSON.stringify(payload), // Send the structured payload
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || "Failed to create event");
  }

  return response.json();
};

export const getAccountInfo = async (email) => {
  // TODO: Add authentication headers if required by the backend @RequireUser annotation
  const response = await fetch(`http://localhost:8080/api/v1/account/${encodeURIComponent(email)}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      // Add Authorization header if needed, e.g.:
      // "Authorization": `Bearer ${localStorage.getItem('authToken')}`
    },
  });

  if (!response.ok) {
    const errorText = await response.text(); // Get error text for better debugging
    throw new Error(`Failed to fetch account info: ${response.status} ${response.statusText} - ${errorText}`);
  }

  return response.json();
};

// Search events by title (actual API implementation)
export const searchEventsByTitle = async (title) => {
  const token = localStorage.getItem("token"); // Get token
  const headers = {
    "Content-Type": "application/json",
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`; // Add token if exists
  }

  const response = await fetch(`http://localhost:8080/api/v1/events/by-title?title=${encodeURIComponent(title)}`, {
    method: "GET",
    headers: headers, // Use headers object
  });

  if (!response.ok) {
    throw new Error("Failed to search events");
  }

  return response.json();
};

// Unregister from an event
export async function unregisterFromEvent(registrationId) {
  const token = localStorage.getItem("token"); // Get token
  const headers = {
    "Content-Type": "application/json",
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`; // Add token if exists
  }

  // TODO: Verify the actual endpoint URL structure for unregistering
  const response = await fetch(`http://localhost:8080/api/v1/registrations/${registrationId}`, { // Assuming full URL needed
    method: "DELETE",
    headers: headers, // Use headers object
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || "Failed to unregister");
  }

  return response.json(); // Assuming the backend returns some response data
}

// Export mock API for direct access when needed
export { mockApi };
